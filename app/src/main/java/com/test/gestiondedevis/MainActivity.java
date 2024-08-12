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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

        // Initialisation des vues
        fab = findViewById(R.id.fab);
        viewCartButton = findViewById(R.id.viewCartButton);
        recyclerView = findViewById(R.id.recyclerView);
        sortCriteriaSpinner = findViewById(R.id.sortCriteriaSpinner);
        sortOrderGroup = findViewById(R.id.sortOrderGroup);

        // Configuration du RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Configuration de la boîte de dialogue de chargement
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
        sortCriteriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCriteria = parent.getItemAtPosition(position).toString();
                boolean ascendingOrder = sortOrderGroup.getCheckedRadioButtonId() == R.id.ascendingOrder;
                saveSortPreferences(selectedCriteria, ascendingOrder);
                sortProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sortOrderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedCriteria = sortCriteriaSpinner.getSelectedItem().toString();
            boolean ascendingOrder = checkedId == R.id.ascendingOrder;
            saveSortPreferences(selectedCriteria, ascendingOrder);
            sortProducts();
        });

        // Charger et appliquer les préférences de tri
        loadSortPreferences();
    }

    // Fonction pour charger les préférences de tri
    private void loadSortPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortCriteria = sharedPreferences.getString("sort_criteria", "Nom"); // Par défaut, tri par nom
        boolean ascendingOrder = sharedPreferences.getBoolean("sort_order", true); // Par défaut, ordre ascendant

        // Appliquer les préférences à l'interface utilisateur
        if (sortCriteria.equals("Prix")) {
            sortCriteriaSpinner.setSelection(1); // Suppose que l'index 1 correspond au tri par prix
        } else {
            sortCriteriaSpinner.setSelection(0); // Suppose que l'index 0 correspond au tri par nom
        }

        if (ascendingOrder) {
            sortOrderGroup.check(R.id.ascendingOrder);
        } else {
            sortOrderGroup.check(R.id.descendingOrder);
        }

        // Appliquer le tri aux produits
        sortProducts();
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

    // Fonction pour sauvegarder les préférences de tri
    private void saveSortPreferences(String sortCriteria, boolean ascendingOrder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sort_criteria", sortCriteria);
        editor.putBoolean("sort_order", ascendingOrder);
        editor.apply(); // Applique les changements de manière asynchrone
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}

