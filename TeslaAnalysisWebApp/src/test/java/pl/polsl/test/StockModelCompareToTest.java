package pl.polsl.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import pl.polsl.model.StockRecord;


/**
 * Unit tests for CompareTo method in StockModel class.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockModelCompareToTest {
    
        
    /**
     * Provides test data for compareTo method tests.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideCompareToData() {
        return Stream.of(
            Arguments.of("Lower price comes first",
                new StockRecord(LocalDate.of(2020, 1, 1), 100.0, 105.0, 99.0, 100.0, 100.0, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 110.0, 115.0, 109.0, 110.0, 110.0, 2000000),
                -1),
            Arguments.of("Higher price comes second",
                new StockRecord(LocalDate.of(2020, 1, 1), 110.0, 115.0, 109.0, 110.0, 110.0, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 100.0, 105.0, 99.0, 100.0, 100.0, 2000000),
                1),
            Arguments.of("Equal prices",
                new StockRecord(LocalDate.of(2020, 1, 1), 105.0, 110.0, 104.0, 105.0, 105.0, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 106.0, 111.0, 105.0, 105.0, 105.0, 2000000),
                0)
        );
    }
    
    /**
     * Tests compareTo method compares by closing price.
     * 
     * @param description test case description
     * @param record1 first record to compare
     * @param record2 second record to compare
     * @param expectedSign expected sign of comparison result (-1, 0, or 1)
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCompareToData")
    public void testCompareTo(String description, StockRecord record1, StockRecord record2, int expectedSign) {
        int result = record1.compareTo(record2);
        
        if (expectedSign < 0) {
            assertTrue(result < 0, 
                "Record with lower closing price should compare as less than");
        } else if (expectedSign > 0) {
            assertTrue(result > 0, 
                "Record with higher closing price should compare as greater than");
        } else {
            assertEquals(0, result, 
                "Records with equal closing prices should compare as equal");
        }
    }
    
    /**
     * Provides test data for compareTo boundary scenarios.
     * 
     * @return stream of test arguments
     */
    private Stream<Arguments> provideCompareToBoundaryData() {
        return Stream.of(
            Arguments.of("Very small price difference",
                new StockRecord(LocalDate.of(2020, 1, 1), 100.0, 105.0, 99.0, 100.001, 100.001, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 100.0, 105.0, 99.0, 100.002, 100.002, 2000000),
                -1),
            Arguments.of("Very large prices",
                new StockRecord(LocalDate.of(2020, 1, 1), 9999.0, 10005.0, 9998.0, 9999.0, 9999.0, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 10000.0, 10006.0, 9999.0, 10000.0, 10000.0, 2000000),
                -1),
            Arguments.of("Very small prices",
                new StockRecord(LocalDate.of(2020, 1, 1), 0.01, 0.015, 0.009, 0.01, 0.01, 1000000),
                new StockRecord(LocalDate.of(2020, 1, 2), 0.02, 0.025, 0.019, 0.02, 0.02, 2000000),
                -1),
            Arguments.of("Same price",
                new StockRecord(LocalDate.of(2020, 1, 1), 105.0, 110.0, 104.0, 105.0, 105.0, 1000000),
                new StockRecord(LocalDate.of(2020, 12, 31), 106.0, 111.0, 105.0, 105.0, 105.0, 2000000),
                0)
        );
    }
    
    /**
     * Tests compareTo method with boundary values.
     * 
     * @param description test case description
     * @param record1 first record to compare
     * @param record2 second record to compare
     * @param expectedSign expected sign of comparison result
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCompareToBoundaryData")
    public void testCompareToBoundary(String description, StockRecord record1, StockRecord record2, int expectedSign) {
        int result = record1.compareTo(record2);
        
        if (expectedSign < 0) {
            assertTrue(result < 0, 
                "Should handle boundary comparisons correctly");
        } else if (expectedSign > 0) {
            assertTrue(result > 0, 
                "Should handle boundary comparisons correctly");
        } else {
            assertEquals(0, result, 
                "Should handle boundary comparisons correctly");
        }
    }
    
    /**
     * Tests compareTo method with null parameter.
     * 
     * @param description test case description
     */
    @ParameterizedTest(name = "Null comparison")
    @MethodSource("provideCompareToData")
    public void testCompareToWithNull(String description, StockRecord record) {
        assertThrows(NullPointerException.class, 
            () -> record.compareTo(null),
            "Should throw NullPointerException when comparing to null");
    }
    
}