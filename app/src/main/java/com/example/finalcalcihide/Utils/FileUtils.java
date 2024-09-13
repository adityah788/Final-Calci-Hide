package com.example.finalcalcihide.Utils;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

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

    // Public method that can be used by other parts of the app
    public static ArrayList<String> getImagePaths(Context context) {
        return loadFilePaths(context, ".dont_delete_me_by_hides/images");
    }




}
