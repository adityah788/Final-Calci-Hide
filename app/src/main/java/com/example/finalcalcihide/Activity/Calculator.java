package com.example.finalcalcihide.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalcalcihide.MainActivity;
import com.example.finalcalcihide.databinding.ActivityCalculatorBinding;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Calculator extends AppCompatActivity {
    private ActivityCalculatorBinding binding;
    private boolean lastNumeric = false;
    private boolean stateError = false;
    private boolean lastDot = false;
    private boolean equalclicked = false;
    private Expression expression;
    private static boolean isInstanceActive = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isInstanceActive = true;


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Calculator.this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit the app?")
                        .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }


    public void onAllClearClick(View view) {
        binding.dataTv.setText("");
        binding.resultTv.setText("");
        stateError = false;
        lastNumeric = false;
        lastDot = false;
        binding.resultTv.setVisibility(View.GONE);
    }

    public void onDigitClick(View view) {
        if (stateError) {
            binding.dataTv.setText(((Button) view).getText());
            stateError = false;
        } else if (equalclicked) {
            onAllClearClick(view);
            binding.dataTv.append(((Button) view).getText());
            equalclicked = false;
        } else {
            binding.dataTv.append(((Button) view).getText());
        }
        lastNumeric = true;
        onEqual();
    }


    public void onEqualClick(View view) {
        onEqual();

        if (binding.dataTv.getText().toString().equals("11223344")) {
            Toast.makeText(this, "Password 11223344 is clicked", Toast.LENGTH_SHORT).show();
        } else if (binding.dataTv.getText().length() == 4 & !(binding.dataTv.getText().toString().equals("6666"))) {
            Toast.makeText(this, "You MF Fraudster", Toast.LENGTH_SHORT).show();
        } else if (binding.dataTv.getText().length() == 4 & (binding.dataTv.getText().toString().equals("6666"))) {

          if (isTaskRoot()){
              startActivity(new Intent(Calculator.this, MainActivity.class));
              finish();
          }
          else {
              finish();
          }

        } else if (binding.resultTv.getText().length() > 1) {
            binding.dataTv.setText(binding.resultTv.getText().toString().substring(1));
        }
        equalclicked = true;
        binding.resultTv.setText("");
    }

    public void onOperatorClick(View view) {
        if (lastNumeric && !stateError) {
            binding.dataTv.append(((Button) view).getText());
            lastDot = false;
            lastNumeric = false;
            onEqual();
        }
    }

    public void onClearClick(View view) {
        binding.dataTv.setText("");
        binding.resultTv.setText("");
        lastNumeric = false;

    }

    public void onBackClick(View view) {
        if (binding.dataTv.getText().length() > 0) {
            binding.dataTv.setText(binding.dataTv.getText().toString().substring(0, binding.dataTv.getText().toString().length() - 1));
            try {
                if (binding.dataTv.getText().length() > 0) {
                    char lastChar = binding.dataTv.getText().toString().charAt(binding.dataTv.getText().toString().length() - 1);
                    if (Character.isDigit(lastChar)) {
                        onEqual();
                    }
                } else {
                    binding.resultTv.setText("");
                    binding.resultTv.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                binding.resultTv.setText("");
                binding.resultTv.setVisibility(View.GONE);
                Log.e("last char Error", e.toString());
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = binding.dataTv.getText().toString();
            expression = new ExpressionBuilder(txt).build();
            try {
                double result = expression.evaluate();
                binding.resultTv.setVisibility(View.VISIBLE);
                binding.resultTv.setText("=" + result);
            } catch (ArithmeticException ex) {
                Log.e("Evaluate Error", ex.toString());
                binding.resultTv.setText("Errorrrr");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    public void onPointClick(View view) {
        if (!stateError && !lastDot) {
            binding.dataTv.append(((Button) view).getText());
            lastDot = true;
            lastNumeric = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Set the flag to false when this activity is destroyed
        isInstanceActive = false;
    }

    public static boolean isActivityActive() {
        return isInstanceActive;
    }


}