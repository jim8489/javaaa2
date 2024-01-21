package com.java.eventregistrationapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private DBHelper DB;
    private static final String PREF_USER_VERIFIED = "user_verified";
    private SharedPreferences sharedPreferences;
    private boolean isUserVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button3);
        Button button2 = findViewById(R.id.button2);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isUserVerified = sharedPreferences.getBoolean(PREF_USER_VERIFIED, false);

        button2.setOnClickListener(v -> {
            DB = new DBHelper(this);
            scanCode();
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            boolean isVerified = checkDataInDatabase(result.getContents());

            // Compare the scanned data with the database
            builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isUserVerified) {
                        // User is already verified; show a "Already Verified" toast message
                        Toast.makeText(MainActivity.this, "User Already Verified", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (isVerified) {
                            isUserVerified = true;
                            saveVerificationStatus(true);
                            Toast.makeText(MainActivity.this, "User Verified", Toast.LENGTH_SHORT).show();
                        } else {
                            // Data is not verified; handle as needed
                            Toast.makeText(MainActivity.this, "User Not Verified", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


            builder.show();
        }
    });

    private boolean checkDataInDatabase(String scannedData) {
        SQLiteDatabase db = DB.getReadableDatabase();
        String query = "SELECT * FROM users WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{scannedData});
        boolean existsInDatabase = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existsInDatabase;
    }

    private void saveVerificationStatus(boolean verified) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_USER_VERIFIED, verified);
        editor.apply();
    }
}
