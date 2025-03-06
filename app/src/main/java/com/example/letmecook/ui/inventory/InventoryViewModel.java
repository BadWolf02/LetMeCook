package com.example.letmecook.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<Map<String, Integer>> inventoryLiveData;
    private final MutableLiveData<String> messageText;
    private final String userId;
    private static final String TAG = "InventoryViewModel";
    private String householdID;
    private final MutableLiveData<String> mText;

    public InventoryViewModel() {
        inventoryLiveData = new MutableLiveData<>(new HashMap<>());
        messageText = new MutableLiveData<>("Loading inventory...");
        userId = FirebaseAuth.getInstance().getUid();
        mText = new MutableLiveData<>();
        mText.setValue("My Inventory");
    }

    public LiveData<Map<String, Integer>> getInventoryLiveData() {
        return inventoryLiveData;
    }

    public LiveData<String> getMessageText() {
        return messageText;
    }

    public String getHouseholdID() {
        return householdID;
    }

    public LiveData<String> getText() {
        return mText;
    }
}