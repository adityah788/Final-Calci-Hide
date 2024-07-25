package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private RecyclerView recyclerView;
    private BucketAdapter bucketAdapter;
    private final String[] projection = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
    private final String[] projection2 = new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
    private List<String> bucketNames = new ArrayList<>();
    private List<String> bitmapList = new ArrayList<>();
    private List<Integer> imageCounts = new ArrayList<>(); // Add a list of image counts
    public static List<String> imagesList = new ArrayList<>();
    public static List<Boolean> selected = new ArrayList<>();
    ImageView backArrow, menuside;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_video_gallery);

        // Get reference to the back arrow ImageView
        backArrow = findViewById(R.id.main_toolbar_back_arrow);
        menuside = findViewById(R.id.main_toobar_menu_icon);
        title = findViewById(R.id.main_toolbar_title);
        menuside.setVisibility(View.GONE);
        title.setText("Hide Images");

        // Set click listener on the back arrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the current activity
            }
        });

        recyclerView = findViewById(R.id.image_video_gallery_recycler_view);

        bucketAdapter = new BucketAdapter(bucketNames, bitmapList, imageCounts, this, this); // Pass imageCounts to adapter
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing_Bucket);

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing));

        recyclerView.setAdapter(bucketAdapter);

        getPicBuckets(); // Load buckets
    }

    @Override
    public void onBucketClick(String bucketName) {
        getPictures(bucketName); // Load images from selected bucket
        Intent intent = new Intent(ImageVideoBucket.this, SelectImagesorVideos.class);
        intent.putExtra("FROM", "Images");
        startActivity(intent);
    }

    public void getPicBuckets() {
        Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"); // Order by date added in descending order
        if (cursor != null && cursor.moveToFirst()) {
            ArrayList<String> bucketNamesTEMP = new ArrayList<>(cursor.getCount());
            ArrayList<String> bitmapListTEMP = new ArrayList<>(cursor.getCount());
            Map<String, Integer> imageCountMap = new HashMap<>(); // Map to store image counts
            HashSet<String> albumSet = new HashSet<>();
            do {
                // Check if the column indices are valid
                int albumIndex = cursor.getColumnIndex(projection[0]);
                int imageIndex = cursor.getColumnIndex(projection[1]);
                if (albumIndex >= 0 && imageIndex >= 0) {
                    String album = cursor.getString(albumIndex);
                    String image = cursor.getString(imageIndex);
                    if (!albumSet.contains(album)) {
                        bucketNamesTEMP.add(album);
                        bitmapListTEMP.add(image);
                        albumSet.add(album);
                        imageCountMap.put(album, 1); // Initialize count
                    } else {
                        // Increment count for the existing album
                        imageCountMap.put(album, imageCountMap.get(album) + 1);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();

            bucketNames.clear();
            bitmapList.clear();
            imageCounts.clear(); // Clear previous data
            bucketNames.addAll(bucketNamesTEMP);
            bitmapList.addAll(bitmapListTEMP);
            for (String album : bucketNamesTEMP) {
                imageCounts.add(imageCountMap.get(album)); // Add counts to the list
            }

            bucketAdapter.notifyDataSetChanged(); // Notify adapter of data changes
        }
    }

    public void getPictures(String bucket) {
        imagesList.clear();
        selected.clear();
        Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection2,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{bucket}, MediaStore.Images.Media.DATE_ADDED + " DESC"); // Order by date added in descending order
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Check if the column indices are valid
                int pathIndex = cursor.getColumnIndex(projection2[1]);
                if (pathIndex >= 0) {
                    String path = cursor.getString(pathIndex);
                    imagesList.add(path);
                    selected.add(false);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
