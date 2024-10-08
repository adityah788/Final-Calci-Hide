package com.example.finalcalcihide.Activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalcalcihide.R;

public class Setting extends AppCompatActivity {

    TextView txtV_change_pass, txtV_security_ques, share_with_frnd, rate_the_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtV_change_pass = findViewById(R.id.tvChangePass);
        txtV_security_ques = findViewById(R.id.tvChangeSecureQuestion);
        share_with_frnd = findViewById(R.id.tvShare);
        rate_the_app = findViewById(R.id.tvRate);

        txtV_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Setting.this, Calculator.class);
                intent.putExtra("RESET_PASSWORD", true); // Pass a boolean value
                startActivity(intent);
            }
        });

        txtV_security_ques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Setting.this, SecutityQues.class));
            }
        });

        share_with_frnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareText = "Check out this amazing app! Download it here: https://play.google.com/store/apps/details?id=" + getPackageName();
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share App via"));
            }
        });

        rate_the_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.rating_dialog, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button submitButton = dialogView.findViewById(R.id.submitRating);
        ImageView closeButton = dialogView.findViewById(R.id.closeDialog);

        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog when close icon is clicked
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating(); // Get the rating selected by the user
                if (rating > 0) { // Check if a rating has been selected
                    dialog.dismiss(); // Close the dialog immediately

                    if (isInternetAvailable()) { // Check for internet availability
                        openPlayStore(); // Open the Play Store if internet is available
                    } else {
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        // No action to open Play Store, just show a message
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
                    // Prompt user to select a rating if none is chosen
                }
            }
        });

        dialog.show();
    }

    // Function to check internet connection
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            Log.d("NetworkStatus", "Is Internet Available: " + isConnected);
            return isConnected;
        } else {
            Log.d("NetworkStatus", "ConnectivityManager is null");
            return false; // Not connected to the internet
        }
    }

    // Function to open Play Store for rating the app
    private void openPlayStore() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goToMarket);
        }
    }
}
