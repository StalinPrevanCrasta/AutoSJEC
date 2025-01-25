package com.example.autosjec;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SuccessActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

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
            Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show();
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
