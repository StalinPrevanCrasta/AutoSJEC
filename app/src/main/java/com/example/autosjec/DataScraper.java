package com.example.autosjec;

import android.content.Context;
import android.os.Environment;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataScraper {
    private final Context context;
    private final WebView webView;

    public DataScraper(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void scrapeAndSave() {
        webView.evaluateJavascript(
                "(function() {" +
                        "let data = {subjectCodes: [], attendance: [], marks: []};" +
                        
                        // Get subject codes and marks
                        "document.querySelectorAll('.bb-tooltip-name').forEach(row => {" +
                        "    let subjectCode = row.querySelector('.name');" +
                        "    let marks = row.querySelector('.value');" +
                        "    if (subjectCode && marks) {" +
                        "        data.subjectCodes.push(subjectCode.textContent.trim());" +
                        "        data.marks.push(marks.textContent.trim());" +
                        "    }" +
                        "});" +

                        // Get attendance
                        "document.querySelectorAll('.bb-chart-arcs-gauge-value').forEach(text => {" +
                        "    data.attendance.push(text.textContent.trim());" +
                        "});" +

                        "return JSON.stringify(data);" +
                        "})();",
                result -> {
                    try {
                        saveToCSV(result);
                    } catch (Exception e) {
                        Toast.makeText(context, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void saveToCSV(String jsonData) {
        try {
            // Clean the JSON string by removing escaped quotes
            if (jsonData.startsWith("\"") && jsonData.endsWith("\"")) {
                jsonData = jsonData.substring(1, jsonData.length() - 1);
            }
            jsonData = jsonData.replace("\\\"", "\"");
            
            System.out.println("Cleaned JSON data: " + jsonData); // Debug log

            // Create filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = "SJEC_Academic_Data_" + timestamp + ".csv";

            // Create directory and file
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dir = new File(path, "SJEC");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);

            // Parse JSON and write to CSV
            JSONObject data = new JSONObject(jsonData);
            JSONArray subjects = data.getJSONArray("subjectCodes");
            JSONArray attendance = data.getJSONArray("attendance");
            JSONArray marks = data.getJSONArray("marks");

            FileWriter writer = new FileWriter(file);
            writer.append("Subject,Attendance,Marks\n");

            int length = Math.min(subjects.length(), Math.min(attendance.length(), marks.length()));
            for (int i = 0; i < length; i++) {
                writer.append(String.format("%s,%s,%s\n",
                    subjects.getString(i).replace(",", ";"), // Escape commas in CSV
                    attendance.getString(i).replace(",", ";"),
                    marks.getString(i).replace(",", ";")));
            }

            writer.flush();
            writer.close();

            Toast.makeText(context, "Data saved to " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage() + "\nJSON data: " + jsonData;
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            System.out.println(errorMsg);
            e.printStackTrace();
        }
    }
}
