package com.example.finalcalcihide.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ToolbarManager {
    private final LinearLayout customToolbarContainer;
    private final LayoutInflater inflater;
    private ImageVideoHideAdapter adapter;
    Context context;
    private ArrayList<String> imagePaths;


    public ToolbarManager(Context context, LinearLayout customToolbarContainer, ImageVideoHideAdapter adapter , ArrayList<String> imagePaths ) {
        this.customToolbarContainer = customToolbarContainer;
        this.inflater = LayoutInflater.from(context);
        this.adapter = adapter;
        this.context = context;
        this.imagePaths = imagePaths;
    }

    public void setToolbarMenu(boolean isAnySelected) {
        customToolbarContainer.removeAllViews();
        View customToolbar = inflater.inflate(
                isAnySelected ? R.layout.contextual_toolbar : R.layout.main_toolbar,
                customToolbarContainer,
                false
        );
        customToolbarContainer.addView(customToolbar);

        if (isAnySelected) {
            setupContextualToolbar(customToolbar);
        } else {
            setupMainToolbar(customToolbar);
        }
    }

    private void setupContextualToolbar(View customToolbar) {
        ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
        selectDeselectAll.setOnClickListener(v -> toggleSelectDeselectAll());

        ImageView cutIcon = customToolbar.findViewById(R.id.contextual_toolbar_cutt);
        cutIcon.setOnClickListener(v -> {
            adapter.clearSelection();
            setToolbarMenu(false);
        });

        updateItemCountText();
    }

    private void setupMainToolbar(View customToolbar) {
        ImageView menuIcon = customToolbar.findViewById(R.id.main_toobar_menu_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> showPopupMenu(menuIcon));
        }
    }

    public void updateItemCountText() {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            if (itemCountText != null) {
                int selectedCount = adapter.getSelectedItemCount();
                int totalCount = adapter.getItemCount();
                itemCountText.setText(String.format("%d/%d", selectedCount, totalCount));
            }
        }
    }

    private void toggleSelectDeselectAll() {
        boolean selectAll = adapter.getSelectedItemCount() < adapter.getItemCount();
        adapter.selectAll(selectAll);
        updateSelectDeselectAllIcon(selectAll);
        updateItemCountText();
    }

    private void updateSelectDeselectAllIcon(boolean selectAll) {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            if (selectDeselectAll != null) {
                selectDeselectAll.setImageResource(selectAll ? R.drawable.baseline_library_add_check_24 : R.drawable.baseline_check_box_outline_blank_24);
            }
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.toolbar_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.dateadded) {
                Toast.makeText(context, "Date Added clicked", Toast.LENGTH_SHORT).show();     // Sort by date

                // Sort by date
                sortImagePathsByDate(imagePaths);

                // Notify the adapter
                adapter.notifyDataSetChanged();

                return true;
            } else if (item.getItemId() == R.id.namee) {
                Toast.makeText(context, "Name clicked", Toast.LENGTH_SHORT).show();
                sortImagePathsByName(imagePaths); // Sort the imagePaths list
                adapter.notifyDataSetChanged(); // Notify adapter about the change
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }


    private void sortImagePathsByName(ArrayList<String> imagePaths ) {
        Collections.sort(imagePaths, (path1, path2) -> {
            File file1 = new File(path1);
            File file2 = new File(path2);
            return file1.getName().compareToIgnoreCase(file2.getName());
        });
    }


    private void sortImagePathsByDate(ArrayList<String> imagePaths) {
        Collections.sort(imagePaths, (path1, path2) -> {
            File file1 = new File(path1);
            File file2 = new File(path2);
            long date1 = file1.lastModified(); // Get last modified time
            long date2 = file2.lastModified();
            return Long.compare(date2, date1); // Sort in descending order
        });
    }
}
