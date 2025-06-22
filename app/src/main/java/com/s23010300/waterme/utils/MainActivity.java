package com.s23010300.waterme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.s23010300.waterme.adapters.PlantAdapter;
import com.s23010300.waterme.database.DatabaseHelper;
import com.s23010300.waterme.database.Plant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private TextView tvSuccessRate, tvMissedRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.rv_plants);
        tvSuccessRate = findViewById(R.id.tv_success_rate);
        tvMissedRate = findViewById(R.id.tv_missed_rate);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        FloatingActionButton fabAddPlant = findViewById(R.id.fab_add_plant);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPlants();

        // Load statistics
        loadStatistics();

        // Set click listeners
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        fabAddPlant.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error opening add plant: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlants();
        loadStatistics();
    }

    private void loadPlants() {
        List<Plant> plants = dbHelper.getAllPlants();
        if (adapter == null) {
            adapter = new PlantAdapter(this, plants);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updatePlants(plants);
        }
    }

    private void loadStatistics() {
        int[] stats = dbHelper.getWateringStats();
        tvSuccessRate.setText("Success: " + stats[0]);
        tvMissedRate.setText("Missed: " + stats[1]);
    }
}