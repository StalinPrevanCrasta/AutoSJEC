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

import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONArray;

public class DataScraper {
    private Context context;
    private WebView webView;

    public DataScraper(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void scrapeAndSave() {
        webView.evaluateJavascript(
            "(function() {" +
            "   var scriptContent = document.querySelector('body > div > div > div > div.uk-grid.uk-grid-stack > div:nth-child(1) > div > script').innerText;" +
            "   var columnsMatch = scriptContent.match(/columns: \\[(.*?)\\,\\s]/);" +
            "   if (columnsMatch && columnsMatch.length > 1) {" +
            "       return columnsMatch[1];" +
            "   } else {" +
            "       return null;" +
            "   }" +
            "})()", value -> {
                    if (value != null && !value.equals("null")) {
                        try {
                            // Ensure value does not have an extra enclosing bracket
                            String formattedValue = value.trim();
                            if (formattedValue.startsWith("[") && formattedValue.endsWith(",]")) {
                                formattedValue = formattedValue.substring(0, formattedValue.length() - 1);
                            }

                            JSONArray columnsArray = new JSONArray("[" + formattedValue + "]");
                            // Generate PDF
                            createPDF(context, columnsArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Failed to parse data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
                    }
            }
        );
    }

    private void createPDF(Context context, JSONArray columnsArray) {
        try {
            // Define directory path
            File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SJEC");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File file = new File(pdfDir, "marks.pdf");

            // Create PDF writer and document
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add Title
            document.add(new Paragraph("CIE Data")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18));

            // Create Table (2 columns: Subject Code, Marks)
            Table table = new Table(2).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Subject Code")));
            table.addHeaderCell(new Cell().add(new Paragraph("Marks")));

            // Populate table with JSON data
            try {
                // Extract the first (and only) string from columnsArray
                String fullString = columnsArray.getString(0).trim();

                // Debugging: Print the raw extracted string
                System.out.println("Extracted JSON String: " + fullString);

                // Convert the string into a proper JSONArray
                JSONArray properArray = new JSONArray("[" + fullString + "]");

                // Iterate over the newly parsed array
                for (int i = 0; i < properArray.length(); i++) {
                    JSONArray row = properArray.getJSONArray(i); // Each item is now an array

                    if (row.length() >= 2) {
                        table.addCell(row.getString(0));  // Subject Code
                        table.addCell(String.valueOf(row.getInt(1)));  // Marks
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                table.addCell("Invalid Data");
                table.addCell("-");
            }




            // Add table to document
            document.add(table);
            document.close();

            Toast.makeText(context, "PDF Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

}
