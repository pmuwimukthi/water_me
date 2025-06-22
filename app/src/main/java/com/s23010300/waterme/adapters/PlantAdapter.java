package com.s23010300.waterme.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.s23010300.waterme.PlantProfileActivity;
import com.s23010300.waterme.R;
import com.s23010300.waterme.database.Plant;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private Context context;
    private List<Plant> plants;

    public PlantAdapter(Context context, List<Plant> plants) {
        this.context = context;
        this.plants = plants;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);

        holder.plantName.setText(plant.getName());

        // Set plant image
        if (plant.getImageName().startsWith("custom_")) {
            // Load custom image from internal storage
            try {
                java.io.File imgFile = new java.io.File(context.getFilesDir(), plant.getImageName());
                if (imgFile.exists()) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.plantImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Set default image if custom image fails to load
                holder.plantImage.setImageResource(R.drawable.plant1);
            }
        } else {
            // Load default plant image
            int imageResource = context.getResources().getIdentifier(
                    plant.getImageName(), "drawable", context.getPackageName());
            if (imageResource != 0) {
                holder.plantImage.setImageResource(imageResource);
            }
        }

        // Show/hide reminder message
        if (plant.isMissedLastWatering()) {
            holder.reminderText.setVisibility(View.VISIBLE);
        } else {
            holder.reminderText.setVisibility(View.GONE);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlantProfileActivity.class);
            intent.putExtra("plant_id", plant.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public void updatePlants(List<Plant> newPlants) {
        this.plants = newPlants;
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView plantImage;
        TextView plantName;
        TextView reminderText;

        PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_plant);
            plantImage = itemView.findViewById(R.id.img_plant);
            plantName = itemView.findViewById(R.id.tv_plant_name);
            reminderText = itemView.findViewById(R.id.tv_reminder);
        }
    }
}