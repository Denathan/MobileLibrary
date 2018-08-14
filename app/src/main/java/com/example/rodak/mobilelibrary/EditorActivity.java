package com.example.rodak.mobilelibrary;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rodak.mobilelibrary.data.BookContract.BookEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;
    Uri imageUri;
    private int quantity;
    private Uri currentBookUri;

    @BindView(R.id.edit_book_name)
    EditText nameEditText;
    @BindView(R.id.edit_book_price)
    EditText priceEditText;
    @BindView(R.id.edit_supplier_name)
    EditText supplierName;
    @BindView(R.id.edit_supplier_phone)
    EditText supplierPhone;
    @BindView(R.id.edit_quantity_text_view)
    TextView quantityTextView;
    @BindView(R.id.button_plus)
    Button plusButton;
    @BindView(R.id.button_minus)
    Button minusButton;
    @BindView(R.id.submit_button)
    Button insertBook;
    @BindView(R.id.call_supplier_button)
    Button callSupplier;

    private boolean bookHasChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this);
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        if (currentBookUri == null) {
            setTitle(getString(R.string.add_book_title));
            supplierName.setEnabled(true);
            invalidateOptionsMenu();
        } else {
            callSupplier.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.edit_book_title));
            supplierName.setEnabled(false);
            getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        nameEditText.setOnTouchListener(onTouchListener);
        priceEditText.setOnTouchListener(onTouchListener);
        supplierName.setOnTouchListener(onTouchListener);
        supplierPhone.setOnTouchListener(onTouchListener);
        minusButton.setOnTouchListener(onTouchListener);
        plusButton.setOnTouchListener(onTouchListener);
        insertBook.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onBackPressed() {
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertBook(View v) {
        if (saveBook()) {
            finish();
        }
    }

    private boolean saveBook() {
        String validNumber = "^[+]?[0-9]{8,15}$";

        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String supplierNameString = supplierName.getText().toString().trim();
        String supplierPhoneString = supplierPhone.getText().toString().trim();
        String quantityString = quantityTextView.getText().toString();

        if (currentBookUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString) &&
                TextUtils.isEmpty(quantityString) &&
                imageUri == null) {
            return true;
        }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.book_name_req), Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.book_price_req), Toast.LENGTH_SHORT).show();
            return false;
        }
        values.put(BookEntry.COLUMN_BOOK_PRICE, priceString);

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.book_quantity_req), Toast.LENGTH_SHORT).show();
            return false;
        }
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityString);

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.supplier_name_req), Toast.LENGTH_SHORT).show();
            return false;
        }
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        if (TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, getString(R.string.supplier_phone_req), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!supplierPhoneString.matches(validNumber)) {
                Toast.makeText(this, getString(R.string.supplier_phone_incorrect), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);


        if (currentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.error_saving_book),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.book_saved),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_saving_book),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (bookHasChanged) {
                    Toast.makeText(this, getString(R.string.book_saved),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (currentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};

        return new CursorLoader(this,
                currentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int sNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int sPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String sName = cursor.getString(sNameColumnIndex);
            String sPhone = cursor.getString(sPhoneColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            nameEditText.setText(name);
            priceEditText.setText(price);
            supplierName.setText(sName);
            supplierPhone.setText(sPhone);
            quantityTextView.setText(Integer.toString(quantity));
        }
    }

    public void callSupplier(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + supplierPhone.getText().toString()));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        supplierName.setText("");
        supplierPhone.setText("");
        quantityTextView.setText("");
    }

    public void increment(View view) {
        quantity++;
        displayQuantity();
    }

    public void decrement(View view) {
        if (quantity == 0) {
            Toast.makeText(this, R.string.no_less_quantity, Toast.LENGTH_SHORT).show();
        } else {
            quantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        quantityTextView.setText(String.valueOf(quantity));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom())) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
}
