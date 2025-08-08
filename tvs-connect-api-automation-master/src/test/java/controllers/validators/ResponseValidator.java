package controllers.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import static configuration.BaseClassTest.softAssert;
import static configuration.Listeners.*;

/**
 * Handles complete API response validation
 */
public class ResponseValidator {
    /**
     * Container for validation results
     *
     * @class
     *
     * @param {boolean} isValid - Overall validation status
     * @returns {boolean} True if all validations passed
     *
     * @param {List<String>} mismatchedFields - List of mismatched fields
     * @returns {List<String>} Paths of mismatched fields
     *
     * @param {List<String>} failedValidations - List of failed validations
     * @returns {List<String>} Failed validation rules
     *
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> mismatchedFields;
        private final List<String> failedValidations;
        private final boolean callCompareAndValidateFieldsMethod;

        public ValidationResult(boolean isValid, List<String> mismatchedFields,
                                List<String> failedValidations, boolean callCompareAndValidateFieldsMethod) {
            this.isValid = isValid;
            this.mismatchedFields = mismatchedFields;
            this.failedValidations = failedValidations;
            this.callCompareAndValidateFieldsMethod = callCompareAndValidateFieldsMethod;
        }

        public boolean isValid() {
            return isValid;
        }

        public List<String> getMismatchedFields() {
            return mismatchedFields;
        }

        public List<String> getFailedValidations() {
            return failedValidations;
        }

        public boolean callCompareAndValidateFieldsMethod() {
            return callCompareAndValidateFieldsMethod;
        }
    }

    /**
     * Checks if a validation rule is "NA" and should be skipped
     *
     * @param {JsonNode} ruleNode - The rule node to check
     * @returns {boolean} True if the rule should be skipped
     */
    private static boolean isNARule(JsonNode ruleNode) {
        return ruleNode != null && ruleNode.isTextual() && "NA".equalsIgnoreCase(ruleNode.asText());
    }

    /**
     * Helper method to check if field should be skipped based on API name
     *
     * @param {String} fieldName - Name of the field
     * @param {String} apiName - Name of the API being tested
     * @returns {boolean} True if the field should be skipped
     */
    private static boolean shouldSkipFieldValidation(String fieldName, String apiName) {
        if (apiName == null) {
            return false;
        }

        // Skip validation for app_user_id if API is addUser
        if ("app_user_id".equals(fieldName) && "addUser".equalsIgnoreCase(apiName)) {
            System.out.println("Skipping validation for app_user_id field as API is addUser");
            return true;
        }

        // Skip validation for charging fields if API is getChargeCumulativeSummary
        if (("total_charging_time".equals(fieldName) ||
                "total_energy_consumed".equals(fieldName) ||
                "total_charging_sessions".equals(fieldName)) &&
                "getChargeCumulativeSummary".equalsIgnoreCase(apiName)) {
            System.out.println("Skipping validation for " + fieldName + " field as API is getChargeCumulativeSummary");
            return true;
        }

        // Skip validation for charging fields if API is getHomeChargerChargingCummulativeData
        if (("numberOfSessions".equals(fieldName) ||
                "chargingTime".equals(fieldName) ||
                "energyConsumedInWh".equals(fieldName)) &&
                "getHomeChargerChargingCummulativeData".equalsIgnoreCase(apiName)) {
            System.out.println("Skipping validation for " + fieldName + " field as API is getHomeChargerChargingCummulativeData");
            return true;
        }

        // Skip validation for charging fields if API is getPortableChargerChargingCummulativeData
        if (("numberOfSessions".equals(fieldName) ||
                "chargingTime".equals(fieldName) ||
                "energyConsumedInWh".equals(fieldName)) &&
                "getPortableChargerChargingCummulativeData".equalsIgnoreCase(apiName)) {
            System.out.println("Skipping validation for " + fieldName + " field as API is getPortableChargerChargingCummulativeData");
            return true;
        }

        return false;
    }

