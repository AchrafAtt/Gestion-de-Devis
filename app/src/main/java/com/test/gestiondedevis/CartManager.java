package com.test.gestiondedevis;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private List<CartItem> cartItems;
    private double taxRate = 0.15; // Example tax rate of 15%

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(DataClass product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                updatePrices(item);
                return;
            }
        }
        CartItem newItem = new CartItem(product, 1, product.getPrice(), calculatePriceWithTax(product.getPrice()));
        cartItems.add(newItem);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : cartItems) {
            totalPrice += item.getTotalPrice();
        }
        return totalPrice;
    }

    public double getTotalPriceWithTax() {
        double totalPriceWithTax = 0;
        for (CartItem item : cartItems) {
            totalPriceWithTax += item.getTotalPriceWithTax();
        }
        return totalPriceWithTax;
    }

    public void removeFromCart(DataClass product) {
        CartItem itemToRemove = null;
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() - 1);
                updatePrices(item);
                if (item.getQuantity() == 0) {
                    itemToRemove = item;
                }
                break;
            }
        }
        if (itemToRemove != null) {
            cartItems.remove(itemToRemove);
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    private void updatePrices(CartItem item) {
        double price = item.getProduct().getPrice() * item.getQuantity();
        item.setTotalPrice(price);
        item.setTotalPriceWithTax(calculatePriceWithTax(price));
    }

    private double calculatePriceWithTax(double price) {
        return price * (1 + taxRate);
    }
}
