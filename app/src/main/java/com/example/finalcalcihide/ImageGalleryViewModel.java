package com.example.finalcalcihide;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ImageGalleryViewModel extends ViewModel {
    private MutableLiveData<ArrayList<String>> imagePathsLiveData = new MutableLiveData<>();

    public LiveData<ArrayList<String>> getImagePaths() {
        return imagePathsLiveData;
    }

    public void loadImagePaths(ArrayList<String> imagePaths) {
        imagePathsLiveData.setValue(imagePaths);
    }
}
