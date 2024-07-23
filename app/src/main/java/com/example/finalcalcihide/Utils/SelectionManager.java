package com.example.finalcalcihide.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SelectionManager {
    private final HashSet<Integer> selectedItems = new HashSet<>();
    private final OnSelectionChangedListener selectionChangedListener;

    public SelectionManager(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifySelectionChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifySelectionChanged();
    }

    public void selectAll(int itemCount) {
        selectedItems.clear();
        for (int i = 0; i < itemCount; i++) {
            selectedItems.add(i);
        }
        notifySelectionChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public boolean isSelected(int position) {
        return selectedItems.contains(position);
    }

    public boolean hasSelection() {
        return !selectedItems.isEmpty();
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedItems);
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(!selectedItems.isEmpty());
        }
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelection);
    }
}
