package configuration;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import api.auth.TokenManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BaseClass {

    public static String baseUri;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("src/test/resources/config.properties"));
            baseUri = properties.getProperty("baseURI");
            if (baseUri == null || baseUri.isEmpty()) {
                throw new RuntimeException("baseUri is missing in config.properties");
            }
            RestAssured.baseURI = baseUri;
            System.out.println("Base URI set to: " + baseUri);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    public static RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + TokenManager.accessToken)
                .header("Token", TokenManager.token)
                .header("Userid", TokenManager.iceUserId)
                .contentType("application/json");
    }
}
