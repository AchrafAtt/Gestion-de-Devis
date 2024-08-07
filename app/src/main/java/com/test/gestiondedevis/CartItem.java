package com.test.gestiondedevis;

public class CartItem {
    private DataClass product;
    private int quantity;
    private double totalPrice;
    private double totalPriceWithTax;

    public CartItem(DataClass product, int quantity, double totalPrice, double totalPriceWithTax) {
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.totalPriceWithTax = totalPriceWithTax;
    }

    // Getters and Setters

    public DataClass getProduct() {
        return product;
    }

    public void setProduct(DataClass product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getTotalPriceWithTax() {
        return totalPriceWithTax;
    }

    public void setTotalPriceWithTax(double totalPriceWithTax) {
        this.totalPriceWithTax = totalPriceWithTax;
    }
}

