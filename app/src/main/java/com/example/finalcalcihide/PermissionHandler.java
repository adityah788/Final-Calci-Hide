package com.example.finalcalcihide;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {

    private static final int REQUEST_PERMISSIONS = 123;

    // Method to request permissions
    public static void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 and above: Request MANAGE_EXTERNAL_STORAGE permission
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(activity, "Please grant MANAGE_EXTERNAL_STORAGE permission in settings.", Toast.LENGTH_LONG).show();
            } else {
                // If permission is already granted, proceed with other permissions
                checkAndRequestPermissions(activity);
            }
        } else {
            // Android 10 and below: request read/write permissions
            checkAndRequestPermissions(activity);
        }
    }

    // Method to check and request the necessary permissions
    private static void checkAndRequestPermissions(Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check for read and write external storage permissions (for Android 10 and below)
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // Request any necessary permissions
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    // Utility method to check if the necessary permissions are granted
    public static boolean checkPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the MANAGE_EXTERNAL_STORAGE permission is granted for Android 11+
            return Environment.isExternalStorageManager();
        } else {
            // Check for read/write permissions for Android 10 and below
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }
    }
}
