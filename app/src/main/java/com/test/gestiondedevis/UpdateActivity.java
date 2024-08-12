package com.test.gestiondedevis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Calendar;
import java.util.Date;

public class UpdateActivity extends AppCompatActivity {

    Spinner updateCategory;
    DatePicker updateDate;
    RadioGroup updateCondition;
    ImageView updateImage;
    EditText updateName, updateDescription, updatePrice;
    String key, oldImageUrl, imageUrl;
    Uri uri;
    DatabaseReference reference;
    StorageReference storageReference;
    Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateImage = findViewById(R.id.updateImage);
        updateName = findViewById(R.id.updateTopic);
        updateDescription = findViewById(R.id.updateDesc);
        updatePrice = findViewById(R.id.updatePrice);
        updateCondition = findViewById(R.id.updateCondition);
        updateCategory = findViewById(R.id.updateCategory);
        updateDate = findViewById(R.id.updateDate);
        updateButton = findViewById(R.id.updateButton);

        // Populate Spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        updateCategory.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Glide.with(UpdateActivity.this).load(bundle.getString("image")).into(updateImage);
            updateName.setText(bundle.getString("name"));
            updateDescription.setText(bundle.getString("description"));
            updatePrice.setText(String.valueOf(bundle.getDouble("price")));
            key = bundle.getString("key");
            oldImageUrl = bundle.getString("image");
        }

        reference = FirebaseDatabase.getInstance().getReference("Produits").child(key);

        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    uri = data.getData();
                    updateImage.setImageURI(uri);
                }
            });

    private void saveData() {
        String name = updateName.getText().toString().trim();
        String description = updateDescription.getText().toString().trim();
        String priceStr = updatePrice.getText().toString().trim();
        String category = updateCategory.getSelectedItem().toString();
        int selectedConditionId = updateCondition.getCheckedRadioButtonId();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || selectedConditionId == -1) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        RadioButton selectedCondition = findViewById(selectedConditionId);
        String condition = selectedCondition.getText().toString();

        int day = updateDate.getDayOfMonth();
        int month = updateDate.getMonth();
        int year = updateDate.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (uri != null) {
            storageReference = FirebaseStorage.getInstance().getReference().child("Android Images").child(uri.getLastPathSegment());
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            updateData(name, description, price, condition, category, date, imageUrl);
                            dialog.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(UpdateActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateData(name, description, price, condition, category, date, oldImageUrl);
            dialog.dismiss();
        }
    }

    private void updateData(String name, String description, double price, String condition, String category, Date date, String imageUrl) {
        DataClass data = new DataClass(name, description, price, imageUrl, condition, category, date);
        reference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateActivity.this, "Produit mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UpdateActivity.this, "Erreur lors de la mise à jour du produit", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
