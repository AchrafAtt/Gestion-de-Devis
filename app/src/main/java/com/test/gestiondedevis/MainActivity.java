package com.test.gestiondedevis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab, viewCartButton;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    DatabaseReference databaseReference;
    MyAdapter myAdapter;
    AlertDialog dialog;
    ValueEventListener valueEventListener;
    Spinner sortCriteriaSpinner;
    RadioGroup sortOrderGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        viewCartButton = findViewById(R.id.viewCartButton);
        recyclerView = findViewById(R.id.recyclerView);
        sortCriteriaSpinner = findViewById(R.id.sortCriteriaSpinner);  // Assurez-vous que c'est bien l'ID dans votre XML
        sortOrderGroup = findViewById(R.id.sortOrderGroup);  // Assurez-vous que c'est bien l'ID dans votre XML

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();
        myAdapter = new MyAdapter(MainActivity.this, dataList, CartManager.getInstance());
        recyclerView.setAdapter(myAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Produits");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataClass dataClass = snapshot.getValue(DataClass.class);
                    if (dataClass != null) {
                        dataClass.setKey(snapshot.getKey());
                        dataList.add(dataClass);
                    }
                }
                sortProducts();  // Tri les produits après récupération
                myAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        viewCartButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Ajoutez ces écouteurs pour déclencher le tri lorsque l'utilisateur change le critère ou l'ordre
        sortCriteriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sortOrderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            sortProducts();
        });
    }

    private void sortProducts() {
        String selectedCriteria = sortCriteriaSpinner.getSelectedItem().toString();
        int selectedOrderId = sortOrderGroup.getCheckedRadioButtonId();
        boolean isAscending = (selectedOrderId == R.id.ascendingOrder);

        Comparator<DataClass> comparator = null;
        switch (selectedCriteria) {
            case "Nom":
                comparator = Comparator.comparing(DataClass::getName);
                break;
            case "Prix":
                comparator = Comparator.comparing(DataClass::getPrice);
                break;
        }

        if (comparator != null) {
            if (!isAscending) {
                comparator = comparator.reversed();
            }
            Collections.sort(dataList, comparator);
        }

        myAdapter.notifyDataSetChanged();  // Mise à jour de l'adaptateur après tri
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}

