package pl.polsl.model;

/**
 * Enum representing different types of stock analysis.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
public enum AnalysisType {
    /** Analysis for highest volume day. */
    HIGHEST_VOLUME_DAY,
    
    /** Analysis for highest volume year. */
    HIGHEST_VOLUME_YEAR,
    
    /** Analysis for price sorting. */
    PRICE_SORTING,
    
    /** Analysis for price-volume correlation. */
    PRICE_VOLUME_CORRELATION
}