package com.example.letmecook.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryViewModel extends ViewModel {

    private final MutableLiveData<String> inventoryText;
    private final FirebaseFirestore db;
    private final String userId;

    public InventoryViewModel() {
        inventoryText = new MutableLiveData<>();
        inventoryText.setValue("Loading inventory...");
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadInventory();
    }

    public LiveData<String> getInventoryText() {
        return inventoryText;
    }

    private void loadInventory() {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> households = (List<String>) documentSnapshot.get("households");
                if (households != null && !households.isEmpty()) {
                    List<String> inventoryItems = new ArrayList<>();
                    for (String householdId : households) {
                        db.collection("households").document(householdId).get()
                                .addOnSuccessListener(householdSnapshot -> {
                                    if (householdSnapshot.exists()) {
                                        Map<String, Object> inventory = (Map<String, Object>) householdSnapshot.get("inventory");
                                        if (inventory != null) {
                                            for (String item : inventory.keySet()) {
                                                if (!inventoryItems.contains(item)) {
                                                    inventoryItems.add(item);
                                                }
                                            }
                                            inventoryText.setValue(String.join("\n", inventoryItems));
                                        }
                                    }
                                });
                    }
                } else {
                    inventoryText.setValue("You are not in any household.");
                }
            }
        }).addOnFailureListener(e -> inventoryText.setValue("Failed to load inventory."));
    }
}
