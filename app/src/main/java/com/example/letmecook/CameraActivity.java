package com.example.letmecook;

import android.app.Activity;
import android.content.ContentValues;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.letmecook.db_tools.SearchDB;
import com.example.letmecook.ui.inventory.InventoryFragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CameraActivity extends AppCompatActivity {
    ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;

    private FirebaseFirestore db;
    private SearchDB searchDB;
    private String householdId;
    private String uid;
    private String product_name;
    private String kcal_energy;
    private String allergens_info;
    private InventoryFragment inventoryFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.preview_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // dynamically get householdId
        searchDB = new SearchDB();
        searchDB.getUserHouseholdID(uid, retrievedHouseholdID -> {
            if (retrievedHouseholdID != null) {
                householdId = retrievedHouseholdID; // Store household ID
                Log.d("CameraActivity", "Household ID retrieved: " + householdId);
            } else {
                Log.e("CameraActivity", "Failed to retrieve Household ID");
                Toast.makeText(CameraActivity.this, "Error retrieving household ID!", Toast.LENGTH_SHORT).show();
            }
        });


        Button sendToInventoryButton = findViewById(R.id.SendToInventory);
        db = FirebaseFirestore.getInstance();
        sendToInventoryButton.setOnClickListener(v -> {
            if (product_name == null || product_name.isEmpty()) {
                Toast.makeText(CameraActivity.this, "No product scanned!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Safely parse calories, defaulting to 0 if invalid
            int caloriesValue = 0;
            try {
                caloriesValue = Integer.parseInt(kcal_energy.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                Log.e("CameraActivity", "Error parsing calories: " + kcal_energy, e);
            }

            // Ensure allergens is a valid list
            List<String> allergensList = new ArrayList<>();
            if (allergens_info != null && !allergens_info.equalsIgnoreCase("No Allergen Info")) {
                allergensList = Arrays.asList(allergens_info.split(","));
            }

            sendProductToIngredients(product_name, caloriesValue, allergensList);
        });


        // listener to start takePhoto method
        findViewById(R.id.TakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderListenableFuture.get();
                    startCameraX(cameraProvider);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }, ContextCompat.getMainExecutor(this));


    }
    private void sendProductToIngredients(String product_name, int kcal_energy, List<String> allergens_info) {
        DocumentReference ingredientRef = db.collection("ingredients").document(product_name);

        // Create ingredient data with the correct structure
        Map<String, Object> ingredientData = new HashMap<>();
        ingredientData.put("name", product_name);
        ingredientData.put("calories", kcal_energy);
        ingredientData.put("category", 0); // Placeholder, update if needed
        ingredientData.put("id", 0); // Placeholder, update if needed
        ingredientData.put("serving_size", "100g"); // Default serving size
        ingredientData.put("allergens", allergens_info);

        // Save to Firestore
        ingredientRef.set(ingredientData)
                .addOnSuccessListener(aVoid -> Log.d("CameraActivity", "Ingredient added to collection successfully!"))
                .addOnFailureListener(e -> Log.e("CameraActivity", "Error adding ingredient: " + e.getMessage()));
    }
private void sendProductToInventory(String product_name, int quantity) {
    DocumentReference householdRef = db.collection("households").document(householdId);

    householdRef.get().addOnSuccessListener(documentSnapshot -> {
        if (documentSnapshot.exists()) {
            // Retrieve existing inventory
            Map<String, Object> inventory = (Map<String, Object>) documentSnapshot.get("inventory");
            if (inventory == null) {
                inventory = new HashMap<>();
            }

            // Update the quantity (increment if exists)
            if (inventory.containsKey(product_name)) {
                int currentQuantity = ((Number) inventory.get(product_name)).intValue();
                inventory.put(product_name, currentQuantity + quantity);
            } else {
                inventory.put(product_name, quantity);
            }

            // Save back to Firestore
            householdRef.update("inventory", inventory)
                    .addOnSuccessListener(aVoid -> System.out.println("Product added successfully!"))
                    .addOnFailureListener(e -> System.err.println("Error adding product: " + e.getMessage()));
        }
    }).addOnFailureListener(e -> System.err.println("Error retrieving inventory: " + e.getMessage()));
}


    public void fetchProductInfo(String barcode) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://world.openfoodfacts.org/api/v2/product/" + barcode + ".json";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Failed to fetch data", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("API_ERROR", "Unexpected response: " + response);
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    JSONObject product = json.getJSONObject("product");

                    String productName = product.optString("product_name", "Unknown Product");
                    JSONObject nutriments = product.optJSONObject("nutriments");

                    String energy = nutriments != null ? nutriments.optString("energy-kcal_100g", "No Data") + " kcal" : "No Data";

                    // Gather Allergy Info
                    String allergens = product.optString("allergens", "No Allergen Info");
                    product_name = productName;
                    kcal_energy = energy;
                    allergens_info = allergens;

                    // Update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        TextView barcodeResult = findViewById(R.id.BarcodeResult);
                        barcodeResult.setText("Product: " + productName + "\nCalories (per 100g): " + energy + "\nAllergen info: " + allergens);
                    });

                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON parsing error", e);
                }
            }
        });
    }


// allows the user to save photos of recipes to their camera roll (can remove if not wanted)
    private void takePhoto() {
        if(imageCapture == null) return;

        String name = System.currentTimeMillis() + "";      // timestamp is the name

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraXStable");
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(CameraActivity.this, "Image Captured" + outputFileResults.getSavedUri(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });

    }

    private void processBarcodeImage(InputImage inputImage, ImageProxy imageProxy){
        TextView barcodeResult = findViewById(R.id.BarcodeResult);
        BarcodeScanner scanner = BarcodeScanning.getClient();       // instance of barcode scanner
        scanner.process(inputImage).addOnSuccessListener(barcodes -> {
            for(Barcode barcode : barcodes){
                String rawValue = barcode.getRawValue();
//                barcodeResult.setText("Scanned value: " + rawValue); // placeholder showing the raw value of the barcode
                fetchProductInfo(rawValue);   // API call for last scanned product
            }
            // Close image after processing it
            imageProxy.close();
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            imageProxy.close();     // double check image is closed
        });
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        PreviewView previewView = findViewById(R.id.preview_view);  // gets the preview_view from XML file
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        imageAnalysis = new ImageAnalysis.Builder().
                setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                @SuppressWarnings("UnsafeOptInUsageError")
                Image mediaImage = image.getImage();
                if (mediaImage != null){
                    InputImage inputImage = InputImage.fromMediaImage(
                            mediaImage, image.getImageInfo().getRotationDegrees()
                    );
                    processBarcodeImage(inputImage, image);
                }
            }
        });

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
