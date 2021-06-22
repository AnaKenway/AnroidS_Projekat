package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.traveller.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private static final String FIREBASE_CHILD ="users";
    private String imgURI=null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    String storageImgPath;
    //pogledaj ovde ako ne radi
    FirebaseStorage storage=FirebaseStorage.getInstance();

    // Create a Cloud Storage reference from the app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        LinearLayout loginLayout=findViewById(R.id.lLogin);
        LinearLayout registerLayout=findViewById(R.id.lRegister);

        Intent i= getIntent();
        boolean isLogin=i.getBooleanExtra("isLogin",true);

        if(isLogin){
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.GONE);

            Button loginButton=(Button)findViewById(R.id.cirLoginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final EditText usernameEditText = findViewById(R.id.editTextLoginUsername);
                    final EditText passwordEditText = findViewById(R.id.editTextLoginPassword);
                    String userName=usernameEditText.getText().toString();
                    String password=passwordEditText.getText().toString();

                    myRef.child("users").child(userName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.getResult().exists()) {
                                Toast.makeText(getApplicationContext(),"Couldn't login",Toast.LENGTH_LONG).show();
                            }
                            else {
                                Object u=task.getResult().getValue();
                                HashMap<String,String> hm=(HashMap<String, String>) u;
                                signIn(hm.get("email"), password, userName);
                            }
                        }
                    });
                }
            });

        }

        else{
            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);

            ImageView camera=(ImageView)findViewById(R.id.imgViewTakeAPhoto);
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

            Button register=(Button) findViewById(R.id.cirRegisterButton);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText etUsername=(EditText) findViewById(R.id.editTextUsername);
                    EditText etEmail=(EditText) findViewById(R.id.editTextEmail);
                    EditText etPassword=(EditText) findViewById(R.id.editTextPassword);
                    EditText etFirstName=(EditText) findViewById(R.id.editTextFirstName);
                    EditText etLastName=(EditText) findViewById(R.id.editTextLastName);
                    EditText etPhoneNumber=(EditText) findViewById(R.id.editTextPhoneNumber);

                    String username=etUsername.getText().toString();
                    String password=etPassword.getText().toString();
                    String email=etEmail.getText().toString();



                    User user=new User(etPassword.getText().toString(),
                            etEmail.getText().toString(),etFirstName.getText().toString(),etLastName.getText().toString(),
                            imgURI,etPhoneNumber.getText().toString(),"0");
                    user.friends.add("Ema");
                    user.friends.add("Marjan");
                    myRef.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.getResult().exists()) {
                                mAuth.createUserWithEmailAndPassword(email,password)
                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()) {
                                                    myRef.child("users").child(username).setValue(user);
                                                    Toast.makeText(getApplicationContext(),"Successfully registered!",Toast. LENGTH_LONG).show();
                                                    Intent i=new Intent(LoginActivity.this,UsersMapsActivity.class);
                                                    i.putExtra("Username", username);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener(){
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, e.getLocalizedMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"User with that username already exists!",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }


    private void reload() { }

    private void signIn(String email, String password, String username){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(LoginActivity.this,UsersMapsActivity.class);
                            i.putExtra("Username", username);
                            startActivity(i);
                            finish();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            // Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication failed.",
                                   // Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
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
                uploadTask = storageRef.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(LoginActivity.this, "Couldn't upload image to cloud",
                                Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        /*storageRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Couldn't get image URL from Firebase Cloud",
                                        Toast.LENGTH_LONG).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imgURI=uri.toString();
                            }
                        });*/
                        //ukoliko nam treba nesto drugo kao img URI, vratiti se i ppogledati
                        //================OVDE===================   
                        imgURI=taskSnapshot.getMetadata().getPath();
                    }
                });
            }catch(Exception ex){
                Log.e("MYAPP", "exception", ex);
            }

        }
    }

}