package com.example.autosjec;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        try {
            String universityNumber = getIntent().getStringExtra("UNIVERSITY_NUMBER");
            String dateOfBirth = getIntent().getStringExtra("DATE_OF_BIRTH");

            WebView webView = new WebView(this);
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
            setContentView(webView);
            webView.loadUrl("https://sjecparents.contineo.in/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
