package pl.polsl.servlet;

import pl.polsl.model.*;
import pl.polsl.view.StockView;
import pl.polsl.entity.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.persistence.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet handling stock analysis requests with JPA persistence.
 * Acts as controller in MVC pattern - coordinates between model and view.
 * Provides access to computational part of the model and saves all operations to database.
 * 
 * @author Blazej Jamrozik
 * @version 2.0
 */
@WebServlet(name = "AnalysisServlet", urlPatterns = {"/AnalysisServlet"})
public class AnalysisServlet extends HttpServlet {

    /**
     * Reference to stock model for business logic operations.
     */
    private StockModel model;
    
    /**
     * Reference to view for rendering HTML output.
     */
    private StockView view;
    
    /**
     * JPA EntityManagerFactory for database operations.
     */
    private EntityManagerFactory emf;
    
    /**
     * Initializes model, view, and EntityManagerFactory (called once per application lifecycle).
     * Model and EMF are stored in ServletContext for shared access.
     * 
     * @throws ServletException if initialization fails
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        if (getServletContext().getAttribute("StockModel") == null) {
            model = new StockModel();
            getServletContext().setAttribute("StockModel", model);

            List<String> history = new ArrayList<>();
            getServletContext().setAttribute("AnalysisHistory", history);
        } else {
            model = (StockModel) getServletContext().getAttribute("StockModel");
        }
        
        if (getServletContext().getAttribute("EntityManagerFactory") == null) {
            try {
                emf = Persistence.createEntityManagerFactory("TeslaStockPU");
                getServletContext().setAttribute("EntityManagerFactory", emf);
                
            } catch (Exception e) {
                throw new ServletException("Failed to initialize EntityManagerFactory: " + e.getMessage(), e);
            }
        } else {
            emf = (EntityManagerFactory) getServletContext().getAttribute("EntityManagerFactory");
        }
        
        view = new StockView(model);
    }
    
    /**
     * Cleans up resources when servlet is destroyed.
     */
    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        super.destroy();
    }
    
    /**
     * Processes requests for both HTTP GET and POST methods.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        if (model == null) {
            model = (StockModel) getServletContext().getAttribute("StockModel");
        }
        if (emf == null) {
            emf = (EntityManagerFactory) getServletContext().getAttribute("EntityManagerFactory");
        }
        
        try (PrintWriter out = response.getWriter()) {
            view.printHtmlStart(out, "Tesla Stock Analysis Results");
            
            String lastAnalysisType = getCookieValue(request, "lastAnalysisType");
            String analysisCountStr = getCookieValue(request, "analysisCount");
            
            try {

                String filePath = validateFilePath(request.getParameter("filePath"));
                AnalysisType analysisType = validateAnalysisType(request.getParameter("analysisType"));
                

                model.loadData(filePath);
                view.printDataLoadedMessage(out, model.getStockRecords().size());
                

                String resultText = performAnalysis(out, analysisType);
                
                try {
                    saveOperationToDatabase(analysisType, filePath, resultText, 
                                           model.getStockRecords().size());
                    out.println("<p style=\"color: green;\"><strong>[DATABASE]</strong> Analysis operation saved to database successfully.</p>");
                } catch (Exception e) {
                    out.println("<p style=\"color: orange;\"><strong>[DATABASE WARNING]</strong> Could not save to database: " + 
                               escapeHtml(e.getMessage()) + "</p>");
                }
                
                addToHistory(analysisType, filePath);

                updateCookies(response, analysisType.name(), analysisCountStr);
                
                int newCount = (analysisCountStr != null) ? 
                    Integer.parseInt(analysisCountStr) + 1 : 1;
                view.printCookieInfo(out, lastAnalysisType, String.valueOf(newCount));
                
            } catch (IllegalArgumentException e) {
                view.printError(out, "Invalid input: " + e.getMessage());
            } catch (TeslaAnalysisException e) {
                view.printError(out, "Analysis error: " + e.getMessage());
            } catch (Exception e) {
                view.printError(out, "Unexpected error: " + e.getMessage());
            }
            
            view.printHtmlEnd(out);
        }
    }
    
    /**
     * Performs analysis based on selected type and returns result as text.
     * 
     * @param out the print writer
     * @param analysisType the type of analysis to perform
     * @return result text for database storage
     * @throws TeslaAnalysisException if analysis fails
     */
    private String performAnalysis(PrintWriter out, AnalysisType analysisType) 
            throws TeslaAnalysisException {
        String resultText = "";
        
        switch (analysisType) {
            case HIGHEST_VOLUME_DAY:
                StockRecord highestVolumeDay = model.getHighestVolumeDay();
                view.printHighestVolumeDay(out, highestVolumeDay);
                resultText = "Date: " + highestVolumeDay.date() + ", Volume: " + highestVolumeDay.volume();
                break;
                
            case HIGHEST_VOLUME_YEAR:
                int highestVolumeYear = model.getYearWithHighestVolume();
                view.printHighestVolumeYear(out, highestVolumeYear);
                resultText = "Year: " + highestVolumeYear;
                break;
                
            case PRICE_SORTING:
                List<StockRecord> sortedData = model.getDataSortedByPrice();
                view.printPriceSorting(out, sortedData);
                resultText = "Sorted " + sortedData.size() + " records by closing price";
                break;
                
            case PRICE_VOLUME_CORRELATION:
                double correlation = model.calculatePriceVolumeCorrelation();
                view.printPriceVolumeCorrelation(out, correlation);
                resultText = "Correlation: " + String.format("%.4f", correlation);
                break;
                
            default:
                throw new TeslaAnalysisException("Unknown analysis type");
        }
        
        return resultText;
    }
    
    /**
     * Saves analysis operation to database using JPA.
     * 
     * @param analysisType type of analysis performed
     * @param filePath file path used
     * @param resultText result of analysis
     * @param recordsCount number of records processed
     * @throws Exception if database operation fails
     */
    private void saveOperationToDatabase(AnalysisType analysisType, String filePath, 
                                        String resultText, int recordsCount) throws Exception {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            AnalysisOperation operation = new AnalysisOperation(
                analysisType.name(),
                filePath,
                LocalDateTime.now(),
                resultText,
                recordsCount
            );
            
            operation.addResultDetail(new AnalysisResultDetail("Analysis Type", analysisType.name()));
            operation.addResultDetail(new AnalysisResultDetail("File Path", filePath));
            operation.addResultDetail(new AnalysisResultDetail("Records Processed", String.valueOf(recordsCount)));
            operation.addResultDetail(new AnalysisResultDetail("Result", resultText));
            
            em.persist(operation);
            em.getTransaction().commit();
            
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new Exception("Database error: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    /**
     * Adds analysis to history stored in ServletContext.
     * Maintains last 10 entries.
     * 
     * @param analysisType the type of analysis performed
     * @param filePath the file path used
     */
    @SuppressWarnings("unchecked")
    private void addToHistory(AnalysisType analysisType, String filePath) {
        List<String> history = (List<String>) getServletContext()
            .getAttribute("AnalysisHistory");
        
        if (history == null) {
            history = new ArrayList<>();
            getServletContext().setAttribute("AnalysisHistory", history);
        }
        
        String timestamp = new java.util.Date().toString();
        String entry = timestamp + " - " + analysisType.name() + " (File: " + filePath + ")";
        
        history.add(0, entry);
        
        if (history.size() > 10) {
            history.remove(history.size() - 1);
        }
    }
    
    /**
     * Validates file path parameter.
     * 
     * @param filePathParam the file path parameter from request
     * @return validated file path
     * @throws IllegalArgumentException if parameter is invalid
     */
    private String validateFilePath(String filePathParam) throws IllegalArgumentException {
        if (filePathParam == null || filePathParam.trim().isEmpty()) {
            throw new IllegalArgumentException("File path parameter is missing");
        }
        return filePathParam.trim();
    }
    
    /**
     * Validates analysis type parameter.
     * 
     * @param analysisTypeParam the analysis type parameter from request
     * @return validated analysis type
     * @throws IllegalArgumentException if parameter is invalid
     */
    private AnalysisType validateAnalysisType(String analysisTypeParam) 
            throws IllegalArgumentException {
        if (analysisTypeParam == null || analysisTypeParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Analysis type parameter is missing");
        }
        
        try {
            return AnalysisType.valueOf(analysisTypeParam.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis type: " + analysisTypeParam);
        }
    }
    
    /**
     * Gets cookie value by name.
     * 
     * @param request the HTTP request
     * @param cookieName the name of the cookie
     * @return cookie value or null if not found
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * Updates cookies with analysis information.
     * 
     * @param response the HTTP response
     * @param analysisType the current analysis type
     * @param analysisCountStr the previous analysis count
     */
    private void updateCookies(HttpServletResponse response, String analysisType, 
                               String analysisCountStr) {
        Cookie lastAnalysisCookie = new Cookie("lastAnalysisType", analysisType);
        lastAnalysisCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(lastAnalysisCookie);
        
        int count = 1;
        if (analysisCountStr != null) {
            try {
                count = Integer.parseInt(analysisCountStr) + 1;
            } catch (NumberFormatException e) {
                count = 1;
            }
        }
        
        Cookie countCookie = new Cookie("analysisCount", String.valueOf(count));
        countCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(countCookie);
    }
    
    /**
     * Escapes HTML special characters.
     * 
     * @param text text to escape
     * @return escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Handles the HTTP GET method.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /**
     * Handles the HTTP POST method.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /**
     * Returns a short description of the servlet.
     * 
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet for performing Tesla stock analysis with database persistence";
    }
}