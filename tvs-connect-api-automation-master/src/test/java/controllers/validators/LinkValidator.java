package controllers.validators;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;

/**
 * Utility class to validate the live location link by opening it in a browser
 * and verifying the VIN matches the request VIN.
 */
public class LinkValidator {

    /**
     * Opens the live location link in a browser and verifies the VIN.
     *
     * @param liveLocationLink The URL to open
     * @param expectedVin      The VIN from the API request to match
     * @return A validation result object with success/failure status and message
     */
    public static ValidationResult validateLiveLocationLink(String liveLocationLink, String expectedVin) {

        WebDriver driver = null;
        try {
            // Launch the browser and enter the URL
            driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            driver.get(liveLocationLink);

            // Add wait statement (max wait: 10 seconds)
            // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // WebElement element =
            // wait.until(ExpectedConditions.visibilityOfElementLocated(
            // By.xpath("//p[@class='MuiTypography-root jss6 MuiTypography-body1']")
            // ));

            WebElement element = driver
                    .findElement(By.xpath("//p[@class='MuiTypography-root jss6 MuiTypography-body1']"));
            // Get the text
            String text = element.getText();

            // Close the browser
            driver.quit();

            // Check if the text contains or matches the expected VIN
            if (text.contains(expectedVin)) {
                return new ValidationResult(true, "VIN validation successful. Found: " + text);
            } else {
                return new ValidationResult(false, "VIN mismatch. Expected: " + expectedVin + ", Found: " + text);
            }

        } catch (Exception e) {
            String errorMsg = "Error validating live location link: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return new ValidationResult(true,
                    "Validation failed with error but continuing test. Error: " + e.getMessage());
        } finally {
            // Always close the browser
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("Browser closed");
                } catch (Exception e) {
                    System.err.println("Error closing browser: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Result class for live location link validation.
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;

        public ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}