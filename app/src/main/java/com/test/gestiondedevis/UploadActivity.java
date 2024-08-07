package com.test.gestiondedevis;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadActivity extends AppCompatActivity {

    private EditText uploadTopic, uploadDesc, uploadPrice;
    private Spinner uploadCategory;
    private DatePicker uploadDate;
    private RadioGroup uploadCondition;
    private Button saveButton;
    private ImageView uploadImage;
    private String imageURL;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadTopic = findViewById(R.id.uploadTopic);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadPrice = findViewById(R.id.uploadPrice);
        uploadCategory = findViewById(R.id.uploadCategory);
        uploadDate = findViewById(R.id.uploadDate);
        uploadCondition = findViewById(R.id.uploadCondition);
        saveButton = findViewById(R.id.saveButton);
        uploadImage = findViewById(R.id.uploadImage);

        // Populate Spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uploadCategory.setAdapter(adapter);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadImage.setImageURI(uri);
                        } else {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(photoPicker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSave();
            }
        });
    }

    private void validateAndSave() {
        String topic = uploadTopic.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String price = uploadPrice.getText().toString().trim();
        String selectedCategory = uploadCategory.getSelectedItem().toString();
        int selectedConditionId = uploadCondition.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(topic)) {
            uploadTopic.setError("Nom du produit est requis");
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            uploadDesc.setError("Description est requise");
            return;
        }

        if (TextUtils.isEmpty(price)) {
            uploadPrice.setError("Prix est requis");
            return;
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue < 0) {
                uploadPrice.setError("Prix doit être positif");
                return;
            }
        } catch (NumberFormatException e) {
            uploadPrice.setError("Prix invalide");
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedConditionId == -1) {
            Toast.makeText(this, "Veuillez sélectionner la condition du produit", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedCondition = findViewById(selectedConditionId);
        String condition = selectedCondition.getText().toString();

        int day = uploadDate.getDayOfMonth();
        int month = uploadDate.getMonth();
        int year = uploadDate.getYear();

        String dateStr = day + "/" + (month + 1) + "/" + year;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Erreur de format de date", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                .child(uri.getLastPathSegment());
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageURL = uri.toString();
                        uploadData(topic, desc, Double.parseDouble(price), condition, selectedCategory, date);
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(UploadActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(UploadActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(String topic, String desc, Double price, String condition, String category, Date date) {
        DataClass data = new DataClass(topic, desc, price, imageURL, condition, category, date);

        FirebaseDatabase.getInstance().getReference("Produits").push()
                .setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UploadActivity.this, "Produit sauvegardé avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(UploadActivity.this, "Erreur lors de la sauvegarde du produit", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
