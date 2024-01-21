package com.java.eventregistrationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FourthActivity extends AppCompatActivity {
    private ImageView qrCodeImageView;
    private EditText username;
    private EditText email;
    private EditText number;
    private EditText college;
    private Button submit;
    private CheckBox checkboxPrice;

    private Button Home;

    DBHelper DB;
    public Context context;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 123;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        username = findViewById(R.id.editTextName);
        email = findViewById(R.id.userEmail);
        number = findViewById(R.id.editTextNumber);
        college = findViewById(R.id.editTextCollege);
        checkboxPrice = findViewById(R.id.checkboxPrice);
        submit = findViewById(R.id.buttonSubmit);
        Home = findViewById(R.id.button4);
        DB = new DBHelper(this);
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }


        Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FourthActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input from EditText fields
                String name = username.getText().toString().trim();
                String em = email.getText().toString().trim();
                String num = number.getText().toString().trim();
                String col = college.getText().toString().trim();
                boolean priceAccepted = checkboxPrice.isChecked();

                if (priceAccepted && !name.isEmpty() && !em.isEmpty() && !num.isEmpty() && !col.isEmpty()) {
                    try {
                        String registrationData = "Name: " + name + "\n" +
                                "Email: " + em + "\n" +
                                "Number: " + num + "\n" +
                                "College: " + col;

                        Bitmap qrCode = generateQRCode(registrationData);
                        qrCodeImageView.setImageBitmap(qrCode);
                    }
                     catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
                qrCodeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadQrCodeAsPdf();
                    }
                });

                Boolean insert = DB.insertData(name, em, num, col);
                if (!name.isEmpty()) {
                    try {
                        // Generate the QR code
                        Bitmap qrCode = generateQRCode(name);

                        qrCodeImageView.setImageBitmap(qrCode);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
                if (!em.isEmpty()) {
                    try {
                        // Generate the QR code
                        Bitmap qrCode = generateQRCode(em);

                        qrCodeImageView.setImageBitmap(qrCode);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }

                if (insert == true) {
                    Toast.makeText(FourthActivity.this, "Tap the QR code to download", Toast.LENGTH_SHORT).show();
                }
                // Check if required fields are empty
                if (name.isEmpty() || em.isEmpty() || num.isEmpty() || col.isEmpty()) {
                    // Show an error dialog or message indicating that required fields are missing
                    showErrorMessage("Please fill in all required fields.");
                } else {
                    Boolean checkuser = DB.checkusername(name);
                    // All required fields are filled; you can proceed with registration
                    // Your registration logic goes here
                }
            }
            });

        }


    private Bitmap generateQRCode(String data) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return bitmap;
    }
    private void showErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE
        );
    }
    private void downloadQrCodeAsPdf() {
        // Get the bitmap from the QR code ImageView
        BitmapDrawable drawable = (BitmapDrawable) qrCodeImageView.getDrawable();
        Bitmap qrCodeBitmap = drawable.getBitmap();

        // Generate a unique filename for the PDF
        String fileName = "QRCode_" + System.currentTimeMillis() + ".pdf";

        try {
            // Get the application's external storage directory
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (externalFilesDir != null) {
                // Additional logging for debugging
                Log.d("PDF_PATH", "Path: " + externalFilesDir.getAbsolutePath());

                // Create a file within the internal storage directory
                File pdfFile = new File(externalFilesDir, fileName);
                Log.d("PDF_FILE", "File: " + pdfFile.getAbsolutePath());


                // Create a file within the internal storage directory
                OutputStream outputStream = new FileOutputStream(pdfFile);

                // Initialize iText PDF writer
                PdfWriter pdfWriter = new PdfWriter(outputStream);
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                Document document = new Document(pdfDocument);

                 // Add QR code image to the PDF
                Image qrCodeImage = new Image(ImageDataFactory.create(bitmapToBytes(qrCodeBitmap)));
                document.add(qrCodeImage);

                 // Close the document
                document.close();

                // Notify the MediaStore about the new file
                 notifyMediaStore(this, pdfFile);

                // Open the PDF using an intent
                 openPdfWithIntent(this, pdfFile);
            } else {
                Log.e("PDF_PATH", "External files directory is null");
                Toast.makeText(this, "QR Code Saved as PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "QR Code Saved as PDF", Toast.LENGTH_SHORT).show();
        }
    }



    private void notifyMediaStore(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }
    }
    private void openPdfWithIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can perform your operations that require this permission
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Storage permission is required to download QR Code as PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

