package seedu.address.model.datetime;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a Visit's datetime in the application.
 * Guarantees: immutable; is valid as declared in {@link #isValidDateTime(String)}
 */
public class DateTime {

    public static final String MESSAGE_CONSTRAINTS_BODY = "Date Time should be of the format dd-MM-yyyy HHmm "
            + "and adhere to the following constraints:\n"
            + "1. The values that substitute 'dd', 'MM', 'yyyy', 'HH' and 'mm' must all be numerical numbers.";
    static final SimpleDateFormat DATE_DISPLAY_FORMATTER = new SimpleDateFormat("dd-MM-yyyy HHmm");
    static final SimpleDateFormat DATE_PARSER_VALIDATOR = new SimpleDateFormat("dd-MM-yyyy HHmm");

    public final LocalDateTime dateTime;

    /**
     * Constructs a {@code DateTime}.
     *
     * @param dateTime A valid dateTime address.
     */
    public DateTime(String dateTime) {
        requireNonNull(dateTime);
        checkArgument(isValidDateTime(dateTime), MESSAGE_CONSTRAINTS_BODY);
        this.dateTime = parseDateTime(dateTime);
    }

    /**
     * Returns the parsed dateTimeTime if a given string is a valid dateTime; else calls checkArgument
     * which will throw an IllegalArgumentException.
     * @return StartDateTIme
     */
    public static LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("dd-MM-yyyy HHmm"));
    }

    /**
     * Returns if a given string is a valid dateTime.
     */
    public static boolean isValidDateTime(String test) {
        try {
            DATE_PARSER_VALIDATOR.parse(test);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Convert date to a string for JSON storage.
     * @return dateTime as a string
     */
    public String toJacksonJsonString() {
        return DATE_PARSER_VALIDATOR.format(dateTime);
    }

    @Override
    public String toString() {
        return DATE_DISPLAY_FORMATTER.format(dateTime);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DateTime // instanceof handles nulls
                && dateTime.equals(((DateTime) other).dateTime)); // state check
    }

    @Override
    public int hashCode() {
        return dateTime.hashCode();
    }

}