    /**
     * Compares and validates JSON structures
     *
     * @param {JsonNode} expected - Expected JSON structure
     * @param {JsonNode} actual - Actual JSON to validate
     * @param {string}   path - Current JSON path (for nested validation)
     * @param {JsonNode} VALIDATION_RULES - Validation rules to apply
     * @param {long}     currentTimeSeconds - Current time in seconds
     * @param {string}   apiName - Name of the API being tested (optional, can be null)
     * @returns {ValidationResult} Validation results container
     * @throws {Exception} If validation fails
     */
    public static ValidationResult compareAndValidateFields(JsonNode expected, JsonNode actual, String path,
                                                            JsonNode VALIDATION_RULES, long currentTimeSeconds, String apiName) throws Exception {
        List<String> mismatchedFields = new ArrayList<>();
        List<String> failedValidations = new ArrayList<>();
        boolean[] allValid = { true };
        boolean finalValidationResult;
        // Debug output
        System.out.println("Comparing at path: " + path);
        System.out.println("VALIDATION_RULES: " + (VALIDATION_RULES != null ? VALIDATION_RULES.toString() : "null"));
        if (apiName != null) {
            System.out.println("API Name: " + apiName);
        }

        if (expected.isObject() && actual.isObject()) {
            // First process the expected fields against actual
            expected.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();
                JsonNode actualValue = actual.get(fieldName);
                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                try {
                    if (actualValue == null) {
                        // Check if this field has an NA validation rule
                        boolean isNAField = VALIDATION_RULES != null &&
                                VALIDATION_RULES.has(fieldName) &&
                                isNARule(VALIDATION_RULES.get(fieldName));

                        if (!isNAField) {
                            mismatchedFields.add(currentPath);
                            softAssert.fail("Field missing in actual response: " + currentPath);
                            allValid[0] = false;
                        } else {
                            System.out.println("Skipping missing field validation for " + currentPath + " as rule is set to NA");
                        }
                        return;
                    }

                    System.out.println("Validating field: " + fieldName + " at path: " + currentPath);

                    // Check if this field has NA validation rule
                    boolean isNAField = VALIDATION_RULES != null &&
                            VALIDATION_RULES.has(fieldName) &&
                            isNARule(VALIDATION_RULES.get(fieldName));

                    if (isNAField) {
                        System.out.println("Skipping all validation for field " + currentPath + " as rule is set to NA");
                        return;
                    }

                    // Skip validation based on API name
                    if (shouldSkipFieldValidation(fieldName, apiName)) {
                        return;
                    }

                    // Standard field validation
                    if (expectedValue.isObject() || expectedValue.isArray()) {
                        ValidationResult nestedResult = compareAndValidateFields(expectedValue, actualValue,
                                currentPath, VALIDATION_RULES, currentTimeSeconds, apiName);
                        if (!nestedResult.isValid()) {
                            allValid[0] = false;
                            mismatchedFields.addAll(nestedResult.getMismatchedFields());
                            failedValidations.addAll(nestedResult.getFailedValidations());
                        }
                    } else {
                        String expectedStr = expectedValue.asText();
                        String actualStr = actualValue.asText();

                        // Skip value comparison for timestamp field if we have validation rules for it
                        boolean skipComparison = (fieldName.contains("timestamp") || fieldName.equals("from_timestamp")
                                || fieldName.equals("start_timestamp") || fieldName.equals("end_timestamp")
                                || fieldName.equals("expiry_time") || fieldName.equals("token")
                                || fieldName.equals("fromTime") || fieldName.equals("toTime")
                                || fieldName.equals("fromDate") || fieldName.equals("toDate")
                                || fieldName.equals("id")
                                || fieldName.equals("geofenceId")
                                || fieldName.equals("geofenceid")
                                || fieldName.equals("emergency_contact_id")
                                || fieldName.equals("expiry_timestamp")
                                || fieldName.equals("request_Id")
                                || fieldName.equals("shared_link")
                                || fieldName.equals("placeId")
                                || fieldName.equals("last_sync")
                                || fieldName.equals("expiryDateTime")

                                &&
                                VALIDATION_RULES != null &&
                                (VALIDATION_RULES.has(fieldName) || VALIDATION_RULES.has("from_timestamp")
                                        || VALIDATION_RULES.has("start_timestamp")
                                        || fieldName.equals("end_timestamp")
                                        || fieldName.equals("expiry_time")
                                        || fieldName.equals("token")
                                        || fieldName.equals("fromTime")
                                        || fieldName.equals("toTime")
                                        || fieldName.equals("fromDate")
                                        || fieldName.equals("toDate")
                                        || fieldName.equals("id")
                                        || fieldName.equals("geofenceId")
                                        || fieldName.equals("geofenceid")
                                        || fieldName.equals("emergency_contact_id")
                                        || fieldName.equals("expiry_timestamp")
                                        || fieldName.equals("request_Id")
                                        || fieldName.equals("shared_link")
                                        || fieldName.equals("placeId")
                                        || fieldName.equals("last_sync")
                                        || fieldName.equals("expiryDateTime")
                                ));

                        if (!skipComparison && !expectedStr.equals(actualStr)) {
                            // Check if this field has an NA validation rule before failing
                            if (!(VALIDATION_RULES != null && VALIDATION_RULES.has(fieldName) &&
                                    isNARule(VALIDATION_RULES.get(fieldName))) &&
                                    !shouldSkipFieldValidation(fieldName, apiName)) {
                                mismatchedFields.add(currentPath);
                                softAssert.fail("Value mismatch for field: " + currentPath +
                                        " (Expected: " + expectedStr + ", Actual: " + actualStr + ")");
                                allValid[0] = false;
                            } else {
                                System.out.println("Skipping value comparison for field " + currentPath + " (NA rule or API-specific skip)");
                            }
                        }

                        // Apply validation rules if they exist and are not "NA"
                        if (VALIDATION_RULES != null && VALIDATION_RULES.has(fieldName)) {
                            JsonNode rulesNode = VALIDATION_RULES.get(fieldName);

                            // Check if the validation rule is "NA" and skip validation if it is
                            if (isNARule(rulesNode)) {
                                System.out.println("Skipping validation for field " + currentPath + " as rule is set to NA");
                            } else {
                                // Skip validation based on API name
                                if (!shouldSkipFieldValidation(fieldName, apiName)) {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    Map<String, Object> rulesMap = objectMapper.convertValue(
                                            rulesNode, new TypeReference<Map<String, Object>>() {
                                            });
                                    if (!validateField(actualValue, rulesMap, currentPath, failedValidations)) {
                                        allValid[0] = false;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    softAssert.fail("Error comparing field " + currentPath + ": " + e.getMessage());
                    e.printStackTrace();
                    allValid[0] = false;
                }
            });

            // Then check for fields in actual that aren't in expected (like timestamp)
            actual.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();

                // Skip fields we've already processed (they exist in expected)
                if (expected.has(fieldName)) {
                    return;
                }

                // Check if this field has NA validation rule
                boolean isNAField = VALIDATION_RULES != null &&
                        VALIDATION_RULES.has(fieldName) &&
                        isNARule(VALIDATION_RULES.get(fieldName));

                if (isNAField) {
                    System.out.println("Skipping all validation for extra field " + fieldName + " as rule is set to NA");
                    return;
                }

                // Skip validation based on API name
                if (shouldSkipFieldValidation(fieldName, apiName)) {
                    return;
                }

                JsonNode actualValue = entry.getValue();
                String currentPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                System.out.println("Found extra field in actual: " + currentPath);

                // Special case for timestamp validation
                if ((fieldName.equals("timestamp") || fieldName.equals("from_timestamp")
                        || fieldName.equals("start_timestamp") || fieldName.equals("end_timestamp")
                        || fieldName.equals("expiry_time")|| fieldName.equals("expiry_timestamp")) &&
                        VALIDATION_RULES != null) {
                    String validationFieldName = fieldName;
                    // Map ts fields to timestamp fields if needed
                    if (fieldName.equals("timestamp") && VALIDATION_RULES.has("timestamp")) {
                        validationFieldName = "timestamp";
                    } else if (fieldName.equals("from_timestamp") && VALIDATION_RULES.has("from_timestamp")) {
                        validationFieldName = "from_timestamp";
                    } else if (fieldName.equals("start_timestamp") && VALIDATION_RULES.has("start_timestamp")) {
                        validationFieldName = "start_timestamp";
                    } else if (fieldName.equals("end_timestamp") && VALIDATION_RULES.has("end_timestamp")) {
                        validationFieldName = "end_timestamp";
                    } else if (fieldName.equals("expiry_time") && VALIDATION_RULES.has("expiry_time")) {
                        validationFieldName = "expiry_time";
                    }else if (fieldName.equals("expiry_timestamp") && VALIDATION_RULES.has("expiry_timestamp")) {
                        validationFieldName = "expiry_timestamp";
                    }else if (fieldName.equals("last_sync") && VALIDATION_RULES.has("last_sync")) {
                        validationFieldName = "last_sync";
                    }else if (fieldName.equals("expiryDateTime") && VALIDATION_RULES.has("expiryDateTime")) {
                        validationFieldName = "expiryDateTime";
                    }

                    if (VALIDATION_RULES.has(validationFieldName)) {
                        JsonNode timeRules = VALIDATION_RULES.get(validationFieldName);

                        // Check if the validation rule is "NA" and skip validation if it is
                        if (isNARule(timeRules)) {
                            System.out.println("Skipping validation for timestamp field " + currentPath + " as rule is set to NA");
                        } else {
                            test.info("Found timestamp field at path: " + currentPath + ", applying special validation");

                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                Map<String, Object> timeRulesMap = objectMapper.convertValue(
                                        timeRules, new TypeReference<Map<String, Object>>() {
                                        });

                                // Display actual timestamp value for debugging
                                System.out.println("Actual timestamp value: " + actualValue.asText());
                                System.out.println("Timestamp validation rules: " + timeRulesMap);

                                // Apply time validation
                                String validationType = timeRulesMap.get("ValidationType").toString();
                                if ("TimeRange".equals(validationType)) {
                                    String timeConstraint = timeRulesMap.get("TimeConstraint").toString();
                                    int offsetSeconds = Integer.parseInt(timeRulesMap.get("OffsetSeconds").toString());

                                    boolean timeValid = FieldValidator.validateTimeRange(
                                            actualValue, timeConstraint, offsetSeconds, currentPath,
                                            currentTimeSeconds);

                                    System.out.println("Timestamp validation result: " + timeValid);

                                    if (!timeValid) {
                                        failedValidations.add(currentPath + ":TimeRange");
                                        allValid[0] = false;
                                    }
                                }
                            } catch (Exception e) {
                                failedValidations.add(currentPath + ":TimeRange");
                                softAssert
                                        .fail("Timestamp validation failed for " + currentPath + ": " + e.getMessage());
                                e.printStackTrace();
                                allValid[0] = false;
                            }
                        }
                    }
                }
                // Special case for fromTime and toTime validation
                else if ((fieldName.equals("fromTime") || fieldName.equals("toTime")) &&
                        VALIDATION_RULES != null && VALIDATION_RULES.has(fieldName)) {

                    JsonNode timeRules = VALIDATION_RULES.get(fieldName);

                    // Check if the validation rule is "NA" and skip validation if it is
                    if (isNARule(timeRules)) {
                        System.out.println("Skipping validation for time field " + currentPath + " as rule is set to NA");
                    } else {
                        test.info("Found time field at path: " + currentPath + ", applying time validation");

                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> timeRulesMap = objectMapper.convertValue(
                                    timeRules, new TypeReference<Map<String, Object>>() {
                                    });

                            // Display actual time value for debugging
                            System.out.println("Actual time value: " + actualValue.asText());
                            System.out.println("Time validation rules: " + timeRulesMap);

                            // Apply time validation
                            if (timeRulesMap.containsKey("TimeConstraint") && timeRulesMap.containsKey("OffsetSeconds")) {
                                String timeConstraint = timeRulesMap.get("TimeConstraint").toString();
                                int offsetSeconds = Integer.parseInt(timeRulesMap.get("OffsetSeconds").toString());

                                boolean timeValid = FieldValidator.validateTimeRange(
                                        actualValue, timeConstraint, offsetSeconds, currentPath,
                                        currentTimeSeconds);

                                System.out.println("Time validation result: " + timeValid);

                                if (!timeValid) {
                                    failedValidations.add(currentPath + ":TimeRange");
                                    allValid[0] = false;
                                }
                            }
                        } catch (Exception e) {
                            failedValidations.add(currentPath + ":TimeRange");
                            softAssert.fail("Time validation failed for " + currentPath + ": " + e.getMessage());
                            e.printStackTrace();
                            allValid[0] = false;
                        }
                    }
                }
                // Special case for fromDate and toDate validation
                else if ((fieldName.equals("fromDate") || fieldName.equals("toDate")) &&
                        VALIDATION_RULES != null && VALIDATION_RULES.has(fieldName)) {

                    JsonNode dateRules = VALIDATION_RULES.get(fieldName);

                    // Check if the validation rule is "NA" and skip validation if it is
                    if (isNARule(dateRules)) {
                        System.out.println("Skipping validation for date field " + currentPath + " as rule is set to NA");
                    } else {
                        test.info("Found date field at path: " + currentPath + ", applying date validation");

                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> dateRulesMap = objectMapper.convertValue(
                                    dateRules, new TypeReference<Map<String, Object>>() {
                                    });

                            // Display actual date value for debugging
                            System.out.println("Actual date value: " + actualValue.asText());
                            System.out.println("Date validation rules: " + dateRulesMap);

                            // Apply date validation
                            if (dateRulesMap.containsKey("DateConstraint") && dateRulesMap.containsKey("OffsetDays")) {
                                String dateConstraint = dateRulesMap.get("DateConstraint").toString();
                                int offsetDays = Integer.parseInt(dateRulesMap.get("OffsetDays").toString());

                                boolean dateValid = FieldValidator.validateDateRange(
                                        actualValue, dateConstraint, offsetDays, currentPath,
                                        currentTimeSeconds);

                                System.out.println("Date validation result: " + dateValid);

                                if (!dateValid) {
                                    failedValidations.add(currentPath + ":DateRange");
                                    allValid[0] = false;
                                }
                            }
                        } catch (Exception e) {
                            failedValidations.add(currentPath + ":DateRange");
                            softAssert.fail("Date validation failed for " + currentPath + ": " + e.getMessage());
                            e.printStackTrace();
                            allValid[0] = false;
                        }
                    }
                }
                else if (VALIDATION_RULES != null && VALIDATION_RULES.has(fieldName)) {
                    // Apply other validation rules if they exist for this field
                    JsonNode rulesNode = VALIDATION_RULES.get(fieldName);

                    // Check if the validation rule is "NA" and skip validation if it is
                    if (isNARule(rulesNode)) {
                        System.out.println("Skipping validation for field " + currentPath + " as rule is set to NA");
                    } else {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> rulesMap = objectMapper.convertValue(
                                    rulesNode, new TypeReference<Map<String, Object>>() {
                                    });
                            if (!validateField(actualValue, rulesMap, currentPath, failedValidations)) {
                                allValid[0] = false;
                            }
                        } catch (Exception e) {
                            failedValidations.add(currentPath);
                            softAssert.fail("Validation failed for extra field " + currentPath + ": " + e.getMessage());
                            e.printStackTrace();
                            allValid[0] = false;
                        }
                    }
                }
            });
        } else if (expected.isArray() && actual.isArray()) {

            // FIXED: Properly handle array size mismatch
            if (expected.size() != actual.size()) {
                System.out.println("Inside array fail.....");
                // Add to mismatchedFields and failedValidations to ensure validation fails
                String errorMessage = "Array size mismatch at path: " + path +
                        " (Expected: " + expected.size() + ", Actual: " + actual.size() + ")";

                mismatchedFields.add(path + " (Array size mismatch)");
                failedValidations.add(path + ":ArraySizeMismatch");
                softAssert.fail(errorMessage);
                allValid[0] = false;

                // FIXED: Return immediately to prevent field comparison when array sizes don't match
                finalValidationResult = false; // Arrays don't match, so validation fails
                return new ValidationResult(finalValidationResult, mismatchedFields, failedValidations, true);
            }

//            for (int i = 0; i < Math.min(expected.size(), actual.size()); i++) {
            for (int i = 0; i < expected.size(); i++) {
                String currentPath = path + "[" + i + "]";
                ValidationResult result = compareAndValidateFields(expected.get(i), actual.get(i), currentPath,
                        VALIDATION_RULES, currentTimeSeconds, apiName);
                if (!result.isValid()) {
                    allValid[0] = false;
                    mismatchedFields.addAll(result.getMismatchedFields());
                    failedValidations.addAll(result.getFailedValidations());
                }
            }
        } else {
            if (!expected.asText().equals(actual.asText())) {
                softAssert.fail("Value mismatch at path: " + path +
                        " (Expected: " + expected.asText() + ", Actual: " + actual.asText() + ")");
                mismatchedFields.add(path);
                allValid[0] = false;
            }
        }

        // FIXED: Include allValid[0] in the final validation result
         finalValidationResult = allValid[0] && mismatchedFields.isEmpty() && failedValidations.isEmpty();
        return new ValidationResult(finalValidationResult, mismatchedFields, failedValidations, true);
    }

    /**
     * Overloaded method for backward compatibility
     */
    public static ValidationResult compareAndValidateFields(JsonNode expected, JsonNode actual, String path,
                                                            JsonNode VALIDATION_RULES, long currentTimeSeconds) throws Exception {
        return compareAndValidateFields(expected, actual, path, VALIDATION_RULES, currentTimeSeconds, null);
    }

    /**
     * Validates a single field against all defined rules
     *
     * @param {JsonNode}           value - Field value to validate
     * @param {Map<String,Object>} rules - Validation rules to apply
     * @param {string}             fieldPath - JSON path for error reporting
     * @param {List<String>}       failedValidations - List to collect failures
     * @returns {boolean} True if all validations pass
     */
    private static boolean validateField(JsonNode value, Map<String, Object> rules,
                                         String fieldPath, List<String> failedValidations) {
        boolean isValid = true;

        for (Map.Entry<String, Object> ruleEntry : rules.entrySet()) {
            String ruleType = ruleEntry.getKey();
            Object ruleValue = ruleEntry.getValue();

            // Skip validation if rule value is "NA"
            if (ruleValue instanceof String && "NA".equalsIgnoreCase((String) ruleValue)) {
                System.out.println("Skipping rule " + ruleType + " for field " + fieldPath + " as value is set to NA");
                continue;
            }

            System.out.println("Applying rule: " + ruleType + " with value: " + ruleValue + " to field: " + fieldPath);

            try {
                boolean ruleValid = false;

                switch (ruleType) {
                    case "Datatype":
                        ruleValid = FieldValidator.validateDataType(value, ruleValue.toString(), fieldPath);
                        break;
                    case "MinLimit":
                        ruleValid = FieldValidator.validateMinLimit(value, ruleValue, fieldPath);
                        break;
                    case "MaxLimit":
                        ruleValid = FieldValidator.validateMaxLimit(value, ruleValue, fieldPath);
                        break;
                    case "Length":
                        ruleValid = FieldValidator.validateLength(value, (Integer) ruleValue, fieldPath);
                        break;
                    case "Regex":
                        ruleValid = FieldValidator.validateRegex(value, ruleValue.toString(), fieldPath);
                        break;
                    case "TimeConstraint":
                        // Handle TimeConstraint with OffsetSeconds for time validation
                        if (rules.containsKey("OffsetSeconds")) {
                            int offsetSeconds = Integer.parseInt(rules.get("OffsetSeconds").toString());
                            ruleValid = FieldValidator.validateTimeRange(value, ruleValue.toString(),
                                    offsetSeconds, fieldPath, System.currentTimeMillis() / 1000);
                        }
                        break;
                    case "DateConstraint":
                        // Handle DateConstraint with OffsetDays for date validation
                        if (rules.containsKey("OffsetDays")) {
                            int offsetDays = Integer.parseInt(rules.get("OffsetDays").toString());
                            ruleValid = FieldValidator.validateDateRange(value, ruleValue.toString(),
                                    offsetDays, fieldPath, System.currentTimeMillis() / 1000);
                        }
                        break;
                    default:
                        test.warning("Unknown validation rule: " + ruleType + " for field " + fieldPath);
                        ruleValid = true; // Don't fail on unknown rule types
                }

                if (!ruleValid) {
                    failedValidations.add(fieldPath + ":" + ruleType);
                    isValid = false;
                }
            } catch (Exception e) {
                failedValidations.add(fieldPath + ":" + ruleType);
                softAssert.fail("Validation failed for " + fieldPath +
                        " (" + ruleType + "): " + e.getMessage());
                e.printStackTrace();
                isValid = false;
            }
        }
        return isValid;
    }
}