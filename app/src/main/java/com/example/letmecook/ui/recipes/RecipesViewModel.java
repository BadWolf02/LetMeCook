package com.example.letmecook.ui.recipes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecipesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RecipesViewModel() {
        mText = new MutableLiveData<>();
        // mText.setValue("This is recipes fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}