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
import com.kurupuxx.tabunganku.services.TransactionService;
import com.kurupuxx.tabunganku.models.Transaction;
import com.kurupuxx.tabunganku.adapters.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<String> categoryList;
    private TransactionService dbHelper;
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
        displayTransactions();
    }

    private void initViews() {
        dbHelper = TransactionService.getInstance(this);
        listViewTransactions = findViewById(R.id.listViewTransactions);
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
        return new PopupWindow(popupView, width, height, true);
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
            Transaction newTransaction = new Transaction(0, selectedCategory, amount, String.valueOf(System.currentTimeMillis()));

            long result = dbHelper.insertTransaction(newTransaction);

            if (result != -1) {
                showToast("Berhasil menambahkan transaksi!");
                displayTransactions();
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

    private void displayTransactions() {
        List<Transaction> transactions = dbHelper.getAllTransactions();
        TransactionAdapter transactionAdapter = new TransactionAdapter(this, transactions);
        listViewTransactions.setAdapter(transactionAdapter);
    }
}
