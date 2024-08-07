package com.test.gestiondedevis;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartEmptyListener {

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
        cartAdapter = new CartAdapter(cartItems, cartManager, this);
        cartRecyclerView.setAdapter(cartAdapter);

        updateTotalPrices();
    }

    private void updateTotalPrices() {
        double totalWithoutTaxValue = 0;
        double totalWithTaxValue = 0;

        for (CartItem item : cartManager.getCartItems()) {
            totalWithoutTaxValue += item.getTotalPrice();
            totalWithTaxValue += item.getTotalPriceWithTax();
        }

        totalWithoutTax.setText(String.format("Total without tax: %.2f", totalWithoutTaxValue));
        totalWithTax.setText(String.format("Total with tax: %.2f", totalWithTaxValue));
    }

    @Override
    public void onCartEmpty() {
        finish(); // Close CartActivity and return to MainActivity
    }
}
