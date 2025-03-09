package com.demo.finalcalcihide.Activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.finalcalcihide.R;

public class Web_Browser extends AppCompatActivity {

    EditText urlInput;
    ImageView clearUrl;
    ProgressBar progressBar;
    WebView webView;

    private long backPressedTime; // Time of the last back press
    private Toast backPressedToast; // Toast message for back press warning

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Initialize views
        urlInput = findViewById(R.id.web_editext);
        clearUrl = findViewById(R.id.cancel_Icon);
        progressBar = findViewById(R.id.web_progess_bar);
        webView = findViewById(R.id.web_view);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Make the EditText ready for typing
        urlInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(urlInput, InputMethodManager.SHOW_IMPLICIT);
        }

        // Set WebView client
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        // Add TextWatcher to handle cancel icon visibility
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearUrl.setVisibility(View.VISIBLE); // Show the cancel icon
                } else {
                    clearUrl.setVisibility(View.GONE); // Hide the cancel icon
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Handle clear button click
        clearUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlInput.setText(""); // Clear the EditText content
            }
        });

        // Handle URL submission through the keyboard's "Go" action
        urlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
                    loadMyUrl(urlInput.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Handle back button press logic using OnBackPressedCallback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack(); // If WebView can go back, go back
                } else {
                    // Double back press to exit
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish(); // Exit the activity
                    } else {
                        // Show the toast to the user
                        backPressedToast = Toast.makeText(Web_Browser.this, "Press back again to exit", Toast.LENGTH_SHORT);
                        backPressedToast.show();
                    }
                    backPressedTime = System.currentTimeMillis(); // Update the time of last back press
                }
            }
        };

        // Register the callback with the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void loadMyUrl(String url) {
        boolean isValidUrl = Patterns.WEB_URL.matcher(url).matches();
        if (isValidUrl) {
            webView.loadUrl(url); // Load valid URL directly
        } else {
            webView.loadUrl("https://www.google.com/search?q=" + url); // Search query on Google
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false; // Let WebView handle the URL loading
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE); // Hide progress bar
        }
    }
}
