package com.example.letmecook.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.letmecook.db_tools.SearchDB;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<String> inventoryText;
    private final String userId;
    private final SearchDB searchDB;
    private final MutableLiveData<String> mText;

    public InventoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is inventory fragment");
        inventoryText = new MutableLiveData<>();
        inventoryText.setValue("Loading inventory...");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        searchDB = new SearchDB();
        loadInventory();
    }

    public LiveData<String> getInventoryText() {
        return inventoryText;
    }

    public LiveData<String> getText() {
        return mText;
    }
}
    private void loadInventory() {
        searchDB.getUserDocumentByIDAsync(userId, userDocument -> {
            if (userDocument != null) {
                String householdID = userDocument.getString("householdID");
                if (householdID != null) {
                    searchDB.getHouseholdByIDAsync(householdID, householdSnapshot -> {
                        if (householdSnapshot != null) {
                            if (householdSnapshot.contains("inventory")) {Object inventoryObj = householdSnapshot.get("inventory");
                                if (inventoryObj != null) {
                                    Map<String, Object> inventoryMap = (Map<String, Object>) inventoryObj;
                                    if (inventoryMap.isEmpty()) {
                                        inventoryText.setValue("Your inventory is empty");
                                    } else {
                                        List<String> inventoryItems = new ArrayList<>(inventoryMap.keySet());
                                        inventoryText.setValue(String.join("\n", inventoryItems));
                                    }
                                } else {
                                    inventoryText.setValue("Your inventory is empty");
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
