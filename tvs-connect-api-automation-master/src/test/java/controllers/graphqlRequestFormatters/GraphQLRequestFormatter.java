package controllers.graphqlRequestFormatters;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for formatting GraphQL requests with dynamic date/time values
 * and
 * handling data transfer between test cases.
 */
public class GraphQLRequestFormatter {
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Combines all formatting methods to process a GraphQL request completely.
     *
     * @param originalRequest The original GraphQL request string
     * @param apiName         The name of the API (for dynamic field updates)
     * @return Fully processed request with all values updated
     */
    public static String processRequest(String originalRequest, String apiName) {
//        String requestWithFormattedConstraints = GraphQLMutationFormatter
//                .graphQLMutationConstraints(originalRequest);
//        String requestWithFormattedDates = DateFormatter.formatDateTimeConstraints(requestWithFormattedConstraints);
        String requestWithFormattedDates = DateFormatter.formatDateTimeConstraints(originalRequest);
        String requestWithFormattedTimes = DateFormatter.formatTimeRangeConstraints(requestWithFormattedDates);
        String requestWithFormattedDateRanges = DateFormatter.formatDateRangeConstraints(requestWithFormattedTimes);

        // Apply full datetime formatting for specific APIs
        String requestWithFullDateTime = DateFormatter.formatFullDateTimeFields(requestWithFormattedDateRanges,
                apiName);

        // Handle dynamic time ranges BEFORE the existing safety check
        String requestWithDynamicTimeRanges = DynamicValuesFormatter.handleDynamicTimeRanges(requestWithFullDateTime,
                apiName);

        String requestWithTimestamps = DateFormatter.updateTimestampFields(requestWithDynamicTimeRanges);
        String finalRequest = DynamicValuesFormatter.updateDynamicFields(requestWithTimestamps, apiName);

        return finalRequest;
    }

}