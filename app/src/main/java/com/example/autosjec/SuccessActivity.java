package com.example.autosjec;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SuccessActivity extends AppCompatActivity {
    private WebView webView;
    private static final int REQUEST_POST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        // Request POST_NOTIFICATIONS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("SJEC Portal");

        try {
            String universityNumber = getIntent().getStringExtra("UNIVERSITY_NUMBER");
            String dateOfBirth = getIntent().getStringExtra("DATE_OF_BIRTH");

            webView = findViewById(R.id.webview);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        String[] dobParts = dateOfBirth.split("-");
                        String day = dobParts[0];
                        String month = dobParts[1];
                        String year = dobParts[2];

                        webView.evaluateJavascript(
                            "document.getElementById('username').value = '" + universityNumber + "';" +
                            "document.getElementById('dd').value = '" + day + "';" +
                            "document.getElementById('mm').value = '" + month + "';" +
                            "document.getElementById('yyyy').value = '" + year + "';" +
                            "putdate(); " +
                            "document.querySelector('.cn-login-btn').click();", null
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            webView.loadUrl("https://sjecparents.contineo.in/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.resource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        } else
        if (item.getItemId() == R.id.action_download) {
            DataScraper ds = new DataScraper(this, webView);
            ds.scrapeAndSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
