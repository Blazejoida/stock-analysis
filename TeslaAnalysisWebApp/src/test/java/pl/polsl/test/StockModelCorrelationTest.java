package pl.polsl.test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


import pl.polsl.model.StockModel;
import pl.polsl.model.TeslaAnalysisException;

/**
 * Unit tests for price-volume correlation calculation in StockModel class.
 * Tests calculatePriceVolumeCorrelation method with various scenarios.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockModelCorrelationTest {
    
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
        File tempFile = File.createTempFile("test_correlation", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Date,Open,High,Low,Close,Adj Close,Volume\n");
            writer.write(records);
        }
        return tempFile;
    }
    
    /**
     * Provides test data for positive correlation scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> providePositiveCorrelationData() {
        return Stream.of(
            Arguments.of("Perfect positive correlation",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,110.0,115.0,109.0,113.0,113.0,2000000\n" +
                "1/4/2020,120.0,125.0,119.0,123.0,123.0,3000000\n",
                1.0),
            Arguments.of("Strong positive correlation",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,105.0,110.0,104.0,108.0,108.0,1500000\n" +
                "1/4/2020,110.0,115.0,109.0,113.0,113.0,2000000\n" +
                "1/5/2020,115.0,120.0,114.0,118.0,118.0,2500000\n",
                1.0),
            Arguments.of("Moderate positive correlation",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,105.0,110.0,104.0,108.0,108.0,1200000\n" +
                "1/4/2020,110.0,115.0,109.0,113.0,113.0,1800000\n" +
                "1/5/2020,108.0,113.0,107.0,111.0,111.0,1500000\n",
                0.8)
        );
    }
    
    /**
     * Tests correlation calculation with positive correlation data.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedMinCorrelation minimum expected correlation value
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if calculation fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("providePositiveCorrelationData")
    public void testPositiveCorrelation(String description, String csvData, double expectedMinCorrelation) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        double correlation = model.calculatePriceVolumeCorrelation();
        
        assertTrue(correlation >= expectedMinCorrelation, 
            "Correlation should be at least " + expectedMinCorrelation);
        assertTrue(correlation >= -1.0 && correlation <= 1.0, 
            "Correlation should be between -1 and 1");
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for negative correlation scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideNegativeCorrelationData() {
        return Stream.of(
            Arguments.of("Perfect negative correlation",
                "1/2/2020,120.0,125.0,119.0,123.0,123.0,1000000\n" +
                "1/3/2020,110.0,115.0,109.0,113.0,113.0,2000000\n" +
                "1/4/2020,100.0,105.0,99.0,103.0,103.0,3000000\n",
                -1.0),
            Arguments.of("Strong negative correlation",
                "1/2/2020,115.0,120.0,114.0,118.0,118.0,1000000\n" +
                "1/3/2020,110.0,115.0,109.0,113.0,113.0,1500000\n" +
                "1/4/2020,105.0,110.0,104.0,108.0,108.0,2000000\n" +
                "1/5/2020,100.0,105.0,99.0,103.0,103.0,2500000\n",
                -1.0),
            Arguments.of("Moderate negative correlation",
                "1/2/2020,110.0,115.0,109.0,113.0,113.0,1000000\n" +
                "1/3/2020,108.0,113.0,107.0,111.0,111.0,1200000\n" +
                "1/4/2020,105.0,110.0,104.0,108.0,108.0,1500000\n" +
                "1/5/2020,107.0,112.0,106.0,110.0,110.0,1300000\n",
                -0.8)
        );
    }
    
    /**
     * Tests correlation calculation with negative correlation data.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedMaxCorrelation maximum expected correlation value
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if calculation fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNegativeCorrelationData")
    public void testNegativeCorrelation(String description, String csvData, double expectedMaxCorrelation) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        double correlation = model.calculatePriceVolumeCorrelation();
        
        assertTrue(correlation <= expectedMaxCorrelation, 
            "Correlation should be at most " + expectedMaxCorrelation);
        assertTrue(correlation >= -1.0 && correlation <= 1.0, 
            "Correlation should be between -1 and 1");
        
        tempFile.delete();
    }
    
    
    /**
     * Provides test data for boundary scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideBoundaryData() {
        return Stream.of(
            Arguments.of("Minimum two data points",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,110.0,115.0,109.0,113.0,113.0,2000000\n"),
            Arguments.of("Very large opening prices and volumes",
                "1/2/2020,10000.0,10005.0,9999.0,10003.0,10003.0,100000000\n" +
                "1/3/2020,11000.0,11005.0,10999.0,11003.0,11003.0,200000000\n" +
                "1/4/2020,12000.0,12005.0,11999.0,12003.0,12003.0,300000000\n"),
            Arguments.of("Small opening prices",
                "1/2/2020,10.0,15.0,9.0,13.0,13.0,1000000\n" +
                "1/3/2020,20.0,25.0,19.0,23.0,23.0,2000000\n" +
                "1/4/2020,30.0,35.0,29.0,33.0,33.0,3000000\n")
        );
    }
    
    /**
     * Tests correlation calculation with boundary values.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if calculation fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBoundaryData")
    public void testCorrelationBoundary(String description, String csvData) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        double correlation = model.calculatePriceVolumeCorrelation();
        
        assertTrue(correlation >= -1.0 && correlation <= 1.0, 
            "Correlation should always be between -1 and 1");
        assertFalse(Double.isNaN(correlation), 
            "Correlation should not be NaN");
        assertFalse(Double.isInfinite(correlation), 
            "Correlation should not be infinite");
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for insufficient data scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideInsufficientDataScenarios() {
        return Stream.of(
            Arguments.of("Empty model - no data", 
                "", 
                "No data available for correlation calculation"),
            Arguments.of("Single data point",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n",
                "Insufficient data points for correlation calculation")
        );
    }
    
    /**
     * Tests exception thrown with insufficient data for correlation.
     * 
     * @param description test case description
     * @param csvData CSV data to test (empty for no data)
     * @param expectedMessagePart expected part of exception message
     * @throws IOException if file operations fail
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsufficientDataScenarios")
    public void testCorrelationInsufficientData(String description, String csvData, String expectedMessagePart) 
            throws IOException {
        if (!csvData.isEmpty()) {
            File tempFile = createTempCsvFile(csvData);
            try {
                model.loadData(tempFile.getAbsolutePath());
            } catch (TeslaAnalysisException e) {
                fail("Data loading should not fail: " + e.getMessage());
            }
            tempFile.delete();
        }
        
        TeslaAnalysisException exception = assertThrows(
            TeslaAnalysisException.class,
            () -> model.calculatePriceVolumeCorrelation(),
            "Should throw exception when insufficient data"
        );
        
        assertTrue(exception.getMessage().contains(expectedMessagePart), 
            "Exception message should contain: " + expectedMessagePart);
    }
    
    /**
     * Provides test data for constant value scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideConstantValueData() {
        return Stream.of(
            Arguments.of("Constant opening price",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,100.0,108.0,102.0,107.5,107.5,2000000\n" +
                "1/4/2020,100.0,110.0,106.0,109.0,109.0,3000000\n"),
            Arguments.of("Constant volume",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,110.0,115.0,109.0,113.0,113.0,1000000\n" +
                "1/4/2020,120.0,125.0,119.0,123.0,123.0,1000000\n"),
            Arguments.of("Both constant",
                "1/2/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,100.0,105.0,99.0,103.0,103.0,1000000\n" +
                "1/4/2020,100.0,105.0,99.0,103.0,103.0,1000000\n")
        );
    }
    
    /**
     * Tests correlation calculation with constant values.
     * Should throw exception as correlation cannot be calculated.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @throws IOException if file operations fail
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideConstantValueData")
    public void testCorrelationConstantValues(String description, String csvData) 
            throws IOException {
        File tempFile = createTempCsvFile(csvData);
        
        try {
            model.loadData(tempFile.getAbsolutePath());
            
            TeslaAnalysisException exception = assertThrows(
                TeslaAnalysisException.class,
                () -> model.calculatePriceVolumeCorrelation(),
                "Should throw exception when denominator is zero"
            );
            
            assertTrue(exception.getMessage().contains("denominator is zero") ||
                      exception.getMessage().contains("Cannot calculate correlation"),
                "Exception should indicate denominator is zero");
        } catch (TeslaAnalysisException e) {
            fail("Data loading should not fail: " + e.getMessage());
        } finally {
            tempFile.delete();
        }
    }
     
}