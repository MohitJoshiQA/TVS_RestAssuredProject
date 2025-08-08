package controllers.graphqlRequestFormatters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Utils.DynamicContext;
import io.restassured.response.Response;

public class DynamicValuesFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Store dynamic values from previous test case responses
    public static final Map<String, Object> dynamicValues = new HashMap<>();

    /**
     * Enhanced method to handle dynamic time ranges for getAllSharedLiveLocation
     * This ensures the query time range includes the actual sharing time
     */
    public static String handleDynamicTimeRanges(String originalRequest, String apiName) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        // Special handling for getAllSharedLiveLocation to ensure proper time range
        if ("getAllSharedLiveLocation".equals(apiName)) {
            // Store the sharing timestamp when shareLiveLocation is called
            Long sharingTimestamp = (Long) dynamicValues.get("sharing_timestamp");

            if (sharingTimestamp != null) {
                // Calculate time range that includes the sharing time with buffer
                long bufferSeconds = 300; // 5 minutes buffer
                long fromTime = sharingTimestamp - bufferSeconds;

                // FIXED: Don't add buffer to current time, use current time as-is
                long toTime = System.currentTimeMillis() / 1000; // Current time only

                // Convert to datetime format
                String fromDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(fromTime), ZoneOffset.UTC).format(DATE_FORMATTER);

                String toDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(toTime), ZoneOffset.UTC).format(DATE_FORMATTER);

                // Replace from_time and to_time in the request
                String modifiedRequest = originalRequest.replaceAll(
                        "(from_time)\\s*:\\s*\"[^\"]+\"",
                        "from_time: \"" + fromDateTime + "\"");

                modifiedRequest = modifiedRequest.replaceAll(
                        "(to_time)\\s*:\\s*\"[^\"]+\"",
                        "to_time: \"" + toDateTime + "\"");

                return modifiedRequest;
            }
        }

        return originalRequest;
    }

    /**
     * Extracts dynamic values from a response that can be used in subsequent test
     * cases.
     *
     * @param apiName  The name of the API
     * @param response The Response object to extract values from
     */
    public static void extractDynamicValues(String apiName, Response response) {
        if (apiName == null || response == null) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.asString());

            // Extract app_user_id from addUser API response
            if (apiName.equals("addUser")) {
                JsonNode userDetails = rootNode.path("data").path("addUser").path("user_details");
                if (userDetails.has("app_user_id")) {
                    String appUserId = userDetails.get("app_user_id").asText();
                    dynamicValues.put("app_user_id", appUserId);
                }
            }

            // For addEmergencyContacts API
            if (apiName.equals("addEmergencyContacts")) {
                JsonNode contactsDetails = rootNode.path("data").path("addEmergencyContacts").path("contacts_details");
                if (contactsDetails.isArray() && contactsDetails.size() > 0) {
                    JsonNode firstContact = contactsDetails.get(0);
                    if (firstContact.has("emergency_contact_id")) {
                        int emergencyContactId = firstContact.get("emergency_contact_id").asInt();
                        dynamicValues.put("emergency_contact_id", emergencyContactId);
                    }
                }
            }

            // For setVehicleSettings API
            if ("setVehicleSettings".equals(apiName)) {
                int seqNo = rootNode.at("/data/setVehicleSettings/seqNo").asInt();
                DynamicContext.set("seqNo", seqNo);
            }

            // For setOverspeedThreshold API
            if ("setOverspeedThreshold".equals(apiName)) {
                int seqNo = rootNode.at("/data/setOverspeedThreshold/seqNo").asInt();
                DynamicContext.set("seqNo", seqNo);
            }

            // For setHomeChargerSettings API
            if ("setHomeChargerSettings".equals(apiName)) {
                int seqNo = rootNode.at("/data/setHomeChargerSettings/seqNo").asInt();
                DynamicContext.set("seqNo", seqNo);
            }

            // For setPortableChargerSettings API
            if ("setPortableChargerSettings".equals(apiName)) {
                int seqNo = rootNode.at("/data/setPortableChargerSettings/seqNo").asInt();
                DynamicContext.set("seqNo", seqNo);
            }

            // For setGeofence API - extract geofenceid
            if ("setGeofence".equals(apiName)) {
                JsonNode geofenceNode = rootNode.at("/data/setGeofence/geofenceid");
                if (!geofenceNode.isMissingNode()) {
                    int geofenceId = geofenceNode.asInt();
                    dynamicValues.put("geofence_id", geofenceId);
                }
            }

            // For getAllGeofenceAlerts API - extract geofenceId
            if ("getAllGeofenceAlerts".equals(apiName)) {
                JsonNode geofenceAlerts = rootNode.path("data").path("getAllGeofenceAlerts").path("GeofenceAlerts");
                if (geofenceAlerts.isArray() && geofenceAlerts.size() > 0) {
                    JsonNode firstAlert = geofenceAlerts.get(0);
                    if (firstAlert.has("geofenceId")) {
                        int geofenceId = firstAlert.get("geofenceId").asInt();
                        dynamicValues.put("geofence_id", geofenceId);
                        System.out.println("Extracted geofenceId from getAllGeofenceAlerts: " + geofenceId);
                    }
                }
            }

            // For shareLiveLocation API - extract request_Id
            if ("shareLiveLocation".equals(apiName)) {
                JsonNode sharedDetails = rootNode.path("data").path("shareLiveLocation").path("shared_details");
                if (sharedDetails.has("request_Id")) {
                    int requestId = sharedDetails.get("request_Id").asInt();
                    dynamicValues.put("request_id", requestId);

                    // Store the timestamp when sharing occurred
                    long currentTimestamp = System.currentTimeMillis() / 1000;
                    dynamicValues.put("sharing_timestamp", currentTimestamp);
                }
            }

            // NEW: For getAllSharedDestinations API - extract placeId
            if ("getAllSharedDestinations".equals(apiName)) {
                JsonNode destinationDetails = rootNode.path("data").path("getAllSharedDestinations").path("destinationDetails");
                if (destinationDetails.isArray() && destinationDetails.size() > 0) {
                    JsonNode firstDestination = destinationDetails.get(0);
                    if (firstDestination.has("placeId")) {
                        String placeId = firstDestination.get("placeId").asText();
                        dynamicValues.put("place_id", placeId);
                        System.out.println("Extracted placeId: " + placeId);
                    }

                    // Also extract location_id if present
                    if (firstDestination.has("location_id")) {
                        String locationId = firstDestination.get("location_id").asText();
                        dynamicValues.put("location_id", locationId);
                        System.out.println("Extracted location_id: " + locationId);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error extracting dynamic values from response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processes dynamic values in requests, such as emergency_contact_id.
     *
     * @param originalRequest The original GraphQL request string
     * @param apiName         The API name of the current request
     * @return Modified request with dynamic values
     */
    public static String updateDynamicFields(String originalRequest, String apiName) {
        if (originalRequest == null || originalRequest.isEmpty()) {
            return originalRequest;
        }

        String modifiedRequest = originalRequest;

        // Handle app_user_id for offBoardUser API
        if (apiName != null && apiName.equals("offBoardUser")) {
            if (modifiedRequest.contains("app_user_id") && dynamicValues.containsKey("app_user_id")) {
                // Pattern for GraphQL field: app_user_id: "value" or app_user_id: value
                Pattern appUserIdPattern = Pattern.compile("app_user_id:\\s*\"?[^\\s,)]+\"?");

                if (appUserIdPattern.matcher(modifiedRequest).find()) {
                    String replacement = "app_user_id: \"" + dynamicValues.get("app_user_id") + "\"";
                    modifiedRequest = appUserIdPattern.matcher(modifiedRequest).replaceAll(replacement);
                }
            }
        }

        // Handle emergency_contact_id for various emergency contact APIs
        if (apiName != null &&
                (apiName.equals("fetchEmergencyContacts") || apiName.equals("updateEmergencyContacts")
                        || apiName.equals("deleteEmergencyContacts"))) {
            if (modifiedRequest.contains("emergency_contact_id") && dynamicValues.containsKey("emergency_contact_id")) {
                Pattern simplePattern = Pattern.compile("emergency_contact_id:\\s*\\d+");
                Pattern arrayValuePattern = Pattern.compile("emergency_contact_id:\\s*\\[\\s*\\d+\\s*\\]");
                Pattern jsonPattern = Pattern.compile("([\"']?)emergency_contact_id\\1:\\s*\\d+");

                Matcher simpleMatch = simplePattern.matcher(modifiedRequest);
                Matcher arrayValueMatch = arrayValuePattern.matcher(modifiedRequest);
                Matcher jsonMatch = jsonPattern.matcher(modifiedRequest);

                if (arrayValueMatch.find()) {
                    String replacement = "emergency_contact_id: [" + dynamicValues.get("emergency_contact_id") + "]";
                    modifiedRequest = arrayValueMatch.replaceAll(replacement);
                } else if (simpleMatch.find()) {
                    String replacement = "emergency_contact_id: " + dynamicValues.get("emergency_contact_id");
                    modifiedRequest = simpleMatch.replaceAll(replacement);
                } else if (jsonMatch.find()) {
                    String quotes = jsonMatch.group(1) != null ? jsonMatch.group(1) : "";
                    String replacement = quotes + "emergency_contact_id" + quotes + ": "
                            + dynamicValues.get("emergency_contact_id");
                    modifiedRequest = jsonMatch.replaceAll(replacement);
                }
            }
        }

        // Handle geofence_id for various geofence APIs
        if (apiName != null && (apiName.equals("getGeofenceList") || apiName.equals("editGeofence")
                || apiName.equals("deleteGeofence"))) {
            if ((modifiedRequest.contains("geofenceId") || modifiedRequest.contains("geofence_id"))
                    && dynamicValues.containsKey("geofence_id")) {
                Pattern simplePattern = Pattern.compile("(geofenceId|geofence_id):\\s*\\d+");
                Pattern arrayValuePattern = Pattern.compile("(geofenceId|geofence_id):\\s*\\[\\s*\\d+\\s*\\]");
                Pattern jsonPattern = Pattern.compile("([\"']?)(geofenceId|geofence_id)\\1:\\s*\\d+");

                Matcher simpleMatch = simplePattern.matcher(modifiedRequest);
                Matcher arrayValueMatch = arrayValuePattern.matcher(modifiedRequest);
                Matcher jsonMatch = jsonPattern.matcher(modifiedRequest);

                if (arrayValueMatch.find()) {
                    String fieldName = arrayValueMatch.group(1);
                    String replacement = fieldName + ": [" + dynamicValues.get("geofence_id") + "]";
                    modifiedRequest = arrayValueMatch.replaceAll(replacement);
                } else if (simpleMatch.find()) {
                    String fieldName = simpleMatch.group(1);
                    String replacement = fieldName + ": " + dynamicValues.get("geofence_id");
                    modifiedRequest = simpleMatch.replaceAll(replacement);
                } else if (jsonMatch.find()) {
                    String quotes = jsonMatch.group(1) != null ? jsonMatch.group(1) : "";
                    String fieldName = jsonMatch.group(2);
                    String replacement = quotes + fieldName + quotes + ": " + dynamicValues.get("geofence_id");
                    modifiedRequest = jsonMatch.replaceAll(replacement);
                }
            }
        }

        // NEW: Handle request_id for getAllSharedLiveLocation API
        if (apiName != null && (apiName.equals("getAllSharedLiveLocation") || apiName.equals("stopLiveLocation"))) {
            if (modifiedRequest.contains("request_id") && dynamicValues.containsKey("request_id")) {
                // Pattern to match request_id: value (with or without quotes)
                Pattern requestIdPattern = Pattern.compile("request_id:\\s*\"?\\d+\"?");

                if (requestIdPattern.matcher(modifiedRequest).find()) {
                    String replacement = "request_id: " + dynamicValues.get("request_id");
                    modifiedRequest = requestIdPattern.matcher(modifiedRequest).replaceAll(replacement);
                }
            }
        }

        // NEW: Handle placeId for destination-related APIs
        if (apiName != null && (apiName.equals("deleteSharedDestination")
                || apiName.equals("getAllSharedDestinations"))) {
            // Handle placeId replacement
            if (modifiedRequest.contains("placeId") && dynamicValues.containsKey("place_id")) {
                // Pattern to match placeId: "value" or placeId: value
                Pattern placeIdPattern = Pattern.compile("placeId:\\s*\"[^\"]*\"");

                if (placeIdPattern.matcher(modifiedRequest).find()) {
                    String replacement = "placeId: \"" + dynamicValues.get("place_id") + "\"";
                    modifiedRequest = placeIdPattern.matcher(modifiedRequest).replaceAll(replacement);
                    System.out.println("Replaced placeId with: " + dynamicValues.get("place_id"));
                }
            }
        }

        return modifiedRequest;
    }

}
