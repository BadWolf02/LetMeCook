package com.example.letmecook.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<Map<String, Integer>> inventoryLiveData;
    private final MutableLiveData<String> messageText;
    private final String userId;
    private final SearchDB searchDB;
    private static final String TAG = "InventoryViewModel";
    private String householdID;

    public InventoryViewModel() {
        inventoryLiveData = new MutableLiveData<>(new HashMap<>());
        messageText = new MutableLiveData<>("Loading inventory...");
        userId = FirebaseAuth.getInstance().getUid();
        searchDB = new SearchDB();
        loadInventory();
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

    public void loadInventory() {
        if (userId == null) {
            Log.e(TAG, "User ID is null. User might not be logged in.");
            messageText.setValue("User not logged in.");
            return;
        }

        searchDB.getUserDocumentByID(userId, userDocument -> {
            if (userDocument != null) {
                householdID = userDocument.getString("householdID");
                if (householdID != null) {
                    searchDB.getHouseholdDocumentByID(householdID, householdSnapshot -> {
                        if (householdSnapshot != null && householdSnapshot.contains("inventory")) {
                            Object inventoryObj = householdSnapshot.get("inventory");
                            if (inventoryObj instanceof Map) {
                                Map<String, Object> rawInventoryMap = (Map<String, Object>) inventoryObj;

                                Map<String, Integer> inventoryMap = new HashMap<>();
                                for (Map.Entry<String, Object> entry : rawInventoryMap.entrySet()) {
                                    if (entry.getValue() instanceof Number) {
                                        inventoryMap.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                                    }
                                }

                                if (inventoryMap.isEmpty()) {
                                    messageText.setValue("Your inventory is empty");
                                } else {
                                    inventoryLiveData.setValue(inventoryMap);
                                    messageText.setValue(""); // Clear loading message
                                }
                            } else {
                                messageText.setValue("Inventory data is invalid.");
                            }
                        } else {
                            messageText.setValue("Your inventory is empty");
                        }
                    });
                } else {
                    messageText.setValue("Household not assigned.");
                }
            } else {
                messageText.setValue("User data missing.");
            }
        });
    }

    public void addIngredient(String ingredient, int quantity) {
        Map<String, Integer> currentInventory = inventoryLiveData.getValue();
        if (currentInventory == null) {
            currentInventory = new HashMap<>();
        }

        int newQuantity = currentInventory.getOrDefault(ingredient, 0) + quantity;
        currentInventory.put(ingredient, newQuantity);

        updateInventory(currentInventory);
        messageText.setValue(ingredient + " added (" + quantity + ")");
    }

    public void setMessage(String message) {
        messageText.setValue(message);
    }



    public void updateInventory(Map<String, Integer> updatedInventory) {
        if (householdID != null) {
            searchDB.updateHouseholdInventory(householdID, updatedInventory, success -> {
                if (success) {
                    inventoryLiveData.setValue(updatedInventory);
                    messageText.setValue("Inventory updated!");
                } else {
                    messageText.setValue("Failed to update inventory.");
                }
            });
        }
    }

    public void refreshInventory() {
        // Fetch latest inventory data from Firestore
        loadInventory();
    }
}
