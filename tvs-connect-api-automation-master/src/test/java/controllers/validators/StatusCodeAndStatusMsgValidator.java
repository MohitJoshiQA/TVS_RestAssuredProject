package controllers.validators;
import java.util.ArrayList;
import java.util.List;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import configuration.BaseClassTest;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.validators.ResponseValidator.ValidationResult;
import static configuration.BaseClassTest.softAssert;

public class StatusCodeAndStatusMsgValidator extends BaseClassTest {

    public static ValidationResult validateStatusFields(String expectedStatusCode, JsonPath responsePath, String apiName, String expectedStatusMsg) {
        List<String> mismatchedFields = new ArrayList<>();
        List<String> failedValidations = new ArrayList<>();
        boolean allValid = true;
        String actualStatusMsg = null;

        try {
            // Check nested path: data.apiName.status
            if (apiName != null && responsePath.get("data." + apiName + ".status") != null) {
                actualStatusCode = String.valueOf(responsePath.getInt("data." + apiName + ".status"));
                actualStatusMsg = responsePath.getString("data." + apiName + ".statusMessage");

                // Check direct path: data.status (for GraphQL syntax errors or when mutation not reached)
            } else if (responsePath.get("data.status") != null) {
                actualStatusCode = String.valueOf(responsePath.getInt("data.status"));
                actualStatusMsg = responsePath.getString("data.statusMessage");

                // Check direct top-level path: apiName.status
            } else if (apiName != null && responsePath.get(apiName + ".status") != null) {
                actualStatusCode = String.valueOf(responsePath.getInt(apiName + ".status"));
                actualStatusMsg = responsePath.getString(apiName + ".statusMessage");

                // No valid status path found
            } else {
                failedValidations.add("Could not extract status code from response - no matching path found");
                allValid = false;
                System.err.println("No valid status path found. Available paths in response:");

                // // Debug: print available paths
                // try {
                //     System.err.println("Response structure: " + responsePath.prettify());
                // } catch (Exception e) {
                //     System.err.println("Could not print response structure");
                // }
                
                return new ValidationResult(allValid, mismatchedFields, failedValidations, allValid);
            }

            // Validate "status"
            if (actualStatusCode != null && !actualStatusCode.equals(expectedStatusCode)) {
                mismatchedFields.add("status");
                failedValidations.add(String.format("Status code mismatch: Expected %s but got %s", expectedStatusCode, actualStatusCode));
                allValid = false;
            }

            // Validate "statusMessage"
            if (actualStatusMsg != null && !actualStatusMsg.equals(expectedStatusMsg)) {
                mismatchedFields.add("statusMessage");
                failedValidations.add("Mismatch in 'statusMessage': expected=" + expectedStatusMsg + ", actual=" + actualStatusMsg);
                allValid = false;
            }

        } catch (Exception ex) {
            softAssert.fail("Error comparing status code and status message: " + ex.getMessage());
            ex.printStackTrace();
            allValid = false;
            failedValidations.add("Exception during validation: " + ex.getMessage());
        }

        System.out.println("Validation complete. ActualStatusCode: " + actualStatusCode + ", ActualStatusMsg: " + actualStatusMsg);
        return new ValidationResult(allValid, mismatchedFields, failedValidations, allValid);
    }
}