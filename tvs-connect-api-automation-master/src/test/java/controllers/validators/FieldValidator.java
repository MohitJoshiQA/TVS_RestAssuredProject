package controllers.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import static configuration.BaseClassTest.softAssert;
import static configuration.Listeners.test;

/**
 * Contains field-level validation methods
 *
 * @class
 */
public class FieldValidator {

    /**
     * Validates field data type
     *
     * @param {JsonNode} value - Actual Response Field value to validate
     * @param {string}   expectedType - Expected data type from Excel - Validation
     *                   Rule Column
     * @param {string}   fieldPath - JSON path for error reporting
     * @returns {boolean} True if validation passes
     */
    public static boolean validateDataType(JsonNode value, String expectedType, String fieldPath) {

        // Skip validation if expectedType is "NA"
        if ("NA".equalsIgnoreCase(expectedType)) {
            System.out.println("Skipping datatype validation for " + fieldPath + " as type is set to NA");
            return true;
        }

        boolean isValid = false;
        String actualType = value.getNodeType().toString().toLowerCase();

        switch (expectedType.toLowerCase()) {
            case "string":
            case "String":
                isValid = value.isTextual();
                break;
            case "int":
            case "integer":
                isValid = value.isInt() || (value.isTextual() && value.asText().matches("-?\\d+"));
                break;
//            case "double":
//            case "float":
//            case "Float":
//                isValid = value.isDouble() || value.isFloat() ||
//                        (value.isTextual() && value.asText().matches("-?\\d+(\\.\\d+)?"));
//                break;
            case "double":
            case "float":
            case "Float":
                isValid = value.isDouble() || value.isFloat() || value.isInt() ||
                        (value.isTextual() && value.asText().matches("-?\\d+(\\.\\d+)?"));
                break;
            case "boolean":
                isValid = value.isBoolean();
                break;
        }

        if (!isValid) {
            softAssert.fail(fieldPath + ": Expected type " + expectedType + " but got " + actualType);
        }
        return isValid;
    }

    /**
     * Validates minimum value limit
     *
     * @param {JsonNode} value - Actual Response Field value to validate
     * @param {Object}   minLimit - Minimum allowed value from Excel - Validation
     *                   Rule Column
     * @param {string}   fieldPath - JSON path for error reporting
     * @returns {boolean} True if validation passes
     */
    public static boolean validateMinLimit(JsonNode value, Object minLimit, String fieldPath) {
        // Skip validation if minLimit is "NA"
        if (minLimit instanceof String && "NA".equalsIgnoreCase((String) minLimit)) {
            return true;
        }

        double actualValue = value.asDouble();
        double min = ((Number) minLimit).doubleValue();

        if (actualValue < min) {
            softAssert.fail(fieldPath + ": Value " + actualValue + " is below minimum limit " + min);
            return false;
        }
        return true;
    }

    /**
     * Validates maximum value limit
     *
     * @param {JsonNode} value - Actual Response Field value to validate
     * @param {Object}   maxLimit - Maximum allowed value from Excel - Validation
     *                   Rule Column
     * @param {string}   fieldPath - JSON path for error reporting
     * @returns {boolean} True if validation passes
     */
    public static boolean validateMaxLimit(JsonNode value, Object maxLimit, String fieldPath) {
        // Skip validation if maxLimit is "NA"
        if (maxLimit instanceof String && "NA".equalsIgnoreCase((String) maxLimit)) {
            return true;
        }

        double actualValue = value.asDouble();
        double max = ((Number) maxLimit).doubleValue();

        if (actualValue > max) {
            softAssert.fail(fieldPath + ": Value " + actualValue + " is above maximum limit " + max);
            return false;
        }
        return true;
    }

    /**
     * Validates string length
     *
     * @param {JsonNode} value - Actual Response Field value to validate
     * @param {int}      expectedLength - Expected string length from Excel -
     *                   Validation
     *                   Rule Column
     * @param {string}   fieldPath - JSON path for error reporting
     * @returns {boolean} True if validation passes
     */
    public static boolean validateLength(JsonNode value, int expectedLength, String fieldPath) {
        // Skip validation if expectedLength is represented as "NA"
        // This check is done at the Map level in ResponseValidator.validateField()

        String strValue = value.asText();
        if (strValue.length() != expectedLength) {
            softAssert.fail(fieldPath + ": Expected length " + expectedLength +
                    " but got " + strValue.length());
            return false;
        }
        return true;
    }

