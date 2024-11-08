package com.example.finalcalcihide.Utils;

import android.app.Activity;
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import android.content.pm.PackageManager;


public class IntruderUtils {

    private static final String TAG = "IntruderUtils";
    private static ImageCapture imageCapture;
    private static ProcessCameraProvider cameraProvider;


    private static final int FILE_PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };




    // SharedPreferences constants
    private static final String PREFS_NAME = "IntruderSelfiePrefs";
    private static final String KEY_NEW_SELFIE = "new_selfie_added";
    private static final String KEY_SELFIE_PATH = "selfie_path";

    // Initialize the camera
    public static void setupCamera(Context context) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((AppCompatActivity) context, cameraSelector, imageCapture);

                Log.d(TAG, "Camera bound to lifecycle.");
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error setting up camera: " + e.getMessage());
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    // Take a selfie and flag the event
    public static void takeSelfie(Context context) {
        if (imageCapture == null || cameraProvider == null) {
            Log.e(TAG, "Camera is not initialized. Call setupCamera() first.");
            Toast.makeText(context, "Camera not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        File directory = new File(context.getFilesDir(), ".dont_delete_me_by_hides/intruderSelfie");
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();
            if (dirsCreated) {
                Log.d(TAG, "Directory created.");
            } else {
                Log.e(TAG, "Failed to create directory.");
                Toast.makeText(context, "Failed to create directory for selfies.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        File file = new File(directory, System.currentTimeMillis() + "_selfie.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Image successfully captured and saved at: " + file.getAbsolutePath());
                        Toast.makeText(context, "Selfie captured and saved!", Toast.LENGTH_SHORT).show();

                        // Set the flag and store selfie path in SharedPreferences
                        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_NEW_SELFIE, true);
                        editor.putString(KEY_SELFIE_PATH, file.getAbsolutePath());
                        editor.apply();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Failed to capture selfie: " + exception.getMessage());
                        Toast.makeText(context, "Failed to capture selfie.", Toast.LENGTH_SHORT).show();
                    }
                });
    }





    public static void setupFileAccess(Activity activity, ActivityResultLauncher<String[]> requestPermissionsLauncher) {
        // Define the required permissions
        String[] requiredPermissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE // If you also need write access
        };

        // Check if permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, check for manage all files permission
            if (!Environment.isExternalStorageManager()) {
                // Request Manage All Files permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivity(intent);
            } else {
                // Permissions are granted, proceed with file access
                Log.d(TAG, "File access permissions granted.");
                // Call your method to access files here
                accessFiles(activity);
            }
        } else {
            // For Android versions below 11, check if permissions are granted
            boolean allPermissionsGranted = true;
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                // Request storage permissions
                requestPermissionsLauncher.launch(requiredPermissions);
            } else {
                // Permissions are granted, proceed with file access
                Log.d(TAG, "File access permissions granted.");
                accessFiles(activity);
            }
        }
    }

    private static void accessFiles(Activity activity) {
        // Your logic for accessing files
        Toast.makeText(activity, "Accessing files...", Toast.LENGTH_SHORT).show();
        // Add your file access code here
    }










    // Access files and handle permissions
    public static void accessFiles(Activity activity, ActivityResultLauncher<String[]> requestPermissionsLauncher, ActivityResultLauncher<Intent> manageAllFilesPermissionLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Request Manage All Files permission for Android 11+
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                manageAllFilesPermissionLauncher.launch(intent);
            } else {
                // Permission already granted, proceed with file access
//                startUnicornFilePicker(activity);
                Toast.makeText(activity, "Per already granted", Toast.LENGTH_LONG).show();

            }
        } else {
            if (!allPermissionsGranted(activity)) {
                // Request storage permissions for Android versions < 11
                requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
            } else {
                // Permission already granted, proceed with file access
//                startUnicornFilePicker(activity);
                Toast.makeText(activity, "Per granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Check if all necessary permissions are granted
    private static boolean allPermissionsGranted(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
