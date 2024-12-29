package com.kurupuxx.tabunganku.models;

public class Transaction {
    private final int id;
    private String category;
    private int amount;
    private final String createdDate;

    public Transaction(int id, String category, int amount, String createdDate) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