    /**
     * Validates against regex pattern
     *
     * @param {JsonNode} value - Actual Response Field value to validate
     * @param {string}   regex - Pattern to match from Excel - Validation
     *                   Rule Column
     * @param {string}   fieldPath - JSON path for error reporting
     * @returns {boolean} True if validation passes
     */
    public static boolean validateRegex(JsonNode value, String regex, String fieldPath) {
        // Skip validation if regex is "NA"
        if ("NA".equalsIgnoreCase(regex)) {
            return true;
        }

        String strValue = value.asText();
        if (!Pattern.matches(regex, strValue)) {
            softAssert.fail(fieldPath + ": Value '" + strValue +
                    "' doesn't match regex pattern '" + regex + "'");
            return false;
        }
        return true;
    }

    /**
     * Converts time string to epoch seconds considering current date
     * Supports both timestamp formats and simple time formats (HH:mm)
     */
    private static long convertToEpochSeconds(String timestamp, long currentTimeSeconds) {
        try {

            // Trim whitespace from timestamp to handle trailing spaces
              timestamp = timestamp.trim();

            // Handle simple time format (HH:mm) - NEW FUNCTIONALITY
            if (timestamp.matches("\\d{1,2}:\\d{2}")) {

                // Parse the time
                String[] timeParts = timestamp.split(":");
                int hours = Integer.parseInt(timeParts[0]);
                int minutes = Integer.parseInt(timeParts[1]);

                // Create LocalTime
                LocalTime time = LocalTime.of(hours, minutes);

                // Get current date
                Instant currentInstant = Instant.ofEpochSecond(currentTimeSeconds);
                LocalDate currentDate = currentInstant.atZone(ZoneOffset.UTC).toLocalDate();

                // Combine current date with the provided time
                long epochSeconds = currentDate.atTime(time).toEpochSecond(ZoneOffset.UTC);
                return epochSeconds;
            }

            // Handle epoch seconds
            if (timestamp.matches("\\d+")) {
                return Long.parseLong(timestamp);
            }
            // Handle formatted timestamps
            else if (timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(timestamp);
                return date.getTime() / 1000;
            } else if (timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(timestamp);
                return date.getTime() / 1000;
            }else if (timestamp.matches("\\w{3} \\d{1,2}, \\d{4} \\d{2}:\\d{2}:\\d{2}")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(timestamp);
                return date.getTime() / 1000;
            }
            return -1;
        } catch (Exception e) {
            System.out.println("Error parsing timestamp: " + timestamp + " - " + e.getMessage());
            return -1;
        }
    }

