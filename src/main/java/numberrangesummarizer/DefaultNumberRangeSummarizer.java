package numberrangesummarizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link NumberRangeSummarizer}.
 * <p>
 * Parses a comma separated string of integers, and can also turn a
 * collection of integers back into a comma separated string where runs of
 * consecutive numbers are collapsed into ranges, e.g. 6, 7, 8 becomes "6-8".
 */
public class DefaultNumberRangeSummarizer implements NumberRangeSummarizer {

    private static final String TOKEN_DELIMITER = ",";
    private static final String RANGE_DELIMITER = "-";
    private static final String OUTPUT_SEPARATOR = ", ";

    @Override
    public Collection<Integer> collect(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(input.split(TOKEN_DELIMITER, -1))
                .map(String::trim)
                .map(this::parseInt)
                .collect(Collectors.toList());
    }

    // Just wraps NumberFormatException with a message that actually says
    // which token failed, since "For input string: ..." on its own isn't
    // that helpful when you're staring at a 50-number input.
    private int parseInt(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number: '" + token + "'", e);
        }
    }

    @Override
    public String summarizeCollection(Collection<Integer> input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return groupIntoRanges(input).stream()
                .map(this::format)
                .collect(Collectors.joining(OUTPUT_SEPARATOR));
    }

    // Building the TreeSet by hand instead of TreeSet::new(Collection) so the
    // null check happens on each element before it's added - TreeSet.contains(null)
    // throws NullPointerException rather than returning false, so checking
    // input.contains(null) up front would blow up on a Collection that already
    // forbids nulls (a TreeSet<Integer>, for example) before we get the chance
    // to report it properly.
    private List<Range> groupIntoRanges(Collection<Integer> numbers) {
        TreeSet<Integer> sorted = new TreeSet<>();
        for (Integer number : numbers) {
            if (number == null) {
                throw new IllegalArgumentException("Collection cannot contain null values");
            }
            sorted.add(number);
        }
        List<Range> ranges = new ArrayList<>();

        Integer rangeStart = null;
        int rangeEnd = 0;
        for (int number : sorted) {
            if (rangeStart == null) {
                rangeStart = number;
            } else if (rangeEnd == Integer.MAX_VALUE || number != rangeEnd + 1) {
                // rangeEnd + 1 would overflow if rangeEnd is already MAX_VALUE,
                // so that case has to end the run instead of being compared
                ranges.add(new Range(rangeStart, rangeEnd));
                rangeStart = number;
            }
            rangeEnd = number;
        }
        ranges.add(new Range(rangeStart, rangeEnd));

        return ranges;
    }

    private String format(Range range) {
        if (range.isSingleNumber()) {
            return String.valueOf(range.getStart());
        }
        return range.getStart() + RANGE_DELIMITER + range.getEnd();
    }
}
