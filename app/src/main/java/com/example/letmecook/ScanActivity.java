package com.example.letmecook;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private Button scan_button;
    private ImageView barcode_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scan_button = findViewById(R.id.scan_button);
        barcode_image = findViewById(R.id.ImageViewBarcode);

        BitmapDrawable drawable = (BitmapDrawable) barcode_image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
                barcodeScanner.process(image).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        for(Barcode barcode : barcodes) {
                            String barcodeData = barcode.getRawValue();
                            Toast.makeText(ScanActivity.this, barcodeData, Toast.LENGTH_LONG).show(); // display the barcode data
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });


    }
}