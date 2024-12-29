package com.kurupuxx.tabunganku.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kurupuxx.tabunganku.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepository extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TabunganKu.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_TRANSACTION = "transactions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CREATED_DATE = "created_date";

    private static TransactionRepository instance;

    public static synchronized TransactionRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionRepository(context.getApplicationContext());
        }
        return instance;
    }

    private TransactionRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                COLUMN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        onCreate(db);
    }

    public long insertTransaction(Transaction transaction) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY, transaction.getCategory());
            values.put(COLUMN_AMOUNT, transaction.getAmount());
            return db.insertOrThrow(TABLE_TRANSACTION, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION + " ORDER BY " + COLUMN_CREATED_DATE + " DESC", null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_DATE))
                    );
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        }
        return transactions;
    }

    public boolean deleteTransaction(int id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            int rowsDeleted = db.delete(TABLE_TRANSACTION, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            return rowsDeleted > 0;
        }
    }

    public boolean updateTransaction(Transaction transaction) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY, transaction.getCategory());
            values.put(COLUMN_AMOUNT, transaction.getAmount());

            int rowsUpdated = db.update(TABLE_TRANSACTION, values, COLUMN_ID + " = ?", new String[]{String.valueOf(transaction.getId())});
            return rowsUpdated > 0;
        }
    }
}
