package com.example.finalcalcihide.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.finalcalcihide.MainActivity;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.IntruderUtils;
import com.example.finalcalcihide.databinding.ActivityCalculatorBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

    private SharedPreferences sharedPreferences, sharedPreferencesintru;
    private boolean isPasswordSet;
    private boolean isTakeSelfieEnabled;

    private StringBuilder inputPassword = new StringBuilder();

    private EditText passtxt1, passtxt2, passtxt3, passtxt4;
    private TextView tvPassDetail;

    private Boolean isInitialPassdone = false;
    private Boolean flagInidone = false;
    private String firstPassword;
    boolean resetPassword;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment if using EdgeToEdge
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.calculator_nav_bar_color));

        passtxt1 = findViewById(R.id.pass_1);
        passtxt2 = findViewById(R.id.Pass_2);
        passtxt3 = findViewById(R.id.Pass_3);
        passtxt4 = findViewById(R.id.Pass_4);
        tvPassDetail = findViewById(R.id.calcu_setpass_subtilte);


        isInstanceActive = true;


        // Shared Preferences to store the password
        sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        sharedPreferencesintru = getSharedPreferences("MyIntruder", MODE_PRIVATE);
        isPasswordSet = sharedPreferences.contains("password");
        isTakeSelfieEnabled = sharedPreferencesintru.getBoolean("take_selfie", false);


        // Retrieve the state of isPasswordSet from SharedPreferences
        isPasswordSet = sharedPreferences.getBoolean("isPasswordSet", false);

        // Check for incoming intent to reset the password
        intent = getIntent();
        resetPassword = intent.getBooleanExtra("RESET_PASSWORD", false); // Retrieve the boolean


        if (!isPasswordSet || resetPassword) {
            // No password set, prompt user to create one
            tvPassDetail.setText("Enter a 4 digit password and press =");

            showRatingBottomSheet();

            Log.d("Reset Password", "!isPasswordSet || resetPassword clicked");

            Toast.makeText(this, "!isPasswordSet || resetPassword clicked", Toast.LENGTH_SHORT).show();

            binding.calcuLinearNewPass.setVisibility(View.VISIBLE);
            binding.calculatorRelativeCalculationTxt.setVisibility(View.GONE);

        } else {
            // Password already set, prompt to enter password
//            tvPassDetail.setText("Enter your 4 digit password and press =");

            binding.calcuLinearNewPass.setVisibility(View.GONE);
            binding.calculatorRelativeCalculationTxt.setVisibility(View.VISIBLE);

        }


        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (resetPassword) {
                    // Just go back without showing the alert
                    finish(); // or call super.onBackPressed();
                } else {
                    // Show the alert dialog
                    new AlertDialog.Builder(Calculator.this).setTitle("Exit App").setMessage("Are you sure you want to exit the app?").setPositiveButton("Yes", (dialog, which) -> finishAffinity()).setNegativeButton("No", null).show();
                }
            }
        });

        binding.calcuReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputPassword.setLength(0);
                updatePasswordFields(inputPassword.toString());

            }
        });


    }


    public void onAllClearClick(View view) {
        Log.d("Calculator", "onAllClearClick called. isPasswordSet: " + isPasswordSet);

        if (!isPasswordSet || resetPassword) {
            // Clear all inputPassword when the password is not set
            inputPassword.setLength(0); // Clear the StringBuilder
            Log.d("Calculator", "Input password cleared.");

            // Update the password fields to reflect the cleared password
            updatePasswordFields(inputPassword.toString());

            Toast.makeText(this, "All password inputs cleared. Set a password to use the calculator.", Toast.LENGTH_SHORT).show();
        } else {
            // Clear calculator inputs
            binding.dataDisplay.setText("");
            binding.resultdisplay.setText("");
            stateError = false;
            lastNumeric = false;
            lastDot = false;
            binding.resultdisplay.setVisibility(View.GONE);
            Log.d("Calculator", "Calculator inputs cleared.");
        }
    }


    // Method to handle digit button clicks
    public void onDigitClick(View view) {
        if (!isPasswordSet || resetPassword) {
            // If password is not set, treat the input as password input
            if (inputPassword.length() < 4) {
                // Append digit to inputPassword
                inputPassword.append(((TextView) view).getText().toString());
                updatePasswordFields(inputPassword.toString());
            }
        } else {
            // If password is set, treat as regular calculator operation
            if (stateError) {
                binding.dataDisplay.setText(((Button) view).getText());
                stateError = false;
            } else if (equalclicked) {
                onAllClearClick(view);
                binding.dataDisplay.append(((Button) view).getText());
                equalclicked = false;
            } else {
                binding.dataDisplay.append(((Button) view).getText());
            }
            lastNumeric = true;
            onEqual();
        }
    }


    public void onEqualClick(View view) {

        // Shared Preferences instance (use a single instance)
        SharedPreferences sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Handle if the password is not set or needs to be reset
        if (!isPasswordSet || resetPassword) {

            // Combine the four EditText fields into a single password string
            String enteredPassword = passtxt1.getText().toString() + passtxt2.getText().toString() + passtxt3.getText().toString() + passtxt4.getText().toString();

            if (enteredPassword.length() == 4) {
                // If this is the first time setting the password
                if (!flagInidone) {
                    firstPassword = enteredPassword;
                    flagInidone = true;
                    enteredPassword = "";  // Clear the entered password for the next step
                    inputPassword.setLength(0);
                    updatePasswordFields(inputPassword.toString());
                }

                // After initial password is set, confirm the entered password
                if (isInitialPassdone) {
                    if (enteredPassword.equals(firstPassword)) {
                        // Save the entered password in SharedPreferences
                        editor.putString("password", enteredPassword);
                        editor.putBoolean("isPasswordSet", true); // Mark password as set
                        editor.apply(); // Save changes

                        isPasswordSet = true; // Update the local flag
                        Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show();
                        wrongPasswordCount = 0; // Reset counter

                        // Show original calculator UI
                        binding.calcuLinearNewPass.setVisibility(View.GONE);
                        binding.calculatorRelativeCalculationTxt.setVisibility(View.VISIBLE);

                        startActivity(new Intent(Calculator.this, SecutityQues.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Enter the correct Password", Toast.LENGTH_LONG).show();
                        Log.d("Hamar calcu", "Enter the correct Password is shown");
                    }
                }
                isInitialPassdone = true;
                binding.calcuSetpassTilte.setText("Confirm Your 4 digit Password ");
                Toast.makeText(Calculator.this, "Confirm the password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a valid 4-digit password", Toast.LENGTH_SHORT).show();
            }
        }
        // If the password is already set
        else {

            onEqual(); // Proceed with normal calculation logic

            // Retrieve the saved password from SharedPreferences
            String savedPassword = sharedPreferences.getString("password", "");
            String enteredPassword = binding.dataDisplay.getText().toString();

            // Reset password if the user enters '123123'
            if (enteredPassword.equals("123123123")) {
                // Clear the saved password
                editor.putString("password", ""); // Clear the password
                editor.putBoolean("isPasswordSet", false); // Mark password as not set
                editor.apply(); // Apply the changes

                // Reset the UI to ask the user to set a new password
                isPasswordSet = false;
                resetPassword = true;
                binding.calcuLinearNewPass.setVisibility(View.VISIBLE);
                binding.calculatorRelativeCalculationTxt.setVisibility(View.GONE);

                Toast.makeText(this, "Password has been reset. Set a new password", Toast.LENGTH_SHORT).show();

                // Clear the password fields
                passtxt1.setText("");
                passtxt2.setText("");
                passtxt3.setText("");
                passtxt4.setText("");

                // Clear all previous activities from the stack and restart the activity
                Intent resetIntent = new Intent(this, Calculator.class);
                resetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                startActivity(resetIntent); // Start the Calculator activity again
                finish(); // Close the current activity

                return;  // Exit early, no need to check the password now
            }

            // If password is correct, proceed with normal logic
            if (enteredPassword.equals(savedPassword)) {
                Toast.makeText(this, "Password is correct", Toast.LENGTH_SHORT).show();
                wrongPasswordCount = 0; // Reset counter

                // Proceed with correct password logic
                if (isTaskRoot()) {
                    startActivity(new Intent(Calculator.this, MainActivity.class));
                    finish();
                } else {
                    finish();
                }
            } else if (enteredPassword.length() == 4 && !enteredPassword.equals(savedPassword) && isTakeSelfieEnabled) {
                // Handle incorrect password
                Toast.makeText(this, "You MF Fraudster", Toast.LENGTH_SHORT).show();
                wrongPasswordCount++;

                // Get the value of "selected_number" from SharedPreferences (default to 3 if not set)
                int selectedNumber = sharedPreferences.getInt("selected_number", 3); // Default to 3 if not set

                // Check if wrongPasswordCount exceeds or equals selected_number
                if (wrongPasswordCount >= selectedNumber) {
                    // Trigger selfie capture after the specified number of failed attempts
                    IntruderUtils.setupAndCaptureSelfie(this);
                    Toast.makeText(this, "Selfie capture triggered due to multiple failed attempts", Toast.LENGTH_SHORT).show();
                }

            } else if (binding.resultdisplay.getText().length() > 1) {
                binding.dataDisplay.setText(binding.resultdisplay.getText().toString().substring(1));
            }

            equalclicked = true;
            binding.resultdisplay.setText("");
        }
    }


    public void onOperatorClick(View view) {
        if (!isPasswordSet || resetPassword) {
            Toast.makeText(this, "Set the password first to use the calculator", Toast.LENGTH_SHORT).show();
            return; // Exit the method, disabling the operation
        }

        // Proceed with normal functionality if the password is set
        if (lastNumeric && !stateError) {
            binding.dataDisplay.append(((Button) view).getText());
            lastDot = false;
            lastNumeric = false;
            onEqual();
        } else {
            binding.dataDisplay.setText(binding.dataDisplay.getText().toString().substring(0, binding.dataDisplay.getText().toString().length() - 1));
            binding.dataDisplay.append(((Button) view).getText());
            lastDot = false;
            lastNumeric = false;
            onEqual();
        }
    }


    public void onClearClick(View view) {

        if (!isPasswordSet || resetPassword) {
            // Clear all inputPassword when the password is not set
            inputPassword.setLength(0); // Clear the StringBuilder

            // Update the password fields to reflect the cleared password
            updatePasswordFields(inputPassword.toString());

            Toast.makeText(this, "All password inputs cleared. Set a password to use the calculator.", Toast.LENGTH_SHORT).show();
        } else {

            binding.dataDisplay.setText("");
            binding.resultdisplay.setText("");
            lastNumeric = false;
            lastDot = false;
        }
    }


    public void onBackClick(View view) {
        // If the password is not set, remove the last digit from password fields
        if (!isPasswordSet || resetPassword) {
            if (inputPassword.length() > 0) {
                // Remove the last digit from the inputPassword
                inputPassword.deleteCharAt(inputPassword.length() - 1);
                updatePasswordFields(inputPassword.toString()); // Update the password fields
            }
            Toast.makeText(this, "Backspace is working... if passwrd is not set", Toast.LENGTH_SHORT).show();
        } else {
            if (binding.dataDisplay.getText().length() > 0) {
                binding.dataDisplay.setText(binding.dataDisplay.getText().toString().substring(0, binding.dataDisplay.getText().toString().length() - 1));
                try {
                    if (binding.dataDisplay.getText().length() > 0) {
                        char lastChar = binding.dataDisplay.getText().toString().charAt(binding.dataDisplay.getText().toString().length() - 1);
                        if (Character.isDigit(lastChar)) {
                            onEqual();
                        }
                    } else {
                        binding.resultdisplay.setText("");
                        binding.resultdisplay.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    binding.resultdisplay.setText("");
                    binding.resultdisplay.setVisibility(View.GONE);
                    Log.e("last char Error", e.toString());
                }
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = binding.dataDisplay.getText().toString();
            expression = new ExpressionBuilder(txt).build();
            try {
                double result = expression.evaluate();
                binding.resultdisplay.setVisibility(View.VISIBLE);
                binding.resultdisplay.setText("=" + result);
            } catch (ArithmeticException ex) {
                Log.e("Evaluate Error", ex.toString());
                binding.resultdisplay.setText("Errorrrr");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    public void onPointClick(View view) {

        // If the password is not set, disable the functionality
        if (!isPasswordSet || resetPassword) {
            Toast.makeText(this, "Set the password first to use the calculator", Toast.LENGTH_SHORT).show();
            return; // Exit the method, disabling the operation
        }


        if (!stateError && !lastDot) {
            binding.dataDisplay.append(((Button) view).getText());
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

    @Override
    protected void onResume() {
        super.onResume();
        isInstanceActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInstanceActive = false;
    }


    public static boolean isActivityActive() {
        return isInstanceActive;
    }


    // Update password fields with the entered digits
    private void updatePasswordFields(String password) {
        passtxt1.setText(password.length() > 0 ? String.valueOf(password.charAt(0)) : "");
        passtxt2.setText(password.length() > 1 ? String.valueOf(password.charAt(1)) : "");
        passtxt3.setText(password.length() > 2 ? String.valueOf(password.charAt(2)) : "");
        passtxt4.setText(password.length() > 3 ? String.valueOf(password.charAt(3)) : "");

    }


    private void showRatingBottomSheet() {
        // Create a BottomSheetDialog instance
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_pass_ani, null);
        dialogView.findViewById(R.id.submitRating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(dialogView);

        // Initialize the LottieAnimationView
        LottieAnimationView animationView = dialogView.findViewById(R.id.ani_hide_unhide);
        if (animationView != null) {
            // Set the animation file and play
            animationView.setAnimation(R.raw.lottie_ani_correct); // Load animation from raw resource
            animationView.setRepeatCount(LottieDrawable.INFINITE); // Set repeat count
            animationView.playAnimation(); // Start the animation
            Log.d("LottieDebug", "LottieAnimation is running ......");

        } else {
            Log.e("LottieDebug", "LottieAnimationView is NULL");
        }

        // Show the BottomSheetDialog
        bottomSheetDialog.show();
    }

}
