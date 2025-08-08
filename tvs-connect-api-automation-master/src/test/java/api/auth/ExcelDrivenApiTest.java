package api.auth;

import Utils.ExcelCommunicator;
import Utils.FileUtils;
import api.auth.TokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ExcelDrivenApiTest {

    String filePath = System.getProperty("user.dir") + "/" + ConfigReader.getExcelFilePath();
    String sheetName = ConfigReader.getExcelSheetName();

    static Map<Integer, Map<Integer, String>> finalUpdates = new HashMap<>();

    @DataProvider(name = "apiDataProvider")
    public Object[][] apiDataProvider() throws Exception {
        int lastRow = ExcelCommunicator.getLastRow(filePath, sheetName);
        Object[][] data = new Object[lastRow][1];

        for (int i = 1; i <= lastRow; i++) {
            Map<String, String> rowData = new HashMap<>();
            rowData.put("rowNumber", String.valueOf(i));
            rowData.put("testCaseId", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 0));
            rowData.put("endpoint", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 4));
            rowData.put("method", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 8));
            rowData.put("requestBody", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 5));
            rowData.put("expectedStatusCode", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 7));
            rowData.put("expectedData", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 6));
            rowData.put("validationRules", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 9));
           // rowData.put("variablesToSave", ExcelCommunicator.getDataFromExcel(filePath, sheetName, i, 13));
            data[i - 1][0] = rowData;
        }
        return data;
    }

    @Test(dataProvider = "apiDataProvider")
    public void runApiTests(Map<String, String> apiData) {
        int rowNumber = Integer.parseInt(apiData.get("rowNumber"));
        String endpoint = replaceDynamicVars(apiData.get("endpoint"));
        String method = apiData.get("method");
        String requestBody = replaceDynamicVars(apiData.get("requestBody"));
        String validationRules = apiData.get("validationRules");
        int expectedStatusCode = Integer.parseInt(apiData.get("expectedStatusCode"));

        String statusResult = "Pass";
        String failureReason = "";
        String actualResponse = "";

        try {
            RestAssured.baseURI = ConfigReader.get("baseURI");

            Response response;
            if ("POST".equalsIgnoreCase(method)) {
                response = given()
                        .relaxedHTTPSValidation()
                        .header("Authorization", "Bearer " + TokenManager.accessToken)
                        .header("Token", TokenManager.token)
                        .header("Userid", TokenManager.iceUserId)
                        .header("Content-Type", "application/json")
                        .header("EVUserid", "null")
                        .body(requestBody)
                        .post(endpoint);
            } else if ("GET".equalsIgnoreCase(method)) {
                response = given()
                        .relaxedHTTPSValidation()
                        .header("Authorization", "Bearer " + TokenManager.accessToken)
                        .header("Token", TokenManager.token)
                        .header("Userid", TokenManager.iceUserId)
                        .header("Content-Type", "application/json")
                        .get(endpoint);
            } else if ("PUT".equalsIgnoreCase(method)) {
                response = given()
                        .relaxedHTTPSValidation()
                        .header("Authorization", "Bearer " + TokenManager.accessToken)
                        .header("Token", TokenManager.token)
                        .header("Userid", TokenManager.iceUserId)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .put(endpoint);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                response = given()
                        .relaxedHTTPSValidation()
                        .header("Authorization", "Bearer " + TokenManager.accessToken)
                        .header("Token", TokenManager.token)
                        .header("Userid", TokenManager.iceUserId)
                        .header("Content-Type", "application/json")
                        .body(requestBody)
                        .delete(endpoint);
            }
            else {
                throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
            }

            Assert.assertNotNull(response, "Response is null");
            actualResponse = response.asPrettyString();

            int actualStatus = response.getStatusCode();
            Assert.assertEquals(actualStatus, expectedStatusCode, "Status code mismatch");

            // 1. Validate result field if present
            String resultValue = null;
            try {
                resultValue = response.jsonPath().getString("Result");
            } catch (Exception ignored) {}
            if (resultValue != null && !resultValue.equalsIgnoreCase("success")) {
                throw new AssertionError("API business validation failed: Result = " + resultValue);
            }

// 2. Handle expected JSON response validation
//            String expectedDataRaw = apiData.get("expectedData");
//            if (expectedDataRaw != null && !expectedDataRaw.trim().equalsIgnoreCase("NA")) {
//                String expectedJson;
//
//                if (expectedDataRaw.startsWith("file:")) {
//                    // Load from file
//                    String fileName = expectedDataRaw.replace("file:", "").trim();
//                    expectedJson = FileUtils.readJsonFileAsString(fileName);  // assumes src/test/resources/expectedResponses
//                } else {
//                    // Inline JSON from Excel
//                    expectedJson = expectedDataRaw.trim();
//                }
//
//                // Do lenient comparison
//                org.skyscreamer.jsonassert.JSONAssert.assertEquals(expectedJson, actualResponse, org.skyscreamer.jsonassert.JSONCompareMode.LENIENT);
//            }

// 3. Rule-based validation
            if (validationRules != null && !validationRules.trim().equalsIgnoreCase("NA")) {
                String[] rules = validationRules.split(";");
                for (String rule : rules) {
                    if (rule.contains("=")) {
                        String[] parts = rule.split("=");
                        String jsonPath = parts[0].trim();
                        String expectedValue = parts[1].trim();
                        String actualValue = response.jsonPath().getString(jsonPath);
                        Assert.assertEquals(actualValue, expectedValue,
                                "JSONPath validation failed for " + jsonPath +
                                        ": expected " + expectedValue + " but got " + actualValue);
                    }
                }
            }
          //  saveResponseVariablesFromExcel(response, rowNumber);  // <- saving response variable here

            System.out.println("✅ Test PASSED for row " + rowNumber);

        } catch (AssertionError | Exception e) {
            statusResult = "Fail";
            failureReason = e.getMessage();
            System.out.println("❌ Test FAILED for row " + rowNumber + ": " + failureReason);
        }

        // Always update finalUpdates regardless of pass or fail
        Map<Integer, String> cellMap = new HashMap<>();
        cellMap.put(10, actualResponse);
        cellMap.put(11, statusResult);
        cellMap.put(12, failureReason);
        finalUpdates.put(rowNumber, cellMap);

        // If failed, make test fail for TestNG reporting
        if ("Fail".equals(statusResult)) {
            Assert.fail("Test failed on row " + rowNumber + ": " + failureReason);
        }
    }


    /**
     * Replace placeholders in strings with live values from TokenManager.
     * Supports ${iceUserId}, ${userVehicleId}, ${vehicleTypeId}, ${token}, ${accessToken}
     */
    private static String replaceDynamicVars(String text) {
        if (text == null || text.equalsIgnoreCase("NA")) {
            return text;
        }
        return text
                .replace("${iceUserId}", TokenManager.iceUserId != null ? TokenManager.iceUserId : "")
                .replace("${userVehicleId}", TokenManager.userVehicleId != null ? TokenManager.userVehicleId : "")
                .replace("${vehicleTypeId}", TokenManager.vehicleTypeId != null ? TokenManager.vehicleTypeId : "")
                .replace("${token}", TokenManager.token != null ? TokenManager.token : "")
                .replace("${accessToken}", TokenManager.accessToken != null ? TokenManager.accessToken : "")
                .replace("${defaultMobile}", ConfigReader.get("defaultMobile"));
    }

