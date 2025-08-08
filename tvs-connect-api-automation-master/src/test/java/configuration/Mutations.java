package configuration;

public class Mutations {

    //serviveAuthToken
    static String generateServiceAuthToken = "{\n" +
            "  generateServiceAuthToken(\n" +
            "    username: \"tvsmdemo\"\n" +
            "    password: \"Tracking@123\"\n" +
            "    roleId: \"roleid\"\n" +
            "    \n" +
            "  ) {\n" +
            "    status\n" +
            "    statusMessage\n" +
            "    token\n" +
            "    expiryDateTime\n" +
            "  }\n" +
            "}\n";

    //Rgenerate user token
    static String regenerateToken= "query {\n" +
            "  regenerateToken(\n" +
            "    vin: \"VehicleVin\", \n" +
            "    app_user_id: \"appUserId\", \n" +
            "  ) {\n" +
            "    status\n" +
            "    statusMessage\n" +
            "    vin\n" +
            "    app_user_id\n" +
            "    token\n" +
            "    expiry_time\n" +
            "  }\n" +
            "}\n";


    //Regenerate Accessory token
    static String regenerateTokenAccessory ="query {\n" +
            "  regenerateToken(\n" +
            "    vin: \"VehicleVin\", \n" +
            "    app_user_id: \"appUserId\", \n" +
            "     device_id: \"deviceId\"\n" +
            "  ) {\n" +
            "    status\n" +
            "    statusMessage\n" +
            "    vin\n" +
            "    app_user_id\n" +
            "    token\n" +
            "    expiry_time\n" +
            "  }\n" +
            "}\n";

    static String getAllRoleDetails ="{\n" +
            "  getAllRoleDetails(\n" +
            "    roleName: \"Servicetoken_role\"\n" +
            "  ) {\n" +
            "    status\n" +
            "    statusMessage\n" +
            "    response {\n" +
            "      role_id\n" +
            "      api_id\n" +
            "      role_name\n" +
            "      role_type\n" +
            "      api_type\n" +
            "      api_name\n" +
            "      createdAt\n" +
            "      updatedAt\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
