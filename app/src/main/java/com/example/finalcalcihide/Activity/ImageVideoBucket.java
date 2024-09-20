package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.BucketAdapter;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ImageVideoBucket extends AppCompatActivity implements BucketAdapter.OnBucketClickListener {
    private static final String TAG = "ImageVideoBucket";

    private RecyclerView recyclerView;
    private BucketAdapter bucketAdapter;

    // Define separate projection arrays for images and videos
    private final String[] imageProjection = new String[]{
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
    };

    private final String[] videoProjection = new String[]{
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED
    };

    private List<String> bucketNames = new ArrayList<>();
    private List<String> bitmapList = new ArrayList<>();
    private List<Integer> mediaCounts = new ArrayList<>(); // Combined count for images and videos

    public static List<String> mediaList = new ArrayList<>(); // Renamed for clarity
    public static List<Boolean> selected = new ArrayList<>();

    private ImageView backArrow, menuside;
    private TextView title;

    private String mediaType; // "Images" or "Videos"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_video_gallery);

        // Initialize UI components
        backArrow = findViewById(R.id.main_toolbar_back_arrow);
        menuside = findViewById(R.id.main_toobar_menu_icon);
        title = findViewById(R.id.main_toolbar_title);
        menuside.setVisibility(View.GONE);

        // Retrieve the media type from intent extras
        mediaType = getIntent().getStringExtra("FROM");
        if (mediaType == null) {
            mediaType = "Images"; // Default to Images if not specified
            Log.w(TAG, "No 'FROM' extra found in intent. Defaulting to 'Images'.");
        }

        // Set the appropriate title based on media type
        if ("Images".equals(mediaType)) {
            title.setText("Hide Images");
        } else if ("Videos".equals(mediaType)) {
            title.setText("Hide Videos");
        } else {
            title.setText("Media");
            Log.w(TAG, "Unknown 'FROM' extra value: " + mediaType);
        }

        // Set click listener on the back arrow
        backArrow.setOnClickListener(v -> finish());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.image_video_gallery_recycler_view);
        bucketAdapter = new BucketAdapter(bucketNames, bitmapList, mediaCounts, this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing_Bucket);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing));
        recyclerView.setAdapter(bucketAdapter);

        // Load the buckets based on media type
        getMediaBuckets();
    }

    @Override
    public void onBucketClick(String bucketName) {
        Intent intent = new Intent(ImageVideoBucket.this, SelectImagesorVideos.class);
        intent.putExtra("FROM", mediaType); // Pass the media type

        if ("Images".equals(mediaType)) {
            getPictures(bucketName);  // Fetch images
        } else if ("Videos".equals(mediaType)) {
            getVideos(bucketName);    // Fetch videos
        }

        startActivity(intent);
    }

    /**
     * Fetches media buckets (images or videos) based on the media type.
     */
    public void getMediaBuckets() {
        HashMap<String, Integer> bucketCountMap = new HashMap<>();
        HashSet<String> bucketSet = new HashSet<>();
        ArrayList<String> bucketNamesTEMP = new ArrayList<>();
        ArrayList<String> bitmapListTEMP = new ArrayList<>();

        // Determine which projection and URI to use based on media type
        String[] projection;
        String uri;
        if ("Images".equals(mediaType)) {
            projection = imageProjection;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
        } else if ("Videos".equals(mediaType)) {
            projection = videoProjection;
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString();
        } else {
            // Handle mixed media types if needed
            projection = imageProjection; // Default to images
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
            Log.w(TAG, "Unsupported media type: " + mediaType + ". Defaulting to Images.");
        }

        // Fetch buckets based on media type
        Cursor cursor = null;
        try {
            if ("Images".equals(mediaType)) {
                cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imageProjection,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_ADDED + " DESC"
                );
            } else if ("Videos".equals(mediaType)) {
                cursor = getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        videoProjection,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_ADDED + " DESC"
                );
            }

            if (cursor != null && cursor.moveToFirst()) {
                int bucketNameIndex = cursor.getColumnIndex(projection[0]);
                int dataIndex = cursor.getColumnIndex(projection[1]);

                // Check if column indices are valid
                if (bucketNameIndex == -1 || dataIndex == -1) {
                    Log.e(TAG, "Invalid column index. bucketNameIndex: " + bucketNameIndex + ", dataIndex: " + dataIndex);
                    return;
                }

                do {
                    String bucket = cursor.getString(bucketNameIndex);
                    String path = cursor.getString(dataIndex);

                    if (bucket == null || bucket.isEmpty()) {
                        bucket = "Unknown"; // Assign to "Unknown" bucket if bucket name is missing
                    }

                    if (!bucketSet.contains(bucket)) {
                        bucketNamesTEMP.add(bucket);
                        bitmapListTEMP.add(path);
                        bucketSet.add(bucket);
                        bucketCountMap.put(bucket, 1); // Initialize count
                    } else {
                        bucketCountMap.put(bucket, bucketCountMap.get(bucket) + 1);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching media buckets", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Clear existing data and update with fetched data
        bucketNames.clear();
        bitmapList.clear();
        mediaCounts.clear();

        bucketNames.addAll(bucketNamesTEMP);
        bitmapList.addAll(bitmapListTEMP);

        for (String bucket : bucketNamesTEMP) {
            int count = bucketCountMap.getOrDefault(bucket, 0);
            mediaCounts.add(count); // Add counts to the list
        }

        // Notify the adapter of data changes
        bucketAdapter.notifyDataSetChanged();
    }

    /**
     * Fetches images based on the selected bucket.
     *
     * @param bucket The name of the bucket to fetch images from.
     */
    public void getPictures(String bucket) {
        mediaList.clear();
        selected.clear();

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageProjection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?",
                    new String[]{bucket},
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(imageProjection[1]);

                if (dataIndex == -1) {
                    Log.e(TAG, "Invalid data index for images. dataIndex: " + dataIndex);
                    return;
                }

                do {
                    String path = cursor.getString(dataIndex);
                    mediaList.add(path);
                    selected.add(false);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching pictures from bucket: " + bucket, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Fetches videos based on the selected bucket.
     *
     * @param bucket The name of the bucket to fetch videos from.
     */
    public void getVideos(String bucket) {
        mediaList.clear();
        selected.clear();

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoProjection,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " =?",
                    new String[]{bucket},
                    MediaStore.Video.Media.DATE_ADDED + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(videoProjection[1]);

                if (dataIndex == -1) {
                    Log.e(TAG, "Invalid data index for videos. dataIndex: " + dataIndex);
                    return;
                }

                do {
                    String path = cursor.getString(dataIndex);
                    mediaList.add(path);
                    selected.add(false);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching videos from bucket: " + bucket, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
