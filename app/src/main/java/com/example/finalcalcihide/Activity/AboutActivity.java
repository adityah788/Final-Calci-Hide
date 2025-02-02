package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalcalcihide.R;

public class AboutActivity extends AppCompatActivity {

    ImageView backarrow;
    TextView creditbutton,privayc_policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        backarrow= findViewById(R.id.about_main_toolbar_back_arrow);
        creditbutton = findViewById(R.id.settincredit);
        privayc_policy = findViewById(R.id.about_privacy_policy);


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));



        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        creditbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this,CreditActivity.class));
            }
        });

        privayc_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the URL you want to open
                String url = "https://sites.google.com/view/calculator-lock-hide-videos/home";

                // Create an Intent to open the URL in a web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                // Start the activity to open the link
                startActivity(browserIntent);
            }
        });



    }
}