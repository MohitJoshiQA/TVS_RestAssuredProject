package controllers;

import configuration.BaseClassTest;
import Utils.*;

import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import configuration.SimulatorDispatcher;
import java.io.IOException;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import static configuration.Listeners.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import controllers.graphqlRequestFormatters.*;
import controllers.validators.LiveLocationValidator;
import controllers.validators.StatusCodeAndStatusMsgValidator;

import java.util.Map;
import java.util.HashMap;

/**
 * Main test class to run API validation using TestNG.
 */
public class TestCaseExecuter extends BaseClassTest {
    static JSONObject json = new JSONObject();

    /**
     * Inner class representing a single test case fetched from Excel.
     */
    static class TestCase {
        private final int row;
        private String rawExpectedStatusCode; // Store raw string before parsing
        private String simulationValue;

        public TestCase(int row) {
            this.row = row;
        }

        // Methods to fetch data from Excel for a given row
        public String getId() throws IOException {
            id = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 0);
            return id;
        }

        public String getName() throws IOException {
            name = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 7).toString();
            return name;
        }

        public String getRequest() throws IOException {
            request = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 4).toString();
            return request;
        }

        /**
         * Helper method to modify the request with dynamic timestamp values and
         * API-specific values if needed
         */
        public String getModifiedRequest() throws IOException {
            String originalRequest = getRequest();

            // Get API name for dynamic field updates
            String apiName = getApiName();

            // Process the request with all formatting using enhanced
            // GraphQLRequestFormatter
            String modifiedRequest = GraphQLRequestFormatter.processRequest(originalRequest, apiName);

            System.out.println("Modified request: " + modifiedRequest);

            return modifiedRequest;
        }

        /**
         * Extracts VIN value from GraphQL request
         */
        public String extractVinFromRequest() throws IOException {
            String request = getRequest();
            Pattern vinPattern = Pattern.compile("vin\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = vinPattern.matcher(request);

            if (matcher.find()) {
                return matcher.group(1);
            }

            return null; // Or throw an exception / warning
        }

        public JsonNode getExpectedJson() throws IOException {
            return ExcelCommunicator.getJsonFromExcel(ExcelFilePath, model, row, 5);
        }

        public String getExpected() throws IOException {
            expected = getExpectedJson().toString();
            return expected;
        }

        // Removed JSON parsing from here - will be handled in test class
        public String getRawExpected() throws IOException {
            return getExpected();
        }

        public String getExpectedStatusCode() throws IOException {
            // Get the raw string from Excel
            rawExpectedStatusCode = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 6);
            expectedStatusCode = rawExpectedStatusCode;
            return expectedStatusCode;
        }

        // Removed JSON parsing from here - will be handled in test class
        public String getRawExpectedStatusCodeString() throws IOException {
            getExpectedStatusCode(); // sets rawExpectedStatusCode
            return expectedStatusCode;
        }

        public String getApiName() throws IOException {
            apiName = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 1);
            return apiName;
        }

        public JsonNode getValidationObject() throws IOException {
            validationObject = ExcelCommunicator.getJsonFromExcel(ExcelFilePath, model, row, 8);
            return validationObject;
        }

        // Removed JSON parsing from here - will be handled in test class
        public String getRawValidationObject() throws IOException {
            JsonNode node = getValidationObject();
            if (node == null || node.toString().trim().isEmpty()) return null;
            return node.toString();
        }

        // Add this method to get simulation value
        public String getSimulationValue() throws IOException {
            simulationValue = ExcelCommunicator.getDataFromExcel(ExcelFilePath, model, row, 2);
            return simulationValue;
        }
    }

    /**
     * Custom exception class for JSON parsing errors with column information
     */
    public static class JSONParsingException extends Exception {
        private final int column;
        private final int row;
        private final String rawValue;

        public JSONParsingException(String message, int column, int row, String rawValue) {
            super(message);
            this.column = column;
            this.row = row;
            this.rawValue = rawValue;
        }

        public String getDetailedMessage() {
            String columnName;
            switch (column) {
                case 5: columnName = "Expected"; break;
                case 6: columnName = "ExpectedStatusCode"; break;
                case 8: columnName = "ValidationObject"; break;
                default: columnName = "Unknown Column " + column;
            }

            return String.format("Invalid JSON in Excel column %d (%s) at row %d\nRaw value: %s\nError: %s",
                    column, columnName, row, rawValue != null ? rawValue : "null", getMessage());
        }
    }

    /**
     * Centralized JSON validation method for all columns
     * Skips validation if column value is "NA" (case-insensitive)
     */
    private static void validateAllJSONFields(TestCase testCase) throws JSONParsingException {
        try {
            // Validate Column 5: Expected Response
            try {
                String rawExpected = testCase.getRawExpected();
                if (shouldValidateJSON(rawExpected)) {
                    new JSONObject(rawExpected);
                    System.out.println("✓ Column 5 (Expected) - JSON validation passed for row " + testCase.row);
                } else {
                    System.out.println("⚠ Column 5 (Expected) - Skipping JSON validation (value: " + rawExpected + ") for row " + testCase.row);
                }
            } catch (JSONException | IOException e) {
                String rawValue = null;
                try {
                    rawValue = testCase.getRawExpected();
                } catch (IOException ignored) {}
                throw new JSONParsingException("Invalid JSON format in Expected field: " + e.getMessage(),
                        5, testCase.row, rawValue);
            }

            // Validate Column 6: Expected Status Code
            try {
                String rawStatusCode = testCase.getRawExpectedStatusCodeString();
                if (shouldValidateJSON(rawStatusCode)) {
                    new JSONObject(rawStatusCode);
                    System.out.println("✓ Column 6 (ExpectedStatusCode) - JSON validation passed for row " + testCase.row);
                } else {
                    System.out.println("⚠ Column 6 (ExpectedStatusCode) - Skipping JSON validation (value: " + rawStatusCode + ") for row " + testCase.row);
                }
            } catch (JSONException | IOException e) {
                String rawValue = null;
                try {
                    rawValue = testCase.getRawExpectedStatusCodeString();
                } catch (IOException ignored) {}
                throw new JSONParsingException("Invalid JSON format in ExpectedStatusCode field: " + e.getMessage(),
                        6, testCase.row, rawValue);
            }

            // Validate Column 8: Validation Object
            try {
                String rawValidation = testCase.getRawValidationObject();
                if (shouldValidateJSON(rawValidation)) {
                    new JSONObject(rawValidation);
                    System.out.println("✓ Column 8 (ValidationObject) - JSON validation passed for row " + testCase.row);
                } else {
                    System.out.println("⚠ Column 8 (ValidationObject) - Skipping JSON validation (value: " + rawValidation + ") for row " + testCase.row);
                }
            } catch (JSONException | IOException e) {
                String rawValue = null;
                try {
                    rawValue = testCase.getRawValidationObject();
                } catch (IOException ignored) {}
                throw new JSONParsingException("Invalid JSON format in ValidationObject field: " + e.getMessage(),
                        8, testCase.row, rawValue);
            }

        } catch (JSONParsingException e) {
            // Re-throw our custom exception
            throw e;
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            throw new JSONParsingException("Unexpected error during JSON validation: " + e.getMessage(),
                    -1, testCase.row, "Unknown");
        }
    }

    /**
     * Helper method to determine if a field value should be validated as JSON
     * Returns false if value is null, empty, or "NA" (case-insensitive)
     */
    private static boolean shouldValidateJSON(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmedValue = value.trim();

        // Check for "NA" case-insensitively - this should handle quoted "NA" as well
        if (trimmedValue.equalsIgnoreCase("NA") ||
                trimmedValue.equalsIgnoreCase("\"NA\"")) {
            return false;
        }

        return true;
    }

    /**
     * Safe JSON parsing methods with proper error handling
     * Returns empty JSON object for "NA" values
     */
    private static JSONObject safeParseJSONObject(String jsonString, int column, int row) throws JSONParsingException {
        try {
            if (!shouldValidateJSON(jsonString)) {
                System.out.println("⚠ Column " + column + " - Returning empty JSON object for value: " + jsonString + " at row " + row);
                return new JSONObject(); // Return empty JSON object for null/empty/NA strings
            }
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new JSONParsingException("JSON parsing failed: " + e.getMessage(), column, row, jsonString);
        }
    }


    /**
     * This method will Perform HTTP POST with the appropriate token
     */
    public static Response RequestMethod(String body, String apiName)
            throws IOException {

        String graphqlBody = json.put("query", body).toString();

        // Log request body
        Markup markup = MarkupHelper.createCodeBlock("Request Body: " + graphqlBody);
        test.info(markup);

        System.out.println("authToken: " + authToken);
        // Perform HTTP POST with the appropriate token
        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(graphqlBody)
                .post(instanceE);
        return response;
    }

    /**
     * Provides test case data from Excel to the test method.
     */
    @DataProvider(name = "testCasesProvider")
    public Object[][] getTestCases() throws IOException {
        int totalTestCases = ExcelCommunicator.getLastRow(ExcelFilePath, model);
        Object[][] data = new Object[totalTestCases][1];

        for (int i = 0; i < totalTestCases; i++) {
            data[i][0] = new TestCase(i + 1);
        }
        return data;
    }

    /**
     * Utility class to build and execute HTTP requests for the test cases.
     */
    public static class RequestBuilderUtil {
        /**
         * Simple result holder for the response and root path.
         */
        public static class RequestResult {
            public final Response response;
            public final String responseRootPath;
            public final JSONObject expectedStatusObj;
            public final String apiName;
            public final Map<String, Object> dynamicValues;

            public RequestResult(Response response, String responseRootPath, JSONObject expectedStatusObj,
                                 String apiName) {
                this.response = response;
                this.responseRootPath = responseRootPath;
                this.expectedStatusObj = expectedStatusObj;
                this.apiName = apiName;
                this.dynamicValues = new HashMap<>();
            }
        }

        /**
         * Builds request from test case details, performs request, and returns result.
         */
        public static RequestResult buildAndExecuteRequest(TestCase testCase, String apiName) throws Exception {
            String expectedStatusCode = testCase.getExpectedStatusCode();
            System.out.println("Expected Status Code: " + expectedStatusCode);

            // Use safe parsing methods
            JSONObject expectedStatus = safeParseJSONObject(expectedStatusCode, 6, testCase.row);
            JSONObject expectedObj = safeParseJSONObject(expected, 5, testCase.row);

            String responseRootPath;
            String responsedataPath = "";

            try {
                if (apiName != null && expectedObj.has("data") && expectedObj.getJSONObject("data").has(apiName)) {
                    responsedataPath = "data." + apiName;
                } else if (apiName != null && expectedObj.has(apiName)) {
                    responsedataPath = apiName;
                } else if (expectedObj.has("data")) {
                    responsedataPath = "data";
                } else {
                    System.err.println("No valid response path found.");
                }

                // Check if responseRootPath is "NA" to determine if we should skip validation
                if (expectedStatus.has("responseRootPath")) {
                    String rootPathValue = expectedStatus.getString("responseRootPath");
                    if ("NA".equalsIgnoreCase(rootPathValue)) {
                        responseRootPath = responsedataPath;
                        System.out.println("Response validation will be skipped as responseRootPath is NA");
                    } else {
                        responseRootPath = responsedataPath + "." + rootPathValue;
                    }
                } else {
                    // If responseRootPath is not provided, throw an exception
                    throw new JSONException("responseRootPath is missing in expected status JSON");
                }

            } catch (JSONException e) {
                String errorMsg = "Missing required fields in expected status JSON: " + e.getMessage() +
                        "\nExpected format: {\"status\": 200, \"statusMessage\": \"message\", \"responseRootPath\": \"path\"}";
                System.err.println(errorMsg);
                throw new JSONException(errorMsg);
            }

            // Get expected data and validation rules from Excel
            JsonNode expectedJson = testCase.getExpectedJson();
            JsonNode validationObject = testCase.getValidationObject();
            System.out.println("Validation Object: " + validationObject);

            // Make the actual HTTP request, passing the current time for timestamp
            // validation
            // Also pass the apiName for token selection
            Response response = TestCaseExecuter.RequestMethod(
                    testCase.getModifiedRequest(), apiName); // Pass apiName to RequestMethod

            return new RequestResult(response, responseRootPath, expectedStatus, apiName);
        }
    }

    /**
     * TestNG test method that runs each scenario end-to-end.
     */
    @Test(dataProvider = "testCasesProvider")
    public void runAllScenario(TestCase testCase) {
        // Initialize a new SoftAssert for each test case to avoid state carrying over
        softAssert = new SoftAssert();
        // Clear error details from previous test cases
        errorDetails = "";

        try {
            // Print the test case ID before starting
            String testCaseId = "Unknown";
            try {
                testCaseId = testCase.getId();
                System.out.println("Executing Test Case ID: " + testCaseId);
            } catch (IOException e) {
                System.err.println("Error getting test case ID: " + e.getMessage());
            }

            // CENTRALIZED JSON VALIDATION - Handle all JSON errors here
            try {
                validateAllJSONFields(testCase);
                System.out.println("JSON validation passed for all fields in test case: " + testCaseId);
            } catch (JSONParsingException e) {
                String errorMsg = e.getDetailedMessage();
                System.err.println("JSON Validation Error: " + errorMsg);

                // Create test in report for proper logging
                String testName = "Scenario: " + testCaseId + " - JSON Validation Error";
                test = extent.createTest(testName);
                test.fail("JSON Validation Failed: " + errorMsg);

                Assert.fail(errorMsg);
                return;
            }

            // Create the test in the report
            String testName = "Scenario: " + testCaseId;
            String apiName = "";
            String methodName = "";
            try {
                apiName = testCase.getApiName();
                methodName = testCase.getName();
                testName += " - " + apiName;
            } catch (IOException e) {
                System.err.println("Error getting API name: " + e.getMessage());
            }
            test = extent.createTest(testName);

            // Initial test data preparation
            try {
                testCase.getName();
                testCase.getExpected();

                // Get simulation value using the method
                String simulationValue = testCase.getSimulationValue();
                System.out.println("Simulation value for test case " + testCaseId + ": " + simulationValue);

                Integer seqNo = (Integer) DynamicContext.get("seqNo");
                if (seqNo == null) {
                    test.warning("seqNo is not set in DynamicContext");
                    seqNo = 0; // or handle gracefully
                }

                if (simulationValue == null || !simulationValue.trim().equalsIgnoreCase("NA")) {
                    System.out.println("Running simulation for test case: " + testCaseId);
                    SimulatorDispatcher.simulate(model, executingInstance, testCase.row, 2, seqNo);
                } else {
                    System.out.println("Skipping simulation for test case: " + testCaseId + " since value is NA");
                }
            } catch (Exception e) {
                String errorMsg = "Error preparing test data: " + e.getMessage();
                test.fail(errorMsg);
                System.err.println(errorMsg);
                Assert.fail(errorMsg);
                return;
            }

            long currentTimeSeconds = System.currentTimeMillis() / 1000;

            // Execute request
            RequestBuilderUtil.RequestResult result;
            try {
                result = RequestBuilderUtil.buildAndExecuteRequest(testCase, apiName);
            } catch (JSONParsingException e) {
                String errorMsg = "JSON parsing error in test case " + testCaseId + ": " + e.getDetailedMessage();
                test.fail(errorMsg);
                Assert.fail(errorMsg);
                return;
            } catch (JSONException e) {
                String errorMsg = "JSON parsing error in test case " + testCaseId + ": " + e.getMessage();
                test.fail(errorMsg);
                e.printStackTrace();
                Assert.fail(errorMsg);
                return;
            } catch (RuntimeException e) {
                Assert.fail(e.getMessage());
                return;
            } catch (Exception e) {
                String errorMsg = "Error executing request for test case " + testCaseId + ": " + e.getMessage();
                test.fail(errorMsg);
                e.printStackTrace();
                Assert.fail(errorMsg);
                return;
            }

            Response response = result.response;
            System.out.println("Actual Response: " + response.asPrettyString());

            // take dynamic values from previous test case
            DynamicValuesFormatter.extractDynamicValues(apiName, response);

            // Parse actual response and expected JSON
            JsonNode actualJson;
            JsonNode expectedJson;
            try {
                actualJson = new ObjectMapper().readTree(response.asString());
                actualResponseBody = response.asPrettyString();
                expectedJson = testCase.getExpectedJson();
            } catch (Exception e) {
                String errorMsg = "Error parsing JSON response for test case " +
                        testCaseId + ": " + e.getMessage();
                test.fail(errorMsg);
                e.printStackTrace();
                Assert.fail(errorMsg);
                return;
            }

            // Get validation rules
            JsonNode validationObject;
            try {
                validationObject = testCase.getValidationObject();
            } catch (Exception e) {
                String errorMsg = "Error getting validation rules for test case " +
                        testCaseId + ": " + e.getMessage();
                test.fail(errorMsg);
                e.printStackTrace();
                Assert.fail(errorMsg);
                return;
            }

            // Log for debugging
            test.info("Validation Rules: "
                    + (validationObject != null ? validationObject.toString() : "No validation rules"));


            JsonNode actualResponse = actualJson;
            JsonNode expectedResponse = expectedJson;

            System.out.println("expectedResponse...."+expectedResponse);

            // Perform deep validation of actual vs expected response
            controllers.validators.ResponseValidator.ValidationResult responseData;
            try {

                // Extract expected status from the result and actual status
                String expectedStatusCode = String.valueOf(result.expectedStatusObj.getInt("status"));
                String expectedStatusMsg = String.valueOf(result.expectedStatusObj.getString("statusMessage"));

                responseData = StatusCodeAndStatusMsgValidator.validateStatusFields(
                        expectedStatusCode, response.jsonPath(), apiName, expectedStatusMsg);

                // After obtaining responseData from validateStatusFields
                boolean skipStandardValidation = ("getTimeFenceAlertConfig".equals(apiName)
                        && ("getTimeFence_by_comparing_setTimeFence_response_with_getTimeFenceAlertConfig_response"
                        .equals(methodName))
                        ||
                        "to_getTimeFenceAlertConfig_response_by_comparing_updateTimeFence".equals(methodName));

                if (!skipStandardValidation && responseData.callCompareAndValidateFieldsMethod()) {

                    // Only traverse path if responseRootPath is not "NA"
                    if (!"NA".equals(result.responseRootPath)) {
                        String[] pathSegments = result.responseRootPath.split("\\.");

                        // Traverse to the target node in both expected and actual JSON
                        for (String segment : pathSegments) {
                            if (segment.isEmpty())
                                continue;

                            // Check if segment exists in actualResponse
                            if (actualResponse.path(segment).isMissingNode()) {
                                String errorMsg = "Path segment '" + segment + "' not found in actual response";
                                test.fail(errorMsg);
                                Assert.fail(errorMsg);
                                return;
                            }

                            actualResponse = actualResponse.path(segment);
                            expectedResponse = expectedResponse.path(segment);
                        }

                        test.info("Performing validation on response section: " + result.responseRootPath);
                    } else {
                        test.info("Performing validation on entire response (responseRootPath is NA)");
                    }

                    responseData = controllers.validators.ResponseValidator.compareAndValidateFields(
                            expectedResponse, actualResponse, "", validationObject, currentTimeSeconds);
                }
            } catch (Exception e) {
                String errorMsg = "Error validating response for test case " +
                        testCaseId + ": " + e.getMessage();
                test.fail(errorMsg);
                e.printStackTrace();
                Assert.fail(errorMsg);
                return;
            }

            // Special validation for live location links
            if (("getAllAlerts".equals(apiName) && ("towAlertActive".equals(methodName)||"crashAlertActive".equals(methodName)
                    ||"crashAlertWhenIncognitoOn".equals(methodName)||"fallAlertActive".equals(methodName))) ||
                    ("shareLiveLocation".equals(apiName) && "to_shareLiveLocation".equals((methodName)))) {
                test.info("Detected '" + apiName + "' method - performing live location validation");
                System.out.println("inside live location validation for " + apiName);

                try {
                    String requestVin = testCase.extractVinFromRequest();
                    System.out.println("requestVin: " + requestVin);

                    if (requestVin != null) {
                        if ("getAllAlerts".equals(apiName)) {
                            // Existing validation for getAllAlerts (towAlertActive)
                            LiveLocationValidator.validateLiveLocationForTow(response, result.responseRootPath,
                                    requestVin, test);
                        } else if ("shareLiveLocation".equals(apiName)) {
                            // New validation for shareLiveLocation
                            LiveLocationValidator.validateLiveLocationForShareLocation(response,
                                    result.responseRootPath, requestVin, test);
                        }
                    } else {
                        test.warning("Could not extract VIN from request, skipping live location validation");
                    }
                } catch (Exception e) {
                    String errorMsg = "Error in live location validation: " + e.getMessage();
                    test.warning(errorMsg);
                    System.err.println(errorMsg);
                    e.printStackTrace();
                    // Don't fail test for this additional validation
                }
            }

            // Special logic for time fence comparison
            if ("setTimeFence".equals(apiName)) {
                JsonNode timeFenceDetails = actualJson.path("data").path("setTimeFence").path("time_fence_details");
                DynamicContext.set("storedTimeFenceDetails", timeFenceDetails);
                test.info("Stored setTimeFence details for future comparison");
            } else if ("updateTimeFence".equals(apiName)) {
                JsonNode timeFenceDetails = actualJson.path("data").path("updateTimeFence").path("time_fence_details");
                DynamicContext.set("storedTimeFenceDetails", timeFenceDetails);
                test.info("Stored updateTimeFence details for future comparison");
            }

            if ("getTimeFenceAlertConfig".equals(apiName)
                    && ("getTimeFence_by_comparing_setTimeFence_response_with_getTimeFenceAlertConfig_response"
                    .equals(methodName))
                    ||
                    "to_getTimeFenceAlertConfig_response_by_comparing_updateTimeFence".equals(methodName)) {

                JsonNode storedDetails = (JsonNode) DynamicContext.get("storedTimeFenceDetails");
                if (storedDetails == null) {
                    test.fail("No stored setTimeFence details found for comparison");
                    Assert.fail("Stored setTimeFence details missing");
                    return;
                }

                JsonNode allTimeFenceConfig = actualJson.path("data").path("getTimeFenceAlertConfig")
                        .path("allTimeFenceConfig");
                String storedId = storedDetails.path("id").asText();
                boolean found = false;

                for (JsonNode config : allTimeFenceConfig) {
                    if (config.path("id").asText().equals(storedId)) {
                        found = true;
                        SoftAssert configSoftAssert = new SoftAssert();

                        storedDetails.fields().forEachRemaining(entry -> {
                            String fieldName = entry.getKey();
                            JsonNode expectedValue = entry.getValue();
                            JsonNode actualValue = config.path(fieldName);

                            if (!expectedValue.equals(actualValue)) {
                                String errorMsg = String.format("Field '%s' mismatch. Expected: %s, Actual: %s",
                                        fieldName, expectedValue.asText(), actualValue.asText());
                                configSoftAssert.fail(errorMsg);
                            }
                        });

                        configSoftAssert.assertAll();
                        break;
                    }
                }

                if (!found) {
                    test.fail("No time fence configuration found with stored ID: " + storedId);
                    Assert.fail("Matching configuration not found in getTimeFence response");
                }
            }

            // Assert and log results
            if (responseData.isValid() &&
                    responseData.getMismatchedFields().isEmpty() &&
                    responseData.getFailedValidations().isEmpty()) {

                test.pass("Validation passed for all fields.");
                // All validations passed successfully
                System.out.println("All validations passed successfully for test case: " + testCaseId);

            } else {
                // Build detailed error message
                StringBuilder errorBuilder = new StringBuilder();

                if (!responseData.getMismatchedFields().isEmpty()) {
                    errorBuilder.append("Mismatched fields: ")
                            .append(responseData.getMismatchedFields().toString())
                            .append(", ");
                }

                if (!responseData.getFailedValidations().isEmpty()) {
                    errorBuilder.append("Failed validations: ")
                            .append(responseData.getFailedValidations().toString());
                }

                errorDetails = errorBuilder.toString();
                test.fail("Validation failed: " + errorDetails);

                // Fail the test with detailed error message
                Assert.fail("Validation failed: " + errorDetails);
            }

            System.out.println("Completed Test Case: " + testCaseId);

        } catch (AssertionError e) {
            // This captures Assert.fail() calls from above
            // We've already logged the error details, so just rethrow
            throw e;
        } catch (Exception e) {
            String errorMsg = "Unexpected exception in test execution: " + e.getMessage();
            test.fail(errorMsg);
            e.printStackTrace();
            Assert.fail(errorMsg);
        }
        // finally {
        // // Clear ThreadLocal context to prevent stale data in subsequent tests on the
        // same thread
        // }
    }
}