package pl.polsl.view;

import pl.polsl.model.*;
import java.io.PrintWriter;
import java.util.List;

/**
 * View class for Tesla stock analysis web application.
 * Generates HTML output, validates user input, and formats data for display.
 * Responsible for all presentation logic in MVC pattern.
 * 
 * @author Blazej Jamrozik
 * @version 2.0
 */
public class StockView {
    
    /**
     * Reference to stock model for accessing data.
     */
    private StockModel model;
    
    /**
     * Prints history page with database operations and cookie information.
     * 
     * @param out the print writer
     * @param operations list of analysis operations from database
     * @param lastAnalysisType last analysis type from cookie
     * @param analysisCount analysis count from cookie
     * @param dataLoaded whether data is currently loaded in memory
     * @param recordsCount number of records in memory
     */
    public void printHistoryPage(PrintWriter out, List<Object> operations, 
                                 String lastAnalysisType, String analysisCount,
                                 boolean dataLoaded, int recordsCount) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Analysis History from Database</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Tesla Stock Analysis - History from Database</h1>");
        
 
        printHistoryCookieInfo(out, lastAnalysisType, analysisCount);
        
        printDatabaseOperations(out, operations);
        
        printHistoryDataStatus(out, dataLoaded, recordsCount);
        
        out.println("<hr>");
        out.println("<form action=\"index.html\" method=\"get\">");
        out.println("<input type=\"submit\" value=\"Back to Analysis Form\">");
        out.println("</form>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Prints cookie information section for history page.
     * 
     * @param out the print writer
     * @param lastAnalysisType last analysis type from cookie
     * @param analysisCount analysis count from cookie
     */
    private void printHistoryCookieInfo(PrintWriter out, String lastAnalysisType, String analysisCount) {
        out.println("<hr>");
        out.println("<h2>Cookie Information</h2>");
        out.println("<p><em>This information is stored in browser cookies (client-side storage)</em></p>");
        out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
        out.println("<tr><th>Data Source</th><th>Parameter</th><th>Value</th></tr>");
        
        out.println("<tr>");
        out.println("<td><strong>[FROM COOKIE]</strong></td>");
        out.println("<td>Last Analysis Type</td>");
        if (lastAnalysisType != null && !lastAnalysisType.isEmpty()) {
            out.println("<td>" + escapeHtml(lastAnalysisType) + "</td>");
        } else {
            out.println("<td><em>None (first visit)</em></td>");
        }
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td><strong>[FROM COOKIE]</strong></td>");
        out.println("<td>Total Analyses Count</td>");
        if (analysisCount != null && !analysisCount.isEmpty()) {
            out.println("<td>" + escapeHtml(analysisCount) + "</td>");
        } else {
            out.println("<td><em>0</em></td>");
        }
        out.println("</tr>");
        
        out.println("</table>");
    }
    
    /**
     * Prints database operations section.
     * 
     * @param out the print writer
     * @param operations list of analysis operations (can contain AnalysisOperation entities or error message)
     */
    private void printDatabaseOperations(PrintWriter out, List<Object> operations) {
        out.println("<hr>");
        out.println("<h2>Analysis Operations History</h2>");
        out.println("<p><em>This information is stored in the database (server-side persistent storage)</em></p>");
        
        if (operations == null || operations.isEmpty()) {
            out.println("<p><strong>[FROM DATABASE]</strong> No analysis operations recorded yet.</p>");
            return;
        }
        
        if (operations.get(0) instanceof String) {
            out.println("<p style=\"color: red;\"><strong>[DATABASE ERROR]</strong> " + 
                       escapeHtml((String)operations.get(0)) + "</p>");
            return;
        }
        
        out.println("<p><strong>[FROM DATABASE]</strong> Total operations in database: " + operations.size() + "</p>");
        out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
        out.println("<tr>");
        out.println("<th>ID</th>");
        out.println("<th>Timestamp</th>");
        out.println("<th>Analysis Type</th>");
        out.println("<th>File Path</th>");
        out.println("<th>Records Count</th>");
        out.println("<th>Result</th>");
        out.println("<th>Details Count</th>");
        out.println("</tr>");
        
        for (Object obj : operations) {
            if (obj instanceof pl.polsl.entity.AnalysisOperation) {
                pl.polsl.entity.AnalysisOperation op = (pl.polsl.entity.AnalysisOperation) obj;
                out.println("<tr>");
                out.println("<td>" + op.getId() + "</td>");
                out.println("<td>" + op.getTimestamp() + "</td>");
                out.println("<td>" + escapeHtml(op.getAnalysisType()) + "</td>");
                out.println("<td>" + escapeHtml(op.getFilePath()) + "</td>");
                out.println("<td>" + op.getRecordsCount() + "</td>");
                out.println("<td>" + escapeHtml(op.getResultText()) + "</td>");
                out.println("<td>" + op.getResultDetails().size() + "</td>");
                out.println("</tr>");
            }
        }
        
        out.println("</table>");
        
        if (operations.get(0) instanceof pl.polsl.entity.AnalysisOperation) {
            pl.polsl.entity.AnalysisOperation mostRecent = 
                (pl.polsl.entity.AnalysisOperation) operations.get(0);
            out.println("<h3>Details of Most Recent Operation (ID: " + mostRecent.getId() + ")</h3>");
            out.println("<p><strong>[FROM DATABASE]</strong> Result details stored in related table:</p>");
            out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
            out.println("<tr><th>Detail Key</th><th>Detail Value</th></tr>");
            
            for (pl.polsl.entity.AnalysisResultDetail detail : mostRecent.getResultDetails()) {
                out.println("<tr>");
                out.println("<td>" + escapeHtml(detail.getDetailKey()) + "</td>");
                out.println("<td>" + escapeHtml(detail.getDetailValue()) + "</td>");
                out.println("</tr>");
            }
            
            out.println("</table>");
        }
    }
    
    /**
     * Prints current data status section for history page.
     * 
     * @param out the print writer
     * @param dataLoaded whether data is loaded
     * @param recordsCount number of records
     */
    private void printHistoryDataStatus(PrintWriter out, boolean dataLoaded, int recordsCount) {
        out.println("<hr>");
        out.println("<h3>Current Application State (In-Memory)</h3>");
        out.println("<p><em>This information is stored in application memory (volatile, lost on restart)</em></p>");
        
        if (dataLoaded) {
            out.println("<p><strong>[IN MEMORY]</strong> Data currently loaded: <strong>Yes</strong></p>");
            out.println("<p><strong>[IN MEMORY]</strong> Number of records in memory: <strong>" + 
                       recordsCount + "</strong></p>");
        } else {
            out.println("<p><strong>[IN MEMORY]</strong> Data currently loaded: <strong>No</strong></p>");
            out.println("<p><em>Please perform an analysis to load data into memory.</em></p>");
        }
    }
    
    /**
     * Constructs view with model reference.
     * 
     * @param model the stock model
     */
    public StockView(StockModel model) {
        this.model = model;
    }
    
    /**
     * Prints HTML document start.
     * 
     * @param out the print writer
     * @param title the page title
     */
    public void printHtmlStart(PrintWriter out, String title) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + title + "</h1>");
    }
    
    /**
     * Prints HTML document end with navigation links.
     * 
     * @param out the print writer
     */
    public void printHtmlEnd(PrintWriter out) {
        out.println("<hr>");
        out.println("<form action=\"index.html\" method=\"get\" style=\"display:inline;\">");
        out.println("<input type=\"submit\" value=\"Back to Analysis Form\">");
        out.println("</form>");
        out.println("<form action=\"HistoryServlet\" method=\"get\" style=\"display:inline; margin-left:10px;\">");
        out.println("<input type=\"submit\" value=\"View History\">");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Prints error message in HTML format.
     * 
     * @param out the print writer
     * @param errorMessage the error message to display
     */
    public void printError(PrintWriter out, String errorMessage) {
        out.println("<div style=\"color: red; border: 2px solid red; padding: 10px; margin: 10px 0;\">");
        out.println("<h2>Error</h2>");
        out.println("<p>" + escapeHtml(errorMessage) + "</p>");
        out.println("</div>");
    }
    
    /**
     * Prints success message for data loading.
     * 
     * @param out the print writer
     * @param recordCount number of records loaded
     */
    public void printDataLoadedMessage(PrintWriter out, int recordCount) {
        out.println("<div style=\"color: green; border: 2px solid green; padding: 10px; margin: 10px 0;\">");
        out.println("<h2>Data Loaded Successfully</h2>");
        out.println("<p>Number of records loaded: " + recordCount + "</p>");
        out.println("</div>");
    }
    
    /**
     * Prints analysis result for highest volume day.
     * 
     * @param out the print writer
     * @param record the stock record with highest volume
     */
    public void printHighestVolumeDay(PrintWriter out, StockRecord record) {
        out.println("<h2>Highest Volume Day Analysis</h2>");
        out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
        out.println("<tr><th>Date</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Volume</th></tr>");
        out.println("<tr>");
        out.println("<td>" + record.date() + "</td>");
        out.println("<td>" + String.format("%.2f", record.open()) + "</td>");
        out.println("<td>" + String.format("%.2f", record.high()) + "</td>");
        out.println("<td>" + String.format("%.2f", record.low()) + "</td>");
        out.println("<td>" + String.format("%.2f", record.close()) + "</td>");
        out.println("<td>" + String.format("%,d", record.volume()) + "</td>");
        out.println("</tr>");
        out.println("</table>");
    }
    
    /**
     * Prints analysis result for highest volume year.
     * 
     * @param out the print writer
     * @param year the year with highest volume
     */
    public void printHighestVolumeYear(PrintWriter out, int year) {
        out.println("<h2>Highest Volume Year Analysis</h2>");
        out.println("<p>Year with highest total trading volume: <strong>" + year + "</strong></p>");
    }
    
    /**
     * Prints price sorted data (limited to first 20 records).
     * 
     * @param out the print writer
     * @param sortedRecords the sorted stock records
     */
    public void printPriceSorting(PrintWriter out, List<StockRecord> sortedRecords) {
        out.println("<h2>Price Sorting Analysis</h2>");
        out.println("<p>Data sorted by closing price (showing first 20 records):</p>");
        out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
        out.println("<tr><th>Date</th><th>Open</th><th>High</th><th>Low</th><th>Close</th><th>Volume</th></tr>");
        
        int displayCount = Math.min(20, sortedRecords.size());
        for (int i = 0; i < displayCount; i++) {
            StockRecord record = sortedRecords.get(i);
            out.println("<tr>");
            out.println("<td>" + record.date() + "</td>");
            out.println("<td>" + String.format("%.2f", record.open()) + "</td>");
            out.println("<td>" + String.format("%.2f", record.high()) + "</td>");
            out.println("<td>" + String.format("%.2f", record.low()) + "</td>");
            out.println("<td>" + String.format("%.2f", record.close()) + "</td>");
            out.println("<td>" + String.format("%,d", record.volume()) + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        
        if (sortedRecords.size() > 20) {
            out.println("<p><em>Total records: " + sortedRecords.size() + "</em></p>");
        }
    }
    
    /**
     * Prints price-volume correlation result.
     * 
     * @param out the print writer
     * @param correlation the correlation coefficient
     */
    public void printPriceVolumeCorrelation(PrintWriter out, double correlation) {
        out.println("<h2>Price-Volume Correlation Analysis</h2>");
        out.println("<p>Pearson correlation coefficient between opening price and volume:</p>");
        out.println("<p><strong>" + String.format("%.4f", correlation) + "</strong></p>");
        
        String interpretation;
        if (correlation > 0.7) {
            interpretation = "Strong positive correlation";
        } else if (correlation > 0.3) {
            interpretation = "Moderate positive correlation";
        } else if (correlation > -0.3) {
            interpretation = "Weak or no correlation";
        } else if (correlation > -0.7) {
            interpretation = "Moderate negative correlation";
        } else {
            interpretation = "Strong negative correlation";
        }
        
        out.println("<p><em>Interpretation: " + interpretation + "</em></p>");
    }
    
    /**
     * Prints cookie information with clear labels about data source.
     * 
     * @param out the print writer
     * @param lastAnalysisType last analysis type from cookie
     * @param analysisCount analysis count from cookie
     */
    public void printCookieInfo(PrintWriter out, String lastAnalysisType, String analysisCount) {
        out.println("<hr>");
        out.println("<h3>Session Information from Cookies</h3>");
        out.println("<p><em>This information is stored in browser cookies (client-side storage)</em></p>");
        out.println("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
        out.println("<tr><th>Data Source</th><th>Information</th><th>Value</th></tr>");
        
        out.println("<tr>");
        out.println("<td><strong>[FROM COOKIE]</strong></td>");
        out.println("<td>Last analysis type</td>");
        if (lastAnalysisType != null && !lastAnalysisType.isEmpty()) {
            out.println("<td>" + escapeHtml(lastAnalysisType) + "</td>");
        } else {
            out.println("<td><em>None (first visit)</em></td>");
        }
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td><strong>[FROM COOKIE]</strong></td>");
        out.println("<td>Total analyses performed in this session</td>");
        if (analysisCount != null && !analysisCount.isEmpty()) {
            out.println("<td>" + escapeHtml(analysisCount) + "</td>");
        } else {
            out.println("<td>1</td>");
        }
        out.println("</tr>");
        
        out.println("</table>");
    }
    
    /**
     * Prints analysis history.
     * 
     * @param out the print writer
     * @param history list of analysis history entries
     */
    public void printHistory(PrintWriter out, List<String> history) {
        out.println("<h2>Analysis History</h2>");
        
        if (history == null || history.isEmpty()) {
            out.println("<p><em>No analysis history available yet.</em></p>");
        } else {
            out.println("<p>Last " + history.size() + " analyses:</p>");
            out.println("<ol>");
            for (String entry : history) {
                out.println("<li>" + escapeHtml(entry) + "</li>");
            }
            out.println("</ol>");
        }
    }
    
    /**
     * Escapes HTML special characters to prevent XSS.
     * 
     * @param text the text to escape
     * @return escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}