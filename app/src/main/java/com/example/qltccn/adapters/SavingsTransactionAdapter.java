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
import com.example.qltccn.models.SavingsTransaction;
import com.example.qltccn.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsTransactionAdapter extends RecyclerView.Adapter<SavingsTransactionAdapter.SavingsViewHolder> {
    private Context context;
    private List<SavingsTransaction> savingsTransactions;
    private OnSavingsTransactionClickListener listener;

    // Interface cho sự kiện click
    public interface OnSavingsTransactionClickListener {
        void onSavingsTransactionClick(SavingsTransaction transaction, int position);
    }

    // Constructor
    public SavingsTransactionAdapter(Context context, List<SavingsTransaction> savingsTransactions) {
        this.context = context;
        this.savingsTransactions = savingsTransactions;
    }

    // Constructor với listener
    public SavingsTransactionAdapter(Context context, List<SavingsTransaction> savingsTransactions, 
                                     OnSavingsTransactionClickListener listener) {
        this.context = context;
        this.savingsTransactions = savingsTransactions;
        this.listener = listener;
    }

    // Set click listener
    public void setOnSavingsTransactionClickListener(OnSavingsTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SavingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings_transaction, parent, false);
        return new SavingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsViewHolder holder, int position) {
        SavingsTransaction transaction = savingsTransactions.get(position);
        
        // Hiển thị thời gian
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(transaction.getDate()));
        holder.dateTextView.setText(dateStr);
        
        // Hiển thị số tiền
        String amountStr = CurrencyUtils.formatCurrency(transaction.getAmount());
        holder.amountTextView.setText(amountStr);
        
        // Hiển thị thông tin giao dịch
        holder.descriptionTextView.setText(transaction.getDescription());
        
        // Hiển thị loại giao dịch và thiết lập icon
        if ("deposit".equals(transaction.getTransactionType())) {
            holder.typeTextView.setText("Nạp tiền");
            holder.iconImageView.setImageResource(R.drawable.ic_income);
            holder.amountTextView.setTextColor(context.getResources().getColor(R.color.income_green));
        } else {
            holder.typeTextView.setText("Rút tiền");
            holder.iconImageView.setImageResource(R.drawable.ic_expense);
            holder.amountTextView.setTextColor(context.getResources().getColor(R.color.expense_red));
        }
        
        // Hiển thị ghi chú nếu có
        if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
            holder.noteTextView.setVisibility(View.VISIBLE);
            holder.noteTextView.setText(transaction.getNote());
        } else {
            holder.noteTextView.setVisibility(View.GONE);
        }
        
        // Set sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSavingsTransactionClick(transaction, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savingsTransactions != null ? savingsTransactions.size() : 0;
    }
    
    // Cập nhật dữ liệu
    public void updateData(List<SavingsTransaction> newData) {
        this.savingsTransactions = newData;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public class SavingsViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView dateTextView, amountTextView, descriptionTextView, typeTextView, noteTextView;

        public SavingsViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.savingsIconImageView);
            dateTextView = itemView.findViewById(R.id.savingsDateTextView);
            amountTextView = itemView.findViewById(R.id.savingsAmountTextView);
            descriptionTextView = itemView.findViewById(R.id.savingsDescriptionTextView);
            typeTextView = itemView.findViewById(R.id.savingsTypeTextView);
            noteTextView = itemView.findViewById(R.id.savingsNoteTextView);
        }
    }
} 