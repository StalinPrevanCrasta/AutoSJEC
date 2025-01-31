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
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONArray;

public class DataScraper {
    private Context context;
    private WebView webView;
    private JSONArray marksArray;
    private JSONArray attendanceArray;

    public DataScraper(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void scrapeAndSave() {
        webView.evaluateJavascript(
            "(function() {" +
            "   var scriptContent = document.querySelector('body > div > div > div > div.uk-grid.uk-grid-stack > div:nth-child(1) > div > script').innerText;" +
            "   var columnsMatch = scriptContent.match(/columns: \\[(.*?)\\,\\s]/);" +
            "   return columnsMatch ? columnsMatch[1] : null;" +
            "})()", value -> {
                if (value != null && !value.equals("null")) {
                    try {
                        String formattedValue = value.trim();
                        if (formattedValue.startsWith("[") && formattedValue.endsWith(",]")) {
                            formattedValue = formattedValue.substring(0, formattedValue.length() - 1);
                        }
                        marksArray = new JSONArray("[" + formattedValue + "]");
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
            "    document," +
            "    null," +
            "    XPathResult.FIRST_ORDERED_NODE_TYPE," +
            "    null" +
            ").singleNodeValue;" +
            "   var columnsMatch = scriptContent.innerText.match(/columns: \\[(.*?)\\,\\s+\\]/);" +
            "   return columnsMatch ? columnsMatch[1] : null;" +
            "})()", value -> {
                if (value != null && !value.equals("null")) {
                    try {
                        String formattedValue = value.trim();
                        if (formattedValue.startsWith("[") && formattedValue.endsWith(",]")) {
                            formattedValue = formattedValue.substring(0, formattedValue.length() - 1);
                        }
                        attendanceArray = new JSONArray("[" + formattedValue + "]");
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
            createPDF();
        }
    }

    private void createPDF() {
        try {
            File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SJEC");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File file = new File(pdfDir, "data.pdf");

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add combined table
            document.add(new Paragraph("CIE Data and Attendance")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold());

            Table table = new Table(3).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Subject Code").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Marks").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Attendance").setBold()));

            try {
                JSONArray marksProperArray = new JSONArray("[" + marksArray.getString(0).trim() + "]");
                JSONArray attendanceProperArray = new JSONArray("[" + attendanceArray.getString(0).trim() + "]");

                for (int i = 0; i < marksProperArray.length(); i++) {
                    JSONArray marksRow = marksProperArray.getJSONArray(i);
                    JSONArray attendanceRow = attendanceProperArray.getJSONArray(i);
                    if (marksRow.length() >= 2 && attendanceRow.length() >= 2) {
                        table.addCell(marksRow.getString(0));
                        table.addCell(String.valueOf(marksRow.getInt(1)));
                        table.addCell(String.valueOf(attendanceRow.getInt(1)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                table.addCell("Invalid Data");
                table.addCell("-");
                table.addCell("-");
            }

            document.add(table);
            document.close();

            Toast.makeText(context, "PDF Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
