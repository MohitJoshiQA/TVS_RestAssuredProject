package configuration;

import Utils.Property;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class BaseClassTest {

    public static String ExcelFilePath;
    public static String DevServiceRole;
    public static final String OUTPUT_FOLDER = "./Reports/";
    public static String FILE_NAME = "TestExecutionReport(DEV).html";
    public static String model;
    public static SoftAssert softAssert;
    public static String id;
    public static String name;
    public static String request;
    public static String expected;
    public static String expectedStatusCode;
    public static String apiName;
    public static JsonNode validationObject;
    public static String statusPath;
    public static String actualStatusCode;
    public static String actualResponseBody;
    public static String errorDetails;

    @BeforeSuite
    public void beforeSuite() throws IOException {
        System.out.println("BeforeSuite");
        // writting service auth token to property file
        DevServiceRole = Property.readProperties("DEV_Service_role");
    }

    public static String serviceAuthToken;
    public static String executingInstance;
    @Parameters({ "instance", "Vin", "UserId", "DeviceId", "Token", "RoleId", "model" })
    @BeforeTest
    public void beforeTest(String instance, String Vin, String UserId, String DeviceID, String Token, String RoleId,String model1)
            throws IOException {
        System.out.println("BeforeTest");
        model = model1;
        executingInstance = instance;

        // reading data from properties file
        instance = Property.readProperties(instance);
        Vin = Property.readProperties(Vin);
        UserId = Property.readProperties(UserId);
        DeviceID = Property.readProperties(DeviceID);
        RoleId = Property.readProperties(RoleId);


        if (executingInstance.equals("DEV_URL")) {
            serviceAuthToken = TokenGenerator.generateServiceAuthToken(instance, RoleId);
            ExcelFilePath = Property.readProperties("DEV_ExcelPath");
        } else if (executingInstance.equals("UAT_URL")) {
            serviceAuthToken = TokenGenerator.generateServiceAuthToken(instance, RoleId);
            ExcelFilePath = Property.readProperties("UAT_ExcelPath");
        } else {
            serviceAuthToken = TokenGenerator.generateServiceAuthToken(instance, RoleId);
            ExcelFilePath = Property.readProperties("PROD_ExcelPath");
        }
        System.out.println("serviceauthtoken= " + serviceAuthToken);

        String tokenData;
//        tokenData = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm90ZWN0ZWQiOiJleUpsYm1NaU9pSkJNalUyUjBOTklpd2lZV3huSWpvaVpHbHlJaXdpYTJsa0lqb2lObUpFVWxaZlNrZE5jVVpJZFhwUVZrWTVUM1p2YjNKWlRVcG9kMEZIVG5Oa1kwbzBNRzVPVEZwaWR5SjkiLCJpdiI6IlJoWkMyMXc2bVFIUE4tVFoiLCJjaXBoZXJ0ZXh0IjoieVFvUGkwcDFES1huUjhEdzFGSHhxd01teHh0bWRBUHpIbTRyWW1YLUxTTl9WdjE3UDFqVER1emlFNnFfZktzSXpvanJacVNMN01ZWTRwRHVZZlI5TzVuRS0wWXlrcmhsTG9yS3I3dXV5X0RFMVR2Ny01RWMxcjdyWlp2VXdVSmNadHZLbDhtX0t3Q1ZWeklNdkZsclFzS3pwM2VsUldxcTZVWmMzTGdQcElrSDZVZl95bXdrLVQ0VzFFOG9pT01LWWlOZ0QzOWR1UDhPY2tJR0RWZUdRQ0xyU3ZDS1lVWEdjRUJRX2h1YVkxZ3FZSzdXcXdWakZGU0E3SUlBSXdCdzJXMDVZdzFQdDFOOGRFQU9WTmZYSThyX3d5RGdsbFIzc0lpblV0NDUwYXJwSXdFaGhCNl9tNmY4cXlIS1ZyRDBVa0N2Q1ZMRF9QUnBoelB3cEhmdjZhOXEtQndaMjU1WXladWxWcXdnNnJIbXc5OElvZzBtM0hOaHF4NUViM1RkSEVsMFBCYUhYZyIsInRhZyI6IjhjeXJmenBxd2ZhSDIzSG5rbzVMamciLCJpYXQiOjE3NDk1NjQ4Mzl9.sIHYbHExt6k5eYa4z7Ppn-uUlT1XfxLLwBliAnVagWE";
        if (DeviceID.equals("null")) {
            tokenData = TokenGenerator.reGenerateToken(instance, Vin, UserId);
        } else {
            tokenData = TokenGenerator.reGenerateToken(instance, Vin, UserId, DeviceID);
        }

        if (model.equals("Onboard")||model.equals("OffBoard")) {
            tokenData = serviceAuthToken;
        }

        if(tokenData == null){
            throw new RuntimeException("Failed to generate regenerate token");
        }

        Property.propertyWriter(Token,tokenData);
    }

    public static String instanceE;
    public static String VinE;
    public static String UserIdE;
    public static String DeviceIDE;
    public static String authToken;

    @Parameters({ "instance", "Vin", "UserId", "DeviceId", "Token" })
    @BeforeClass
    public static void beforeClass(String instance, String Vin, String UserId, String DeviceID, String Token) throws InterruptedException, IOException {
        System.out.println("BeforeClass");
        instanceE = Property.readProperties(instance);
        VinE = Property.readProperties(Vin);
        UserIdE = Property.readProperties(UserId);
        DeviceIDE = Property.readProperties(DeviceID);
        authToken = Property.readProperties(Token);

        // We're moving softAssert initialization to each test method to avoid state sharing
        // This line is now commented out to avoid creating a class-level softAssert
        // softAssert = new SoftAssert();
    }

    @AfterClass
    public void afterClass() {
        System.out.println("AfterClass");
        // We don't call softAssert.assertAll() here anymore as it's called in each test method
    }

    @AfterTest
    public void afterTest() {
        System.out.println("After Test");
        // Cleanup after test execution
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("After Suite");
    }
}