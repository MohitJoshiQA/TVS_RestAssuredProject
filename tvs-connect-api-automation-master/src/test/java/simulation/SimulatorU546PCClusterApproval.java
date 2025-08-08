//package simulation;

//public class SimulatorU546PCClusterApproval {
//}

package simulation;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import static Utils.ExcelCommunicator.getDataFromExcel;

public class SimulatorU546PCClusterApproval {
    private static String BROKER_URL;
    private static String CLIENT_ID;
    private static String USERNAME;
    private static String PASSWORD;
    private static String TOPIC;
    private static String CERT_FILE;
    private static String KEY_FILE;
    private static String MESSAGE_FILE;

    public static void initializer(String instance) {
        if (instance.equals("DEV_URL")) {
            BROKER_URL = "ssl://dev-p360.tvsmotor.net:8883";
            CLIENT_ID = "simulator-u546-pcc";
            USERNAME = "tvsmdev";
            PASSWORD = "TvsmDevMqttBroker";
            TOPIC = "u546-pcc-telem-test";
            CERT_FILE = "./src/test/java/simulation/DEVcert1.pem";
            KEY_FILE = "./src/test/java/simulation/DEVclientNew.p12";
            MESSAGE_FILE = "./src/test/java/simulation/DEV_Automation.xlsx";
        } else if (instance.equals("UAT_URL")) {
            BROKER_URL = "ssl://p360uatiot.tvsmotor.com:8883";
            CLIENT_ID = "simulator-u546-pcc";
            USERNAME = "tvsmprod";
            PASSWORD = "TvsmProdMqttBroker";
            TOPIC = "u546-pcc-telem-test";
            CERT_FILE = "./src/test/java/simulation/UATclient-cert.pem";
            KEY_FILE = "./src/test/java/simulation/UATclient.p12";
            MESSAGE_FILE = "./src/test/java/simulation/UAT_Automation.xlsx";
        } else {
            BROKER_URL = "ssl://p360iot.tvsmotor.com:8883";
            CLIENT_ID = "simulator-u546-pcc";
            USERNAME = "tvsmprod";
            PASSWORD = "TvsmProdMqttBroker";
            TOPIC = "u546-pcc-telem-test";
            CERT_FILE = "./src/test/java/simulation/UATclient-cert.pem";
            KEY_FILE = "./src/test/java/simulation/UATclient.p12";
            MESSAGE_FILE = "./src/test/java/simulation/PROD_Automation.xlsx";
        }
    }

    public static SSLSocketFactory getSocketFactory(final String certFile, final String keyFile) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(keyFile), "Tharan@47".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "Tharan@47".toCharArray());
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null);
        return context.getSocketFactory();
    }

    public static String getChecksum(String msg) {
        int checksum = 0;
        int endIndex = msg.indexOf("*");
        if (endIndex == -1) endIndex = msg.length();

        // Skip index 0 (which is '#')
        for (int i = 1; i < endIndex; i++) {
            checksum ^= msg.charAt(i);
        }
        return String.valueOf(checksum);
    }

    public static void U546PCClusterApproval(String instance, String sheetName, int rowIndex, int colIndex, int seqNo) {
        initializer(instance);

        try {
            // MQTT setup
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setSocketFactory(getSocketFactory(CERT_FILE, KEY_FILE));

            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect(options);
            System.out.println("Connected to MQTT broker");

            // Read the cell content (may contain multiple packets separated by #)
            String cellData = getDataFromExcel(MESSAGE_FILE, sheetName, rowIndex, colIndex);
//            cellData = "#RESC,SRV,555434556777899,3047,7,OK,OK,OK,OK,OK,OK,OK*53";
            String[] packets = cellData.split("#");

            for (String packet : packets) {
                packet = packet.trim();
                if (packet.isEmpty() || packet.length() < 10 || !packet.contains("*")) {
                    continue;
                }

                // Ensure it starts with #
                if (!packet.startsWith("#")) {
                    packet = "#" + packet;
                }

                // Remove checksum
                String basePacket = packet.substring(1, packet.indexOf("*")); // remove leading #
                String[] parts = basePacket.split(",");

                // Replace 4th element (index 3) with seqNo
                if (parts.length >= 4) {
                    parts[3] = String.valueOf(seqNo);
                }

                String updatedBasePacket = String.join(",", parts);
                String updatedPacket = "#" + updatedBasePacket + "*" + getChecksum(updatedBasePacket);

                // Publish
                MqttMessage message = new MqttMessage(updatedPacket.getBytes());
                message.setQos(1);
                client.publish(TOPIC, message);
                System.out.println("Published MQTT Packet: " + updatedPacket);

                // Wait before sending next
                TimeUnit.SECONDS.sleep(5);
            }

            client.disconnect();
            System.out.println("MQTT connection closed");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
