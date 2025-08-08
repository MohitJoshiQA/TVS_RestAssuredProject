package api.auth;

import Utils.FileUtils;
import api.auth.TokenManager;
import configuration.BaseClass;
import configuration.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class AuthFlowTest extends BaseClass {

    @DataProvider(name = "vinProvider")
    public Object[][] vinProvider() {
        return new Object[][] {
               // {"MD626AK52S2B00076"},
                  {"MD627ACA48S19A1173"}
                  //{"MD626AK55S2D00027"}
                // add more VINs here
        };
    }

    @Test(dataProvider = "vinProvider")
    public void requestOtpFetchTokenAndVehicleData(String vin) {
        String mobileNumber = ConfigReader.get("defaultMobile");
        String otp = ConfigReader.get("defaultOtp");

        // Request OTP
        Response otpResponse = given()
                .contentType("application/json")
                .body("{\"MobileNumber\":\"" + mobileNumber + "\"}")
                .post("/api/v3/UserLogin/Loginv3");

        Assert.assertEquals(otpResponse.getStatusCode(), 200);

        // Verify OTP
        Response verifyResponse = given()
                .contentType("application/json")
                .header("Content-Type", "application/json")
                .header("ICEUserId", "4333247")
                .header("EVUserId", "null")
                .header("version", "2")
                .body("{\"MobileNumber\":\"" + mobileNumber + "\",\"Otp\":\"" + otp + "\"}")
                .post("/api/v3/UserLogin/VerifyLoginOtp");

        Assert.assertEquals(verifyResponse.getStatusCode(), 200);

        System.out.println("Full verifyResponse: " + verifyResponse.asPrettyString());

        // Set tokens and user id
        TokenManager.token = verifyResponse.jsonPath().getString("Data.iceUser[0].Token");
        TokenManager.accessToken = verifyResponse.jsonPath().getString("Data.iceUser[0].AccessToken");
        TokenManager.iceUserId = verifyResponse.jsonPath().getString("Data.iceUser[0].UserId");


        System.out.println("✅ Auth token fetched successfully: " + TokenManager.token);
        System.out.println("✅ Auth access token fetched successfully: " + TokenManager.accessToken);
        System.out.println("✅ ICE User ID fetched successfully: " + TokenManager.iceUserId);

        // Fetch vehicle info for the current VIN
        VehicleUtils.VehicleInfo vehicleInfo = VehicleUtils.fetchVehicleInfoByVin(vin);


        if (vehicleInfo != null) {
            TokenManager.userVehicleId = vehicleInfo.userVehicleId;
            TokenManager.vehicleTypeId = vehicleInfo.vehicleTypeId;
            System.out.println("✅ userVehicleId fetched successfully: " + vehicleInfo.userVehicleId);
            System.out.println("✅ vehicleTypeId fetched successfully: " + vehicleInfo.vehicleTypeId);
        } else {
            System.out.println("❌ Vehicle info NOT found for VIN: " + vin);
        }
    }
//    @Test(dependsOnMethods = "requestOtpFetchTokenAndVehicleData")
//    public void validateFaqApiResponse() throws IOException {
//        // 1. Hit the API
//        Response response = given()
//                .header("Token", TokenManager.token)
//                .header("Userid", TokenManager.iceUserId)
//                .contentType("application/json")
//                .get("/03/api/faq/getfaq");
//
//        // 2. Load expected JSON from file
//        String expectedJson = FileUtils.readJsonFileAsString("getfaq.json");
//
//        // 3. Assert actual == expected using JsonAssert or direct string comparison
//        String actualJson = response.getBody().asString();
//
//        //using org.skyscreamer.jsonassert for lenient comparison:
//        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
//    }
}