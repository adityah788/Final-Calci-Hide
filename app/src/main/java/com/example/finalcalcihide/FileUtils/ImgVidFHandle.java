package com.example.finalcalcihide.FileUtils;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImgVidFHandle {

    private static final String TAGG = "IMediaCopyUtil";



    public static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                file);
    }

    protected static boolean copyMediaToPrivateStorage(Context context, ArrayList<String> mediaPaths) {
        try {
            File imageRootDir = new File(context.getFilesDir(), ".dont_delete_me_by_hides/images");
            File videoRootDir = new File(context.getFilesDir(), ".dont_delete_me_by_hides/videos");

            // Ensure image directory exists
            if (!imageRootDir.exists()) {
                if (!imageRootDir.mkdirs()) {
                    Log.e(TAGG, "Failed to create image root directory");
                    return false;
                }
            }

            // Ensure video directory exists
            if (!videoRootDir.exists()) {
                if (!videoRootDir.mkdirs()) {
                    Log.e(TAGG, "Failed to create video root directory");
                    return false;
                }
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("media_paths", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String mediaPath : mediaPaths) {
                File mediaFile = new File(mediaPath);

                if (mediaFile.exists() && mediaFile.canRead()) {
                    File rootDir = isVideoFile(mediaFile) ? videoRootDir : imageRootDir;
                    String copiedMediaPath = new File(rootDir, "copied_media_" + System.currentTimeMillis() + getFileExtension(mediaFile)).getAbsolutePath();

                    try (FileInputStream inputStream = new FileInputStream(mediaFile);
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         FileOutputStream outputStream = new FileOutputStream(copiedMediaPath);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }

                        editor.putString(copiedMediaPath, mediaPath); // Store the mapping of copied media path to original path
                        editor.apply();

                        // Verify that the file was copied successfully
                        File copiedFile = new File(copiedMediaPath);
                        if (copiedFile.exists()) {
                            Log.d(TAGG, "Successfully copied file to: " + copiedMediaPath);

                            // Delete the original file from the gallery
                            Uri mediaUri = getMediaContentUri(context, mediaFile);
                            if (mediaUri != null) {
                                int deletedRows = context.getContentResolver().delete(mediaUri, null, null);
                                if (deletedRows > 0) {
                                    Log.d(TAGG, "Deleted original file: " + mediaPath);
                                } else {
                                    Log.e(TAGG, "Failed to delete original file: " + mediaPath);
                                }
                            } else {
                                Log.e(TAGG, "Failed to get content URI for: " + mediaPath);
                            }
                        } else {
                            Log.e(TAGG, "Failed to copy file to: " + copiedMediaPath);
                            return false;
                        }
                    } catch (IOException e) {
                        Log.e(TAGG, "Error copying file: " + mediaPath, e);
                        return false;
                    }
                } else {
                    Log.e(TAGG, "File not found or not readable: " + mediaPath);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAGG, "Unexpected error copying media", e);
            return false;
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf("."));
        } catch (Exception e) {
            return ""; // No extension.
        }
    }

    private static boolean isVideoFile(File file) {
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
        for (String extension : videoExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static Uri getMediaContentUri(Context context, File mediaFile) {
        String filePath = mediaFile.getAbsolutePath();
        Uri contentUri = null;
        Cursor cursor;

        // Check if the file is an image
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                }
            } finally {
                cursor.close();
            }
        }

        if (contentUri != null) {
            return contentUri;
        }

        // If the file is not an image, check if it is a video
        cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                }
            } finally {
                cursor.close();
            }
        }

        return contentUri;
    }

    protected static boolean moveMediaBackToOriginalLocations(Context context, List<String> selectedPaths) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("media_paths", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String copiedPath : selectedPaths) {
                String originalPath = sharedPreferences.getString(copiedPath, null);
                if (originalPath != null) {
                    File copiedFile = new File(copiedPath);

                    if (copiedFile.exists() && copiedFile.canRead()) {
                        File originalFile = new File(originalPath);

                        try (FileInputStream inputStream = new FileInputStream(copiedFile);
                             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                             FileOutputStream outputStream = new FileOutputStream(originalFile);
                             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                            // Move the file contents back to the original location
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                bufferedOutputStream.write(buffer, 0, bytesRead);
                            }

                            // Scan the file to update the media gallery
                            MediaScannerConnection.scanFile(context, new String[]{originalFile.getAbsolutePath()}, null,
                                    (path, uri) -> {
                                        if (uri != null) {
                                            Log.d(TAGG, "Media successfully scanned into gallery: " + path);
                                            // Optionally, update shared preferences or perform additional actions
                                            editor.apply();
                                            Log.d(TAGG, "Successfully moved media back to original location: " + originalPath);

                                            // Delete the copied file after successful move
                                            if (copiedFile.delete()) {
                                                Log.d(TAGG, "Copied file deleted successfully: " + copiedPath);
                                            } else {
                                                Log.e(TAGG, "Failed to delete copied file: " + copiedPath);
                                            }
                                        } else {
                                            Log.e(TAGG, "Failed to scan media into gallery: " + path);
                                        }
                                    });

                        } catch (IOException e) {
                            Log.e(TAGG, "Error moving file back: " + originalPath, e);
                            return false;
                        }
                    } else {
                        Log.e(TAGG, "Copied media file not found or not readable: " + copiedPath);
                        return false;
                    }
                } else {
                    Log.e(TAGG, "Original media path not found for: " + copiedPath);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAGG, "Unexpected error moving media back", e);
            return false;
        }
    }



    protected static boolean moveMediaToNewLocation(Context context, List<String> selectedPaths) {
        File imageRootDir = new File(context.getFilesDir(), ".dont_delete_me_by_hides/recycle");

        // Ensure the target directory exists
        if (!imageRootDir.exists()) {
            if (!imageRootDir.mkdirs()) {
                Log.e(TAGG, "Failed to create directory: " + imageRootDir.getAbsolutePath());
                return false;
            }
        }

        try {
            for (String sourcePath : selectedPaths) {
                File sourceFile = new File(sourcePath);
                if (sourceFile.exists() && sourceFile.canRead()) {
                    // Define the target path in the new location
                    File targetFile = new File(imageRootDir, sourceFile.getName());

                    try (FileInputStream inputStream = new FileInputStream(sourceFile);
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         FileOutputStream outputStream = new FileOutputStream(targetFile);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                        // Copy the file contents to the new location
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }

                        // Delete the source file after copying successfully
                        if (sourceFile.delete()) {
                            Log.d(TAGG, "Source file deleted successfully: " + sourcePath);
                        } else {
                            Log.e(TAGG, "Failed to delete source file: " + sourcePath);
                            return false;
                        }

                    } catch (IOException e) {
                        Log.e(TAGG, "Error copying file: " + sourcePath, e);
                        return false;
                    }
                } else {
                    Log.e(TAGG, "Source media file not found or not readable: " + sourcePath);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAGG, "Unexpected error moving media", e);
            return false;
        }
    }


    public   static boolean copyfilesToPrivateStorage(Context context, ArrayList<String> files) {
        try {
            File filesRootDir = new File(context.getFilesDir(), ".dont_delete_me_by_hides/files");

            // Ensure image directory exists
            if (!filesRootDir.exists()) {
                if (!filesRootDir.mkdirs()) {
                    Log.e(TAGG, "Failed to create image root directory");
                    return false;
                }
            }


            SharedPreferences sharedPreferences = context.getSharedPreferences("file_media_paths", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for ( String file : files) {
//                String mediaPath = mediaModel.getMediaPath();
                File mediaFile = new File(file);

                if (mediaFile.exists() && mediaFile.canRead()) {
                    File rootDir = filesRootDir;
                    String copiedMediaPath = new File(rootDir, "copied_media_" + System.currentTimeMillis() + getFileExtension(mediaFile)).getAbsolutePath();

                    try (FileInputStream inputStream = new FileInputStream(mediaFile);
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         FileOutputStream outputStream = new FileOutputStream(copiedMediaPath);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }

                        editor.putString(copiedMediaPath, file); // Store the mapping of copied media path to original path
                        editor.apply();

                        // Verify that the file was copied successfully
                        File copiedFile = new File(copiedMediaPath);
                        if (copiedFile.exists()) {
                            Log.d(TAGG, "Successfully copied file to: " + copiedMediaPath);

                            // Delete the original file from the gallery
                            Uri mediaUri = getMediaContentUri(context, mediaFile);
                            if (mediaUri != null) {
                                int deletedRows = context.getContentResolver().delete(mediaUri, null, null);
                                if (deletedRows > 0) {
                                    Log.d(TAGG, "Deleted original file: " + file);
                                } else {
                                    Log.e(TAGG, "Failed to delete original file: " + file);
                                }
                            } else {
                                Log.e(TAGG, "Failed to get content URI for: " + file);
                            }
                        } else {
                            Log.e(TAGG, "Failed to copy file to: " + copiedMediaPath);
                            return false;
                        }
                    } catch (IOException e) {
                        Log.e(TAGG, "Error copying file: " + file, e);
                        return false;
                    }
                } else {
                    Log.e(TAGG, "File not found or not readable: " + file);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAGG, "Unexpected error copying media", e);
            return false;
        }
    }



    public static boolean copyImagesToPrivateStorageWrapper(Context context, ArrayList<String> mediaPaths) {
        // Directly call the protected method with the ArrayList<String>
        return copyMediaToPrivateStorage(context, mediaPaths);
    }


    // Public getter and setter for moveImagesBackToOriginalLocations
    public static boolean moveImagesBackToOriginalLocationsWrapper(Context context, List<String> selectedImagePaths) {
        return moveMediaBackToOriginalLocations(context, selectedImagePaths);
    }


    public static boolean moveImagesBackToRecycleLocationsWrapper(Context context, List<String> selectedImagePaths) {
        return moveMediaToNewLocation(context, selectedImagePaths);
    }

}
