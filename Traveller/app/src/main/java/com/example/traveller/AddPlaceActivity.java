package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.traveller.models.CoffeeShop;
import com.example.traveller.models.Doctor;
import com.example.traveller.models.Monument;
import com.example.traveller.models.Restaurant;
import com.example.traveller.models.TravelAgency;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPlaceActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String userName;
    //private LatLng loc;
    private boolean isMonument=false;
    private boolean isDoctor=false;
    private boolean isCoffeeShop=false;
    private boolean isRestaurant=false;
    private boolean isTravelAgency=false;
    private boolean isCameraOpened = false;

    String imgUrl,type;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    String storageImgPath;
    //pogledaj ovde ako ne radi
    FirebaseStorage storage=FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent i=getIntent();
        type=i.getStringExtra("type");
        userName=i.getStringExtra("userName");
        double lat=i.getDoubleExtra("latitude",0.0);
        double lon=i.getDoubleExtra("longitude",0.0);


        //"Monument", "Coffee Shop", "Doctor", "Restaurant","Travel Agency"
        switch (type) {
            case "Monument":
                setContentView(R.layout.activity_add_monument);
                isMonument=true;
                type="monuments";
                break;
            case "Coffee Shop":
                setContentView(R.layout.layout_add_coffee_shop);
                isCoffeeShop=true;
                type="coffee_shops";
                break;
            case "Doctor":
                setContentView(R.layout.layout_add_doctor);
                isDoctor=true;
                type="doctors";
                break;
            case "Restaurant":
                setContentView(R.layout.layout_add_restaurant);
                isRestaurant=true;
                type="restaurants";
                break;
            case "Travel Agency":
                setContentView(R.layout.layout_add_travel_agency);
                isTravelAgency=true;
                type="travel_agencies";
                break;
        }

        EditText etPlaceName=findViewById(R.id.editTextPlaceName);
        EditText etPlaceDesc=findViewById(R.id.editTextPlaceDesc);
        EditText etPlaceAddress=findViewById(R.id.editTextPlaceAdress);
        ImageButton imgBtn=findViewById(R.id.imgBtnCamera);
        Button btnSave=findViewById(R.id.btnAddPlace);
        Button btnCancel=findViewById(R.id.btnCancelPlace);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCameraOpened)
                {
                    while (imgUrl == null) {}
                }
                else
                {
                    Toast.makeText(AddPlaceActivity.this, "Please, take a piucture of place before adding.", Toast.LENGTH_LONG).show();
                    return;
                }
                String placeName = etPlaceName.getText().toString();
                String placeAddress = etPlaceAddress.getText().toString();
                String placeDesc = etPlaceDesc.getText().toString();

                if(isMonument){
                    EditText etMonumentAge=findViewById(R.id.editTextMonumentAge);
                    String monumentAge = etMonumentAge.getText().toString();

                    Monument monument = new Monument(placeName, placeDesc, placeAddress, Double.toString(lat), Double.toString(lon), (float)0.0, monumentAge, imgUrl);
                    myRef.child("places").child("monuments").child(placeName).setValue(monument).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPlaceActivity.this, "Place Successfully added!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }else if(isCoffeeShop){
                    EditText etMobileNumber = findViewById(R.id.editTextCoffeShopPhone);
                    EditText etVisitingHours = findViewById(R.id.editTextVisitingHours);
                    Switch pet = findViewById(R.id.switchPetFriendly);
                    Switch wifi = findViewById(R.id.switchHasWifi);
                    Switch cardPay = findViewById(R.id.switchCardPay);

                    String mobileNumber = etMobileNumber.getText().toString();
                    String visitingHours = etVisitingHours.getText().toString();

                    CoffeeShop cs = new CoffeeShop(placeName, placeDesc, placeAddress, Double.toString(lat), Double.toString(lon), (float)0.0, imgUrl, visitingHours, pet.isChecked(), mobileNumber, wifi.isChecked(), cardPay.isChecked());
                    myRef.child("places").child("coffee_shops").child(placeName).setValue(cs).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPlaceActivity.this, "Place Successfully added!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else if(isRestaurant){
                    EditText etMobileNumber = findViewById(R.id.editTextRestaurantPhone);
                    EditText etVisitingHours = findViewById(R.id.editTextVisitingHours);
                    EditText etCuisine = findViewById(R.id.editTextCuisine);

                    Switch pet = findViewById(R.id.switchPetFriendly);
                    Switch wifi = findViewById(R.id.switchHasWifi);
                    Switch cardPay = findViewById(R.id.switchCardPay);

                    String mobileNumber = etMobileNumber.getText().toString();
                    String visitingHours = etVisitingHours.getText().toString();
                    String cuisine = etCuisine.getText().toString();

                    Restaurant r = new Restaurant(placeName, placeDesc, placeAddress, Double.toString(lat), Double.toString(lon), (float)0.0, imgUrl, visitingHours, cuisine, pet.isChecked(), mobileNumber, wifi.isChecked(), cardPay.isChecked());
                    myRef.child("places").child("restaurants").child(placeName).setValue(r).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPlaceActivity.this, "Place Successfully added!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }else if(isDoctor){
                    EditText etMobileNumber = findViewById(R.id.editTextDoctorPhoneNumber);
                    EditText etVisitingHours = findViewById(R.id.editTextDoctorWorkingHours);
                    EditText etField = findViewById(R.id.editTextDoctorFieldOfWork);

                    String mobileNumber = etMobileNumber.getText().toString();
                    String visitingHours = etVisitingHours.getText().toString();
                    String field = etField.getText().toString();

                    Doctor d = new Doctor(placeName,placeDesc,placeAddress,Double.toString(lat), Double.toString(lon), (float)0.0, imgUrl, field ,visitingHours, mobileNumber);
                    myRef.child("places").child("doctors").child(placeName).setValue(d).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPlaceActivity.this, "Place Successfully added!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else{
                    //travel agency
                    EditText etMobileNumber = findViewById(R.id.editTextTravelAgencyPhoneNumber);
                    EditText etVisitingHours = findViewById(R.id.editTextTravelAgencyVisitingHours);

                    String mobileNumber = etMobileNumber.getText().toString();
                    String visitingHours = etVisitingHours.getText().toString();

                    TravelAgency ta = new TravelAgency(placeName,placeDesc,placeAddress, Double.toString(lat), Double.toString(lon), (float)0.0, imgUrl, visitingHours, mobileNumber);
                    myRef.child("places").child("travel_agencies").child(placeName).setValue(ta).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddPlaceActivity.this, "Place Successfully added!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }

            }
        });

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                isCameraOpened = true;
            }
        });

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("MYAPP", "exception", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch(Exception e){
                    Log.e("MYAPP", "exception", e);
                }

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        storageImgPath=imageFileName;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //kod koji treba da se izvrsi kad se slika
            galleryAddPic();
            try {
                InputStream stream = new FileInputStream(new File(currentPhotoPath));
                StorageReference storageRef = storage.getReference("places/"+type+"/"+storageImgPath);
                UploadTask uploadTask;
                uploadTask = storageRef.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(AddPlaceActivity.this, "Couldn't upload image to cloud",
                                Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imgUrl=taskSnapshot.getMetadata().getPath();
                    }
                });
            }catch(Exception ex){
                Log.e("MYAPP", "exception", ex);
            }

        }
    }
}