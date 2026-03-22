package pl.polsl.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import pl.polsl.model.StockModel;
import pl.polsl.model.StockRecord;
import pl.polsl.model.TeslaAnalysisException;

/**
 * Unit tests for price sorting functionality in StockModel class.
 * Tests getDataSortedByPrice method with various scenarios.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockModelSortingTest {
    
    /** Reference to the model. */
    private StockModel model;
    
    /**
     * Sets up test environment before each test.
     */
    @BeforeEach
    public void setUp() {
        model = new StockModel();
    }
    
    
    /**
     * Helper method to create temporary CSV file for testing.
     * 
     * @param records CSV records without header
     * @return created File object
     * @throws IOException if file creation fails
     */
    private File createTempCsvFile(String records) throws IOException {
        File tempFile = File.createTempFile("test_sorting", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Date,Open,High,Low,Close,Adj Close,Volume\n");
            writer.write(records);
        }
        return tempFile;
    }
    
    
    /**
     * Provides test data for price sorting tests.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideSortingData() {
        return Stream.of(
            Arguments.of("Already sorted ascending",
                "1/2/2020,100.5,105.0,99.0,100.0,100.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,105.0,105.0,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,110.0,110.0,3000000\n",
                new double[]{100.0, 105.0, 110.0}),
            Arguments.of("Sorted descending - needs reversal",
                "1/2/2020,100.5,105.0,99.0,110.0,110.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,105.0,105.0,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,100.0,100.0,3000000\n",
                new double[]{100.0, 105.0, 110.0}),
            Arguments.of("Random order",
                "1/2/2020,100.5,105.0,99.0,105.0,105.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,100.0,100.0,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,110.0,110.0,3000000\n",
                new double[]{100.0, 105.0, 110.0}),
            Arguments.of("Mixed order with duplicates in middle",
                "1/2/2020,100.5,105.0,99.0,110.0,110.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,105.0,105.0,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,105.0,105.0,3000000\n" +
                "1/5/2020,110.0,112.0,108.0,100.0,100.0,4000000\n",
                new double[]{100.0, 105.0, 105.0, 110.0})
        );
    }
    
    /**
     * Tests sorting stock data by closing price.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedClosePrices expected closing prices in sorted order
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if sorting fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSortingData")
    public void testGetDataSortedByPrice(String description, String csvData, double[] expectedClosePrices) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        List<StockRecord> sortedData = model.getDataSortedByPrice();
        
        assertNotNull(sortedData, "Sorted data should not be null");
        assertEquals(expectedClosePrices.length, sortedData.size(), 
            "Sorted data should contain all records");
        
        for (int i = 0; i < expectedClosePrices.length; i++) {
            assertEquals(expectedClosePrices[i], sortedData.get(i).close(), 0.001,
                "Record at position " + i + " should have correct closing price");
        }
        
        for (int i = 1; i < sortedData.size(); i++) {
            assertTrue(sortedData.get(i-1).close() <= sortedData.get(i).close(),
                "Data should be sorted in ascending order by closing price");
        }
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for boundary price values.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideBoundaryPriceData() {
        return Stream.of(
            Arguments.of("Single record",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n",
                new double[]{103.0}),
            Arguments.of("All equal prices",
                "1/2/2020,100.5,105.0,99.0,105.0,105.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,105.0,105.0,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,105.0,105.0,3000000\n",
                new double[]{105.0, 105.0, 105.0}),
            Arguments.of("Very small price differences",
                "1/2/2020,100.5,105.0,99.0,100.001,100.001,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,100.002,100.002,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,100.003,100.003,3000000\n",
                new double[]{100.001, 100.002, 100.003})
        );
    }
    
    /**
     * Tests sorting with boundary price values.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedClosePrices expected closing prices in sorted order
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if sorting fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBoundaryPriceData")
    public void testGetDataSortedByPriceBoundary(String description, String csvData, double[] expectedClosePrices) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        List<StockRecord> sortedData = model.getDataSortedByPrice();
        
        assertEquals(expectedClosePrices.length, sortedData.size(), 
            "Should handle boundary cases correctly");
        
        for (int i = 0; i < expectedClosePrices.length; i++) {
            assertEquals(expectedClosePrices[i], sortedData.get(i).close(), 0.0001,
                "Record at position " + i + " should have correct closing price");
        }
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for testing exception scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideEmptyModelDescriptions() {
        return Stream.of(
            Arguments.of("Empty model - no data loaded"),
            Arguments.of("Empty model - before any operation")
        );
    }
    
    /**
     * Tests exception thrown when calling getDataSortedByPrice on empty model.
     * 
     * @param description test case description
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideEmptyModelDescriptions")
    public void testGetDataSortedByPriceNoData(String description) {
        TeslaAnalysisException exception = assertThrows(
            TeslaAnalysisException.class,
            () -> model.getDataSortedByPrice(),
            "Should throw exception when no data available"
        );
        
        assertTrue(exception.getMessage().contains("No data"), 
            "Exception message should indicate no data available");
    }
   
}