    /**
     * Validates timestamp is within specified time range
     * Updated to handle simple time formats (HH:mm) as well as full timestamps
     *
     * @param value JsonNode representing the timestamp (in epoch, formatted, or simple time)
     * @param timeConstraint e.g. "current"
     * @param offsetSeconds e.g. -900 (15 minutes before)
     * @param fieldPath Field path for logging
     * @param currentTimeSeconds Current epoch seconds
     * @return true if valid, false otherwise
     */
    public static boolean validateTimeRange(JsonNode value, String timeConstraint, int offsetSeconds,
                                            String fieldPath, long currentTimeSeconds) {
        // Skip validation if timeConstraint is "NA"
        if ("NA".equalsIgnoreCase(timeConstraint)) {
            return true;
        }

        String actaulValue = value.asText();
        System.out.println("actaulValue: "+actaulValue);

        // Use updated method that accepts currentTimeSeconds
        long actualValueTimeSeconds = convertToEpochSeconds(actaulValue, currentTimeSeconds);
        System.out.println("offsetSeconds....."+offsetSeconds);
        System.out.println("actualValueTimeSeconds....."+actualValueTimeSeconds);
        System.out.println("currentTimeSeconds....."+currentTimeSeconds);

        if (actualValueTimeSeconds == -1) {
            softAssert.fail(fieldPath + ": Unable to parse timestamp format: " + actaulValue);
            return false;
        }

        test.info("Validating timestamp: " + actualValueTimeSeconds +
                " (Current time: " + currentTimeSeconds + ", Offset: " + offsetSeconds + ")");

        if ("current".equalsIgnoreCase(timeConstraint)) {
            // For simple time formats, we need to be more flexible with validation
            // since we're comparing times on the same day
            long minTime, maxTime;

            // For simple time formats (HH:mm), we compare against current time on same day
            if (actaulValue.matches("\\d{1,2}:\\d{2}")) {
                // Get current time in same format for comparison
                Instant currentInstant = Instant.ofEpochSecond(currentTimeSeconds);
                LocalTime currentTime = currentInstant.atZone(ZoneOffset.UTC).toLocalTime();
                LocalDate currentDate = currentInstant.atZone(ZoneOffset.UTC).toLocalDate();
                long currentTimeOnSameDay = currentDate.atTime(currentTime).toEpochSecond(ZoneOffset.UTC);

                if (offsetSeconds >= 0) {
                    minTime = currentTimeOnSameDay;
                    maxTime = currentTimeOnSameDay + offsetSeconds;
                } else {
                    minTime = currentTimeOnSameDay + offsetSeconds; // offsetSeconds is negative
                    maxTime = currentTimeOnSameDay;
                }
            } else {
                // For full timestamps, use original logic
                if (offsetSeconds >= 0) {
                    minTime = currentTimeSeconds;
                    maxTime = currentTimeSeconds + offsetSeconds;
                } else {
                    minTime = currentTimeSeconds + offsetSeconds; // offsetSeconds is negative
                    maxTime = currentTimeSeconds;
                }
            }

            System.out.println("maxTime: "+maxTime);
            System.out.println("valueTimeSeconds: "+actualValueTimeSeconds);
            System.out.println("minTime: "+minTime);
            test.info("Valid time range: [" + minTime + " to " + maxTime + "]");

            if (actualValueTimeSeconds < minTime || actualValueTimeSeconds > maxTime) {
                long diffSeconds = Math.abs(actualValueTimeSeconds - currentTimeSeconds);
                long diffMinutes = diffSeconds / 60;

                softAssert.fail(fieldPath + ": Timestamp " + actualValueTimeSeconds +
                        " is not within expected range [" + minTime + " to " + maxTime + "]" +
                        " (Difference: " + diffMinutes + " minutes)");
                return false;
            }
            test.info("Timestamp validation passed for " + fieldPath);
            return true;
        }

        softAssert.fail(fieldPath + ": Unsupported time constraint: " + timeConstraint);
        return false;
    }

    /**
     * Validates date is within specified date range
     *
     * @param value JsonNode representing the date (in yyyy-MM-dd format)
     * @param dateConstraint e.g. "today"
     * @param offsetDays e.g. 0 (same day), 1 (tomorrow), -1 (yesterday)
     * @param fieldPath Field path for logging
     * @param currentTimeSeconds Current epoch seconds
     * @return true if valid, false otherwise
     */
    public static boolean validateDateRange(JsonNode value, String dateConstraint, int offsetDays,
                                            String fieldPath, long currentTimeSeconds) {
        if ("NA".equalsIgnoreCase(dateConstraint)) {
            System.out.println("Skipping date range validation for " + fieldPath + " as constraint is set to NA");
            return true;
        }

        String dateStr = value.asText();
        System.out.println("Validating date: " + dateStr + " with constraint: " + dateConstraint + " and offset: " + offsetDays);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date actualDate = sdf.parse(dateStr);
            LocalDate actualLocalDate = actualDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            Instant currentInstant = Instant.ofEpochSecond(currentTimeSeconds);
            LocalDate currentDate = currentInstant.atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate expectedDate;

            if ("today".equalsIgnoreCase(dateConstraint)) {
                expectedDate = currentDate.plusDays(offsetDays);
            } else {
                softAssert.fail(fieldPath + ": Unsupported date constraint: " + dateConstraint);
                return false;
            }

            System.out.println("Expected date: " + expectedDate + ", Actual date: " + actualLocalDate);

            if (!actualLocalDate.isEqual(expectedDate)) {
                softAssert.fail(fieldPath + ": Date " + dateStr + " is not as expected. Expected: " + expectedDate + ", Actual: " + actualLocalDate);
                return false;
            }

            test.info("Date validation passed for " + fieldPath);
            return true;
        } catch (ParseException e) {
            softAssert.fail(fieldPath + ": Unable to parse date " + dateStr + " - " + e.getMessage());
            return false;
        }
    }
}