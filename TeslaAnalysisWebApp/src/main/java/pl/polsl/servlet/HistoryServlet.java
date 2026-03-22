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
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet handling analysis history requests with database access.
 * Acts as controller - coordinates between model and view.
 * Displays all analysis operations from database.
 * 
 * @author Blazej Jamrozik
 * @version 2.0
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/HistoryServlet"})
public class HistoryServlet extends HttpServlet {

    /**
     * Reference to stock model for accessing current state.
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
     * Initializes view with model reference and gets EMF.
     * 
     * @throws ServletException if initialization fails
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        model = (StockModel) getServletContext().getAttribute("StockModel");
        if (model == null) {
            model = new StockModel();
            getServletContext().setAttribute("StockModel", model);
        }
        
        emf = (EntityManagerFactory) getServletContext().getAttribute("EntityManagerFactory");
        
        view = new StockView(model);
    }
    
    /**
     * Processes requests for both HTTP GET and POST methods.
     * Controller method - retrieves data and delegates rendering to view.
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
            // Read cookies
            String lastAnalysisType = getCookieValue(request, "lastAnalysisType");
            String analysisCount = getCookieValue(request, "analysisCount");
            
            // Get database operations
            List<Object> operations = getDatabaseOperations();
            
            // Get current data status
            boolean dataLoaded = model != null && model.isDataLoaded();
            int recordsCount = dataLoaded ? model.getStockRecords().size() : 0;
            
            // Delegate rendering to view
            view.printHistoryPage(out, operations, lastAnalysisType, analysisCount, 
                                 dataLoaded, recordsCount);
        }
    }
    
    /**
     * Retrieves all analysis operations from database.
     * 
     * @return list of operations or error message
     */
    private List<Object> getDatabaseOperations() {
        List<Object> result = new ArrayList<>();
        
        if (emf == null) {
            result.add("Database connection not available. Please perform an analysis first.");
            return result;
        }
        
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            TypedQuery<AnalysisOperation> query = em.createNamedQuery(
                "AnalysisOperation.findAll", AnalysisOperation.class);
            List<AnalysisOperation> operations = query.getResultList();
            
            result.addAll(operations);
            
        } catch (Exception e) {
            result.clear();
            result.add("Error reading from database: " + e.getMessage());
        } finally {
            if (em != null) {
                em.close();
            }
        }
        
        return result;
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
        return "Servlet for displaying analysis history from database";
    }
}