package controllers.graphqlRequestFormatters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Processes a GraphQL request to replace TimeConstraint objects with formatted
     * date strings.
     *
     * @param originalRequest The original GraphQL request string
     * @return Modified request with formatted date strings
     */
    public static String formatDateTimeConstraints(String originalRequest) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

//        // Matches: time_to_share_till: { ValidationType: "TimeRange", TimeConstraint: "current", OffsetSeconds: 60 }
//        // Converts to: time_to_share_till: "yyyy-MM-dd HH:mm:ss"
//        Pattern timeToSharePattern = Pattern.compile(
//                "(time_to_share_till)\\s*:\\s*\\{\\s*" +
//                        "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*,?\\s*" +
//                        "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
//                        "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
//                        "\\}",
//                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
//
//        Matcher timeToShareMatcher = timeToSharePattern.matcher(originalRequest);
//        StringBuffer modifiedRequest = new StringBuffer();
//
//        while (timeToShareMatcher.find()) {
//            String fieldName = timeToShareMatcher.group(1);
//            String timeConstraint = timeToShareMatcher.group(2);
//            int offsetSeconds = Integer.parseInt(timeToShareMatcher.group(3));
//
//            String formattedDateTime = calculateFormattedDateTime(timeConstraint, offsetSeconds);
//            timeToShareMatcher.appendReplacement(modifiedRequest, fieldName + ": \"" + formattedDateTime + "\"");
//        }
//        timeToShareMatcher.appendTail(modifiedRequest);

        // Matches: startDateTime: { "ValidationType": "TimeRange", "TimeConstraint": "today_start", "OffsetSeconds": 0 }
        // Converts to: startDateTime: "yyyy-MM-dd HH:mm:ss"
//        Pattern timeConstraintPattern = Pattern.compile(
//                "(startDateTime|endDateTime|start_datetime|end_datetime|from_date|to_date)\\s*:\\s*\\{\\s*" +
//                        "\"ValidationType\"\\s*:\\s*\"TimeRange\"\\s*,\\s*" +
//                        "\"TimeConstraint\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*" +
//                        "\"OffsetSeconds\"\\s*:\\s*(-?\\d+)\\s*" +
//                        "\\}",
//                Pattern.DOTALL
//        );

        // Matches: startDateTime: { "ValidationType": "TimeRange", "TimeConstraint": "today_start", "OffsetSeconds": 0 }
        // Converts to: startDateTime: "yyyy-MM-dd HH:mm:ss"
        Pattern timeConstraintPattern = Pattern.compile(
                "(from_time|to_time|time_to_share_till|startDateTime|endDateTime|start_datetime|end_datetime|from_date|to_date)\\s*:\\s*\\{\\s*" +
                        "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*,?\\s*" +
                        "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                        "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
                        "\\}",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = timeConstraintPattern.matcher(originalRequest);
        StringBuffer finalRequest = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String timeConstraint = matcher.group(2);
            int offsetSeconds = Integer.parseInt(matcher.group(3));

            String formattedDate = calculateFormattedDateTime(timeConstraint, offsetSeconds);
            matcher.appendReplacement(finalRequest, fieldName + ": \"" + formattedDate + "\"");
        }
        matcher.appendTail(finalRequest);

        return finalRequest.toString();
    }

    public static String formatTimeRangeConstraints(String originalRequest) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        // Matches: fromTime: { ValidationType: "TimeRange", TimeConstraint: "current", OffsetSeconds: 120 }
        // Converts to: fromTime: "HH:mm"
