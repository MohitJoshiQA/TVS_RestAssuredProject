 package controllers.graphqlRequestFormatters;

 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 public class GraphQLMutationFormatter {
     /**
      * Enhanced method to handle GraphQL mutation structure without "input" wrapper
      * This handles the direct field assignment format in GraphQL mutations
      * Fixed to properly handle JSON objects with quoted keys
      */
     public static String graphQLMutationConstraints(String originalRequest) {
         if (originalRequest == null || originalRequest.isEmpty()) {
             return originalRequest;
         }

         String modifiedRequest = originalRequest;

         /** === TIME PATTERN ===
          * Matches formats like:
          * fromTime: {
          *   ValidationType: "TimeRange",
          *   TimeConstraint: "NOW(current)",
          *   OffsetSeconds: -3600
          *  }
          */
         Pattern timePattern = Pattern.compile(
                 "(fromTime|toTime|from_time|to_time)\\s*:\\s*\\{\\s*" +
                         "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"TimeRange\"\\s*,?\\s*" +
                         "(?:\"TimeConstraint\"|TimeConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                         "(?:\"OffsetSeconds\"|OffsetSeconds)\\s*:\\s*(-?\\d+)\\s*" +
                         "\\}",
                 Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

         Matcher timeMatcher = timePattern.matcher(modifiedRequest);
         StringBuffer timeBuffer = new StringBuffer();

         while (timeMatcher.find()) {
             String fieldName = timeMatcher.group(1);
             String timeConstraint = timeMatcher.group(2);
             int offsetSeconds = Integer.parseInt(timeMatcher.group(3));

             String formattedTime = DateFormatter.calculateFormattedTime(timeConstraint, offsetSeconds);
             timeMatcher.appendReplacement(timeBuffer, fieldName + ": \"" + formattedTime + "\"");
         }
         timeMatcher.appendTail(timeBuffer);
         modifiedRequest = timeBuffer.toString();

         /** === DATE PATTERN ===
         *   Matches formats like:
         *   fromDate: {
         *   ValidationType: "DateRange",
         *   DateConstraint: "TODAY",
         *   OffsetDays: -2
         * }
         */
         Pattern datePattern = Pattern.compile(
                 "(fromDate|toDate)\\s*:\\s*\\{\\s*" +
                         "(?:\"ValidationType\"|ValidationType)\\s*:\\s*\"DateRange\"\\s*,?\\s*" +
                         "(?:\"DateConstraint\"|DateConstraint)\\s*:\\s*\"([^\"]+)\"\\s*,?\\s*" +
                         "(?:(?:\"OffsetDays\"|OffsetDays)\\s*:\\s*(-?\\d+)\\s*)?" +
                         "\\}",
                 Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

         Matcher dateMatcher = datePattern.matcher(modifiedRequest);
         StringBuffer dateBuffer = new StringBuffer();

         while (dateMatcher.find()) {
             String fieldName = dateMatcher.group(1);
             String dateConstraint = dateMatcher.group(2);
             String offsetDaysStr = dateMatcher.group(3);
             int offsetDays = offsetDaysStr != null ? Integer.parseInt(offsetDaysStr) : 0;

             String formattedDate = DateFormatter.calculateFormattedDate(dateConstraint, offsetDays);
             dateMatcher.appendReplacement(dateBuffer, fieldName + ": \"" + formattedDate + "\"");
         }
         dateMatcher.appendTail(dateBuffer);

         return dateBuffer.toString();
     }

 }