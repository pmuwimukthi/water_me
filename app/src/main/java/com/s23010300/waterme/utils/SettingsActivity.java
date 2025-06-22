package com.s23010300.waterme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.s23010300.waterme.database.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private EditText etLatitude, etLongitude, etEmail;
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnGetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        ImageButton btnBack = findViewById(R.id.btn_back_settings);
        etLatitude = findViewById(R.id.et_latitude);
        etLongitude = findViewById(R.id.et_longitude);
        etEmail = findViewById(R.id.et_email);
        Button btnSave = findViewById(R.id.btn_save_settings);
        btnGetLocation = findViewById(R.id.btn_get_location);

        // Load existing settings
        loadSettings();

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveSettings());

        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void getCurrentLocation() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Get location
        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        btnGetLocation.setEnabled(true);
                        btnGetLocation.setText("Get Current Location");

                        if (location != null) {
                            // Set latitude and longitude in the text fields
                            etLatitude.setText(String.valueOf(location.getLatitude()));
                            etLongitude.setText(String.valueOf(location.getLongitude()));
                            Toast.makeText(SettingsActivity.this,
                                    "Location obtained successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this,
                                    "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("Get Current Location");
                    Toast.makeText(SettingsActivity.this,
                            "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSettings() {
        double[] location = dbHelper.getLocation();
        String email = dbHelper.getEmail();

        if (location[0] != 0.0) {
            etLatitude.setText(String.valueOf(location[0]));
        }
        if (location[1] != 0.0) {
            etLongitude.setText(String.valueOf(location[1]));
        }
        if (!email.isEmpty()) {
            etEmail.setText(email);
        }
    }

    private void saveSettings() {
        String latStr = etLatitude.getText().toString().trim();
        String lonStr = etLongitude.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        double latitude = 0.0;
        double longitude = 0.0;

        try {
            if (!latStr.isEmpty()) {
                latitude = Double.parseDouble(latStr);
            }
            if (!lonStr.isEmpty()) {
                longitude = Double.parseDouble(lonStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email if provided
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save settings
        dbHelper.updateSettings(latitude, longitude, email);
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}