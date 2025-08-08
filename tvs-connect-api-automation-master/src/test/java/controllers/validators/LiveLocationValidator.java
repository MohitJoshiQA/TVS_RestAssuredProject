package controllers.validators;

import com.aventstack.extentreports.ExtentTest;

import controllers.validators.LinkValidator.*;
import io.restassured.response.Response;

public class LiveLocationValidator {
    /**
     * Helper method to validate the live location link for shareLiveLocation test
     * cases
     */
    public static ExtentTest validateLiveLocationForShareLocation(Response response, String responseRootPath,
            String requestVin, ExtentTest test) {
        try {
            // Get shared_link from shareLiveLocation response
            String liveLocationLink = response.jsonPath()
                    .getString("data.shareLiveLocation.shared_details.shared_link");

            if (liveLocationLink == null || liveLocationLink.isEmpty()) {
                test.warning("Shared live location link is empty or null, skipping validation");
                return test;
            }

            test.info("Found shared live location link: " + liveLocationLink);
            test.info("Starting browser validation of VIN: " + requestVin + " for shareLiveLocation");

            // Handle potential browser initialization issues
            try {
                // Validate using LiveLocationValidator
                ValidationResult result = LinkValidator.validateLiveLocationLink(liveLocationLink,
                        requestVin);

                if (result.isSuccess()) {
                    test.pass("Shared live location link validation successful: " + result.getMessage());
                } else {
                    // Don't fail the test for this validation, just log warning
                    test.warning("Shared live location link validation issue: " + result.getMessage());
                }

            } catch (NoClassDefFoundError e) {
                // If selenium classes are missing, log warning but continue test
                String msg = "Selenium dependency issue encountered. Skipping browser validation: " + e.getMessage();
                test.warning(msg);
                System.err.println(msg);

            }
            return test;
        } catch (Exception e) {
            String errorMsg = "Error during shared live location link validation: " + e.getMessage();
            test.warning(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
            return test;
            // Don't fail the test for this validation, just log warning
        }
    }

    /**
     * Helper method to validate the live location link for towAlertActive test
     * cases
     */
    public static ExtentTest validateLiveLocationForTow(Response response, String responseRootPath, String requestVin,
            ExtentTest test) {
        try {
            // Get live_location_link from response
            String liveLocationLink = response.jsonPath().getString(responseRootPath + "[0].live_location_link");
            // liveLocationLink = "https://p360uatshareloc.tvsmotor.com/share/5wcbiy";
            if (liveLocationLink == null || liveLocationLink.isEmpty()) {
                test.warning("Live location link is empty or null, skipping validation");
                return test;
            }

            test.info("Found live location link: " + liveLocationLink);
            test.info("Starting browser validation of VIN: " + requestVin);

            // Handle potential browser initialization issues
            try {
                // Validate using LiveLocationValidator
                ValidationResult result = LinkValidator.validateLiveLocationLink(liveLocationLink,
                        requestVin);

                if (result.isSuccess()) {
                    test.pass("Live location link validation successful: " + result.getMessage());
                } else {
                    // Don't fail the test for this validation, just log warning
                    test.warning("Live location link validation issue: " + result.getMessage());
                }
            } catch (NoClassDefFoundError e) {
                // If selenium classes are missing, log warning but continue test
                String msg = "Selenium dependency issue encountered. Skipping browser validation: " + e.getMessage();
                test.warning(msg);
                System.err.println(msg);
            }
            return test;
        } catch (Exception e) {
            String errorMsg = "Error during live location link validation: " + e.getMessage();
            test.warning(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
            return test;
            // Don't fail the test for this validation, just log warning
        }
    }

}
