//package simulation;
//
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSocketFactory;
//import java.io.FileInputStream;
//import java.security.KeyStore;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import static Utils.ExcelCommunicator.getDataFromExcel;
//
//public class PccSimulation {
////    private static final String BROKER_URL = "ssl://dev-p360.tvsmotor.net:8883";
////    private static final String CLIENT_ID = "simulator-u546-pcc";
////    private static final String USERNAME = "tvsmdev";
////    private static final String PASSWORD = "TvsmDevMqttBroker";
////    private static final String TOPIC = "u546-pcc-telem-test";
////    private static final String CERT_FILE = "./src/test/java/simulation/DEVcert1.pem";
////    private static final String KEY_FILE = "./src/test/java/simulation/DEVclientNew.p12";
////    private static final String MESSAGE_FILE = "./src/test/java/simulation/DEV_Automation.xlsx";
//
//    private static String BROKER_URL;
//    private static String CLIENT_ID;
//    private static String USERNAME;
//    private static String PASSWORD;
//    private static String TOPIC;
//    private static String CERT_FILE;
//    private static String KEY_FILE;
//    private static String MESSAGE_FILE;
//
//    public static void initializer(String instance) {
//
//        if (instance.equals("DEV_URL")) {
//            BROKER_URL = "ssl://dev-p360.tvsmotor.net:8883";
//            CLIENT_ID = "simulator-u546-pcc";
//            USERNAME = "tvsmdev";
//            PASSWORD = "TvsmDevMqttBroker";
//            TOPIC = "u546-pcc-telem-test";
//            CERT_FILE = "./src/test/java/simulation/DEVcert1.pem";
//            KEY_FILE = "./src/test/java/simulation/DEVclientNew.p12";
//            MESSAGE_FILE = "./src/test/java/simulation/DEV_Automation.xlsx";
//
//        } else if (instance.equals("UAT_URL")) {
//            BROKER_URL = "ssl://p360uatiot.tvsmotor.com:8883";
//            CLIENT_ID = "simulator-u546-pcc";
//            USERNAME = "tvsmprod";
//            PASSWORD = "TvsmProdMqttBroker";
//            TOPIC = "u546-pcc-telem-test";
//            CERT_FILE = "./src/test/java/simulation/UATclient-cert.pem";
//            KEY_FILE = "./src/test/java/simulation/UATclient.p12";
//            MESSAGE_FILE = "./src/test/java/simulation/UAT_Automation.xlsx";
//
//        } else {
//            BROKER_URL = "ssl://p360iot.tvsmotor.com:8883";
//            CLIENT_ID = "simulator-u546-pcc";
//            USERNAME = "tvsmprod";
//            PASSWORD = "TvsmProdMqttBroker";
//            TOPIC = "u546-pcc-telem-test";
//            CERT_FILE = "./src/test/java/simulation/UATclient-cert.pem";
//            KEY_FILE = "./src/test/java/simulation/UATclient.p12";
//            MESSAGE_FILE = "./src/test/java/simulation/PROD_Automation.xlsx";
//        }
//    }
//
//    public static SSLSocketFactory getSocketFactory(final String certFile, final String keyFile) throws Exception {
//        KeyStore keyStore = KeyStore.getInstance("PKCS12");
//        keyStore.load(new FileInputStream(keyFile), "Tharan@47".toCharArray());
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        kmf.init(keyStore, "Tharan@47".toCharArray());
//        SSLContext context = SSLContext.getInstance("TLS");
//        context.init(kmf.getKeyManagers(), null, null);
//        return context.getSocketFactory();
//    }
//
//    public static String getChecksum(String msg) {
//        int checksum = 0;
//
//        // Find the index of '*'
//        int endIndex = msg.indexOf("*");
//
//        // Calculate the checksum using XOR for characters between index 1 and endIndex
//        for (int i = 1; i < endIndex; i++) {
//            checksum ^= msg.charAt(i);
//        }
//        // Return the checksum as an integer
//        return String.valueOf(checksum);
//    }
//
//    public static void U546_PCC(String instance,String sheetName, int rowIndex, int colIndex) {
//        initializer(instance);
//        try {
//
//            // Initialize MQTT client
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setUserName(USERNAME);
//            options.setPassword(PASSWORD.toCharArray());
//            options.setSocketFactory(getSocketFactory(CERT_FILE, KEY_FILE));
//            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
//            client.connect(options);
//            System.out.println("Connected to MQTT broker");
//
//            // Read messages from the file
//            List<String> messages = new ArrayList<>();
//            messages.add(getDataFromExcel(MESSAGE_FILE, sheetName, rowIndex, colIndex));
//            for (String line : messages) {
//                if (line.trim().isEmpty() || line.startsWith("#") || line.length() < 10) {
//                    continue;
//                }
//                // Split fields and update timestamp
//                String[] fields = line.split(",");
//
//                ZonedDateTime currentutcTime = ZonedDateTime.now(ZoneId.of("UTC"));
//
//                String starttime = String.valueOf(currentutcTime.toEpochSecond());
//                fields[5] = starttime;
//
//                // Calculate checksum and update the appropriate field
//                String checksum = getChecksum(String.join(",", fields));
//                fields[fields.length - 1] = "*" + checksum; // Assuming the checksum is the last field
//
//                // Reconstruct the updated packet
//                String updatedLine = String.join(",", fields);
//
//                // Publish the message
//                MqttMessage message = new MqttMessage(updatedLine.getBytes());
//                message.setQos(1);
//                client.publish(TOPIC, message);
//                System.out.println("Message published successfully: " + updatedLine);
//
//                // Delay between messages
//                TimeUnit.SECONDS.sleep(5);
//            }
//
//            // Disconnect after all messages are sent
//            client.disconnect();
//            System.out.println("MQTT connection closed");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static Utils.ExcelCommunicator.getDataFromExcel;