//        Pattern timeRangePattern = Pattern.compile(
//                "(fromTime|toTime)\\s*:\\s*\\{\\s*" +
//                        "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*" +
//                        "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*" +
//                        "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
//                        "\\}",
//                Pattern.DOTALL);

        Pattern timeRangePattern = Pattern.compile(
                "(fromTime|toTime)\\s*:\\s*\\{\\s*" +
                        "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*,?\\s*" +
                        "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                        "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
                        "\\}",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = timeRangePattern.matcher(originalRequest);
        StringBuffer modifiedRequest = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String timeConstraint = matcher.group(2);
            int offsetSeconds = Integer.parseInt(matcher.group(3));

            String formattedTime = calculateFormattedTime(timeConstraint, offsetSeconds);
            matcher.appendReplacement(modifiedRequest, fieldName + ": \"" + formattedTime + "\"");
        }
        matcher.appendTail(modifiedRequest);

        return modifiedRequest.toString();
    }

    public static String formatDateRangeConstraints(String originalRequest) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        // Matches: fromDate: { ValidationType: "DateRange", DateConstraint: "today", OffsetDays: 1 }
        // Converts to: fromDate: "yyyy-MM-dd"
        Pattern dateRangePattern = Pattern.compile(
                "(fromDate|toDate)\\s*:\\s*\\{\\s*" +
                        "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"DateRange\"\\s*,?\\s*" +
                        "(?:\"DateConstraint\"|DateConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                        "(?:\"OffsetDays\"|OffsetDays)\\s*:\\s*(-?\\d+)\\s*" +
                        "\\}",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = dateRangePattern.matcher(originalRequest);
        StringBuffer modifiedRequest = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String dateConstraint = matcher.group(2);
            String offsetDaysStr = matcher.group(3);
            int offsetDays = offsetDaysStr != null ? Integer.parseInt(offsetDaysStr) : 0;

            String formattedDate = calculateFormattedDate(dateConstraint, offsetDays);
            matcher.appendReplacement(modifiedRequest, fieldName + ": \"" + formattedDate + "\"");
        }
        matcher.appendTail(modifiedRequest);

        return modifiedRequest.toString();
    }

    /**
     * Calculates the formatted date/time string in UTC based on the time constraint
     * and offset.
     *
     * @param timeConstraint The time constraint (e.g., "current", "today_start",
     *                       "today_end")
     * @param offsetSeconds  The offset in seconds to apply
     * @return Formatted date/time string in UTC
     */
    public static String calculateFormattedDateTime(String timeConstraint, int offsetSeconds) {
        LocalDateTime dateTime;

        switch (timeConstraint.toLowerCase()) {
            case "current":
                dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(System.currentTimeMillis() / 1000 + offsetSeconds),
                        ZoneOffset.UTC);
                break;
            default:
                throw new IllegalArgumentException("Unsupported TimeConstraint: " + timeConstraint);
        }

        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Calculates the formatted time string (HH:mm) based on the time constraint and
     * offset.
     *
     * @param timeConstraint The time constraint (e.g., "current", "morning_start",
     *                       "evening_end")
     * @param offsetSeconds  The offset in seconds to apply
     * @return Formatted time string (HH:mm)
     */
    public static String calculateFormattedTime(String timeConstraint, int offsetSeconds) {
        LocalTime time;

        switch (timeConstraint.toLowerCase()) {
            case "current":
                time = LocalTime.now(ZoneOffset.UTC).plusSeconds(offsetSeconds);
                break;
            default:
                throw new IllegalArgumentException("Unsupported TimeConstraint for time: " + timeConstraint);
        }

        return time.format(TIME_FORMATTER);
    }

    /**
     * Calculates the formatted date string (yyyy-MM-dd) based on the date
     * constraint and offset.
     *
     * @param dateConstraint The date constraint (e.g., "today", "current")
     * @param offsetDays     The offset in days to apply (positive for future,
     *                       negative for past)
     * @return Formatted date string (yyyy-MM-dd)
     */
    public static String calculateFormattedDate(String dateConstraint, int offsetDays) {
        LocalDate date;

        switch (dateConstraint.toLowerCase()) {
            case "today":
            case "current":
                date = LocalDate.now(ZoneOffset.UTC).plusDays(offsetDays);
                break;
            default:
                throw new IllegalArgumentException("Unsupported DateConstraint: " + dateConstraint);
        }

        return date.format(DATE_ONLY_FORMATTER);
    }

    /**
     * Processes JSON objects within GraphQL queries that contain from_timestamp and
     * to_timestamp.
     *
     * @param originalRequest The original GraphQL request string
     * @return Modified request with updated timestamp values
     */
    public static String updateTimestampFields(String originalRequest) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long fromTimeSeconds = currentTimeSeconds - 10;

        // Replace the timestamp placeholders with actual values
        // Using regex to find and replace timestamp values in JSON format
        // Matches: to_timestamp: 9999999999 (placeholder/unix seconds format)
        // Converts to: from_timestamp and to_timestamp: current epoch seconds
        String modifiedRequest = originalRequest.replaceAll(
                "(\\\"to_timestamp\\\"\\s*:\\s*)[^,}\\s]+",
                "\"to_timestamp\": " + currentTimeSeconds);

        modifiedRequest = modifiedRequest.replaceAll(
                "(\\\"from_timestamp\\\"\\s*:\\s*)[^,}\\s]+",
                "\"from_timestamp\": " + fromTimeSeconds);

        return modifiedRequest;
    }

    /**
     * Enhanced method to handle from_time and to_time fields that require full
     * datetime format for specific APIs
     */
    public static String formatFullDateTimeFields(String originalRequest, String apiName) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        // Only apply this formatting for getAllSharedLiveLocation API or similar APIs
        // that need full datetime
        if (apiName != null && (apiName.equals("getAllSharedLiveLocation")||apiName.equals("getCricketMatchList"))) {
            // First, handle the constraint-based format

            // Matches: from_time: { ValidationType: "TimeRange", TimeConstraint: "today_start", OffsetSeconds: 0 }
            // Converts to: from_time: "yyyy-MM-dd HH:mm:ss"
            Pattern fullDateTimePattern = Pattern.compile(
                    "(from_time|to_time)\\s*:\\s*\\{\\s*" +
                            "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*,?\\s*" +
                            "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                            "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
                            "\\}",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            Matcher constraintMatcher = fullDateTimePattern.matcher(originalRequest);
            StringBuffer modifiedRequest = new StringBuffer();

            while (constraintMatcher.find()) {
                String fieldName = constraintMatcher.group(1);
                String timeConstraint = constraintMatcher.group(2);
                int offsetSeconds = Integer.parseInt(constraintMatcher.group(3));

                // Use full datetime format instead of just time
                String formattedDateTime = DateFormatter.calculateFormattedDateTime(timeConstraint, offsetSeconds);
                constraintMatcher.appendReplacement(modifiedRequest, fieldName + ": \"" + formattedDateTime + "\"");
            }
            constraintMatcher.appendTail(modifiedRequest);

            // Second, handle cases where from_time/to_time are already in simple time
            // format (HH:mm) Matches: from_time: "10:30"
            // This is the key fix - convert existing time format to full datetime
            // Converts to: from_time: "yyyy-MM-dd HH:mm:ss" (uses current UTC date)
            Pattern simpleTimePattern = Pattern.compile(
                    "(from_time|to_time)\\s*:\\s*\"(\\d{2}:\\d{2})\"",
                    Pattern.CASE_INSENSITIVE);

            Matcher simpleTimeMatcher = simpleTimePattern.matcher(modifiedRequest.toString());
            StringBuffer finalRequest = new StringBuffer();

            while (simpleTimeMatcher.find()) {
                String fieldName = simpleTimeMatcher.group(1);
                String timeValue = simpleTimeMatcher.group(2); // e.g., "10:14"

                // Convert simple time to full datetime using current date
                String currentDate = LocalDate.now(ZoneOffset.UTC).format(DATE_ONLY_FORMATTER);
                String fullDateTime = currentDate + " " + timeValue + ":00";

                simpleTimeMatcher.appendReplacement(finalRequest, fieldName + ": \"" + fullDateTime + "\"");
            }
            simpleTimeMatcher.appendTail(finalRequest);

            return finalRequest.toString();
        }

        return originalRequest;
    }

}
