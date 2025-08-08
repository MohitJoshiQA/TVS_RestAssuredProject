package configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/test/resources/config.properties");
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Missing property in config.properties: " + key);
        }
        return value.trim();
    }

    public static String getExcelFilePath() {
        return get("excel.file.path");
    }

    public static String getExcelSheetName() {
        return get("excel.sheet.name");
    }
}
