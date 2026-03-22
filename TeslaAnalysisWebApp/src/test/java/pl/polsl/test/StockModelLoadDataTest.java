package pl.polsl.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import pl.polsl.model.StockModel;
import pl.polsl.model.TeslaAnalysisException;

/**
 * Unit tests for StockModel.loadData() method.
 * Tests normal cases, boundary cases, and exceptional cases.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */

class StockModelLoadDataTest {
    
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
     * @param fileName name of the file
     * @param content content to write
     * @return created File object
     * @throws IOException if file creation fails
     */
    private File createTempCsvFile(String fileName, String content) throws IOException {
        File tempFile = File.createTempFile(fileName, ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
     
    /**
     * Test loading file with invalid format (not CSV).
     * Exceptional case test - wrong file format.
     * 
     * @param filePath path to invalid format file
     */
    @ParameterizedTest(name = "Test invalid file format: {0}")
    @ValueSource(strings = {
        "src/test/resources/invalid_format.txt",
        "src/test/resources/other_invalid_format.pdf"
    })
    void testLoadDataWithInvalidFormatFile(String filePath) {
        
        TeslaAnalysisException exception = assertThrows(TeslaAnalysisException.class,
            () -> model.loadData(filePath),
            "Loading non-CSV file should throw TeslaAnalysisException for file: " + filePath);
        
        assertNotNull(exception.getMessage(),
            "Exception should have a message for invalid format file: " + filePath);
    }
    
    /**
     * Tests loading valid data from CSV file.
     * Verifies that data is correctly loaded and accessible.
     * 
     * @param fileName name of temporary test file
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if data loading fails
     */
    @ParameterizedTest(name = "Loading valid data from file: {0}")
    @ValueSource(strings = {"test_valid_1.csv", "test_valid_2.csv", "test_valid_3.csv"})
    public void testLoadValidData(String fileName) throws IOException, TeslaAnalysisException {
        
        File tempFile = createTempCsvFile(fileName,
            "Date,Open,High,Low,Close,Adj Close,Volume\n" +
            "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
            "1/3/2020,103.0,108.0,102.0,107.5,107.5,1500000\n");
        
        model.loadData(tempFile.getAbsolutePath());
        
        assertTrue(model.isDataLoaded(), "Data should be loaded successfully");
        assertEquals(2, model.getStockRecords().size(), "Should load exactly 2 records");
        
        tempFile.delete();
    }
    
    /**
     * Tests loading data from file with empty lines.
     * Empty lines should be filtered out during loading.
     * 
     * @param lineCount number of empty lines to test
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if data loading fails
     */
    @ParameterizedTest(name = "Loading data with {0} empty lines")
    @ValueSource(ints = {1, 3, 5})
    public void testLoadDataWithEmptyLines(int lineCount) throws IOException, TeslaAnalysisException {
        StringBuilder content = new StringBuilder("Date,Open,High,Low,Close,Adj Close,Volume\n");
        content.append("1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n");
        
        for (int i = 0; i < lineCount; i++) {
            content.append("\n");
        }
        
        content.append("1/3/2020,103.0,108.0,102.0,107.5,107.5,1500000\n");
        
        File tempFile = createTempCsvFile("test_empty_lines.csv", content.toString());
        
        model.loadData(tempFile.getAbsolutePath());
        
        assertEquals(2, model.getStockRecords().size(), 
            "Should load 2 records ignoring empty lines");
        
        tempFile.delete();
    }
    
    /**
     * Tests loading data from file with malformed records.
     * Malformed records should be skipped without causing failure.
     * 
     * @param malformedRecord the malformed CSV record to test
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if data loading fails
     */
    @ParameterizedTest(name = "Loading data with malformed record: {0}")
    @ValueSource(strings = {
        "invalid,data,here",
        "1/2/2020,not_a_number,105.0,99.0,103.0,103.0,1000000",
        "1/2/2020,100.5,105.0,99.0,103.0,103.0,not_a_number"
    })
    public void testLoadDataWithMalformedRecords(String malformedRecord) 
            throws IOException, TeslaAnalysisException {
        String content = "Date,Open,High,Low,Close,Adj Close,Volume\n" +
            "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
            malformedRecord + "\n" +
            "1/3/2020,103.0,108.0,102.0,107.5,107.5,1500000\n";
        
        File tempFile = createTempCsvFile("test_malformed.csv", content);
        
        model.loadData(tempFile.getAbsolutePath());
        
        assertEquals(2, model.getStockRecords().size(), 
            "Should load 2 valid records, skipping malformed one");
        
        tempFile.delete();
    }
    
    
    /**
     * Tests loading from non-existent file.
     * Should throw TeslaAnalysisException.
     * 
     * @param filePath path to non-existent file
     */
    @ParameterizedTest(name = "Loading from non-existent file: {0}")
    @ValueSource(strings = {
        "nonexistent.csv",
        "path/to/nonexistent.csv",
        "C:/nonexistent/file.csv"
    })
    public void testLoadDataFromNonExistentFile(String filePath) {
        TeslaAnalysisException exception = assertThrows(
            TeslaAnalysisException.class,
            () -> model.loadData(filePath),
            "Should throw exception for non-existent file"
        );
        
        assertNotNull(exception.getMessage(), "Exception should have a message");
    }
    
    /**
     * Tests loading data with null file path.
     * Should throw TeslaAnalysisException.
     * 
     * @param filePath null file path
     */
    @ParameterizedTest(name = "Loading data with null file path")
    @NullSource
    public void testLoadDataWithNullPath(String filePath) {
        assertThrows(
            TeslaAnalysisException.class,
            () -> model.loadData(filePath),
            "Should throw exception for null file path"
        );
    }
    
    /**
     * Tests loading data multiple times.
     * Previous data should be cleared on each load.
     * 
     * @param recordCount number of records in each load
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if data loading fails
     */
    @ParameterizedTest(name = "Loading data multiple times with {0} records")
    @ValueSource(ints = {1, 2, 5})
    public void testLoadDataMultipleTimes(int recordCount) 
            throws IOException, TeslaAnalysisException {
        StringBuilder content = new StringBuilder("Date,Open,High,Low,Close,Adj Close,Volume\n");
        for (int i = 1; i <= recordCount; i++) {
            content.append(String.format("1/%d/2020,100.0,105.0,99.0,103.0,103.0,1000000\n", i));
        }
        
        File tempFile = createTempCsvFile("test_multiple.csv", content.toString());
        
        // First load
        model.loadData(tempFile.getAbsolutePath());
        assertEquals(recordCount, model.getStockRecords().size(), 
            "Should load correct number of records on first load");
        
        // Second load
        model.loadData(tempFile.getAbsolutePath());
        assertEquals(recordCount, model.getStockRecords().size(), 
            "Should clear previous data and load fresh data");
        
        tempFile.delete();
    }
    
    /**
     * Tests loading data with minimum required columns.
     * Records with fewer than 7 columns should be skipped.
     * 
     * @param columnCount number of columns to test
     * @throws IOException if file operations fail
     * @throws TeslaAnalysisException if data loading fails
     */
    @ParameterizedTest(name = "Loading data with {0} columns")
    @ValueSource(ints = {6, 5, 3, 1})
    public void testLoadDataWithInsufficientColumns(int columnCount) 
            throws IOException, TeslaAnalysisException {
        StringBuilder invalidRecord = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            if (i > 0) invalidRecord.append(",");
            invalidRecord.append("value");
        }
        
        String content = "Date,Open,High,Low,Close,Adj Close,Volume\n" +
            "1/2/2020,100.5,105.0,99.0,103.0,103.0,1000000\n" +
            invalidRecord + "\n" +
            "1/3/2020,103.0,108.0,102.0,107.5,107.5,1500000\n";
        
        File tempFile = createTempCsvFile("test_columns.csv", content);
        
        model.loadData(tempFile.getAbsolutePath());
        
        assertEquals(2, model.getStockRecords().size(), 
            "Should skip records with insufficient columns");
        
        tempFile.delete();
    }
    
}
