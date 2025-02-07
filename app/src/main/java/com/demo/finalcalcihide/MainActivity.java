package com.demo.finalcalcihide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.demo.finalcalcihide.Activity.FinalFileActivity;
import com.demo.finalcalcihide.Activity.ImagesHidden;
import com.demo.finalcalcihide.Activity.Intruder;
import com.demo.finalcalcihide.Activity.NoteActivityRecyclerView;
import com.demo.finalcalcihide.Activity.RecycleBin;
import com.demo.finalcalcihide.Activity.Setting;
import com.demo.finalcalcihide.Activity.VideoHidden;
import com.demo.finalcalcihide.Activity.Web_Browser;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayoutImage, relativeLayoutIntruder, relativeLayoutVideos, relativeLayoutRecycleBin, relativeLayoutFile, relativeLayoutNotes, relativeLayoutBrowser;

    ImageView btnSetting;
    LinearLayout linearLayoutlongwebBrowser;


    private static final int REQUEST_CODE_STORAGE = 100; // Permission request code for storage
    private static final int REQUEST_CODE_CREATE_FILE = 101; // Request code for creating files via SAF


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
        relativeLayoutBrowser = findViewById(R.id.new_main_web_browser);
        linearLayoutlongwebBrowser = findViewById(R.id.new_main_web_browser_long);

        // Check if a new selfie has been added
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNewSelfie = sharedPreferences.getBoolean(KEY_NEW_SELFIE, false);
        String selfiePath = sharedPreferences.getString(KEY_SELFIE_PATH, null);

        if (isNewSelfie && selfiePath != null) {
//            showNewImageAlert(selfiePath);
            showNewImageDialogCustom(selfiePath);


            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }


        relativeLayoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isManageExternalStoragePermissionGranted()) {
                    startActivity(new Intent(MainActivity.this, ImagesHidden.class));
                } else if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.R) {

                    showdeleteiconDialog("Permission Required ","To function properly, we need storage permission to encrypt your files securely.");

                } else {
                    showdeleteiconDialog("Permission Required ","To function properly, we need All Files Access permission to encrypt your files securely." );
                }
            }
        });

        relativeLayoutVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isManageExternalStoragePermissionGranted()) {
                    startActivity(new Intent(MainActivity.this, VideoHidden.class));
                } else if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.R) {
                    // If permissions are not granted, request them


//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            1);
                    showdeleteiconDialog("Permission Required ","To function properly, we need storage permission to encrypt your files securely.");

                } else {
                    showdeleteiconDialog("Permission Required ","To function properly, we require All Files Access permission to encrypt your files securely." );
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

                if (isManageExternalStoragePermissionGranted()) {
                    startActivity(new Intent(MainActivity.this, FinalFileActivity.class));
                } else if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.R) {

                    showdeleteiconDialog("Permission Required ","To function properly, we need storage permission to encrypt your files securely.");

                } else {
                    showdeleteiconDialog("Permission Required ","To function properly, we need All Files Access permission to encrypt your files securely." );
                }


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

        relativeLayoutBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Web_Browser.class));

            }
        });

        linearLayoutlongwebBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Web_Browser.class));
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
//            showNewImageAlert(selfiePath);
            showNewImageDialogCustom(selfiePath);

            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }

//        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
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




    private void showNewImageDialogCustom(String imagePath) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_for_intruder, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

//        TextView title = dialogView.findViewById(R.id.custom_alert_Title_intruder);
//        TextView body = dialogView.findViewById(R.id.custom_alert_body_intruder);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog_intruder);
        ImageView close = dialogView.findViewById(R.id.custom_alert_intruder_close);
//        ImageView imageView = new ImageView(this); // Create an ImageView
        ImageView intruderrealimage = dialogView.findViewById(R.id.custom_alert_intruder_image);


        // Load image from file
        File latestImageFile = new File(imagePath);
        intruderrealimage.setImageURI(Uri.fromFile(latestImageFile));
//        intruderrealimage.setAdjustViewBounds(true);
//        imageView.setMaxWidth(800); // Adjust size as needed

        // Add ImageView dynamically to the layout
        LinearLayout mainLayout = (LinearLayout) dialogView.findViewById(R.id.custom_alert_body_intruder).getParent();
//        mainLayout.addView(imageView, 1); // Insert below subtitle

        AlertDialog dialog = builder.create();
        dialog.show();

        close.setOnClickListener(v -> dialog.dismiss());

        confirmTextView.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, Intruder.class));
        });
    }




//    private void showNewimageDialogcustom(String Poptitle, String subtitle) {
//            LayoutInflater inflater = getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.custom_alert_for_all, null);
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
//            builder.setView(dialogView);
//
//            TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel_alert_dialog);
//            TextView title = dialogView.findViewById(R.id.custom_alert_Title);
//            TextView body = dialogView.findViewById(R.id.custom_alert_body);
//            TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog);
//
//
//            AlertDialog dialog = builder.create();
//            title.setText(Poptitle);
//            body.setText(subtitle);
//            dialog.show();
//
//            cancelTextView.setOnClickListener(v -> dialog.dismiss());
//
//            confirmTextView.setOnClickListener(v -> {
//
//                requestManageExternalStoragePermission();
//                dialog.dismiss();
//
//            });
//
//
//        }




    private void showdeleteiconDialog(String Poptitle, String subtitle) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_for_all, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel_alert_dialog);
        TextView title = dialogView.findViewById(R.id.custom_alert_Title);
        TextView body = dialogView.findViewById(R.id.custom_alert_body);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog);

        AlertDialog dialog = builder.create();
        title.setText(Poptitle);
        body.setText(subtitle);
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
        } else if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.R) {

            // For below Android 11, check if read and write permissions are granted
            boolean readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            Log.e("Main Acitivity", String.valueOf(readPermission));
            Log.e("Main Acitivity", String.valueOf(writePermission));

            return readPermission && writePermission;

            // For devices below API 30, assume permission is granted or implement other logic
        }
        return false;
    }

    private void requestManageExternalStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + getPackageName()));
            requestPermissionLauncher.launch(intent);
        }

        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE);
            Log.d("MainAcitivity", "Permission wala show hua hoga");
        }


    }
}


