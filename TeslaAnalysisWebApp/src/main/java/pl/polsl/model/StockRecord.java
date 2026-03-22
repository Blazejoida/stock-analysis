package pl.polsl.model;

import java.time.LocalDate;

/**
 * Record representing Tesla stock data for a single day.
 * Immutable data carrier for stock information.
 * 
 * @param date the date of the stock data
 * @param open the opening price
 * @param high the highest price of the day
 * @param low the lowest price of the day
 * @param close the closing price
 * @param adjClose the adjusted closing price
 * @param volume the trading volume
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
public record StockRecord(
    LocalDate date,
    double open,
    double high,
    double low,
    double close,
    double adjClose,
    long volume
) implements Comparable<StockRecord> {
    
    /**
     * Gets year from date.
     * 
     * @return year
     */
    public int getYear() {
        return date.getYear();
    }
    
    /**
     * Compares by closing price for sorting.
     * 
     * @param other other StockRecord to compare
     * @return comparison result
     */
    @Override
    public int compareTo(StockRecord other) {
        return Double.compare(this.close, other.close);
    }
}
