package pl.polsl.model;

/**
 * Custom exception for Tesla analysis application.
 * 
 * @author Blazej Jamrozik
 * @version 1.0 - Basic custom exception with message constructor
 * @version 2.0 - Added constructor with Throwable cause parameter
 */

public class TeslaAnalysisException extends Exception {
    
    /**
     * Constructs exception with message.
     * 
     * @param message error message
     */
    public TeslaAnalysisException(String message) {
        super(message);
    }
    
    /**
     * Constructs exception with message and cause.
     * 
     * @param message error message
     * @param cause cause of exception
     */
    public TeslaAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
