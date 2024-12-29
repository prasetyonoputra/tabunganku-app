package com.kurupuxx.tabunganku.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kurupuxx.tabunganku.R;
import com.kurupuxx.tabunganku.services.TransactionService;
import com.kurupuxx.tabunganku.models.Transaction;

import java.util.List;

public class TransactionAdapter extends BaseAdapter {
    private final Context context;
    private final List<Transaction> transactions;
    private final TransactionService dbHelper;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.dbHelper = TransactionService.getInstance(context);
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Object getItem(int position) {
        return transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView transactionDetails;
        ImageButton deleteButton;
        ImageButton updateButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.transactionDetails = convertView.findViewById(R.id.transactionDetails);
            viewHolder.deleteButton = convertView.findViewById(R.id.buttonDelete);
            viewHolder.updateButton = convertView.findViewById(R.id.buttonUpdate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = transactions.get(position);
        viewHolder.transactionDetails.setText(transaction.getCategory() + " - Rp " + transaction.getAmount());

        viewHolder.deleteButton.setOnClickListener(v -> deleteTransaction(transaction, position));
        viewHolder.updateButton.setOnClickListener(v -> showUpdatePopup(transaction));

        return convertView;
    }

    private void deleteTransaction(Transaction transaction, int position) {
        boolean isDeleted = dbHelper.deleteTransaction(transaction.getId());
        if (isDeleted) {
            transactions.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdatePopup(Transaction transaction) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_transaction, null);
        EditText categoryEditText = view.findViewById(R.id.categoryEditText);
        EditText amountEditText = view.findViewById(R.id.amountEditText);

        categoryEditText.setText(transaction.getCategory());
        amountEditText.setText(String.valueOf(transaction.getAmount()));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Transaction")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newCategory = categoryEditText.getText().toString().trim();
                    String amountString = amountEditText.getText().toString().trim();

                    if (newCategory.isEmpty() || amountString.isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            int newAmount = Integer.parseInt(amountString);
                            transaction.setCategory(newCategory);
                            transaction.setAmount(newAmount);

                            boolean isUpdated = dbHelper.updateTransaction(transaction);
                            if (isUpdated) {
                                notifyDataSetChanged();
                                Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
