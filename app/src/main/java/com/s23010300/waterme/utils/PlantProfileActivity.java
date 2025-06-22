package com.s23010300.waterme;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.s23010300.waterme.database.DatabaseHelper;
import com.s23010300.waterme.database.Plant;
import java.util.List;

public class PlantProfileActivity extends AppCompatActivity implements SensorEventListener {
    private DatabaseHelper dbHelper;
    private Plant plant;
    private int plantId;
    private RecyclerView rvHistory;

    // Shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime = 0;
    private static final int SHAKE_TIME_INTERVAL = 2000; // 2 seconds between shakes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_profile);

        // Get plant ID from intent
        plantId = getIntent().getIntExtra("plant_id", -1);
        if (plantId == -1) {
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        plant = dbHelper.getPlant(plantId);

        if (plant == null) {
            finish();
            return;
        }

        // Initialize views
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvPlantNameHeader = findViewById(R.id.tv_plant_name_header);
        ImageView imgPlantProfile = findViewById(R.id.img_plant_profile);
        TextView tvPlantNameProfile = findViewById(R.id.tv_plant_name_profile);
        TextView tvPlantDescription = findViewById(R.id.tv_plant_description);
        TextView tvWateringFrequency = findViewById(R.id.tv_watering_frequency);
        Button btnWaterNow = findViewById(R.id.btn_water_now);
        Button btnDeletePlant = findViewById(R.id.btn_delete_plant);
        rvHistory = findViewById(R.id.rv_watering_history);

        // Set data
        tvPlantNameHeader.setText(plant.getName());
        tvPlantNameProfile.setText(plant.getName());
        tvPlantDescription.setText(plant.getDescription().isEmpty() ? "No description" : plant.getDescription());
        tvWateringFrequency.setText("Water: " + plant.getWateringFrequency() + " times per week");

        // Set plant image
        if (plant.getImageName().startsWith("custom_")) {
            // Load custom image from internal storage
            try {
                java.io.File imgFile = new java.io.File(getFilesDir(), plant.getImageName());
                if (imgFile.exists()) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imgPlantProfile.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Load default plant image
            int imageResource = getResources().getIdentifier(
                    plant.getImageName(), "drawable", getPackageName());
            if (imageResource != 0) {
                imgPlantProfile.setImageResource(imageResource);
            }
        }

        // Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        loadHistory();

        // Initialize shake detection
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());

        btnWaterNow.setOnClickListener(v -> {
            waterPlant();
        });

        btnDeletePlant.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Plant")
                    .setMessage("Are you sure you want to delete " + plant.getName() + "?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (dbHelper.deletePlant(plantId)) {
                                Toast.makeText(PlantProfileActivity.this, "Plant deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(PlantProfileActivity.this, "Failed to delete plant", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register shake sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Shake to water " + plant.getName() + "!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister shake sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > SHAKE_TIME_INTERVAL) {
                    lastShakeTime = currentTime;
                    waterPlant();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for shake detection
    }

    private void waterPlant() {
        dbHelper.waterPlant(plantId);
        Toast.makeText(this, plant.getName() + " watered successfully!", Toast.LENGTH_SHORT).show();
        loadHistory();

        // Add haptic feedback
        android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100); // Vibrate for 100ms
        }
    }

    private void loadHistory() {
        List<String[]> history = dbHelper.getWateringHistory(plantId);
        HistoryAdapter adapter = new HistoryAdapter(history);
        rvHistory.setAdapter(adapter);
    }

    // History Adapter
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private final List<String[]> history;

        public HistoryAdapter(List<String[]> history) {
            this.history = history;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            String[] item = history.get(position);
            String date = item[0];
            boolean success = Boolean.parseBoolean(item[1]);

            holder.text1.setText("Date: " + date);
            holder.text2.setText(success ? "✓ Watered" : "✗ Missed");
            holder.text2.setTextColor(success ? 0xFF4CAF50 : 0xFFF44336);
        }

        @Override
        public int getItemCount() {
            return history.size();
        }

        static class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            public HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}