package com.demo.finalcalcihide.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.demo.finalcalcihide.MainActivity;
import com.demo.finalcalcihide.R;

import java.util.Arrays;
import java.util.List;

public class SecutityQues extends AppCompatActivity {

    private Spinner securityQuestionSpinner;
    private EditText securityAnswer;
    private Button submitAnswer;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secutity_ques);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));



        // Initialize views
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        securityAnswer = findViewById(R.id.security_answer);
        submitAnswer = findViewById(R.id.submit_answer);

        sharedPreferences = getSharedPreferences("CalculatorPrefss", MODE_PRIVATE);

        // Define the security questions, including a placeholder
        List<String> securityQuestions = Arrays.asList(
                "Select a question", // Placeholder hint
                "What is your favorite color?",
                "What was the name of your first pet?",
                "What is your mother's maiden name?",
                "What is the name of the city you were born in?",
                "What is your favorite food?",
                "What was your high school mascot?",
                "What is the name of your favorite book?",
                "What is your dream job?",
                "What is your favorite movie?",
                "What is your favorite sport?"
        );

        // Adapter to display the security questions in the spinner
        ArrayAdapter<String> madapter =  new ArrayAdapter<>(this, R.layout.spiner_tv, securityQuestions);
        madapter.setDropDownViewResource(R.layout.spiner_tv);

        // Set the adapter to the Spinner
        securityQuestionSpinner.setAdapter(madapter);

        // Set up the submit button listener
        submitAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedQuestion = securityQuestionSpinner.getSelectedItem().toString();
                String answer = securityAnswer.getText().toString().trim();

                if (selectedQuestion.equals("Select a question")) {
                    Toast.makeText(SecutityQues.this, "Please select a security question", Toast.LENGTH_SHORT).show();
                } else if (answer.isEmpty()) {
                    Toast.makeText(SecutityQues.this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the selected question and answer in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("security_question", selectedQuestion);
                    editor.putString("security_answer", answer);
                    editor.apply(); // or editor.commit(); if you need synchronous saving

                    // Show a confirmation message
                    Log.d("Security Question" , "Security question and answer saved!");

                    startActivity(new Intent(SecutityQues.this, MainActivity.class));
                    finish();
                }
            }
        });
    }
}
