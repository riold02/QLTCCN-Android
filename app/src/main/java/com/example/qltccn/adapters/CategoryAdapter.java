package com.example.qltccn.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.models.Category;
import com.example.qltccn.utils.IconUtils;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private OnCategoryClickListener listener;
    private OnCategorySelectListener selectListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
        void onCategoryLongClick(Category category, int position);
    }
    
    // Interface đơn giản cho việc chọn danh mục trong màn hình tìm kiếm
    public interface OnCategorySelectListener {
        void onCategorySelect(Category category, int position);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }
    
    // Constructor mới cho màn hình tìm kiếm
    public CategoryAdapter(List<Category> categories, OnCategorySelectListener selectListener) {
        this.categories = categories;
        this.selectListener = selectListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        
        // Set category name
        holder.txtCategoryName.setText(category.getName());
        
        // Set category icon
        if (category.getIconName() != null && !category.getIconName().isEmpty()) {
            int iconResId = IconUtils.getIconResourceId(context, category.getIconName());
            holder.imgCategoryIcon.setImageResource(iconResId);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, holder.getAdapterPosition());
            }
            if (selectListener != null) {
                selectListener.onCategorySelect(category, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCategoryLongClick(category, holder.getAdapterPosition());
                return true;
            }
            return false;
        });
        
        // Ẩn nút xóa nếu đang trong chế độ chọn danh mục
        if (selectListener != null && holder.imgDeleteCategory != null) {
            holder.imgDeleteCategory.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // Update data
    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    // Add item
    public void addItem(Category category) {
        categories.add(category);
        notifyItemInserted(categories.size() - 1);
    }

    // Remove item
    public void removeItem(int position) {
        if (position >= 0 && position < categories.size()) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Update item
    public void updateItem(Category category, int position) {
        if (position >= 0 && position < categories.size()) {
            categories.set(position, category);
            notifyItemChanged(position);
        }
    }

    // ViewHolder class
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtCategoryName;
        private final ImageView imgCategoryIcon;
        private final ImageView imgDeleteCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            imgDeleteCategory = itemView.findViewById(R.id.imgDeleteCategory);
        }
    }
} 