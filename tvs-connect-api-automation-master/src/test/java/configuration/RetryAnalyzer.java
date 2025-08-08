package configuration;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer
{private int retryCount = 0;
    private final int maxRetryCount = 3; // Max number of retries
    private final int[] retryDelays = {1000, 3000, 5000}; // Delays in milliseconds (1s, 3s, 5s)

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            try {
                System.out.println("Retrying test: " + result.getName() + " | Attempt: " + (retryCount + 1) +
                        " | Delay: " + retryDelays[retryCount] + "ms");
                Thread.sleep(retryDelays[retryCount]); // Delay before retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Retry interrupted: " + e.getMessage());
            }
            retryCount++;
            return true; // Retry the test
        }
        return false; // Stop retrying
    }
}
