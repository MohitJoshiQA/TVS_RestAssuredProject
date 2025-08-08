package configuration;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

public class TokenGenerator {

	// Method to generate service auth token
	public static String generateServiceAuthToken(String baseUrl, String serviceRoleId) {
		String graphqlQuery = Mutations.generateServiceAuthToken.replace("roleid", serviceRoleId);
		JSONObject json = new JSONObject();
		String graphqlBody = json.put("query", graphqlQuery).toString();

		System.out.println("graphqlBody: "+graphqlBody);
		// Make the API request
		Response response = RestAssured.given()
				.header("Content-Type", "application/json")
				.body(graphqlBody)
				.post(baseUrl);
		String serviveAuthToken=response.jsonPath().get("data.generateServiceAuthToken.token");
		return serviveAuthToken;
	}
	// Helper method to make a GraphQL request and extract the token
	private static String makeGraphQLRequest(String baseUrl, String Body, String jsonPath) {
		// Construct the GraphQL body
		JSONObject json = new JSONObject();
		String graphqlBody = json.put("query", Body).toString();

		System.out.println("URL ="+baseUrl);
		System.out.println("graphqlBody: "+graphqlBody);
		// Make the API request
		Response response = RestAssured.given().contentType("application/json")
				.header("Authorization", "Bearer " + BaseClassTest.serviceAuthToken)
				.body(graphqlBody)
				.post(baseUrl);

		response.prettyPrint();
		// Extract the token from the response JSON using the provided jsonPath
		return response.jsonPath().getString(jsonPath);
	}
	// Method to regenerate token
	public static String reGenerateToken(String baseUrl, String vinNo, String appUserId) {
		String graphqlQuery = Mutations.regenerateToken
				.replace("VehicleVin", vinNo)
				.replace("appUserId", appUserId);
		// Provide the correct JSON path for this case
		return makeGraphQLRequest(baseUrl, graphqlQuery, "data.regenerateToken.token");
	}

	// Method to regenerate accessory token
	public static String reGenerateToken(String baseUrl,String vinNo, String appUserId, String deviceId) {
		String graphqlQuery = Mutations.regenerateTokenAccessory
				.replace("VehicleVin", vinNo)
				.replace("appUserId", appUserId)
				.replace("deviceId", deviceId);
		// Provide the correct JSON path for this case
		return makeGraphQLRequest(baseUrl, graphqlQuery, "data.regenerateToken.token");
	}
}
