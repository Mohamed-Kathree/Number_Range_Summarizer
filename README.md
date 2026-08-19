# Number Range Summarizer

Takes a comma delimited string of integers and returns a comma delimited string
where any runs of consecutive numbers are collapsed into ranges.

```
Input:  "1,3,6,7,8,12,13,14,15,21,22,23,24,31"
Output: "1, 3, 6-8, 12-15, 21-24, 31"
```

## Assumptions

The brief only really specifies the happy path, so here's what I assumed for
everything else, along with the tests that back each one up:

- Whitespace around a number doesn't matter (`" 1 , 2 "` is fine) - see
  `ignoresWhitespaceAroundEachNumber`.
- `null`, empty, or blank input just means "no numbers", not an error - see
  `returnsAnEmptyCollectionForNullOrBlankInput`.
- The input doesn't need to be sorted already, and the output is always
  ascending - see `unsortedInputIsSortedBeforeGrouping`.
- Duplicate numbers are just noise and get removed - see
  `duplicatesAreCollapsedEvenWhenTheyBridgeARun`.
- Negative numbers are allowed. A range like -3 to -1 prints as `-3--1`,
  which looks a bit odd but is unambiguous given "-" is also the range
  separator - see `doesNotUnderflowAtIntegerMinValue`, which exercises the
  same double-dash formatting between two negative numbers.
- "Consecutive" means the numbers differ by exactly 1, and a run only turns
  into a range once it's 2 or more numbers - a single number just prints on
  its own.
- A bad token (`"1,,2"`, `"1,a,2"`) is treated as invalid input and throws an
  `IllegalArgumentException` naming the offending token, rather than being
  silently skipped. Skipping bad tokens is arguably just as valid a choice,
  but failing loudly seemed like the safer default for something that's
  meant to parse numbers.
- `summarizeCollection` on `null` or an empty collection returns `""`, never
  `null`.
- A collection containing a `null` element is treated as invalid input.

## Design

Two classes handle everything:

- **`DefaultNumberRangeSummarizer`** implements the provided interface.
  `collect` turns the string into a list of integers; `summarizeCollection`
  groups them into runs and formats the result. The grouping step uses a
  `TreeSet` to sort and de-duplicate in one move, then walks through once,
  extending the current run for as long as the next number is exactly one
  higher.
- **`Range`** is a small immutable start/end pair, just there to make the
  grouping loop and the formatting step easier to follow instead of passing
  arrays of ints around.

Didn't see the need to split parsing/grouping/formatting into three separate
classes for something this size - two felt like the right amount of
structure without turning it into more files than logic.

## Java 8 notes

Streams are used for the two places where they read better than a loop:
splitting/parsing the input string, and joining the final ranges into a
string with `Collectors.joining(", ")`. The actual grouping logic (deciding
where one run ends and the next begins) is a plain `for` loop instead,
since each step depends on the result of the last one - trying to force that
into a stream would probably end up harder to read, not easier.

## Complexity

`O(n log n)` overall, driven by the sort inside `TreeSet`. Grouping and
formatting after that are both a single linear pass, so `O(n)` extra space.

## How to run

```bash
mvn clean test       # run the tests
mvn clean package    # build the jar and run the tests
```
