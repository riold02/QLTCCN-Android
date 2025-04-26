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
import com.example.qltccn.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsTransactionAdapter extends RecyclerView.Adapter<SavingsTransactionAdapter.SavingsTransactionViewHolder> {

    private final Context context;
    private final List<SavingsTransaction> transactions;
    private final SimpleDateFormat dateFormat;

    public SavingsTransactionAdapter(Context context, List<SavingsTransaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public SavingsTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings_transaction, parent, false);
        return new SavingsTransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsTransactionViewHolder holder, int position) {
        SavingsTransaction transaction = transactions.get(position);
        
        // Thiết lập dữ liệu cho mỗi item
        holder.tvDescription.setText(transaction.getDescription());
        
        // Sử dụng định dạng ngày trực tiếp với SimpleDateFormat
        Date transactionDate = new Date(transaction.getDate());
        holder.tvDate.setText(dateFormat.format(transactionDate));
        
        // Định dạng số tiền và phân biệt màu sắc theo loại giao dịch
        String prefix = "";
        int textColor;
        
        if ("deposit".equals(transaction.getTransactionType())) {
            // Gửi tiền - hiển thị màu xanh
            prefix = "+";
            textColor = context.getResources().getColor(R.color.income_color);
            // Sử dụng biểu tượng khác cho gửi tiền
            holder.ivTransactionIcon.setImageResource(R.drawable.ic_money);
        } else {
            // Rút tiền - hiển thị màu đỏ và dấu trừ
            prefix = "-";
            textColor = context.getResources().getColor(R.color.expense_color);
            // Sử dụng biểu tượng khác cho rút tiền
            holder.ivTransactionIcon.setImageResource(R.drawable.ic_expense);
        }
        
        // Định dạng và hiển thị số tiền với prefix
        String formattedAmount = prefix + CurrencyUtils.formatAmount(transaction.getAmount());
        holder.tvAmount.setText(formattedAmount);
        holder.tvAmount.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // ViewHolder class
    static class SavingsTransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTransactionIcon;
        TextView tvDescription, tvAmount, tvDate;

        public SavingsTransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }

    // Phương thức cập nhật dữ liệu
    public void updateData(List<SavingsTransaction> newTransactions) {
        this.transactions.clear();
        this.transactions.addAll(newTransactions);
        notifyDataSetChanged();
    }
} 