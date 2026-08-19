package numberrangesummarizer;

import java.util.Objects;

/**
 * Holds a start and end value for one run of consecutive numbers.
 * Used internally while grouping - not part of the public API.
 */
class Range {

    private final int start;
    private final int end;

    Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }

    // A range only counts as a "real" range once it covers more than one
    // number - a single value should just print as itself, e.g. "5" not "5-5".
    boolean isSingleNumber() {
        return start == end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Range)) {
            return false;
        }
        Range other = (Range) o;
        return start == other.start && end == other.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return start + ".." + end;
    }
}
