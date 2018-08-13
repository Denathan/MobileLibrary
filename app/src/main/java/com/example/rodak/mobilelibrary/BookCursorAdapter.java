package com.example.rodak.mobilelibrary;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rodak.mobilelibrary.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    private MainActivity activity;

    BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.activity = (MainActivity) context;
    }

    TextView nameTextView;
    TextView priceTextView;
    TextView quantityTextView;
    TextView buy;
    TextView availableTextView;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final long id;
        final int mQuantity;

        nameTextView = (TextView) view.findViewById(R.id.text_book);
        priceTextView = (TextView) view.findViewById(R.id.text_book_price);
        quantityTextView = (TextView) view.findViewById(R.id.text_book_quantity);
        buy = (TextView) view.findViewById(R.id.buy);
        availableTextView = (TextView) view.findViewById(R.id.text_available);

        id = cursor.getLong(cursor.getColumnIndex(BookEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        String bookName = cursor.getString(nameColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);

        mQuantity = quantity;

        nameTextView.setText(bookName);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(String.valueOf(quantity));
        if (quantity == 0) {
            quantityTextView.setVisibility(View.GONE);
            availableTextView.setTextColor(activity.getResources().getColor(R.color.red));
            availableTextView.setText(activity.getString(R.string.sold_out));
        } else {
            quantityTextView.setVisibility(View.VISIBLE);
            quantityTextView.setTextColor(activity.getResources().getColor(R.color.lightDarkLine));
            availableTextView.setTextColor(activity.getResources().getColor(R.color.lightDarkLine));
            availableTextView.setText(activity.getString(R.string.available_count));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBookClick(id);
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    activity.onBuyNowClick(id, mQuantity);
                } else {
                    Toast.makeText(activity, R.string.sold_out, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
