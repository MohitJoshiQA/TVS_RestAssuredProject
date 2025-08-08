package configuration;

import com.infobip.ApiClient;
import com.infobip.ApiKey;
import com.infobip.BaseUrl;
import com.infobip.api.EmailApi;
import com.infobip.model.EmailReportsResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MailIntegration {
    private static final String BASE_URL = "https://jdvwek.api.infobip.com/email/3/send";
    private static final String API_KEY = "140b29edc57550874a64f63b02c09cc4-f39626d6-4535-4e7f-859d-f7a7bd714b9e";

    private static final String SENDER_EMAIL_ADDRESS = "no-reply-dev@p360.tvsmotor.com";

    static String failedAPIs;
    static SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
    static java.util.Date date = new Date();
    static String Date = formatter.format(date);

    private static final List<String> RECIPIENT_EMAIL_ADDRESSES_Internal = Arrays.asList(
//            "sanjeev@zeliot.in",
//            "shiji@zeliot.in",
//            "rakshithks@zeliot.in",
//           "Meghana.TD@tvsmotor.com"
//             "charitra.kocheri@tvsmotor.com"
    );


    public static void infobip_apiemail_DEV() throws Exception {


        if (!Listeners.failedAPIs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Listeners.failedAPIs.size(); i++) {
                String apiResponses = Listeners.failedAPIs.get(i);
                sb.append((i + 1)).append(". ").append(apiResponses).append("\n");
            }

            failedAPIs = sb.toString();

            String emailSubject = "TVSM NEW DEV APIs Report";

            String link = "https://dev-p360.tvsmotor.net/altair";

            String emailBody = "Hi Team," + "\r\n"
                    + "Please find the execution report attached for TVSM DEV APIs Report generated on " + Date + "." + "\r\n"
                    + "\r\n"

                    + "Total Passed Tests: " + Listeners.passedTestCount + "\r\n"
                    + "Total Failed Tests: " + Listeners.failedTestCount + "\r\n"
                    + "Total Skipped Tests: " + Listeners.skippedTestCount + "\r\n" + "\r\n"
                    + "The failed APIs are as follows:" + "\r\n"

                    + failedAPIs + "\r\n"

                    + "For Above FAILED APIS, Kindly recheck the API In Playground." + "\r\n" + "\r\n"

                    + "Playground Link: " + link + "\r\n" + "\r\n"

                    + "Thanks & Regards" + "\r\n"

                    + "Automatic API Mail Generator";




            ApiClient apiClient = ApiClient.forApiKey(ApiKey.from(API_KEY))
                    .withBaseUrl(BaseUrl.from(BASE_URL)).build();
            EmailApi sendEmailApi = new EmailApi(apiClient);
            // Adjust the file path
            String emailFileName = "./Reports/TestExecutionReport(DEV).pdf"; // Adjust the file path

            File attachmentFile = new File(emailFileName);


            Object emailResponse = sendEmailApi
                    .sendEmail(RECIPIENT_EMAIL_ADDRESSES_Internal)
                    .from(SENDER_EMAIL_ADDRESS)
                    .subject(emailSubject)
                    .text(emailBody)

                    .attachment(Collections.singletonList(attachmentFile))
                    .execute();

            System.out.println("Response body: " + emailResponse);

            // Get delivery reports. It may take a few seconds to show the above-sent message.
            EmailReportsResult reportsResponse = sendEmailApi.getEmailDeliveryReports().execute();
            System.out.println(reportsResponse.getResults());


        } else {
            String emailSubject = "TVSM DEV APIs Report";

            String emailBody = "Hi Team," + "\r\n"
                    + "Please find the execution report attached for TVSM DEV APIs Report." + Date + "." + "\r\n"
                    + "\r\n" + "All the APIS are Passed." + "\r\n" + "\r\n"

                    + "Thanks & Regards" + "\r\n"

                    + "Automatic API Mail Generator";


            ApiClient apiClient = ApiClient.forApiKey(ApiKey.from(API_KEY))
                    .withBaseUrl(BaseUrl.from(BASE_URL)).build();
            EmailApi sendEmailApi = new EmailApi(apiClient);
            // Adjust the file path
            String emailFileName = "./Reports/TestExecutionReport(DEV).pdf"; // Adjust the file path

            File attachmentFile = new File(emailFileName);

            Object emailResponse = sendEmailApi
                    .sendEmail(RECIPIENT_EMAIL_ADDRESSES_Internal)
                    .from(SENDER_EMAIL_ADDRESS)
                    .subject(emailSubject)
                    .text(emailBody)
                    .attachment(Collections.singletonList(attachmentFile))
                    .execute();

            System.out.println("Response body: " + emailResponse);

            // Get delivery reports. It may take a few seconds to show the above-sent message.
            EmailReportsResult reportsResponse = sendEmailApi.getEmailDeliveryReports().execute();
            System.out.println(reportsResponse.getResults());

        }

    }
}
