package pl.polsl.model;

//import lombok.Getter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Model for Tesla stock data analysis.
 * Uses type-safe collections and stream processing.
 * 
 * @author Blazej Jamrozik
 * @version 2.0
 */
//@Getter
public class StockModel {
    /** Type-safe collection of stock records. */
    private final List<StockRecord> stockRecords;
    
    /**
     * Constructs StockModel.
     */
    public StockModel() {
        this.stockRecords = new ArrayList<>();
    }
    
     /**
     * Gets stock records.
     * 
     * @return list of stock records
     */
    public List<StockRecord> getStockRecords() {
        return new ArrayList<>(stockRecords);
    }
    
    /**
     * Loads data from CSV file using streams.
     * 
     * @param filePath path to CSV file
     * @throws TeslaAnalysisException if loading fails
     */
    public void loadData(String filePath) throws TeslaAnalysisException {
        stockRecords.clear();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            
            List<StockRecord> loadedRecords = br.lines()
                .skip(1) // Skip header
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    String[] values = line.split(",");
                    if (values.length >= 7) {
                        try {
                            LocalDate date = LocalDate.parse(values[0].trim(), formatter);
                            double open = Double.parseDouble(values[1].trim());
                            double high = Double.parseDouble(values[2].trim());
                            double low = Double.parseDouble(values[3].trim());
                            double close = Double.parseDouble(values[4].trim());
                            double adjClose = Double.parseDouble(values[5].trim());
                            long volume = Long.parseLong(values[6].trim());
                            
                            return new StockRecord(date, open, high, low, close, adjClose, volume);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            stockRecords.addAll(loadedRecords);
            
            if (stockRecords.isEmpty()) {
                throw new TeslaAnalysisException("No valid data loaded from file");
            }
            
        } catch (Exception e) {
            throw new TeslaAnalysisException("Error loading data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets highest volume day using streams.
     * 
     * @return stock record with highest volume
     * @throws TeslaAnalysisException if no data available
     */
    public StockRecord getHighestVolumeDay() throws TeslaAnalysisException {
        if (stockRecords.isEmpty()) {
            throw new TeslaAnalysisException("No data available");
        }
        
        return stockRecords.stream()
            .max(Comparator.comparingLong(StockRecord::volume))
            .orElseThrow(() -> new TeslaAnalysisException("No data available"));
    }
    
    /**
     * Gets data sorted by closing price using streams.
     * 
     * @return sorted list of stock records
     * @throws TeslaAnalysisException if no data available
     */
    public List<StockRecord> getDataSortedByPrice() throws TeslaAnalysisException {
        if (stockRecords.isEmpty()) {
            throw new TeslaAnalysisException("No data available");
        }
        
        return stockRecords.stream()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Gets year with highest total volume using streams.
     * 
     * @return year with highest volume
     * @throws TeslaAnalysisException if no data available
     */
    public int getYearWithHighestVolume() throws TeslaAnalysisException {
        if (stockRecords.isEmpty()) {
            throw new TeslaAnalysisException("No data available");
        }
        
        Map<Integer, Long> yearVolumes = stockRecords.stream()
            .collect(Collectors.groupingBy(
                StockRecord::getYear,
                Collectors.summingLong(StockRecord::volume)
            ));
        
        return yearVolumes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new TeslaAnalysisException("No data available"));
    }
    
    /**
     * Calculates Pearson correlation between opening price and volume.
     * 
     * @return Pearson correlation coefficient
     * @throws TeslaAnalysisException if calculation fails or no data available
     */
    public double calculatePriceVolumeCorrelation() throws TeslaAnalysisException {
        if (stockRecords.isEmpty()) {
            throw new TeslaAnalysisException("No data available for correlation calculation");
        }
        
        if (stockRecords.size() < 2) {
            throw new TeslaAnalysisException("Insufficient data points for correlation calculation");
        }
        
        int n = stockRecords.size();
        double sumOpen = 0.0;
        double sumVolume = 0.0;
        double sumOpenVolume = 0.0;
        double sumOpenSquared = 0.0;
        double sumVolumeSquared = 0.0;
        
        for (StockRecord record : stockRecords) {
            double open = record.open();
            double volume = (double) record.volume();
            
            sumOpen += open;
            sumVolume += volume;
            sumOpenVolume += open * volume;
            sumOpenSquared += open * open;
            sumVolumeSquared += volume * volume;
        }
        
        double numerator = n * sumOpenVolume - sumOpen * sumVolume;
        double denominator = Math.sqrt(
            (n * sumOpenSquared - sumOpen * sumOpen) * 
            (n * sumVolumeSquared - sumVolume * sumVolume)
        );
        
        if (denominator == 0) {
            throw new TeslaAnalysisException("Cannot calculate correlation - denominator is zero");
        }
        
        return numerator / denominator;
    }
    
    /**
     * Gets analysis description based on type.
     * 
     * @param analysisType type of analysis
     * @return description of analysis
     */
    public String getAnalysisDescription(AnalysisType analysisType) {
        return switch (analysisType) {
            case HIGHEST_VOLUME_DAY -> "Finds the day with highest trading volume";
            case HIGHEST_VOLUME_YEAR -> "Finds the year with highest total trading volume";
            case PRICE_SORTING -> "Sorts stock data by closing price in ascending order";
            case PRICE_VOLUME_CORRELATION -> "Calculates Pearson correlation between opening price and volume";
        };
    }
    
    /**
     * Checks if data is loaded.
     * 
     * @return true if data loaded
     */
    public boolean isDataLoaded() {
        return !stockRecords.isEmpty();
    }
}