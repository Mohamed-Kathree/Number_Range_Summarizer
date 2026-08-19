package numberrangesummarizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RangeTest {

    @Test
    void storesTheStartAndEndItWasGiven() {
        Range range = new Range(6, 8);

        assertEquals(6, range.getStart());
        assertEquals(8, range.getEnd());
    }

    @Test
    void isSingleNumberWhenStartAndEndAreTheSame() {
        assertTrue(new Range(4, 4).isSingleNumber());
    }

    @Test
    void isNotSingleNumberWhenEndIsAfterStart() {
        assertFalse(new Range(6, 8).isSingleNumber());
    }

    @Test
    void twoRangesWithTheSameBoundsAreEqual() {
        assertEquals(new Range(1, 3), new Range(1, 3));
        assertEquals(new Range(1, 3).hashCode(), new Range(1, 3).hashCode());
    }

    @Test
    void rangesWithDifferentBoundsAreNotEqual() {
        assertNotEquals(new Range(1, 3), new Range(1, 4));
    }
}
