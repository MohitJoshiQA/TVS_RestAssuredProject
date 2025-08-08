package ReportGenerator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvReportGenerator {
    public static void csvGenerator(ArrayList<String> TestCaseId, ArrayList<String> Model,
                                    ArrayList<String> APIName, ArrayList<String> MethodName, ArrayList<String> Request,
                                    ArrayList<String> ExpectedHTTPcode, ArrayList<String> ActualHTTPcode,
                                    ArrayList<String> ExpectedResponse, ArrayList<String> Response,
                                    ArrayList<String> Status, ArrayList<String> Throwable, ArrayList<String> skippedApi) throws IOException {

        // Generate a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH-mm").format(new Date());
        String filename = "./Reports/" + timestamp + " Report.xlsx";

        System.out.println("Excel report filename: " + filename);

        File file = new File(filename);
        Workbook workbook = new XSSFWorkbook();

        // Ensure the Reports directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("Failed to create Reports directory.");
                return; // Exit if directory creation fails
            }
        }

        // ** Group data by Model ** //
        Map<String, List<Integer>> modelIndexMap = new LinkedHashMap<>();
        for (int i = 0; i < Model.size(); i++) {
            modelIndexMap.computeIfAbsent(Model.get(i), k -> new ArrayList<>()).add(i);
        }

        // ** Iterate through each unique model and create separate sheets ** //
        for (Map.Entry<String, List<Integer>> entry : modelIndexMap.entrySet()) {
            String modelName = entry.getKey();
            List<Integer> indices = entry.getValue();

            // Create a new sheet for each model
            Sheet sheet = workbook.createSheet(modelName);

            // Create Header Row
            Row rowHeader = sheet.createRow(0);
            String[] headers = {"Testcase ID", "Model", "API Name", "Method Name", "Request",
                    "Expected Status Code", "Actual Status Code", "Expected Response",
                    "Actual Response", "Status", "Failure Reason"};

            for (int i = 0; i < headers.length; i++) {
                rowHeader.createCell(i).setCellValue(headers[i]);
            }

            // Populate Data for the current model
            int rowIndex = 1;
            for (int index : indices) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(safeGet(TestCaseId, index));
                row.createCell(1).setCellValue(safeGet(Model, index));
                row.createCell(2).setCellValue(safeGet(APIName, index));
                row.createCell(3).setCellValue(safeGet(MethodName, index));
                row.createCell(4).setCellValue(safeGet(Request, index));
                row.createCell(5).setCellValue(safeGet(ExpectedHTTPcode, index));
                row.createCell(6).setCellValue(safeGet(ExpectedResponse, index));
                row.createCell(7).setCellValue(safeGet(ActualHTTPcode, index));
                row.createCell(8).setCellValue(safeGet(Response, index));
                row.createCell(9).setCellValue(safeGet(Status, index));
                row.createCell(10).setCellValue(safeGet(Throwable, index));
            }
        }

        // Write to File
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            System.out.println("Excel file written successfully: " + filename);
        } finally {
            workbook.close();
        }
    }

    // Helper method to avoid NullPointerException
    private static String safeGet(ArrayList<String> list, int index) {
        return (list != null && index < list.size() && list.get(index) != null) ? list.get(index) : "";
    }
}
