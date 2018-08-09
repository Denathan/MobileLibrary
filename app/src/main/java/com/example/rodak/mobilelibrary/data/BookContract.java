package com.example.rodak.mobilelibrary.data;

import android.provider.BaseColumns;

public final class BookContract {

    private BookContract() {
    }

    public static final class BookEntry implements BaseColumns {

        /**
         * Name of database table for books
         */
        public final static String TABLE_NAME = "books";

        /**
         * Unique ID number for the book (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_NAME = "book";

        /**
         * Price of the book.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_PRICE = "price";

        /**
         * Number of books.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_QUANTITY = "quantity";

        /**
         * Name of the supplier.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME = "supplier";

        /**
         * Supplier's phone number.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_PHONE = "phone";

    }

}
