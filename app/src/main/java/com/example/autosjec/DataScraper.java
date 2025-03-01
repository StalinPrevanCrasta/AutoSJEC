package com.example.autosjec;

import android.content.Context;
import android.os.Environment;
import android.webkit.WebView;
import android.widget.Toast;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class DataScraper {
    private Context context;
    private WebView webView;
    private JSONArray marksArray;
    private JSONArray attendanceArray;
    private JSONArray subjectsArray;

    public DataScraper(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void scrapeAndSave() {
        // Extract the main table first
        webView.evaluateJavascript(
                "(function() {" +
                        "   var scriptContent = document.querySelector('body > div > div > div > div.uk-grid.uk-grid-stack > div:nth-child(1) > div > script').innerText;" +
                        "   var columnsMatch = scriptContent.match(/columns: \\[(.*?)\\,\\s]/);" +
                        "   return columnsMatch ? columnsMatch[1] : null;" +
                        "})()", value -> {
                    if (value != null && !value.equals("null")) {
                        try {
                            marksArray = new JSONArray("[" + value.trim() + "]");
                            checkAndCreatePDF();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Failed to parse marks data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No marks data found", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        webView.evaluateJavascript(
                "(function() {" +
                        "   var scriptContent = document.evaluate(" +
                        "    \"/html/body/div/div/div/div[4]/div[2]/div/script\"," +
                        "    document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                        "   var columnsMatch = scriptContent.innerText.match(/columns: \\[(.*?)\\,\\s+\\]/);" +
                        "   return columnsMatch ? columnsMatch[1] : null;" +
                        "})()", value -> {
                    if (value != null && !value.equals("null")) {
                        try {
                            attendanceArray = new JSONArray("[" + value.trim() + "]");
                            checkAndCreatePDF();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Failed to parse attendance data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Attendance not found", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkAndCreatePDF() {
        if (marksArray != null && attendanceArray != null) {
            scrapeSubjects();
        }
    }

    private void scrapeSubjects() {
        // 1️⃣ Click the navigation button
        webView.evaluateJavascript(
                "(function() {" +
                        "   var button = document.evaluate(" +
                        "       \"/html/body/div/div/div/div[5]/div/div/div/table/tbody/tr[1]/td[5]/a/button\"," +
                        "       document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                        "   if (button) button.click();" +
                        "   return button ? 'Clicked' : 'Not Found';" +
                        "})()", value -> {
                    if ("\"Clicked\"".equals(value)) {
                        Toast.makeText(context, "Navigating to subjects...", Toast.LENGTH_SHORT).show();
                        extractSubjects();
                    } else {
                        Toast.makeText(context, "Button not found!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void extractSubjects() {
        // 2️⃣ Extract subject list
        webView.evaluateJavascript(
                "(function() {" +
                        "   var subjects = [];" +
                        "   var list = document.evaluate(" +
                        "       \"/html/body/div/div/div/div[4]/ul/li\"," +
                        "       document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);" +
                        "   for (var i = 0; i < list.snapshotLength; i++) {" +
                        "       subjects.push({index: i, name: list.snapshotItem(i).innerText.trim()});" +
                        "   }" +
                        "   return JSON.stringify(subjects);" +
                        "})()", value -> {
                    try {
                        subjectsArray = new JSONArray(value);
                        scrapeMarks(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Failed to fetch subjects", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void scrapeMarks(int index) {
        if (index >= subjectsArray.length()) {
            createPDF();
            return;
        }

        try {
            JSONObject subject = subjectsArray.getJSONObject(index);
            int subjectIndex = subject.getInt("index");
            String subjectName = subject.getString("name");

            // 3️⃣ Click each subject to load marks
            webView.evaluateJavascript(
                    "(function() {" +
                            "   var subject = document.evaluate(" +
                            "       \"/html/body/div/div/div/div[4]/ul/li[" + (subjectIndex + 1) + "]\"," +
                            "       document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                            "   if (subject) subject.click();" +
                            "   return subject ? 'Clicked' : 'Not Found';" +
                            "})()", value -> {
                        if ("\"Clicked\"".equals(value)) {
                            extractMarks(subjectIndex, subjectName);
                        } else {
                            scrapeMarks(index + 1);
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            scrapeMarks(index + 1);
        }
    }

    private void extractMarks(int index, String subjectName) {
        // 4️⃣ Extract marks table
        webView.evaluateJavascript(
                "(function() {" +
                        "   var table = document.evaluate(" +
                        "       \"/html/body/div/div/div/div[4]/div[3]/div/div/div/table\"," +
                        "       document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                        "   if (!table) return null;" +
                        "   var headers = [];" +
                        "   table.querySelectorAll('thead th').forEach(th => headers.push(th.innerText.trim()));" +
                        "   var marks = [];" +
                        "   table.querySelectorAll('tbody tr:first-child td').forEach(td => marks.push(td.innerText.trim()));" +
                        "   return JSON.stringify({subject: '" + subjectName + "', headers: headers, marks: marks});" +
                        "})()", value -> {
                    try {
                        if (!"null".equals(value)) {
                            JSONObject marksData = new JSONObject(value);
                            subjectsArray.getJSONObject(index).put("marksData", marksData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    scrapeMarks(index + 1);
                }
        );
    }

    private void createPDF() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "contineoSJEC.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // **Original Table**
            document.add(new Paragraph("CIE Data and Attendance")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold());

            Table table = new Table(3).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Subject Code").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Marks").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Attendance").setBold()));
            document.add(table);

            // **Append Subject-wise Marks**
            for (int i = 0; i < subjectsArray.length(); i++) {
                JSONObject subject = subjectsArray.getJSONObject(i);
                if (subject.has("marksData")) {
                    JSONObject marksData = subject.getJSONObject("marksData");
                    document.add(new Paragraph(marksData.getString("subject")).setBold());
                }
            }

            document.close();
            Toast.makeText(context, "PDF Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
