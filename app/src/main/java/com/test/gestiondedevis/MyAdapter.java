package com.test.gestiondedevis;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<DataClass> dataList;
    private CartManager cartManager;

    public MyAdapter(Context context, List<DataClass> dataList, CartManager cartManager) {
        this.context = context;
        this.dataList = dataList;
        this.cartManager = cartManager;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataClass dataClass = dataList.get(position);

        Glide.with(context).load(dataClass.getImage()).into(holder.recImage);
        holder.recTitle.setText(dataClass.getName());
        holder.recDesc.setText(dataClass.getDescription());
        holder.recPrice.setText(String.valueOf(dataClass.getPrice()));

        // Format the date if necessary
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = dateFormat.format(dataClass.getDate());

        holder.recCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", dataClass.getName());
            intent.putExtra("description", dataClass.getDescription());
            intent.putExtra("price", dataClass.getPrice());
            intent.putExtra("image", dataClass.getImage());
            intent.putExtra("key", dataClass.getKey());
            intent.putExtra("category", dataClass.getCategory());
            intent.putExtra("condition", dataClass.getCondition());
            intent.putExtra("date", dateStr); // Pass formatted date as string

            context.startActivity(intent);
        });

        holder.addToCartButton.setOnClickListener(view -> {
            cartManager.addToCart(dataClass);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView recImage;
    TextView recTitle, recDesc, recPrice;
    CardView recCard;
    Button addToCartButton;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDesc = itemView.findViewById(R.id.recDesc);
        recPrice = itemView.findViewById(R.id.recPrice);
        recCard = itemView.findViewById(R.id.recCard);
        addToCartButton = itemView.findViewById(R.id.addToCartButton);
    }
}
