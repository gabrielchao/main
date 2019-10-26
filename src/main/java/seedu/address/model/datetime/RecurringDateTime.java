package seedu.address.model.datetime;

import static java.util.Objects.isNull;

import static seedu.address.commons.util.CollectionUtil.collectionToString;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the frequency of a recurring appointment
 */
public class RecurringDateTime {

    public static final String MESSAGE_CONSTRAINTS = "Recurring " + DateTime.MESSAGE_CONSTRAINTS_BODY;
    public static final int EXPECTED_FREQUENCY_ARRAY_LENGTH = 6;

    private final long years;
    private final long months;
    private final long weeks;
    private final long days;
    private final long hours;
    private final long minutes;

    /**
     * Constructs an {@code RecurringDateTime}.
     *
     * @param freqArray
     */
    public RecurringDateTime(Long[] freqArray) {
        requireAllNonNull(Arrays.asList(freqArray));
        this.years = isNull(freqArray[0]) ? 0 : freqArray[0];
        this.months = freqArray[1];
        this.weeks = freqArray[2];
        this.days = freqArray[3];
        this.hours = freqArray[4];
        this.minutes = freqArray[5];
    }

    public Long getYears() {
        return years;
    }

    public Long getMonths() {
        return months;
    }

    public Long getWeeks() {
        return weeks;
    }

    public Long getDays() {
        return days;
    }

    public Long getHours() {
        return hours;
    }

    public Long getMinutes() {
        return minutes;
    }

    /**
     * Converts the JSON storage String to an array of type long
     * @return Long[] array
     */
    public static Long[] frequencyStringToLong(String freq) {
        Long[] freqLongArray = Arrays.asList(freq.split("\n"))
                .stream()
                .map(x -> Long.parseLong(x))
                .toArray(Long[]::new);
        return freqLongArray;
    }

    /**
     * @return boolean, which is true if frequency is valid
     */
    public static boolean isValidFrequency(String freq) {
        Long[] freqLongArray = frequencyStringToLong(freq);
        return freqLongArray.length == EXPECTED_FREQUENCY_ARRAY_LENGTH;
    }

    /**
     * @return Long, which is 0 if string passed in is null
     */
    public static Long getSingleFrequencyAsLong(String freq) {
        if (isNull(freq) || "".equals(freq)) {
            return Long.parseLong("0");
        } else {
            return Long.parseLong(freq);
        }
    }

    /**
     * Convert recurring dateTime to a string for JSON storage.
     * @return dateTime as a string
     */
    public String toJacksonJsonString() {
        List<Long> freqList = new ArrayList<>();
        Collections.addAll(freqList, years, months, weeks, days, hours, minutes);
        return collectionToString(freqList);
    }

    /**
     * Gets recurring appointment's next date and time based on current one
     */
    public DateTime getNextAppointmentDateTime(StartDateTime currentAppointmentDateTime) {
        LocalDateTime nextAppointmentDateTime =
            currentAppointmentDateTime.dateTime
                    .plusYears(years)
                    .plusMonths(months)
                    .plusWeeks(weeks)
                    .plusDays(days)
                    .plusHours(hours)
                    .plusMinutes(minutes);

        return new DateTime(nextAppointmentDateTime.toString());
    }
}
