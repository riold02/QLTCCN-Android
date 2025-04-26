package com.example.qltccn.adapters;

import android.content.Context;
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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private Context context;
    private List<Transaction> transactions;
    private OnTransactionClickListener listener;

    // Interface cho sự kiện click
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction, int position);
    }

    // Constructor
    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }
    
    // New constructor for use without context
    public TransactionAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    // Set click listener
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        
        // Set category and description
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDescription.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                ? transaction.getDescription() : "Không có mô tả");
        
        // Set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvDate.setText(dateFormat.format(new Date(transaction.getDate())));
        
        // Format số tiền
        String formattedAmount = CurrencyUtils.formatVND(transaction.getAmount());
        
        // Set màu sắc dựa vào loại giao dịch
        if ("deposit".equalsIgnoreCase(transaction.getCategory())) {
            // Nạp tiền - hiển thị màu xanh lá cây
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.income_color));
            holder.tvAmount.setText("+ " + formattedAmount);
        } else {
            // Chi tiêu - hiển thị màu đỏ
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.expense_color));
            holder.tvAmount.setText("- " + formattedAmount);
        }
        
        // Set icon based on category
        int iconResource = getCategoryIcon(transaction.getCategory());
        holder.ivCategoryIcon.setImageResource(iconResource);
    }

    // Hàm lấy icon dựa trên category
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
        return transactions != null ? transactions.size() : 0;
    }

    // Update data
    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategory, tvDescription, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick(transactions.get(position), position);
                }
            });
        }
    }
} 