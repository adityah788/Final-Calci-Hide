package com.example.finalcalcihide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.Activity.FileShow;
import com.example.finalcalcihide.Activity.FinalFileActivity;
import com.example.finalcalcihide.Activity.ImagesHidden;
import com.example.finalcalcihide.Activity.Intruder;
import com.example.finalcalcihide.Activity.NoteActivityRecyclerView;
import com.example.finalcalcihide.Activity.NotesActivity;
import com.example.finalcalcihide.Activity.RecycleBin;
import com.example.finalcalcihide.Activity.Setting;
import com.example.finalcalcihide.Activity.VideoHidden;
import com.example.finalcalcihide.Utils.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayoutImage, relativeLayoutIntruder, relativeLayoutVideos, relativeLayoutRecycleBin, relativeLayoutFile, relativeLayoutNotes;

    ImageView btnSetting;

    // SharedPreferences constants
    private static final String PREFS_NAME = "IntruderSelfiePrefs";
    private static final String KEY_NEW_SELFIE = "new_selfie_added";
    private static final String KEY_SELFIE_PATH = "selfie_path";


    private ActivityResultLauncher<Intent> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Handle the result if needed
                if (isManageExternalStoragePermissionGranted()) {
                    // Permission granted, proceed with file management
                } else {
                    // Permission not granted, inform the user
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));

        relativeLayoutImage = findViewById(R.id.main_Images);
        relativeLayoutIntruder = findViewById(R.id.r_intruder);
        relativeLayoutVideos = findViewById(R.id.new_main_vidoes);
        relativeLayoutRecycleBin = findViewById(R.id.new_main_recycle_bin);
        relativeLayoutFile = findViewById(R.id.new_main_file);
        btnSetting = findViewById(R.id.new_main_setting);
        relativeLayoutNotes = findViewById(R.id.new_main_note);

        // Check if a new selfie has been added
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNewSelfie = sharedPreferences.getBoolean(KEY_NEW_SELFIE, false);
        String selfiePath = sharedPreferences.getString(KEY_SELFIE_PATH, null);

        if (isNewSelfie && selfiePath != null) {
            showNewImageAlert(selfiePath);

            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }



        relativeLayoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isManageExternalStoragePermissionGranted()){
                    startActivity(new Intent(MainActivity.this, ImagesHidden.class));
                }else {
                    showdeleteiconDialog();
                }


            }
        });

        relativeLayoutVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isManageExternalStoragePermissionGranted()){
                    startActivity(new Intent(MainActivity.this, VideoHidden.class));
                }else {
                    showdeleteiconDialog();
                }

            }
        });


        relativeLayoutIntruder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Intruder.class));
            }
        });


        relativeLayoutRecycleBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecycleBin.class));
            }
        });

        relativeLayoutFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FinalFileActivity.class));

            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Setting.class));
            }
        });


        relativeLayoutNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoteActivityRecyclerView.class));

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        // Check if a new selfie has been added
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNewSelfie = sharedPreferences.getBoolean(KEY_NEW_SELFIE, false);
        String selfiePath = sharedPreferences.getString(KEY_SELFIE_PATH, null);

        if (isNewSelfie && selfiePath != null) {
            showNewImageAlert(selfiePath);

            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }

        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
    }


    // Method to show alert when a new image is added
    private void showNewImageAlert(String imagePath) {
        // Get the latest image file
        File latestImageFile = new File(imagePath);

        // Create an ImageView and set the image
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.fromFile(latestImageFile));
        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(800); // Adjust as needed

        new AlertDialog.Builder(this)
                .setTitle("Intruder Selfie Captured!")
                .setMessage("A new selfie has been captured due to multiple failed password attempts.")
                .setView(imageView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showdeleteiconDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_for_all, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel_alert_dialog);
        TextView title = dialogView.findViewById(R.id.custom_alert_Title);
        TextView body = dialogView.findViewById(R.id.custom_alert_body);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog);


        AlertDialog dialog = builder.create();
        title.setText("Permission Required ");
        body.setText("To function properly, we require All Files Access permission to encrypt your files securely.");
        dialog.show();

        cancelTextView.setOnClickListener(v -> dialog.dismiss());

        confirmTextView.setOnClickListener(v -> {

                requestManageExternalStoragePermission();
                dialog.dismiss();

        });


    }


    private boolean isManageExternalStoragePermissionGranted() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // For devices below API 30, assume permission is granted or implement other logic
            return true; // Adjust as needed for your app's logic
        }
    }

    private void requestManageExternalStoragePermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + getPackageName()));
        requestPermissionLauncher.launch(intent);
    }
}


