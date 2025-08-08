package api.auth;

import configuration.BaseClass;
import configuration.ConfigReader;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AuthoFlowTestU745 extends BaseClass {
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
                .header("version", "2")
                .header("EVUserId", "null")
                .body("{\"MobileNumber\":\"" + mobileNumber + "\",\"Otp\":\"" + otp + "\"}")
                .post("/api/v3/UserLogin/VerifyLoginOtp");

        Assert.assertEquals(verifyResponse.getStatusCode(), 200);

        System.out.println("Full verifyResponse: " + verifyResponse.asPrettyString());

        // Set tokens and user id
        TokenManager.token = verifyResponse.jsonPath().getString("Data.iceUser[0].Token");
        TokenManager.accessToken = verifyResponse.jsonPath().getString("Data.iceUser[0].AccessToken");
        TokenManager.iceUserId = verifyResponse.jsonPath().getString("Data.iceUser[0].UserId");
        //TokenManager.evUserId = verifyResponse.jsonPath().getInt("Data.evUser[0].UserId");

        System.out.println("✅ Auth token fetched successfully: " + TokenManager.token);
        System.out.println("✅ Auth access token fetched successfully: " + TokenManager.accessToken);
        System.out.println("✅ ICE User ID fetched successfully: " + TokenManager.iceUserId);
       // System.out.println(" ICE User ID fetched successfully: " + TokenManager.evUserId);


        // Fetch vehicle info for the current VIN


        Response vehicleU745 = given()
                .contentType("application/json")
                .header("Content-Type", "application/json")
                .header("userid", TokenManager.iceUserId)
                .header("token",TokenManager.token)
                .header("ICEUserId", TokenManager.iceUserId)
                .header("EVUserId", "null")
                .body("{\n" +
                        "    \"UserId\":"+TokenManager.iceUserId+",\n" +
                        "    \"VehicleName\": \"TVS JUPITER\",\n" +
                        "    \"SERIES\": \"JUPITER\",\n" +
                        "    \"CONNECTED\": \"1\",\n" +
                        "    \"NickName\": \"Jupiter Red Panther\",\n" +
                        //  "    \"theme\": 3,\n" +
                        "    \"DMP_MODEL_ID\": \"6\",\n" +
                        "    \"Vin\": \"\",\n" +
                        "    \"VinRegisteredMobileNumber\": \"\",\n" +
                        "    \"FRAME_NO\": \"MD627ACA48S19A1173\",\n" +
                        "    \"CONTACT_NO\": \""+ConfigReader.get("defaultMobile")+ "\",\n" +
                        "    \"CONTACT_TYPE\": \"\",\n" +
                        "    \"SALE_DATE\": \"2024-01-01T00:00:00+05:30\",\n" +
                        "    \"REG_NO\": \"Ka 009\",\n" +
                        "    \"ENGINE_NO\": \"BK4P1004214\",\n" +
                        "    \"DEALER_ID\": \"10964\",\n" +
                        "    \"CUSTOMER_NAME\": \"{{name}}\",\n" +
                        "    \"CUSTOMER_ID\": \"\",\n" +
                        "    \"TOV_PART_ID\": \"K6191570CK\",\n" +
                        "    \"TOV_MODEL_ID\": \"\",\n" +
                        "    \"EMAIL_ADDRESS\": \"\",\n" +
                        "    \"LAST_SERVICE_DATE\": \"2023-01-12T05:53:12.657Z\",\n" +
                        "    \"LAST_SERVICE_TYPE\": \"\",\n" +
                        "    \"DMP_PART_ID\": \"0\",\n" +
                        "    \"DESCRIPTION\": \"\",\n" +
                        "    \"ACTIVE\": \"1\",\n" +
                        "    \"COLOR\": \"\",\n" +
                        "    \"BRAND_CODE\": \"\",\n" +
                        "    \"BRAND_DESC\": \"\",\n" +
                        "    \"IOT\": \"\",\n" +
                        "    \"MAC_ID\": \"\"\n" +
                        "}")
                .post("/api/v3/Vehicle/AddVehicle");


        Assert.assertEquals(vehicleU745.getStatusCode(), 200);

        System.out.println("Full verifyResponse: " + vehicleU745.asPrettyString());
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
