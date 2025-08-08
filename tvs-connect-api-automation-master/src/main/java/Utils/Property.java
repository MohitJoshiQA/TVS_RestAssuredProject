package Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Property {

        static String filePath = "./src/main/resources/config.properties"; // Path to the properties file
        static Properties properties = new Properties();
        static FileInputStream fileInput ;
        public static void propertyWriter(String tokenName, String token) throws IOException {
            // Load the properties file
            fileInput = new FileInputStream(filePath);
            properties.load(fileInput);
            fileInput.close();
            // Modify or add new properties
            properties.setProperty(tokenName,token); // Updating existing key
            // Save changes to the properties file
            FileOutputStream fileOutput = new FileOutputStream(filePath);
            properties.store(fileOutput, "added "+tokenName+" to property file");
            fileOutput.close();
        }
        public static String readProperties(String key) throws IOException {
            fileInput = new FileInputStream(filePath);
            properties.load(fileInput);

            fileInput.close();
            String token = properties.getProperty(key);
            return token;
        }
    }




