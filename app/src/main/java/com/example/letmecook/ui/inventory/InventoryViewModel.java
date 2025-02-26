package com.example.letmecook.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<String> inventoryText;
    private final MutableLiveData<String> mText;
    private final String userId;
    private final SearchDB searchDB;

    private static final String TAG = "InventoryViewModel";

    public InventoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is inventory fragment");

        inventoryText = new MutableLiveData<>();
        inventoryText.setValue("Loading inventory...");

        userId = FirebaseAuth.getInstance().getUid();
        searchDB = new SearchDB();

        loadInventory();
    }

    public LiveData<String> getInventoryText() {
        return inventoryText;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void loadInventory() {
        if (userId == null) {
            Log.e(TAG, "User ID is null. User might not be logged in.");
            inventoryText.setValue("User not logged in.");
            return;
        }

        searchDB.getUserDocumentByID(userId, userDocument -> {
            if (userDocument != null) {
                String householdID = userDocument.getString("householdID");
                if (householdID != null) {
                    searchDB.getHouseholdDocumentByID(householdID, householdSnapshot -> {
                        if (householdSnapshot != null) {
                            if (householdSnapshot.contains("inventory")) {
                                Object inventoryObj = householdSnapshot.get("inventory");
                                if (inventoryObj instanceof Map) {
                                    Map<String, Object> inventoryMap = (Map<String, Object>) inventoryObj;
                                    if (inventoryMap.isEmpty()) {
                                        inventoryText.setValue("Your inventory is empty");
                                    } else {
                                        StringBuilder inventoryTextBuilder = new StringBuilder();
                                        for (Map.Entry<String, Object> entry : inventoryMap.entrySet()) {
                                            String ingredient = entry.getKey();
                                            Object quantity = entry.getValue();

                                            if (quantity instanceof Number) {
                                                inventoryTextBuilder.append(ingredient)
                                                        .append(": ")
                                                        .append(quantity)
                                                        .append("\n");
                                            } else {
                                                inventoryTextBuilder.append(ingredient)
                                                        .append(": ? (Invalid quantity)\n");
                                            }
                                        }
                                        inventoryText.setValue(inventoryTextBuilder.toString());
                                    }
                                } else {
                                    Log.e(TAG, "Inventory is not a map. Type: " + (inventoryObj != null ? inventoryObj.getClass().getSimpleName() : "null"));
                                    inventoryText.setValue("Inventory data is invalid.");
                                }
                            } else {
                                inventoryText.setValue("Your inventory is empty");
                            }
                        } else {
                            inventoryText.setValue("Household not found");
                        }
                    });
                } else {
                    inventoryText.setValue("Household not assigned.");
                }
            } else {
                inventoryText.setValue("User data missing.");
            }
        });
    }
}
