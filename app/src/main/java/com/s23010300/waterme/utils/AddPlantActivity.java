package com.s23010300.waterme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.s23010300.waterme.database.DatabaseHelper;
import com.s23010300.waterme.database.Plant;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddPlantActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST = 100;

    private EditText etPlantName, etPlantDescription;
    private RadioGroup rgFrequency, rgPlantImage;
    private ImageView imgCustomPlant;
    private Button btnUploadImage, btnTakePhoto;
    private DatabaseHelper dbHelper;
    private String selectedImageName = "plant1";
    private boolean isCustomImage = false;
    private Bitmap customImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        try {
            etPlantName = findViewById(R.id.et_plant_name);
            etPlantDescription = findViewById(R.id.et_plant_description);
            rgFrequency = findViewById(R.id.rg_frequency);
            rgPlantImage = findViewById(R.id.rg_plant_image);
            imgCustomPlant = findViewById(R.id.img_custom_plant);
            btnUploadImage = findViewById(R.id.btn_upload_image);
            btnTakePhoto = findViewById(R.id.btn_take_photo);
            Button btnAddPlant = findViewById(R.id.btn_add_plant);

            // Set click listeners
            btnUploadImage.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            });

            btnTakePhoto.setOnClickListener(v -> {
                if (checkCameraPermission()) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            });

            btnAddPlant.setOnClickListener(v -> addPlant());

            // When custom image is selected, uncheck all radio buttons
            imgCustomPlant.setOnClickListener(v -> {
                if (isCustomImage) {
                    rgPlantImage.clearCheck();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    customImageBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    imgCustomPlant.setImageBitmap(customImageBitmap);
                    isCustomImage = true;
                    rgPlantImage.clearCheck();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                customImageBitmap = (Bitmap) data.getExtras().get("data");
                imgCustomPlant.setImageBitmap(customImageBitmap);
                isCustomImage = true;
                rgPlantImage.clearCheck();
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        String fileName = "custom_" + System.currentTimeMillis() + ".png";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addPlant() {
        String name = etPlantName.getText().toString().trim();
        String description = etPlantDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter plant name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected frequency
        int frequency = 1;
        int selectedFrequencyId = rgFrequency.getCheckedRadioButtonId();
        if (selectedFrequencyId == R.id.rb_once) {
            frequency = 1;
        } else if (selectedFrequencyId == R.id.rb_twice) {
            frequency = 2;
        } else if (selectedFrequencyId == R.id.rb_thrice) {
            frequency = 3;
        } else if (selectedFrequencyId == R.id.rb_daily) {
            frequency = 7;
        }

        // Get selected plant image or use custom image
        String imageName = "plant1";
        if (isCustomImage && customImageBitmap != null) {
            imageName = saveImageToInternalStorage(customImageBitmap);
            if (imageName == null) {
                Toast.makeText(this, "Failed to save custom image", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            int selectedImageId = rgPlantImage.getCheckedRadioButtonId();
            if (selectedImageId == R.id.rb_plant1) {
                imageName = "plant1";
            } else if (selectedImageId == R.id.rb_plant2) {
                imageName = "plant2";
            } else if (selectedImageId == R.id.rb_plant3) {
                imageName = "plant3";
            } else if (selectedImageId == R.id.rb_plant4) {
                imageName = "plant4";
            } else if (selectedImageId == R.id.rb_plant5) {
                imageName = "plant5";
            } else if (selectedImageId == R.id.rb_plant6) {
                imageName = "plant6";
            }
        }

        // Create and save plant
        Plant plant = new Plant(name, imageName, description, frequency);
        long result = dbHelper.addPlant(plant);

        if (result != -1) {
            Toast.makeText(this, "Plant added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add plant", Toast.LENGTH_SHORT).show();
        }
    }
}