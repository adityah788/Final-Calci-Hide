package com.example.finalcalcihide.Activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class Setting extends AppCompatActivity {

    TextView txtV_change_pass, txtV_security_ques, share_with_frnd, rate_the_app,about;
    ImageView backarrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        txtV_change_pass = findViewById(R.id.settingChangePass);
        txtV_security_ques = findViewById(R.id.settinggChangeSecureQuestion);
        share_with_frnd = findViewById(R.id.settingShare);
        rate_the_app = findViewById(R.id.settingRate);
        backarrow = findViewById(R.id.setting_main_toolbar_back_arrow);
        about = findViewById(R.id.settingabout);

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
                showRatingBottomSheet();
            }
        });

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Setting.this,AboutActivity.class));
            }
        });

    }

    private void showRatingBottomSheet() {
        // Create a BottomSheetDialog instance
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.rating_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        AppCompatRatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button submitButton = dialogView.findViewById(R.id.submitRating);
        TextView closeButton = dialogView.findViewById(R.id.rating_dialog_close);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss(); // Close the dialog when close icon is clicked
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating(); // Get the rating selected by the user
                if (rating > 0) { // Check if a rating has been selected
                    bottomSheetDialog.dismiss(); // Close the dialog immediately

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

        // Show the BottomSheetDialog
        bottomSheetDialog.show();
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
