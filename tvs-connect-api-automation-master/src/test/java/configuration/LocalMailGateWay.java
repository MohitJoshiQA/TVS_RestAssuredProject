package configuration;

import okhttp3.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LocalMailGateWay {
    static String failedAPIs;
    static SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
    static java.util.Date date = new Date();
    static String Date = formatter.format(date);

    static String emailList ="";

    //"sanjev@zeliot.in;"+"meghana@zeliot.in;"+"rakshithks@zeliot.in;"+"pranav@zeliot.in";


    public static void apiemailDEV() throws Exception {

        if (!Listeners.failedAPIs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Listeners.failedAPIs.size(); i++) {
                String apiResponses = Listeners.failedAPIs.get(i);
                sb.append((i + 1)).append(". ").append(apiResponses).append("\n");
            }

            failedAPIs = sb.toString();

            String emailSubject = "TVSM DEV APIs Report";

            String link = "https://dev-p360.tvsmotor.net/altair";

            String emailBody = "Hi Team," + "\r\n"
                    + "Please find the execution report attached for TVSM DEV APIs generated on " + Date + "." + "\r\n"
                    + "\r\n"

                    + "Total Passed Tests: " + Listeners.passedTestCount + "\r\n"
                    + "Total Failed Tests: "+ Listeners.failedTestCount + "\r\n"
                    + "Total Skipped Tests: " +  Listeners.skippedTestCount+"\r\n" + "\r\n"
                    + "The failed APIs are as follows:"+"\r\n"

                    + failedAPIs + "\r\n"

                    + "For Above FAILED APIs, Kindly recheck the API In Playground." + "\r\n" + "\r\n"

                    + "Playground Link: " + link + "\r\n" + "\r\n"

                    + "Thanks & Regards" + "\r\n"

                    + "Automatic API Mail Generator";
            String emailFileName = "DEV API Report" + "(" + Date + ")" + ".html";

            OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES).build();

            @SuppressWarnings("deprecation")
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("toList", emailList).addFormDataPart("subject", emailSubject)
                    .addFormDataPart("text", emailBody)
                    .addFormDataPart("file", emailFileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File("./Reports/TestExecutionReport(DEV).html")))
                    .build();
            Request request = new Request.Builder()
                    .url("https://boschindia-mobilitysolutions.com/emailservice/api/v1/email").method("POST", body)
                    .addHeader("accessKey",
                            "tazw7KwpX1HjyHVfgen68GHY51cQEXTjMiiWst/ccQ4bcgxk+qFnaHtZHoME9fM2LrKCgHUIsuCsol+MEhJY5Q==")
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println(response);

        } else {
            String emailSubject = "TVSM DEV APIs Report";

            String emailBody = "Hi Team," + "\r\n"
                    + "Please find the execution report attached for TVSM DEV APIs generated on " + Date + "." + "\r\n"
                    + "\r\n" + "All the APIs are Passed." + "\r\n" + "\r\n"

                    + "Thanks & Regards" + "\r\n"

                    + "Automatic API Mail Generator";

            String emailFileName = "DEV API Report" + "(" + Date + ")" + ".html";
            OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MINUTES).build();

            @SuppressWarnings("deprecation")
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("toList", emailList).addFormDataPart("subject", emailSubject)
                    .addFormDataPart("text", emailBody)
                    .addFormDataPart("file", emailFileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"), new File(

                                    "./Reports/TestExecutionReport(DEV).html")))
                    .build();
            Request request = new Request.Builder()
                    .url("https://boschindia-mobilitysolutions.com/emailservice/api/v1/email").method("POST", body)
                    .addHeader("accessKey",
                            "tazw7KwpX1HjyHVfgen68GHY51cQEXTjMiiWst/ccQ4bcgxk+qFnaHtZHoME9fM2LrKCgHUIsuCsol+MEhJY5Q==")
                    .build();

            Response response = client.newCall(request).execute();
            System.out.println(response);

        }
    }
}