public class PccSimulation {
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

        // Find the index of '*'
        int endIndex = msg.indexOf("*");

        // Calculate the checksum using XOR for characters between index 1 and endIndex
        for (int i = 1; i < endIndex; i++) {
            checksum ^= msg.charAt(i);
        }
        // Return the checksum as an integer
        return String.valueOf(checksum);
    }

    public static void U546_PCC(String instance, String sheetName, int rowIndex, int colIndex) {
        initializer(instance);
        try {
            // Initialize MQTT client
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setSocketFactory(getSocketFactory(CERT_FILE, KEY_FILE));
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect(options);
            System.out.println("Connected to MQTT broker");

            // Read the cell content (may contain multiple packets separated by #)
            String cellData = getDataFromExcel(MESSAGE_FILE, sheetName, rowIndex, colIndex);

            if (cellData == null || cellData.trim().isEmpty()) {
                System.out.println("No valid data found in Excel cell.");
                return;
            }

            // Split the cell data into multiple packets using '#' as a delimiter
            String[] packets = cellData.split("#");

            for (String packet : packets) {
                packet = packet.trim();
                if (packet.isEmpty() || packet.startsWith("#") || packet.length() < 10) {
                    continue;
                }

                // Split fields and update timestamp
                String[] fields = packet.split(",");
                ZonedDateTime currentutcTime = ZonedDateTime.now(ZoneId.of("UTC"));
                String starttime = String.valueOf(currentutcTime.toEpochSecond());
                fields[5] = starttime; // Assuming timestamp is at index 5 for PCC

                // Calculate checksum
                String checksum = getChecksum(String.join(",", fields));
                fields[fields.length - 1] = "*" + checksum; // Assuming checksum is the last field

                // Reconstruct the updated packet
                String updatedPacket = String.join(",", fields);

                // Publish the message
                MqttMessage message = new MqttMessage(updatedPacket.getBytes());
                message.setQos(1);
                client.publish(TOPIC, message);
                System.out.println("Published MQTT Packet: " + updatedPacket);

                // Delay between messages
                TimeUnit.SECONDS.sleep(5);
            }

            // Disconnect after all messages are sent
            client.disconnect();
            System.out.println("MQTT connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}