package api.auth;

import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class VehicleUtils {

    public static class VehicleInfo {
        public String userVehicleId;
        public String vehicleTypeId;
    }

    /**
     * Fetches vehicle info by VIN from the user's vehicle list.
     * Returns VehicleInfo object if found, else null.
     */
    public static VehicleInfo fetchVehicleInfoByVin(String vin) {
        Response response = given()
                .relaxedHTTPSValidation()
                .header("Token", TokenManager.token)
                .header("Userid", TokenManager.iceUserId)
                .header("Content-Type", "application/json")
                .header("countryid", "null")
                .get("/api/Vehicle/GetVahicleList?userid=" + TokenManager.iceUserId);

        if (response.getStatusCode() != 200) {
            System.out.println("Failed to fetch vehicle list. Status: " + response.getStatusCode());
            return null;
        }

        // Extract list of vehicles from response JSON path "Data"
        List<Map<String, Object>> vehicles = response.jsonPath().getList("Data");

        if (vehicles == null || vehicles.isEmpty()) {
            System.out.println("No vehicles found for user: " + TokenManager.iceUserId);
            return null;
        }

        for (Map<String, Object> vehicle : vehicles) {
            String vehicleVin = (String) vehicle.get("FRAME_NO");
            if (vin.equalsIgnoreCase(vehicleVin)) {
                VehicleInfo info = new VehicleInfo();
                info.userVehicleId = String.valueOf(vehicle.get("UserVehicleId"));
                info.vehicleTypeId = String.valueOf(vehicle.get("VehicleTypeId"));
                return info;
            }
        }

        System.out.println("Vehicle with VIN '" + vin + "' not found for user: " + TokenManager.iceUserId);
        return null;
    }
}
