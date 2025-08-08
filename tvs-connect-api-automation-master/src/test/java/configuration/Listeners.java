package configuration;
import com.aventstack.extentreports.*;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.*;

import java.io.File;
import java.util.ArrayList;

import static ReportGenerator.CsvReportGenerator.csvGenerator;

public class Listeners extends BaseClassTest implements ITestListener
{
    public static ExtentReports extent;
    public static ExtentTest test;

    public static ArrayList<String> TestCaseId = new ArrayList<>();
    public static ArrayList<String> Model = new ArrayList<>();
    public static ArrayList<String> APIName = new ArrayList<>();
    public static ArrayList<String> MethodList = new ArrayList<>();
    public static ArrayList<String> Request = new ArrayList<>();
    public static ArrayList<String> ExpectedHTTPcode = new ArrayList<>();
    public static ArrayList<String> ExpectedResponse = new ArrayList<>();
    public static ArrayList<String> ActualHTTPcode = new ArrayList<>();
    public static ArrayList<String> ActaulResponse = new ArrayList<>();
    public static ArrayList<String> APIStatus = new ArrayList<>();
    public static ArrayList<String> Throwable = new ArrayList<>();
    public static ArrayList<String> passedAPIs = new ArrayList<>();
    public static ArrayList<String> failedAPIs = new ArrayList<>();
    public static ArrayList<String> skippedAPIs = new ArrayList<>();

    // Test data lists
    public static int passedTestCount = 0;
    public static int failedTestCount = 0;
    public static int skippedTestCount = 0;

    @Override
    public void onTestStart(ITestResult result) {
        test = extent.createTest(result.getName());
        // Capture the package name (Model) for each test
//        String packageName = result.getTestClass().getRealClass().getPackage().getName();
//        Model.add(model);

        // Capture the API name (class name)
//        String className = result.getTestClass().getRealClass().getSimpleName(); // Class name (API name)
       // APIName.add(className); // Store the class name (API name)
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.log(Status.PASS, MarkupHelper.createLabel(name + " - Test Case Passed", ExtentColor.GREEN));
        passedTestCount++;
        
        APIStatus.add("PASS");
        TestCaseId.add(id);
        Model.add(model);
        APIName.add(apiName);
        MethodList.add(name);
        Request.add(request);
        ExpectedHTTPcode.add(expectedStatusCode);
        ExpectedResponse.add(expected);
        ActualHTTPcode.add(actualStatusCode);
        ActaulResponse.add(actualResponseBody);
        Throwable.add("NA");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test Case Failed", ExtentColor.RED));
        test.fail(result.getThrowable());
        failedTestCount++;
//        failedAPIs.add(result.getName());
//        MethodList.add(result.getName());
       // APIStatus.add("FAIL");

        APIStatus.add("FAIL");
        TestCaseId.add(id);
        Model.add(model);
        APIName.add(apiName);
        MethodList.add(name);
        Request.add(request);
        ExpectedHTTPcode.add(expectedStatusCode);
        ExpectedResponse.add(expected);
        ActualHTTPcode.add(actualStatusCode);
        ActaulResponse.add(actualResponseBody);
//        Throwable.add(errorDetails+"/n"+result.getThrowable().toString());
        Throwable.add(errorDetails);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " - Test Case Skipped", ExtentColor.YELLOW));
        skippedTestCount++;
//        skippedAPIs.add(result.getName());
//        MethodList.add(result.getName());
        APIStatus.add("SKIP");
        Throwable.add("NA");
        Request.add("NA");
        ActaulResponse.add("NA");
        TestCaseId.add(id);
        Model.add(model);
        APIName.add(apiName);
        MethodList.add(name);

    }

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(OUTPUT_FOLDER + FILE_NAME);
        final File confFile = new File("./src/main/java/extentReport/extentReportConfig.xml");

        try {
            // Load the configuration file
            htmlReporter.loadXMLConfig(confFile);

            // Initialize ExtentReports and attach the reporter
            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);

        } catch (Exception e) {
            // Handle the exception if loading the config file or initializing ExtentReports fails
            throw new RuntimeException("Error while setting up the ExtentReports reporter.", e);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        // Generate CSV report after all tests are executed
        try {
            csvGenerator(TestCaseId,Model, APIName, MethodList, Request,ExpectedHTTPcode,ExpectedResponse,ActualHTTPcode,ActaulResponse, APIStatus, Throwable, skippedAPIs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

