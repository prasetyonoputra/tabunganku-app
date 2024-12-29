package com.kurupuxx.tabunganku.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kurupuxx.tabunganku.R;
import com.kurupuxx.tabunganku.helpers.DatabaseHelper;
import com.kurupuxx.tabunganku.models.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<String> categoryList;
    private DatabaseHelper dbHelper;
    private ListView listViewTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        initViews();
        setupWindowInsets();
        initializeData();

        findViewById(R.id.button).setOnClickListener(this::showPopupWindow);

        // Menampilkan semua transaksi di awal
        displayTransactions();
    }

    private void initViews() {
        dbHelper = DatabaseHelper.getInstance(this);
        listViewTransactions = findViewById(R.id.listViewTransactions);

        dbHelper.getWritableDatabase();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeData() {
        categoryList = new ArrayList<>();
        categoryList.add("Tabungan");
        categoryList.add("Makanan");
        categoryList.add("Lain-lain");
    }

    private void showPopupWindow(View view) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_window, null);

        PopupWindow popupWindow = createPopupWindow(popupView);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        setupPopupContent(popupView, popupWindow);
    }

    private PopupWindow createPopupWindow(View popupView) {
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        boolean focusable = true;
        return new PopupWindow(popupView, width, height, focusable);
    }

    private void setupPopupContent(View popupView, PopupWindow popupWindow) {
        Spinner spinnerCategory = popupView.findViewById(R.id.spinnerCategory);
        EditText amountInput = popupView.findViewById(R.id.amount_input);

        setupCategorySpinner(spinnerCategory);

        popupView.findViewById(R.id.button2).setOnClickListener(v -> handlePopupSubmit(spinnerCategory, amountInput, popupWindow));
    }

    private void setupCategorySpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void handlePopupSubmit(Spinner spinnerCategory, EditText amountInput, PopupWindow popupWindow) {
        try {
            String selectedCategory = Objects.requireNonNull(spinnerCategory.getSelectedItem()).toString();
            int amount = parseAmount(amountInput);

            Transaction newTransaction = new Transaction(0, selectedCategory, amount, String.valueOf(new Date().getTime()));

            long result = dbHelper.insertTransaction(newTransaction);

            if (result != -1) {
                showToast("Berhasil menambahkan transaksi!");
                displayTransactions(); // Refresh ListView setelah menambahkan transaksi baru
            } else {
                throw new Exception("Gagal menambahkan transaksi!");
            }
        } catch (Exception e) {
            showToast(e.getLocalizedMessage());
        } finally {
            popupWindow.dismiss();
        }
    }

    private int parseAmount(EditText amountInput) throws NumberFormatException {
        String amountString = amountInput.getText().toString().trim();
        if (amountString.isEmpty()) {
            throw new NumberFormatException("Jumlah tidak boleh kosong");
        }
        return Integer.parseInt(amountString);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Menampilkan semua transaksi di ListView
    private void displayTransactions() {
        List<Transaction> transactions = dbHelper.getAllTransactions();

        ArrayList<String> transactionDetails = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionDetails.add(transaction.getCategory() + " - Rp " + transaction.getAmount());
        }

        ArrayAdapter<String> transactionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactionDetails);
        listViewTransactions.setAdapter(transactionAdapter);
    }
}
