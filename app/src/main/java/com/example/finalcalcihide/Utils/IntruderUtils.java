package com.example.finalcalcihide.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

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

public class IntruderUtils {

    private static final String TAG = "IntruderUtils";
    private static ImageCapture imageCapture;
    private static ProcessCameraProvider cameraProvider;

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
}
