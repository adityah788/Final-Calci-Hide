package com.example.finalcalcihide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalcalcihide.Activity.ImagesHidden;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayoutImage,relativeLayoutNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayoutImage= findViewById(R.id.main_Images);
        relativeLayoutNote = findViewById(R.id.new_main_file);


        relativeLayoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });



        relativeLayoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, ImagesHidden.class));

            }
        });

    }
}
