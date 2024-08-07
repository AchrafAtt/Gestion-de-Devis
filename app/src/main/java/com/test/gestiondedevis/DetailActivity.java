package com.test.gestiondedevis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    TextView name, description, price;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        name = findViewById(R.id.productName);
        description = findViewById(R.id.productDescription);
        price = findViewById(R.id.productPrice);
        image = findViewById(R.id.productImage);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            name.setText(bundle.getString("name"));
            description.setText(bundle.getString("description"));
            price.setText(bundle.getString("price"));
            Glide.with(this).load(bundle.getString("image")).into(image);
        }
    }
}