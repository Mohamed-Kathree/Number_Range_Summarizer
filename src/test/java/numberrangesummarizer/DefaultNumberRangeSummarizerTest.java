package numberrangesummarizer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultNumberRangeSummarizerTest {

    // straight from the interface Javadoc, so this has to work end to end
    private static final String SAMPLE_INPUT = "1,3,6,7,8,12,13,14,15,21,22,23,24,31";
    private static final String SAMPLE_OUTPUT = "1, 3, 6-8, 12-15, 21-24, 31";

    private final NumberRangeSummarizer summarizer = new DefaultNumberRangeSummarizer();

    @Test
    void sampleInputProducesTheExpectedOutput() {
        Collection<Integer> collected = summarizer.collect(SAMPLE_INPUT);

        assertEquals(SAMPLE_OUTPUT, summarizer.summarizeCollection(collected));
    }

    @Test
    void handlesALargeInputWithoutBlowingUp() {
        // not a real benchmark, just cheap evidence that this isn't doing
        // anything quadratic under the hood
        List<Integer> numbers = IntStream.range(0, 100_000).boxed().collect(Collectors.toList());

        assertEquals("0-99999", summarizer.summarizeCollection(numbers));
    }

    @Nested
    class Collect {

        // null, empty and blank strings all mean "no numbers", not an error
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void returnsAnEmptyCollectionForNullOrBlankInput(String input) {
            assertTrue(summarizer.collect(input).isEmpty());
        }

        @Test
        void parsesACommaDelimitedListOfIntegers() {
            assertEquals(Arrays.asList(1, 2, 3), summarizer.collect("1,2,3"));
        }

        @Test
        void ignoresWhitespaceAroundEachNumber() {
            assertEquals(Arrays.asList(1, 2), summarizer.collect(" 1 , 2 "));
        }

        @Test
        void supportsNegativeNumbers() {
            assertEquals(Arrays.asList(-3, -2, -1), summarizer.collect("-3,-2,-1"));
        }

        // decided to fail fast on bad input rather than silently drop it -
        // felt more honest than pretending the input was fine
        @ParameterizedTest
        @CsvSource({
                "'1,,2'",
                "'1,a,2'",
                "'1,2,'"
        })
        void throwsOnAnyTokenThatIsNotAValidInteger(String input) {
            assertThrows(IllegalArgumentException.class, () -> summarizer.collect(input));
        }

        @Test
        void exceptionMessageMentionsTheBadToken() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> summarizer.collect("1,a,2"));

            // NumberFormatException is itself an IllegalArgumentException, and its
            // default message ("For input string: \"a\"") also contains "a" - so
            // both of these checks are needed to actually prove parseInt wraps it
            // with a clearer message rather than just letting it propagate as-is
            assertTrue(exception.getMessage().startsWith("Not a number:"));
            assertTrue(exception.getMessage().contains("a"));
        }
    }

    @Nested
    class SummarizeCollection {

        @Test
        void returnsEmptyStringForNullInput() {
            assertEquals("", summarizer.summarizeCollection(null));
        }

        @Test
        void returnsEmptyStringForEmptyInput() {
            assertEquals("", summarizer.summarizeCollection(new ArrayList<>()));
        }

        @Test
        void aSingleNumberIsPrintedAlone() {
            assertEquals("1", summarizer.summarizeCollection(Arrays.asList(1)));
        }

        @Test
        void twoConsecutiveNumbersBecomeARange() {
            assertEquals("6-7", summarizer.summarizeCollection(Arrays.asList(6, 7)));
        }

        @Test
        void twoNonConsecutiveNumbersStaySeparate() {
            assertEquals("1, 3", summarizer.summarizeCollection(Arrays.asList(1, 3)));
        }

        @Test
        void aFullyConsecutiveListBecomesOneRange() {
            assertEquals("1-5", summarizer.summarizeCollection(Arrays.asList(1, 2, 3, 4, 5)));
        }

        @Test
        void unsortedInputIsSortedBeforeGrouping() {
            assertEquals("1-3", summarizer.summarizeCollection(Arrays.asList(3, 1, 2)));
        }

        @Test
        void duplicatesAreCollapsedEvenWhenTheyBridgeARun() {
            assertEquals("1-3", summarizer.summarizeCollection(Arrays.asList(1, 2, 2, 3)));
        }

        @Test
        void aRangeCanCrossZero() {
            assertEquals("-2-1", summarizer.summarizeCollection(Arrays.asList(-2, -1, 0, 1)));
        }

        @Test
        void aCollectionContainingNullIsRejected() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> summarizer.summarizeCollection(Arrays.asList(1, null, 3)));

            assertTrue(exception.getMessage().toLowerCase().contains("null"));
        }

        @Test
        void doesNotOverflowAtIntegerMaxValue() {
            List<Integer> input = Arrays.asList(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);

            assertEquals((Integer.MAX_VALUE - 1) + "-" + Integer.MAX_VALUE,
                    summarizer.summarizeCollection(input));
        }

        @Test
        void doesNotUnderflowAtIntegerMinValue() {
            List<Integer> input = Arrays.asList(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);

            assertEquals(Integer.MIN_VALUE + "-" + (Integer.MIN_VALUE + 1),
                    summarizer.summarizeCollection(input));
        }

        // TreeSet.contains(null) throws NullPointerException instead of returning
        // false, so a naive input.contains(null) null-check would break on this
        // exact input instead of reporting it as invalid
        @Test
        void acceptsATreeSetAsInput() {
            assertEquals("1-3", summarizer.summarizeCollection(new TreeSet<>(Arrays.asList(1, 2, 3))));
        }
    }
}
