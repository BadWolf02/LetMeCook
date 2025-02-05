package com.example.letmecook.tools;


import android.content.Context;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
public class barcode_API {

    public barcode_API(Context context) {
        // initialise the scanner
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13,      // standard food product barcode type
                                Barcode.FORMAT_UPC_A,       // also common for products
                                Barcode.FORMAT_QR_CODE      // allows QR codes
                        )
                        .build();
    }
}
