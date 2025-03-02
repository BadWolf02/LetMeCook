package com.example.letmecook.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<Map<String, String>> userData = new MutableLiveData<>();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel() {
        importUserData();
        mText = new MutableLiveData<>();
        // mText.setValue("Invite to your household");
    }

    private void importUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("username", documentSnapshot.getString("username"));
                    data.put("email", documentSnapshot.getString("email"));
                    data.put("householdID", documentSnapshot.getString("householdID"));

                    userData.setValue(data);
                } else {
                    errorMessage.setValue("User data not found");
                }
            }).addOnFailureListener(e -> errorMessage.setValue("Failed to load user data"));
        } else {
            errorMessage.setValue("User not logged in");
        }
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<Map<String, String>> getUserData() {
        return userData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}