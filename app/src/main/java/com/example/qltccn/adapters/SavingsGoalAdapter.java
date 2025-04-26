package com.example.qltccn.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.activities.SavingsGoalDetailActivity;
import com.example.qltccn.models.SavingsGoal;
import com.example.qltccn.utils.CurrencyUtils;

import java.util.List;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder> {

    private final Context context;
    private final List<SavingsGoal> savingsGoals;

    public SavingsGoalAdapter(Context context, List<SavingsGoal> savingsGoals) {
        this.context = context;
        this.savingsGoals = savingsGoals;
    }

    @NonNull
    @Override
    public SavingsGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings_goal, parent, false);
        return new SavingsGoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsGoalViewHolder holder, int position) {
        SavingsGoal goal = savingsGoals.get(position);
        
        // Thiết lập dữ liệu cho mỗi item
        holder.tvTitle.setText(goal.getTitle());
        holder.tvAmount.setText(CurrencyUtils.formatAmount(goal.getCurrentAmount()));
        holder.tvProgress.setText(String.format("%.0f%%", goal.getProgressPercentage()));
        
        // Thiết lập icon theo loại mục tiêu
        setGoalIcon(holder.ivIcon, goal.getCategoryType());
        
        // Thiết lập sự kiện click để mở chi tiết mục tiêu
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavingsGoalDetailActivity.class);
            intent.putExtra("GOAL_ID", goal.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return savingsGoals.size();
    }

    // ViewHolder class
    static class SavingsGoalViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvAmount, tvProgress;
        LinearLayout container;

        public SavingsGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivGoalIcon);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvAmount = itemView.findViewById(R.id.tvGoalAmount);
            tvProgress = itemView.findViewById(R.id.tvGoalProgress);
            container = itemView.findViewById(R.id.goalContainer);
        }
    }

    // Phương thức cập nhật dữ liệu
    public void updateData(List<SavingsGoal> newGoals) {
        this.savingsGoals.clear();
        this.savingsGoals.addAll(newGoals);
        notifyDataSetChanged();
    }

    // Phương thức thiết lập icon cho mục tiêu dựa vào loại
    private void setGoalIcon(ImageView imageView, String categoryType) {
        switch (categoryType) {
            case "travel":
                imageView.setImageResource(R.drawable.ic_travel);
                break;
            case "house":
                imageView.setImageResource(R.drawable.ic_newhome);
                break;
            case "car":
                imageView.setImageResource(R.drawable.ic_car);
                break;
            case "wedding":
                imageView.setImageResource(R.drawable.ic_wedding);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_expense);
                break;
        }
    }
} 