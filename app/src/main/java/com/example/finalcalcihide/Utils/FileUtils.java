package com.example.finalcalcihide.Utils;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;

import androidx.media3.common.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtils {

    // Example of an internal utility method
    private static ArrayList<String> loadFilePaths(Context context, String directoryName) {
        ArrayList<String> arrayList = new ArrayList<>();
        File storageDir = new File(context.getFilesDir(), directoryName);
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        arrayList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        arrayList.sort(Collections.reverseOrder());
        return arrayList;
    }

    /**
     * Deletes the files specified by the given list of paths.
     *
     * @param paths The list of file paths to delete.
     */
    public static void deleteFiles(List<String> paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    Log.e(TAG, "Failed to delete file: " + path);
                }
            } else {
                Log.e(TAG, "File does not exist: " + path);
            }
        }
    }

    // Public method that can be used by other parts of the app
    public static ArrayList<String> getImagePaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/images");
    }

    public static ArrayList<String> getIntruderPaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/intruderSelfie");
    }

}
