package com.example.letmecook.ui.shopping;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShoppingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ShoppingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is shopping list fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
