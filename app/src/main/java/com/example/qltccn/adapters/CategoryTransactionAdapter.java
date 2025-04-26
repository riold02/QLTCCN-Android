package com.example.qltccn.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;
import com.example.qltccn.models.Transaction;
import com.example.qltccn.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoryTransactionAdapter extends RecyclerView.Adapter<CategoryTransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction, int position);
    }

    public CategoryTransactionAdapter(List<Transaction> transactionList, OnItemClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    public void updateData(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        
        holder.transactionTitle.setText(transaction.getDescription());
        
        // Định dạng ngày giờ
        String formattedDate = formatDate(transaction.getDate());
        holder.transactionDate.setText(formattedDate);
        
        // Định dạng số tiền và màu sắc
        String formattedAmount = CurrencyUtils.formatVND(transaction.getAmount());
        if ("deposit".equalsIgnoreCase(transaction.getCategory())) {
            // Nạp tiền - hiển thị màu xanh lá cây
            holder.transactionAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.income_color));
            holder.transactionAmount.setText("+ " + formattedAmount);
        } else {
            // Chi tiêu - hiển thị màu đỏ
            holder.transactionAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.expense_color));
            holder.transactionAmount.setText("- " + formattedAmount);
        }
        
        // Xử lý avatar/icon nếu có
        holder.transactionIcon.setImageResource(getCategoryIcon(transaction.getCategory()));
        
        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaction, position);
            }
        });
    }
    
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private int getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "food":
                return R.drawable.ic_food;
            case "transport":
                return R.drawable.ic_transport;
            case "medicine":
                return R.drawable.ic_medicine;
            case "groceries":
                return R.drawable.ic_groceries;
            case "rent":
                return R.drawable.ic_rent;
            case "gifts":
                return R.drawable.ic_gifts;
            case "savings":
                return R.drawable.ic_savings;
            case "entertainment":
                return R.drawable.ic_entertainment;
            case "deposit":
                return R.drawable.ic_savings;
            default:
                return R.drawable.ic_category;
        }
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionTitle;
        TextView transactionAmount;
        TextView transactionDate;
        ImageView transactionIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTitle = itemView.findViewById(R.id.transactionTitle);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
        }
    }
} 