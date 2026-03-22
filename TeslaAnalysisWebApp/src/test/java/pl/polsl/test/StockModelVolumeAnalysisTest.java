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
import pl.polsl.model.StockRecord;
import pl.polsl.model.TeslaAnalysisException;

/**
 * Unit tests for volume analysis methods in StockModel class.
 * Tests getHighestVolumeDay and getYearWithHighestVolume methods.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockModelVolumeAnalysisTest {
    
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
        File tempFile = File.createTempFile("test_volume", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("Date,Open,High,Low,Close,Adj Close,Volume\n");
            writer.write(records);
        }
        return tempFile;
    }
    
    /**
     * Provides test data for highest volume day tests.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideHighestVolumeDayData() {
        return Stream.of(
            Arguments.of("Single record", 
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n",
                1000000L),
            Arguments.of("Multiple records - highest at beginning",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,5000000\n" +
                "1/3/2020,103.0,108.0,102.0,107.5,107.5,1000000\n" +
                "1/4/2020,107.5,110.0,106.0,109.0,109.0,2000000\n",
                5000000L),
            Arguments.of("Multiple records - highest in middle",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,107.5,107.5,8000000\n" +
                "1/4/2020,107.5,110.0,106.0,109.0,109.0,2000000\n",
                8000000L),
            Arguments.of("Multiple records - highest at end",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,107.5,107.5,2000000\n" +
                "1/4/2020,107.5,110.0,106.0,109.0,109.0,9000000\n",
                9000000L)
        );
    }
    
    /**
     * Tests finding day with highest volume with valid data.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedVolume expected highest volume
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if analysis fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHighestVolumeDayData")
    public void testGetHighestVolumeDay(String description, String csvData, Long expectedVolume) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        StockRecord result = model.getHighestVolumeDay();
        
        assertNotNull(result, "Result should not be null");
        assertEquals(expectedVolume, result.volume(), 
            "Should return record with highest volume");
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for boundary volume values.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideBoundaryVolumeData() {
        return Stream.of(
            Arguments.of("Minimum volume value (1)",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1\n",
                1L),
            Arguments.of("Very large volume value",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,999999999999\n",
                999999999999L),
            Arguments.of("Equal volumes - should return first occurrence",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,5000000\n" +
                "1/3/2020,103.0,108.0,102.0,107.5,107.5,5000000\n" +
                "1/4/2020,107.5,110.0,106.0,109.0,109.0,5000000\n",
                5000000L)
        );
    }
    
    /**
     * Tests finding highest volume day with boundary values.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedVolume expected volume
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if analysis fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBoundaryVolumeData")
    public void testGetHighestVolumeDayBoundary(String description, String csvData, Long expectedVolume) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        StockRecord result = model.getHighestVolumeDay();
        
        assertEquals(expectedVolume, result.volume(), 
            "Should handle boundary volume values correctly");
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for testing exception when no data loaded.
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
     * Tests exception thrown when calling getHighestVolumeDay on empty model.
     * 
     * @param description test case description
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideEmptyModelDescriptions")
    public void testGetHighestVolumeDayNoData(String description) {
        TeslaAnalysisException exception = assertThrows(
            TeslaAnalysisException.class,
            () -> model.getHighestVolumeDay(),
            "Should throw exception when no data available"
        );
        
        assertTrue(exception.getMessage().contains("No data"), 
            "Exception message should indicate no data available");
    }
    
    /**
     * Provides test data for year with highest volume tests.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideHighestVolumeYearData() {
        return Stream.of(
            Arguments.of("Single year",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
                "2/3/2020,103.0,108.0,102.0,107.5,107.5,2000000\n",
                2020),
            Arguments.of("Multiple years - 2020 highest",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,5000000\n" +
                "2/3/2020,103.0,108.0,102.0,107.5,107.5,6000000\n" +
                "1/4/2021,107.5,110.0,106.0,109.0,109.0,2000000\n" +
                "2/5/2021,110.0,112.0,108.0,111.0,111.0,1000000\n",
                2020),
            Arguments.of("Multiple years - 2021 highest",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
                "2/3/2020,103.0,108.0,102.0,107.5,107.5,2000000\n" +
                "1/4/2021,107.5,110.0,106.0,109.0,109.0,5000000\n" +
                "2/5/2021,110.0,112.0,108.0,111.0,111.0,6000000\n",
                2021),
            Arguments.of("Multiple years - 2022 highest",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,2000000\n" +
                "1/3/2021,103.0,108.0,102.0,107.5,107.5,3000000\n" +
                "1/4/2022,107.5,110.0,106.0,109.0,109.0,8000000\n",
                2022)
        );
    }
    
    /**
     * Tests finding year with highest total volume.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedYear expected year with highest volume
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if analysis fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHighestVolumeYearData")
    public void testGetYearWithHighestVolume(String description, String csvData, Integer expectedYear) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        int result = model.getYearWithHighestVolume();
        
        assertEquals(expectedYear, result, 
            "Should return year with highest total volume");
        
        tempFile.delete();
    }
    
    /**
     * Provides test data for boundary year scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideBoundaryYearData() {
        return Stream.of(
            Arguments.of("Equal total volumes across years - returns one year",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,5000000\n" +
                "1/3/2021,103.0,108.0,102.0,107.5,107.5,5000000\n" +
                "1/4/2022,107.5,110.0,106.0,109.0,109.0,5000000\n",
                2020),
            Arguments.of("Many days in single year",
                "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
                "1/3/2020,103.0,108.0,102.0,107.5,107.5,1000000\n" +
                "1/4/2020,107.5,110.0,106.0,109.0,109.0,1000000\n" +
                "1/5/2020,110.0,112.0,108.0,111.0,111.0,1000000\n" +
                "1/6/2020,112.0,115.0,110.0,113.0,113.0,1000000\n",
                2020)
        );
    }
    
    /**
     * Tests finding year with highest volume with boundary scenarios.
     * 
     * @param description test case description
     * @param csvData CSV data to test
     * @param expectedYear expected year with highest volume
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if analysis fails
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBoundaryYearData")
    public void testGetYearWithHighestVolumeBoundary(String description, String csvData, Integer expectedYear) 
            throws IOException, TeslaAnalysisException {
        File tempFile = createTempCsvFile(csvData);
        model.loadData(tempFile.getAbsolutePath());
        
        int result = model.getYearWithHighestVolume();
       
        assertTrue(result >= 2010 && result <= 2022, 
            "Result should be a valid year within reasonable range");
        
        assertEquals(expectedYear, result, 
            "Should return correct year with highest volume for boundary case");
        
        tempFile.delete();
    }
    
    /**
     * Tests exception thrown when calling getYearWithHighestVolume on empty model.
     * 
     * @param description test case description
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideEmptyModelDescriptions")
    public void testGetYearWithHighestVolumeNoData(String description) {
        TeslaAnalysisException exception = assertThrows(
            TeslaAnalysisException.class,
            () -> model.getYearWithHighestVolume(),
            "Should throw exception when no data available"
        );
        
        assertTrue(exception.getMessage().contains("No data"), 
            "Exception message should indicate no data available");
    }
   
}