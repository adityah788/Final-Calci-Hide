package com.example.finalcalcihide.Activity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.R;

public class CreditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Handle back arrow click
        ImageView backArrow = findViewById(R.id.credit_main_toolbar_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Set credits content
        TextView tvCredits = findViewById(R.id.tvCredits);
        String creditsHtml =
                "<p>Icons made by <a href=\"https://www.flaticon.com/authors/tempo-doloe\">Tempo_doloe</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.freepik.com\">Freepik</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/smashicons\">Smashicons</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/vector-stall\">Vector Stall</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Uicons by <a href=\"https://www.flaticon.com/uicons\">Flaticon</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/wahyu-adam\">Wahyu Adam</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/vectorslab\">Vectorslab</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/icon-home\">Icon Home</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/justicon\">Justicon</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/marz-gallery\">Marz Gallery</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/ariefstudio\">Ariefstudio</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/roman-kacerek\">Roman Káčerek</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/ghozi-muhtarom\">Ghozi Muhtarom</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"
                        + "<p>Icons made by <a href=\"https://www.flaticon.com/authors/sakurai\">Sakurai</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"

                        // Flaticon team attribution
                        + "<p>This icon is made by <a href=\"https://www.flaticon.com/authors/freepik\">Freepik</a> from <a href=\"https://www.flaticon.com\">www.flaticon.com</a></p>"

                        // Freepik attribution
                        + "<p>Image by <a href=\"https://www.freepik.com\">Juicy Fish</a> from <a href=\"https://www.freepik.com\">www.freepik.com</a></p>"


        // Add Lottie animation credit here
        + "<p>Animation by Aditya from <a href=\"https://lottiefiles.com\">www.lottiefiles.com</a></p>";

        tvCredits.setText(Html.fromHtml(creditsHtml, Html.FROM_HTML_MODE_LEGACY));
        tvCredits.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
