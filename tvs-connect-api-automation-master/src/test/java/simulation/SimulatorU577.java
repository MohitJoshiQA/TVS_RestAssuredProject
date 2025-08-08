package simulation;

import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyStore;
import static Utils.ExcelCommunicator.getDataFromExcel;
import java.util.Arrays;

public class SimulatorU577 {
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
            CLIENT_ID = "simulator-u577";
            USERNAME = "tvsmdev";
            PASSWORD = "TvsmDevMqttBroker";
            TOPIC = "u577-telem-test";
            CERT_FILE = "./src/test/java/simulation/DEVcert1.pem";
            KEY_FILE = "./src/test/java/simulation/DEVclientNew.p12";
            MESSAGE_FILE = "./src/test/java/simulation/DEV_Automation.xlsx";

        } else if (instance.equals("UAT_URL")) {
            BROKER_URL = "ssl://p360uatiot.tvsmotor.com:8883";
            CLIENT_ID = "simulator-u577";
            USERNAME = "tvsmprod";
            PASSWORD = "TvsmProdMqttBroker";
            TOPIC = "u577-telem-test";
            CERT_FILE = "./src/test/java/simulation/UATclient-cert.pem";
            KEY_FILE = "./src/test/java/simulation/UATclient.p12";
            MESSAGE_FILE = "./src/test/java/simulation/UAT_Automation.xlsx";

        } else {
            BROKER_URL = "ssl://p360iot.tvsmotor.com:8883";
            CLIENT_ID = "simulator-u577";
            USERNAME = "tvsmprod";
            PASSWORD = "TvsmProdMqttBroker";
            TOPIC = "u577-telem-test";
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

    public static void U577(String instance, String sheetName, int rowIndex, int colIndex) throws IOException {
        initializer(instance);
        try {
            String clientId = CLIENT_ID + "-" + System.currentTimeMillis();
            MqttClient client = new MqttClient(BROKER_URL, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            options.setMaxInflight(50);
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setSocketFactory(getSocketFactory(CERT_FILE, KEY_FILE));

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                    while (!client.isConnected()) {
                        try {
                            System.out.println("Attempting to reconnect...");
                            Thread.sleep(3000);
                            client.reconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Received: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Message delivered.");
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("Connected to: " + serverURI);
                }
            });

            client.connect(options);
            System.out.println("Connected to MQTT broker");

            String cellData = getDataFromExcel(MESSAGE_FILE, sheetName, rowIndex, colIndex);
            System.out.println("Simulation Packet: " + cellData);
            if (cellData == null || cellData.trim().isEmpty()) {
                System.out.println("No valid data found in Excel cell.");
                return;
            }

            String[] messages = cellData.split("#");
            for (String line : messages) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                byte[] packet = hexStringToByteArray(line);

                // Find "TCU" pattern
                int startIndex = findPattern(packet, "TCU");
                if (startIndex == -1) {
                    System.out.println("Pattern 'TCU' not found.");
                    continue;
                }

                // Move 90 bytes forward to locate timestamp
                int timestampPosition = startIndex + 90;

                // Extract old timestamp
                int oldTimestamp = ByteBuffer.wrap(Arrays.copyOfRange(packet, timestampPosition, timestampPosition + 4))
                        .order(ByteOrder.BIG_ENDIAN).getInt();
                System.out.println("📌 Extracted Timestamp: " + oldTimestamp);

                // Update with current timestamp
                int currentTime = (int) (System.currentTimeMillis() / 1000);
                ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
                buffer.putInt(currentTime);
                System.arraycopy(buffer.array(), 0, packet, timestampPosition, 4);

                System.out.println("✅ Updated Timestamp: " + currentTime + " at position " + timestampPosition);

                // Publish the modified packet
                MqttMessage message = new MqttMessage(packet);
                message.setQos(1);
                client.publish(TOPIC, message);
                System.out.println("Published: " + bytesToHex(packet));

                // Delay before sending next message
                Thread.sleep(5000);
            }

            client.disconnect();
            System.out.println("MQTT connection closed");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] hexStringToByteArray(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    private static int findPattern(byte[] data, String pattern) {
        byte[] patternBytes = pattern.getBytes();
        for (int i = 0; i <= data.length - patternBytes.length; i++) {
            boolean found = true;
            for (int j = 0; j < patternBytes.length; j++) {
                if (data[i + j] != patternBytes[j]) {
                    found = false;
                    break;
                }
            }
            if (found)
                return i;
        }
        return -1;
    }
}
