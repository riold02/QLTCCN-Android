package com.example.qltccn.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qltccn.R;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final SwipeToDeleteListener listener;
    private final Drawable deleteIcon;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Paint clearPaint;

    public interface SwipeToDeleteListener {
        void onItemDelete(int position);
    }

    public SwipeToDeleteCallback(Context context, SwipeToDeleteListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;
        
        // Thiết lập icon xóa
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        
        // Thiết lập background màu đỏ
        backgroundColor = Color.parseColor("#F44336"); // màu đỏ
        background = new ColorDrawable(backgroundColor);
        
        // Tạo paint cho việc clear background
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Không xử lý sự kiện di chuyển
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        // Gọi listener để xử lý xóa
        if (listener != null) {
            listener.onItemDelete(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        // Không vẽ khi item đang được kéo trở lại vị trí ban đầu
        if (dX == 0 && !isCurrentlyActive) {
            clearCanvas(c, itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        // Vẽ background màu đỏ
        background.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        background.draw(c);

        // Tính toán vị trí của icon xóa
        int iconMargin = (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
        int iconRight = itemView.getRight() - iconMargin;

        // Vẽ icon xóa
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        deleteIcon.draw(c);
    }
    
    private void clearCanvas(Canvas c, float left, float top, float right, float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }
} 