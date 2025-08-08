package Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ExcelCommunicator {
    /**
     * Gets data from Excel cell with improved error messages
     */
    public static String getDataFromExcel(String filepath, String sheetName, int rowIndex, int colIndex) throws IOException {
        FileInputStream file = null;
        Workbook workbook = null;
        String cellData = "";

        try {
            file = new FileInputStream(filepath);
            workbook = new XSSFWorkbook(file);

            // Get the desired sheet
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in the workbook.");
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                throw new RuntimeException("Row " + rowIndex + " is empty or does not exist in the sheet '" + sheetName + "'.");
            }

            Cell cell = row.getCell(colIndex);
            if (cell == null) {
                throw new RuntimeException("Cell at row " + rowIndex + ", column " + colIndex + " is empty.");
            }

            // Get cell value based on cell type with better error handling
            if (cell.getCellType() == CellType.STRING) {
                cellData = cell.getStringCellValue().trim();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                double numValue = cell.getNumericCellValue();
                // Convert to string, handling integer values properly
                if (numValue == Math.floor(numValue)) {
                    cellData = String.valueOf((long)numValue);
                } else {
                    cellData = String.valueOf(numValue);
                }
            } else if (cell.getCellType() == CellType.BOOLEAN) {
                cellData = String.valueOf(cell.getBooleanCellValue());
            } else if (cell.getCellType() == CellType.BLANK) {
                cellData = "";
            } else {
                throw new RuntimeException("Unsupported cell type at row " + rowIndex + ", column " + colIndex +
                        ": " + cell.getCellType());
            }

        } catch (Exception e) {
            throw new IOException("Error reading Excel cell at row " + rowIndex + ", column " + colIndex +
                    " in sheet '" + sheetName + "': " + e.getMessage(), e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (file != null) {
                file.close();
            }
        }

        return cellData;
    }

    public static int getLastRow(String filepath, String sheetName) throws IOException {
        FileInputStream file = null;
        Workbook workbook = null;
        int rowCount = 0;

        try {
            file = new FileInputStream(filepath);
            workbook = new XSSFWorkbook(file);

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in the workbook.");
            }

            rowCount = sheet.getLastRowNum();
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (file != null) {
                file.close();
            }
        }

        return rowCount;
    }

    /**
     * Get JSON node from Excel with better error handling
     */
    public static JsonNode getJsonFromExcel(String filepath, String sheetName, int rowIndex, int colIndex) throws IOException {
        String jsonString = getDataFromExcel(filepath, sheetName, rowIndex, colIndex);

        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new RuntimeException("JSON data at row " + rowIndex + ", column " + colIndex + " is empty or invalid.");
        }

        try {
            return parseJson(jsonString);
        } catch (Exception e) {
            // Add extra context about the location of the problematic data
            throw new IOException("Invalid JSON format at row " + rowIndex + ", column " + colIndex +
                    " in sheet '" + sheetName + "': " + e.getMessage() +
                    "\nRaw data: " + jsonString, e);
        }
    }

    /**
     * Parse JSON string handling special case for "NA"
     *
     * @param jsonString The JSON string to be parsed
     * @return JsonNode representation of the parsed JSON
     * @throws IOException If parsing fails
     */
    public static JsonNode parseJson(String jsonString) throws IOException {
        // First trim the input to handle whitespaces around the entire string
        if (jsonString != null) {
            jsonString = jsonString.trim();

            // Special case for "NA" value
            if (jsonString.equalsIgnoreCase("NA")) {
                return new TextNode("NA");
            }
        }

        // Regular JSON parsing with improved error messages
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            // Create a more descriptive error that includes a snippet of the problematic JSON
            String snippet = jsonString;
            if (jsonString != null && jsonString.length() > 50) {
                snippet = jsonString.substring(0, 50) + "...";
            }
            throw new IOException("Failed to parse JSON: " + e.getMessage() +
                    "\nJSON snippet: " + snippet, e);
        }
    }

    /**
     * Validates if a string is valid JSON
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        try {
            // Try to parse with both Jackson and org.json for thorough validation
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(jsonString);

            // If we get here, Jackson parsing succeeded
            try {
                new org.json.JSONObject(jsonString);
                // Both parsers accepted it as valid
                return true;
            } catch (JSONException e) {
                // Jackson accepted but org.json rejected - could be an edge case
                // Let's trust Jackson in this case
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Enhanced method to get clean JSON data from Excel
     */
    public static String getCleanJsonFromExcel(String filePath, String sheetName, int rowNum, int colNum)
            throws IOException {
        String rawData = getDataFromExcel(filePath, sheetName, rowNum, colNum);
        return cleanExcelJsonData(rawData);
    }

    /**
     * Cleans JSON data that may have been corrupted by Excel
     */
    private static String cleanExcelJsonData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // Remove Excel's invisible characters and formatting
        data = data.replaceAll("^[\\uFEFF\\uFFFE\\u200B-\\u200D\\u2060\\u0000-\\u001F\\u007F-\\u009F]+", "");

        // Trim all types of whitespace
        data = data.trim();

        // Handle Excel's tendency to add extra quotes or escape characters
        if (data.startsWith("\"") && data.endsWith("\"") && !data.substring(1, data.length()-1).contains("\"")) {
            data = data.substring(1, data.length()-1);
        }

        // Fix double-escaped quotes
        data = data.replace("\\\"", "\"");

        // Remove any non-printable characters except newlines, tabs, and carriage returns
        data = data.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        return data;
    }

    /**
     * Writes data to the specified cell in the Excel sheet and saves to a new output file
     *
     * The new file will be named <original>_output.xlsx
     */
    /**
     * Writes data to the specified cell in the Excel sheet and saves to a new output file.
     * Accumulates changes and writes only once at the end.
     */
    public static void writeAllDataToExcel(String filepath, String sheetName, Map<Integer, Map<Integer, String>> updates)
            throws IOException {
        FileInputStream file = null;
        Workbook workbook = null;

        try {
            file = new FileInputStream(filepath);
            workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in workbook.");
            }

            // apply all changes
            for (Map.Entry<Integer, Map<Integer, String>> rowEntry : updates.entrySet()) {
                int rowIndex = rowEntry.getKey();
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                for (Map.Entry<Integer, String> cellEntry : rowEntry.getValue().entrySet()) {
                    int colIndex = cellEntry.getKey();
                    String data = cellEntry.getValue();

                    // Truncate data if it exceeds Excel's max cell length
                    if (data != null && data.length() > 32000) {
                        data = data.substring(0, 32000) + "...(truncated)";
                    }

                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellValue(data);
                    System.out.println("DEBUG: Writing row " + rowIndex + " col " + colIndex + " data length=" + (data != null ? data.length() : 0));
                }
            }

            // DO NOT close input stream here!
            // file.close();

            // New file name
            String newFilePath = filepath.replace(".xlsx", "_output.xlsx");
            FileOutputStream outFile = new FileOutputStream(newFilePath);
            workbook.write(outFile);
            outFile.close();

            System.out.println("✅ Wrote ALL data to new Excel file: " + newFilePath);

        } catch (Exception e) {
            throw new IOException("Error writing to Excel: " + e.getMessage(), e);
        } finally {
            if (workbook != null) workbook.close();
            if (file != null) file.close();
        }
    }




}