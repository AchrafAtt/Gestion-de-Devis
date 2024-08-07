package com.test.gestiondedevis;

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
    private OnCartEmptyListener onCartEmptyListener;

    public interface OnCartEmptyListener {
        void onCartEmpty();
    }

    public CartAdapter(List<CartItem> cartItems, CartManager cartManager, OnCartEmptyListener onCartEmptyListener) {
        this.cartItems = cartItems;
        this.cartManager = cartManager;
        this.onCartEmptyListener = onCartEmptyListener;
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
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                cartManager.removeFromCart(cartItem.getProduct());
                cartItems.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, cartItems.size());

                if (cartItems.isEmpty() && onCartEmptyListener != null) {
                    onCartEmptyListener.onCartEmpty();
                }
            }
        });
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
