package com.example.finalcalcihide.FileUtils;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImgVidFHandle {

    private static final String TAGG = "IMediaCopyUtil";


    public static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                file);
    }

    protected static boolean copyMediaToPrivateStorage(Context context, ArrayList<String> mediaPaths) {
        try {
            File imageRootDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/images");
            File videoRootDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/videos");

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
                    Log.e(TAGG, "Fel not found or not readable: " + mediaPath);
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


    public static String getVideoDuration(File file) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file.getAbsolutePath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillis = Long.parseLong(time);

            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
                    TimeUnit.MILLISECONDS.toSeconds(timeInMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00"; // Default duration if there is an error
        } finally {
            retriever.release();
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

        // Check if the file is a video
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

        if (contentUri != null) {
            return contentUri;
        }

        // Check if the file is a document (e.g., PDF)
        cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                new String[]{MediaStore.Files.FileColumns._ID},
                MediaStore.Files.FileColumns.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                    contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
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



    protected static boolean moveFilesBackToOriginalLocations(Context context, List<String> selectedPaths) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("file_media_paths", Context.MODE_PRIVATE);
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



    protected static boolean moveMediaToNewLocation(Context context, List<String> selectedPaths, @Nullable String sourceMarker) {
        File imageRootDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/recycle");

        // Ensure the target directory exists
        if (!imageRootDir.exists() && !imageRootDir.mkdirs()) {
            Log.e(TAGG, "Failed to create directory: " + imageRootDir.getAbsolutePath());
            return false;
        }

        try {
            // Determine if a marker needs to be added
            String markerSuffix = (sourceMarker != null) ? "_" + sourceMarker : "";

            for (String sourcePath : selectedPaths) {
                File sourceFile = new File(sourcePath);

                if (sourceFile.exists() && sourceFile.canRead()) {
                    // Extract file extension and name without extension
                    String originalFileName = sourceFile.getName();
                    String fileExtension = getFileExtension(originalFileName);
                    String fileNameWithoutExtension = removeFileExtension(originalFileName);

                    // Append the marker before the file extension, if applicable
                    String targetFileName = fileNameWithoutExtension + markerSuffix + "."+fileExtension;
                    File targetFile = new File(imageRootDir, targetFileName);

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

                        // Delete the source file after successfully copying
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

    private static String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName; // Return the original name if no extension is found
    }


    public static boolean copyfilesToPrivateStorage(Context context, ArrayList<String> files) {
        try {
            // Root directory in private storage
            File filesRootDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/files");

            // Ensure the files directory exists
            if (!filesRootDir.exists() && !filesRootDir.mkdirs()) {
                Log.e(TAGG, "Failed to create files root directory");
                return false;
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("file_media_paths", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String file : files) {
                File mediaFile = new File(file);

                if (mediaFile.exists() && mediaFile.canRead()) {
                    File copiedFile = new File(filesRootDir, mediaFile.getName());
                    String copiedMediaPath = copiedFile.getAbsolutePath();

                    try (FileInputStream inputStream = new FileInputStream(mediaFile);
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                         FileOutputStream outputStream = new FileOutputStream(copiedMediaPath);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }

                        editor.putString(copiedMediaPath, file); // Store the mapping of copied path to original path
                        editor.apply();

                        if (copiedFile.exists()) {
                            Log.d(TAGG, "Successfully copied file to: " + copiedMediaPath);

                            // Attempt to delete the original file
                            Uri mediaUri = getMediaContentUri(context, mediaFile);
                            if (mediaUri != null) {
                                int deletedRows = context.getContentResolver().delete(mediaUri, null, null);
                                if (deletedRows > 0) {
                                    Log.d(TAGG, "Deleted original file: " + file);
                                } else {
                                    Log.e(TAGG, "Failed to delete original file: " + file);
                                }
                            } else {
                                Log.e(TAGG, "Content URI not found for: " + file + ". Skipping deletion.");
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
            Log.e(TAGG, "Unexpected error copying files", e);
            return false;
        }
    }

    protected static boolean moveDataToImagesorVideos(Context context, List<String> selectedPaths) {
        File imagesDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/images");
        File videosDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/videos");
        File filesDir = new File(context.getExternalFilesDir(null), ".dont_delete_me_by_hides/files");



        // Ensure the target directories exist
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            Log.e(TAGG, "Failed to create images directory: " + imagesDir.getAbsolutePath());
            return false;
        }
        if (!videosDir.exists() && !videosDir.mkdirs()) {
            Log.e(TAGG, "Failed to create videos directory: " + videosDir.getAbsolutePath());
            return false;
        }
        if (!filesDir.exists() && !filesDir.mkdirs()) {
            Log.e(TAGG, "Failed to create files directory: " + filesDir.getAbsolutePath());
            return false;
        }

        try {
            for (String sourcePath : selectedPaths) {
                File sourceFile = new File(sourcePath);
                if (sourceFile.exists() && sourceFile.canRead()) {
                    // Determine if it's an image or video by file extension
                    File targetDir;
                    String originalFileName = sourceFile.getName();
                    // Check if the file contains the sourceMarker
                    if (originalFileName.contains("FinalFileActivity")) {
                        // Remove the marker and move the file to .dont_delete_me_by_hides/files
                        targetDir = filesDir;
//                        originalFileName = originalFileName.replace("_" + sourceMarker, "");
                    } else {
                        // Determine target directory based on file type
                        String extension = getFileExtension(originalFileName).toLowerCase();

                        if (isImage(extension)) {
                            targetDir = imagesDir;
                        } else if (isVideo(extension)) {
                            targetDir = videosDir;
                        } else {
                            Log.e(TAGG, "Unsupported file type: " + sourcePath);
                            return false;
                        }
                    }

                    // Define the target file in the new location
                    File targetFile = new File(targetDir, sourceFile.getName());

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

    // Helper method to extract file extension
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return ""; // No extension found
        }
    }

    // Helper method to check if a file is an image
    private static boolean isImage(String extension) {
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        return Arrays.asList(imageExtensions).contains(extension);
    }

    // Helper method to check if a file is a video
    private static boolean isVideo(String extension) {
        String[] videoExtensions = {"mp4", "avi", "mov", "mkv", "flv", "wmv", "webm"};
        return Arrays.asList(videoExtensions).contains(extension);
    }


    protected static boolean deleteDataPermanent(Context context, List<String> selectedPaths) {
        try {
            for (String path : selectedPaths) {
                File fileToDelete = new File(path);

                if (fileToDelete.exists()) {
                    // Attempt to delete the file
                    if (fileToDelete.delete()) {
                        Log.d(TAGG, "Fel deleted successfully: " + path);
                    } else {
                        Log.e(TAGG, "Failed to delete file: " + path);
                        return false;
                    }
                } else {
                    Log.e(TAGG, "Fel not found: " + path);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAGG, "Error deleting files permanently", e);
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

    public static boolean moveFilesBackToOriginalLocationsWrapper(Context context, List<String> selectedImagePaths) {
        return moveFilesBackToOriginalLocations(context, selectedImagePaths);
    }




    public static boolean moveImagesBackToRecycleLocationsWrapper(Context context, List<String> selectedImagePaths,@Nullable String sourceMarker) {
        return moveMediaToNewLocation(context, selectedImagePaths,sourceMarker);
    }


    public static boolean restoredatatoImageorVideo(Context context, List<String> selectedImagePaths) {
        return moveDataToImagesorVideos(context, selectedImagePaths);
    }

    public static boolean deteleDataPermanentWrapper(Context context, List<String> selectedImagePaths) {
        return deleteDataPermanent(context, selectedImagePaths);
    }
}
