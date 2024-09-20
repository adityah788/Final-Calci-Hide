package com.example.finalcalcihide.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.MainActivity;
import com.example.finalcalcihide.Utils.IntruderUtils;
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

    private int wrongPasswordCount = 0; // Counter for wrong password attempts
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment if using EdgeToEdge
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isInstanceActive = true;

        // Handle back press
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

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, set up the camera
            IntruderUtils.setupCamera(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                IntruderUtils.setupCamera(this);
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to take selfies.", Toast.LENGTH_SHORT).show();
            }
        }
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

        String enteredPassword = binding.dataTv.getText().toString();

        if (enteredPassword.equals("11223344")) {
            Toast.makeText(this, "Password 11223344 is clicked", Toast.LENGTH_SHORT).show();
            wrongPasswordCount = 0; // Reset counter
        } else if (enteredPassword.length() == 4 && !enteredPassword.equals("6666")) {
            Toast.makeText(this, "You MF Fraudster", Toast.LENGTH_SHORT).show();
            wrongPasswordCount++;

            if (wrongPasswordCount > 2) {
                // More than 2 wrong attempts, trigger selfie capture
                IntruderUtils.takeSelfie(this);
                Toast.makeText(this, "Selfie capture triggered due to multiple failed attempts", Toast.LENGTH_SHORT).show();
            }
        } else if (enteredPassword.length() == 4 && enteredPassword.equals("6666")) {
            // Correct password logic
            if (isTaskRoot()) {
                startActivity(new Intent(Calculator.this, MainActivity.class));
                finish();
            } else {
                finish();
            }
            wrongPasswordCount = 0; // Reset counter
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
