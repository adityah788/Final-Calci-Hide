package com.demo.finalcalcihide.Utils;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    // Example of an internal utility method
    private static ArrayList<String> loadFilePaths(Context context, String directoryName) {
        ArrayList<String> arrayList = new ArrayList<>();
        File storageDir = new File(context.getExternalFilesDir(null), directoryName);

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

        // Sort the file paths based on last modified date (newest first)
        arrayList.sort((path1, path2) -> {
            File file1 = new File(path1);
            File file2 = new File(path2);
            return Long.compare(file2.lastModified(), file1.lastModified());
        });

        return arrayList;
    }


    /**
     * Deletes the files specified by the given list of paths.
     *
     * @param paths The list of file paths to delete.
     */
    @OptIn(markerClass = UnstableApi.class)
    public static void deleteFiles(List<String> paths) {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    Log.e(TAG, "Failed to delete file: " + path);
                }
            } else {
                Log.e(TAG, "Fel does not exist: " + path);
            }
        }
    }

    // Public method that can be used by other parts of the app
    public static ArrayList<String> getImagePaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/images");
    }

    public static ArrayList<String> getVideoPaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/videos");
    }

    public static ArrayList<String> getRecyclePaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/recycle");
    }

    public static ArrayList<String> getIntruderPaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/intruderSelfie");
    }

    public static ArrayList<String> getFilePaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/files");
    }

}
