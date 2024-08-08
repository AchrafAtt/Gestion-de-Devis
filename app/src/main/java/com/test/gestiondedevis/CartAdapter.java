package com.test.gestiondedevis;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private CartManager cartManager;
    private Context context;
    private CartUpdateListener cartUpdateListener;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartManager cartManager, CartUpdateListener cartUpdateListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartManager = cartManager;
        this.cartUpdateListener = cartUpdateListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.productName.setText(cartItem.getProduct().getName());
        holder.productQuantity.setText(String.format("Quantity: %d", cartItem.getQuantity()));
        holder.productPrice.setText(String.format("Price: %.2f", cartItem.getTotalPrice()));
        holder.productPriceWithTax.setText(String.format("Price with tax: %.2f", cartItem.getTotalPriceWithTax()));

        holder.removeFromCartButton.setOnClickListener(view -> {
            if (cartItem.getQuantity() == 1) {
                cartManager.removeFromCart(cartItem.getProduct());
            } else {
                cartManager.removeFromCart(cartItem.getProduct());
            }
            cartUpdateListener.onCartUpdated();
            if (cartManager.getTotalPrice() == 0) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }

        });

        // Add a click listener to the remove button if the quantity is equal to 0


    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        TextView productName, productQuantity, productPrice, productPriceWithTax;
        Button removeFromCartButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productPrice = itemView.findViewById(R.id.productPrice);
            productPriceWithTax = itemView.findViewById(R.id.productPriceWithTax);
            removeFromCartButton = itemView.findViewById(R.id.removeFromCartButton);
        }
    }
}
