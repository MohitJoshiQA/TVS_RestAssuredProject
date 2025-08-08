package Utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

    public class FileUtils {

        public static String readJsonFileAsString(String relativePath) throws IOException {
            return new String(Files.readAllBytes(Paths.get("src/test/resources/" + relativePath)));
        }
    }

