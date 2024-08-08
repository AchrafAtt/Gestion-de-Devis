package com.test.gestiondedevis;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartUpdateListener {

    RecyclerView cartRecyclerView;
    CartAdapter cartAdapter;
    CartManager cartManager;

    TextView totalWithoutTax;
    TextView totalWithTax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(); // Use singleton instance

        totalWithoutTax = findViewById(R.id.totalWithoutTax);
        totalWithTax = findViewById(R.id.totalWithTax);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CartItem> cartItems = cartManager.getCartItems();
        cartAdapter = new CartAdapter(this, cartItems, cartManager, this);
        cartRecyclerView.setAdapter(cartAdapter);

        updateTotalPrices();
    }

    private void updateTotalPrices() {
        double totalWithoutTaxValue = cartManager.getTotalPrice();
        double totalWithTaxValue = cartManager.getTotalPriceWithTax();

        totalWithoutTax.setText(String.format("Total without tax: %.2f", totalWithoutTaxValue));
        totalWithTax.setText(String.format("Total with tax: %.2f", totalWithTaxValue));
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrices();
        if (cartManager.getTotalPrice() == 0) {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cartManager.getTotalPrice() == 0) {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
