package com.example.finalcalcihide.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.PopupMenu;

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.Adapter.IntruderAdap;
import com.example.finalcalcihide.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ToolbarManager {
    private final LinearLayout customToolbarContainer;
    private final LayoutInflater inflater;
    private ImageVideoHideAdapter imageVideoAdapter;
    private IntruderAdap intruderAdapter;
    Context context;
    private ArrayList<String> imagePaths;
    Activity activity;

    // Constructor accepting either ImageVideoHideAdapter or IntruderAdap
    public ToolbarManager(Context context, LinearLayout customToolbarContainer,
                          Object adapter, ArrayList<String> imagePaths, Activity activity) {
        this.customToolbarContainer = customToolbarContainer;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.imagePaths = imagePaths;
        this.activity = activity;

        // Determine the adapter type
        if (adapter instanceof ImageVideoHideAdapter) {
            this.imageVideoAdapter = (ImageVideoHideAdapter) adapter;
        } else if (adapter instanceof IntruderAdap) {
            this.intruderAdapter = (IntruderAdap) adapter;
        }
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
            clearSelection();
            setToolbarMenu(false);
        });

        updateItemCountText();
    }

    private void setupMainToolbar(View customToolbar) {
        ImageView menuIcon = customToolbar.findViewById(R.id.main_toobar_menu_icon);
        ImageView backArrow = customToolbar.findViewById(R.id.main_toolbar_back_arrow);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> showPopupMenu(menuIcon));
        }
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activity.finish(); // Close the activity

            }
        });

    }

    public void updateItemCountText() {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            if (itemCountText != null) {
                int selectedCount = getSelectedItemCount();
                int totalCount = getItemCount();
                itemCountText.setText(String.format("%d/%d", selectedCount, totalCount));
            }
        }
    }

    private void toggleSelectDeselectAll() {
        boolean selectAll = getSelectedItemCount() < getItemCount();
        selectAllItems(selectAll);
        updateSelectDeselectAllIcon(selectAll);
        updateItemCountText();
    }

    private void updateSelectDeselectAllIcon(boolean selectAll) {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            if (selectDeselectAll != null) {
                selectDeselectAll.setImageResource(selectAll
                        ? R.drawable.baseline_library_add_check_24
                        : R.drawable.baseline_check_box_outline_blank_24);
            }
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.toolbar_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.dateadded) {
                Toast.makeText(context, "Date Added clicked", Toast.LENGTH_SHORT).show();

                // Sort by date
                sortImagePathsByDate(imagePaths);

                // Notify the adapter
                notifyAdapterDataChanged();

                return true;
            } else if (item.getItemId() == R.id.namee) {
                Toast.makeText(context, "Name clicked", Toast.LENGTH_SHORT).show();

                // Sort by name
                sortImagePathsByName(imagePaths);
                notifyAdapterDataChanged();

                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void sortImagePathsByName(ArrayList<String> imagePaths) {
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
            long date1 = file1.lastModified();
            long date2 = file2.lastModified();
            return Long.compare(date2, date1);
        });
    }

    // Helper method to clear selection for both adapter types
    private void clearSelection() {
        if (imageVideoAdapter != null) {
            imageVideoAdapter.clearSelection();
        } else if (intruderAdapter != null) {
            intruderAdapter.clearSelection();
        }
    }

    // Helper method to get the selected item count
    private int getSelectedItemCount() {
        if (imageVideoAdapter != null) {
            return imageVideoAdapter.getSelectedItemCount();
        } else if (intruderAdapter != null) {
            return intruderAdapter.getSelectedItemCount();
        }
        return 0;
    }

    // Helper method to get the total item count
    private int getItemCount() {
        if (imageVideoAdapter != null) {
            return imageVideoAdapter.getItemCount();
        } else if (intruderAdapter != null) {
            return intruderAdapter.getItemCount();
        }
        return 0;
    }

    // Helper method to select or deselect all items
    private void selectAllItems(boolean selectAll) {
        if (imageVideoAdapter != null) {
            imageVideoAdapter.selectAll(selectAll);
        } else if (intruderAdapter != null) {
            intruderAdapter.selectAll(selectAll);
        }
    }

    // Helper method to notify adapter data changes
    private void notifyAdapterDataChanged() {
        if (imageVideoAdapter != null) {
            imageVideoAdapter.notifyDataSetChanged();
        } else if (intruderAdapter != null) {
            intruderAdapter.notifyDataSetChanged();
        }
    }
}