//    private void saveResponseVariablesFromExcel(Response response, int rowNum) {
//        try {
//            String variablesToSaveRaw = ExcelCommunicator.getDataFromExcel(filePath, sheetName, rowNum, 13); // Assuming column N = index 13
//            if (variablesToSaveRaw == null || variablesToSaveRaw.trim().equalsIgnoreCase("NA")) return;
//
//            String[] variableNames = variablesToSaveRaw.split("\\r?\\n");
//
//            for (String variableName : variableNames) {
//                variableName = variableName.trim();
//                if (variableName.isEmpty()) continue;
//
//                String value = findFirstMatchingKeyInJson(response, variableName);
//                if (value != null) {
//                    TokenManager.saveVariable(variableName, value);
//                    System.out.println("✅ Saved variable: " + variableName + " = " + value);
//                } else {
//                    System.out.println("⚠️ No match found in response for key: " + variableName);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Error while saving variables from response: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private String findFirstMatchingKeyInJson(Response response, String key) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(response.asString());
//            return findKeyRecursive(root, key);
//        } catch (Exception e) {
//            System.err.println("❌ Failed to parse JSON response for key lookup: " + e.getMessage());
//            return null;
//        }
//    }
//
//    private String findKeyRecursive(JsonNode node, String key) {
//        if (node.has(key)) {
//            return node.get(key).asText();
//        }
//        for (JsonNode child : node) {
//            String result = findKeyRecursive(child, key);
//            if (result != null) return result;
//        }
//        return null;
//    }

    @AfterSuite
    public void afterSuiteWriteBack() {
        try {
            System.out.println("Writing results to Excel. Number of rows to update: " + finalUpdates.size());
            System.out.println("Output file path: " + filePath.replace(".xlsx", "_output.xlsx"));
            ExcelCommunicator.writeAllDataToExcel(filePath, sheetName, finalUpdates);
            System.out.println("✅ All results written to Excel in one shot");
        } catch (Exception e) {
            System.err.println("❌ Exception while writing Excel output:");
            e.printStackTrace();
        }
    }

}
