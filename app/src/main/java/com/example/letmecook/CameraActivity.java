package com.example.letmecook;

import android.app.Activity;
import android.content.ContentValues;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
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
//                Toast.makeText(this, "Barcode Detected: "+ rawValue, Toast.LENGTH_SHORT).show();
                String scannedBarcode = rawValue;
                barcodeResult.setText("Scanned value: " + scannedBarcode);
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
