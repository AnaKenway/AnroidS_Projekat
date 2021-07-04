package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.traveller.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
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
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    String userName;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    FirebaseStorage storage=FirebaseStorage.getInstance();
    String firstName;
    String lastName;
    String phoneNumber;
    String oldImgURI;
    HashMap<String,String> hm;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String imgURI=null;
    String currentPhotoPath;
    String storageImgPath;
    private ProgressBar spinner;        //defined the progressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Button btnCancel=(Button)findViewById(R.id.btnEditProfileCancel);
        Button btnSave=(Button)findViewById(R.id.btnEditProfileSave);

        Intent i=getIntent();
        userName=i.getStringExtra("userName");

        EditText etFirstName=(EditText) findViewById(R.id.editProfileTextFirstName);
        EditText etLastName=(EditText) findViewById(R.id.editProfileTextLastName);
        EditText etPhoneNumber=(EditText) findViewById(R.id.editProfileTextPhoneNumber);

        spinner = (ProgressBar)findViewById(R.id.uploadPicProgressBar);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue();
                hm=(HashMap<String,String>) o;

                firstName=hm.get("firstName");
                lastName=hm.get("lastName");
                phoneNumber=hm.get("phoneNumber");
                oldImgURI=hm.get("imgUrl");

                etFirstName.setText(hm.get("firstName"));
                etLastName.setText(hm.get("lastName"));
                etPhoneNumber.setText(hm.get("phoneNumber"));
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!etFirstName.getText().toString().equals(""))
                    firstName=etFirstName.getText().toString();
                if(!etLastName.getText().toString().equals(""))
                    lastName=etLastName.getText().toString();
                if(!etPhoneNumber.getText().toString().equals(""))
                    phoneNumber=etPhoneNumber.getText().toString();

                DatabaseReference myRefUser=myRef.child("users").child(userName);
                myRefUser.child("firstName").setValue(firstName);
                myRefUser.child("lastName").setValue(lastName);
                myRefUser.child("phoneNumber").setValue(phoneNumber);
                if(imgURI!=null)
                {
                    myRefUser.child("imgUrl").setValue(imgURI);
                    //brisanje stare slike sa storage-a
                    StorageReference storageRef2=storage.getReference(oldImgURI);
                    storageRef2.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "Obrisana stara slika sa cloud-a",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Greska: nije obrisana stara slika sa cloud-a",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                finish();
            }
        });

        ImageView camera=findViewById(R.id.imgViewProfileTakeAPhoto);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //otvoriti kameru
                //slikati
                //snimiti sliku na serveru
                //preuzeti uri
                //i sacuvati u imgURI
                dispatchTakePictureIntent();

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
                StorageReference storageRef = storage.getReference("user_profile_pictures/"+storageImgPath);
                UploadTask uploadTask;
                spinner.setVisibility(View.VISIBLE);
                uploadTask = storageRef.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(EditProfileActivity.this, "Couldn't upload image to cloud",
                                Toast.LENGTH_LONG).show();
                        spinner.setVisibility(View.GONE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imgURI=taskSnapshot.getMetadata().getPath();
                        spinner.setVisibility(View.GONE);
                    }
                });


            }catch(Exception ex){
                Log.e("MYAPP", "exception", ex);
            }

        }
    }

}