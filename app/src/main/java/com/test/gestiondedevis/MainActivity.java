package com.test.gestiondedevis;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab, viewCartButton, switchLang;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    DatabaseReference databaseReference;
    MyAdapter myAdapter;
    AlertDialog dialog;
    ValueEventListener valueEventListener;
    Spinner sortCriteriaSpinner;
    RadioGroup sortOrderGroup;
    Button addToCartButton;  // Reference to your button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        fab = findViewById(R.id.fab);
        viewCartButton = findViewById(R.id.viewCartButton);
        switchLang = findViewById(R.id.switchLang);
        recyclerView = findViewById(R.id.recyclerView);
        sortCriteriaSpinner = findViewById(R.id.sortCriteriaSpinner);
        sortOrderGroup = findViewById(R.id.sortOrderGroup);


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up loading dialog
        dialog = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(R.layout.progress_layout)
                .create();
        dialog.show();

        // Initialize data list and adapter
        dataList = new ArrayList<>();
        myAdapter = new MyAdapter(MainActivity.this, dataList, CartManager.getInstance());
        recyclerView.setAdapter(myAdapter);

        // Set up Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Produits");
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
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
                sortProducts();  // Sort products after retrieval
                myAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
            }
        });

        // Set up click listeners
        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, UploadActivity.class));
        });

        viewCartButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        });

        // Language switch button
        switchLang.setOnClickListener(view -> {
            String currentLang = Locale.getDefault().getLanguage();
            setLocale(currentLang.equals("fr") ? "en" : "fr");
        });

        // Sort criteria and order listeners
        sortCriteriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSorting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sortOrderGroup.setOnCheckedChangeListener((group, checkedId) -> updateSorting());

        // Load and apply sorting preferences
        loadSortPreferences();
    }

    private void updateSorting() {
        String selectedCriteria = sortCriteriaSpinner.getSelectedItem().toString();
        boolean ascendingOrder = sortOrderGroup.getCheckedRadioButtonId() == R.id.ascendingOrder;
        saveSortPreferences(selectedCriteria, ascendingOrder);
        sortProducts();
    }

    // Function to change the locale
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Manually update UI elements
        myAdapter.updateLanguage();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // Update UI elements with the correct language strings
    private void updateUIElements() {
        addToCartButton.setText(getString(R.string.add_to_cart));
        // Update other UI elements here if needed
    }



    private void loadSortPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortCriteria = sharedPreferences.getString("sort_criteria", "Nom");
        boolean ascendingOrder = sharedPreferences.getBoolean("sort_order", true);

        sortCriteriaSpinner.setSelection(sortCriteria.equals("Prix") ? 1 : 0);
        sortOrderGroup.check(ascendingOrder ? R.id.ascendingOrder : R.id.descendingOrder);
        sortProducts();
    }

    private void sortProducts() {
        String selectedCriteria = sortCriteriaSpinner.getSelectedItem().toString();
        boolean isAscending = sortOrderGroup.getCheckedRadioButtonId() == R.id.ascendingOrder;

        Comparator<DataClass> comparator = selectedCriteria.equals("Prix")
                ? Comparator.comparing(DataClass::getPrice)
                : Comparator.comparing(DataClass::getName);

        if (!isAscending) comparator = comparator.reversed();

        Collections.sort(dataList, comparator);
        myAdapter.notifyDataSetChanged();
    }

    private void saveSortPreferences(String sortCriteria, boolean ascendingOrder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString("sort_criteria", sortCriteria)
                .putBoolean("sort_order", ascendingOrder)
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}

