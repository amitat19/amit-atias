package com.example.project;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private List<Bitmap> images;

    public GalleryAdapter(List<Bitmap> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.imageView.setImageBitmap(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_gallery_item);
        }
    }
} 