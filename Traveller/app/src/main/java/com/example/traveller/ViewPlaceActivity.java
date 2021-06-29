package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Rating;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ViewPlaceActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String userName;
    private String tag,placeName;
    private FirebaseStorage storage=FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private Bitmap bmp;
    private Long rating, raters;
    private String userPoints;
    private HashMap<String,String> visitedPlaces;
    private HashMap<String,String> favoritePlaces;
    HashMap<String,String> userHashMapPoints;
    HashMap<String,HashMap<String,String>> userHashMapPlaces;
    private boolean isFave=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i=getIntent();
        tag=i.getStringExtra("tag");
        userName=i.getStringExtra("userName");
        placeName=i.getStringExtra("placeName");

        myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                {
                    Object o=dataSnapshot.getValue();
                    userHashMapPoints=(HashMap<String,String>) o;
                    userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;
                }
            }
        });



        if(tag.equals("monument")) {
            setContentView(R.layout.layout_view_monument);

            myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)
                    {
                        Object o=dataSnapshot.getValue();
                        userHashMapPoints=(HashMap<String,String>) o;
                        userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;

                        if(userHashMapPoints.get("points")!=null)
                            userPoints=userHashMapPoints.get("points");
                        else userPoints="0";
                        if(userHashMapPlaces.get("visitedPlaces")!=null) {
                            visitedPlaces = userHashMapPlaces.get("visitedPlaces");
                            if(visitedPlaces.get(placeName)!=null){
                                Switch sw=findViewById(R.id.switchVisitedMonument);
                                sw.setChecked(true);
                            }

                            if(userHashMapPlaces.get("favoritePlaces")!=null) {
                                favoritePlaces = userHashMapPlaces.get("favoritePlaces");
                                if(favoritePlaces.get(placeName)!=null) {
                                    ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
                                    imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                                    imgHeart.setColorFilter(R.color.heartColor);
                                    //imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                                    isFave=true;

                                }
                            }
                        }
                    }
                }
            });

            TextView txtViewMonumentName = findViewById(R.id.textViewMonumentViewName);
            TextView txtViewMonumentAddress = findViewById(R.id.textViewMonumentAddress);
            TextView txtViewMonumentDesc = findViewById(R.id.textViewMonumentViewDescription);
            ImageView imgMonument = findViewById(R.id.imgViewMonumentPhoto);
            RatingBar ratingBar = findViewById(R.id.ratingBarMonument);

            myRef.child("places").child("monuments").child(placeName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    Object o=dataSnapshot.getValue();
                    HashMap<String,String> hm1=(HashMap<String, String>) o;
                    HashMap<String,Long> hmLong=(HashMap<String, Long>) o;

                    rating=hmLong.get("rating");
                    long l=rating;
                    raters=hmLong.get("numOfRaters");
                    long longRaters=raters;
                    txtViewMonumentName.setText(hm1.get("name"));
                    txtViewMonumentDesc.setText(hm1.get("description"));
                    txtViewMonumentAddress.setText(hm1.get("address"));

                    String imgUrl=hm1.get("imgUrl");
                    storageRef=storage.getReference(imgUrl);

                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 800, 600, false);
                            imgMonument.setImageBitmap(smallBitmap);
                        }
                    });

                    ratingBar.setRating((float)l/longRaters);

                    Button btnRate=findViewById(R.id.btnRateMonument);
                    btnRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float rez=ratingBar.getRating();
                            raters=raters+1;
                            rating=rating+(long)rez;
                            myRef.child("places").child("monuments").child(placeName).child("numOfRaters").setValue(raters);
                            myRef.child("places").child("monuments").child(placeName).child("rating").setValue(rating);
                            ratingBar.setRating((float)rating/raters);

                            Integer p= Integer.parseInt(userPoints);
                            p+=1;
                            userPoints=p.toString();
                            myRef.child("users").child(userName).child("points").setValue(userPoints).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    Toast.makeText(ViewPlaceActivity.this, "Thank you for rating!", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                }
            });

            Switch sw=findViewById(R.id.switchVisitedMonument);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                    else{
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

            ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFave){
                        isFave=false;
                        imgHeart.setImageResource(R.drawable.outline_favorite_border_black_48dp);
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });

                    }
                    else{
                        isFave=true;
                        imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

        }
        else if(tag.equals("coffeeShop")){

            setContentView(R.layout.activity_view_place);

            myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)
                    {
                        Object o=dataSnapshot.getValue();
                        userHashMapPoints=(HashMap<String,String>) o;
                        userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;

                        if(userHashMapPoints.get("points")!=null)
                            userPoints=userHashMapPoints.get("points");
                        else userPoints="0";
                        if(userHashMapPlaces.get("visitedPlaces")!=null) {
                            visitedPlaces = userHashMapPlaces.get("visitedPlaces");
                            if(visitedPlaces.get(placeName)!=null){
                                Switch sw=findViewById(R.id.switchVisitedMonument);
                                sw.setChecked(true);
                            }

                            if(userHashMapPlaces.get("favoritePlaces")!=null) {
                                favoritePlaces = userHashMapPlaces.get("favoritePlaces");
                                if(favoritePlaces.get(placeName)!=null) {
                                    ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
                                    imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                                    imgHeart.setColorFilter(R.color.heartColor);
                                    isFave=true;

                                }
                            }
                        }
                    }
                }
            });

            TextView txtViewMonumentName = findViewById(R.id.textViewMonumentViewName);
            TextView txtViewMonumentAddress = findViewById(R.id.textViewMonumentAddress);
            TextView txtViewMonumentDesc = findViewById(R.id.textViewMonumentViewDescription);
            ImageView imgMonument = findViewById(R.id.imgViewMonumentPhoto);
            RatingBar ratingBar = findViewById(R.id.ratingBarMonument);
            TextView txtViewVisitingHours=findViewById(R.id.textViewVisitingHours);
            TextView txtViewPhoneNumber=findViewById(R.id.textViewPhoneNumber);
            CheckedTextView cTVHasWifi=findViewById(R.id.checkedTextViewHasWifi);
            CheckedTextView cTVIsPetFriendly=findViewById(R.id.checkedTextViewIsPetFriendly);
            CheckedTextView cTVHasCardPay=findViewById(R.id.checkedTextViewHasCardPay);

            myRef.child("places").child("coffee_shops").child(placeName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    Object o=dataSnapshot.getValue();
                    HashMap<String,String> hm1=(HashMap<String, String>) o;
                    HashMap<String,Long> hmLong=(HashMap<String, Long>) o;
                    HashMap<String,Boolean> hmBoolean=(HashMap<String, Boolean>) o;

                    rating=hmLong.get("rating");
                    long l=rating;
                    raters=hmLong.get("numOfRaters");
                    long longRaters=raters;
                    txtViewMonumentName.setText(hm1.get("name"));
                    txtViewMonumentDesc.setText(hm1.get("description"));
                    txtViewMonumentAddress.setText(hm1.get("address"));
                    txtViewVisitingHours.setText(hm1.get("visitingHours"));
                    txtViewPhoneNumber.setText(hm1.get("phoneNumber"));
                    Boolean hasWifi=hmBoolean.get("hasWifi");
                    Boolean hasCardPay=hmBoolean.get("cardPay");
                    Boolean isPetFriendly=hmBoolean.get("petFriendly");

                    if(!hasWifi)
                    {
                        cTVHasWifi.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    if(!hasCardPay)
                    {
                        cTVHasCardPay.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    if(!isPetFriendly)
                    {
                        cTVIsPetFriendly.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    String imgUrl=hm1.get("imgUrl");
                    storageRef=storage.getReference(imgUrl);

                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 800, 600, false);
                            imgMonument.setImageBitmap(smallBitmap);
                        }
                    });

                    ratingBar.setRating((float)l/longRaters);

                    Button btnRate=findViewById(R.id.btnRateMonument);
                    btnRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float rez=ratingBar.getRating();
                            raters=raters+1;
                            rating=rating+(long)rez;
                            myRef.child("places").child("coffee_shops").child(placeName).child("numOfRaters").setValue(raters);
                            myRef.child("places").child("coffee_shops").child(placeName).child("rating").setValue(rating);
                            ratingBar.setRating((float)rating/raters);

                            Integer p= Integer.parseInt(userPoints);
                            p+=1;
                            userPoints=p.toString();
                            myRef.child("users").child(userName).child("points").setValue(userPoints).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    Toast.makeText(ViewPlaceActivity.this, "Thank you for rating!", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                }
            });

            Switch sw=findViewById(R.id.switchVisitedMonument);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                    else{
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

            ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFave){
                        isFave=false;
                        imgHeart.setImageResource(R.drawable.outline_favorite_border_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.black));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });

                    }
                    else{
                        isFave=true;
                        imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

        }
        else if(tag.equals("restaurant")){

            setContentView(R.layout.layout_view_restaurant);

            myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)
                    {
                        Object o=dataSnapshot.getValue();
                        userHashMapPoints=(HashMap<String,String>) o;
                        userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;

                        if(userHashMapPoints.get("points")!=null)
                            userPoints=userHashMapPoints.get("points");
                        else userPoints="0";
                        if(userHashMapPlaces.get("visitedPlaces")!=null) {
                            visitedPlaces = userHashMapPlaces.get("visitedPlaces");
                            if(visitedPlaces.get(placeName)!=null){
                                Switch sw=findViewById(R.id.switchVisitedMonument);
                                sw.setChecked(true);
                            }

                            if(userHashMapPlaces.get("favoritePlaces")!=null) {
                                favoritePlaces = userHashMapPlaces.get("favoritePlaces");
                                if(favoritePlaces.get(placeName)!=null) {
                                    ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
                                    imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                                    imgHeart.setColorFilter(R.color.heartColor);
                                    isFave=true;

                                }
                            }
                        }
                    }
                }
            });

            TextView txtViewMonumentName = findViewById(R.id.textViewMonumentViewName);
            TextView txtViewMonumentAddress = findViewById(R.id.textViewMonumentAddress);
            TextView txtViewMonumentDesc = findViewById(R.id.textViewMonumentViewDescription);
            ImageView imgMonument = findViewById(R.id.imgViewMonumentPhoto);
            RatingBar ratingBar = findViewById(R.id.ratingBarMonument);
            TextView txtViewVisitingHours=findViewById(R.id.textViewVisitingHours);
            TextView txtViewPhoneNumber=findViewById(R.id.textViewPhoneNumber);
            TextView txtViewCuisine=findViewById(R.id.textViewMonumentViewCuisine);
            CheckedTextView cTVHasWifi=findViewById(R.id.checkedTextViewHasWifi);
            CheckedTextView cTVIsPetFriendly=findViewById(R.id.checkedTextViewIsPetFriendly);
            CheckedTextView cTVHasCardPay=findViewById(R.id.checkedTextViewHasCardPay);

            myRef.child("places").child("restaurants").child(placeName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    Object o=dataSnapshot.getValue();
                    HashMap<String,String> hm1=(HashMap<String, String>) o;
                    HashMap<String,Long> hmLong=(HashMap<String, Long>) o;
                    HashMap<String,Boolean> hmBoolean=(HashMap<String, Boolean>) o;

                    rating=hmLong.get("rating");
                    long l=rating;
                    raters=hmLong.get("numOfRaters");
                    long longRaters=raters;
                    txtViewMonumentName.setText(hm1.get("name"));
                    txtViewMonumentDesc.setText(hm1.get("description"));
                    txtViewMonumentAddress.setText(hm1.get("address"));
                    txtViewVisitingHours.setText(hm1.get("visitingHours"));
                    txtViewPhoneNumber.setText(hm1.get("phoneNumber"));
                    txtViewCuisine.setText(hm1.get("cuisine"));
                    Boolean hasWifi=hmBoolean.get("hasWifi");
                    Boolean hasCardPay=hmBoolean.get("cardPay");
                    Boolean isPetFriendly=hmBoolean.get("petFriendly");

                    if(!hasWifi)
                    {
                        cTVHasWifi.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    if(!hasCardPay)
                    {
                        cTVHasCardPay.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    if(!isPetFriendly)
                    {
                        cTVIsPetFriendly.setCheckMarkDrawable(R.drawable.outline_cancel_red_48dp);
                    }

                    String imgUrl=hm1.get("imgUrl");
                    storageRef=storage.getReference(imgUrl);

                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 800, 600, false);
                            imgMonument.setImageBitmap(smallBitmap);
                        }
                    });

                    ratingBar.setRating((float)l/longRaters);

                    Button btnRate=findViewById(R.id.btnRateMonument);
                    btnRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float rez=ratingBar.getRating();
                            raters=raters+1;
                            rating=rating+(long)rez;
                            myRef.child("places").child("restaurants").child(placeName).child("numOfRaters").setValue(raters);
                            myRef.child("places").child("restaurants").child(placeName).child("rating").setValue(rating);
                            ratingBar.setRating((float)rating/raters);

                            Integer p= Integer.parseInt(userPoints);
                            p+=1;
                            userPoints=p.toString();
                            myRef.child("users").child(userName).child("points").setValue(userPoints).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    Toast.makeText(ViewPlaceActivity.this, "Thank you for rating!", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                }
            });

            Switch sw=findViewById(R.id.switchVisitedMonument);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                    else{
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

            ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFave){
                        isFave=false;
                        imgHeart.setImageResource(R.drawable.outline_favorite_border_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.black));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });

                    }
                    else{
                        isFave=true;
                        imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });
        }

        else if(tag.equals("doctor")){

            setContentView(R.layout.layout_view_doctor);

            myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)
                    {
                        Object o=dataSnapshot.getValue();
                        userHashMapPoints=(HashMap<String,String>) o;
                        userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;

                        if(userHashMapPoints.get("points")!=null)
                            userPoints=userHashMapPoints.get("points");
                        else userPoints="0";
                        if(userHashMapPlaces.get("visitedPlaces")!=null) {
                            visitedPlaces = userHashMapPlaces.get("visitedPlaces");
                            if(visitedPlaces.get(placeName)!=null){
                                Switch sw=findViewById(R.id.switchVisitedMonument);
                                sw.setChecked(true);
                            }

                            if(userHashMapPlaces.get("favoritePlaces")!=null) {
                                favoritePlaces = userHashMapPlaces.get("favoritePlaces");
                                if(favoritePlaces.get(placeName)!=null) {
                                    ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
                                    imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                                    imgHeart.setColorFilter(R.color.heartColor);
                                    //imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                                    isFave=true;

                                }
                            }
                        }
                    }
                }
            });

            TextView txtViewMonumentName = findViewById(R.id.textViewMonumentViewName);
            TextView txtViewMonumentAddress = findViewById(R.id.textViewMonumentAddress);
            TextView txtViewMonumentDesc = findViewById(R.id.textViewMonumentViewDescription);
            ImageView imgMonument = findViewById(R.id.imgViewMonumentPhoto);
            RatingBar ratingBar = findViewById(R.id.ratingBarMonument);
            TextView txtViewWorkingHours=findViewById(R.id.textViewVisitingHours);
            TextView txtViewPhoneNumber=findViewById(R.id.textViewPhoneNumber);
            TextView txtViewFieldOfWork=findViewById(R.id.textViewMonumentViewFieldOfWork);

            myRef.child("places").child("doctors").child(placeName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    Object o=dataSnapshot.getValue();
                    HashMap<String,String> hm1=(HashMap<String, String>) o;
                    HashMap<String,Long> hmLong=(HashMap<String, Long>) o;

                    rating=hmLong.get("rating");
                    long l=rating;
                    raters=hmLong.get("numOfRaters");
                    long longRaters=raters;
                    txtViewMonumentName.setText(hm1.get("name"));
                    txtViewMonumentDesc.setText(hm1.get("description"));
                    txtViewMonumentAddress.setText(hm1.get("address"));
                    txtViewWorkingHours.setText(hm1.get("workingHours"));
                    txtViewFieldOfWork.setText(hm1.get("fieldOfWork"));
                    txtViewPhoneNumber.setText(hm1.get("phoneNumber"));

                    String imgUrl=hm1.get("imgUrl");
                    storageRef=storage.getReference(imgUrl);

                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 800, 600, false);
                            imgMonument.setImageBitmap(smallBitmap);
                        }
                    });

                    ratingBar.setRating((float)l/longRaters);

                    Button btnRate=findViewById(R.id.btnRateMonument);
                    btnRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float rez=ratingBar.getRating();
                            raters=raters+1;
                            rating=rating+(long)rez;
                            myRef.child("places").child("doctors").child(placeName).child("numOfRaters").setValue(raters);
                            myRef.child("places").child("doctors").child(placeName).child("rating").setValue(rating);
                            ratingBar.setRating((float)rating/raters);

                            Integer p= Integer.parseInt(userPoints);
                            p+=1;
                            userPoints=p.toString();
                            myRef.child("users").child(userName).child("points").setValue(userPoints).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    Toast.makeText(ViewPlaceActivity.this, "Thank you for rating!", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                }
            });

            Switch sw=findViewById(R.id.switchVisitedMonument);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                    else{
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

            ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFave){
                        isFave=false;
                        imgHeart.setImageResource(R.drawable.outline_favorite_border_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.black));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });

                    }
                    else{
                        isFave=true;
                        imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

        }

        else if(tag.equals("travelAgency")){
            setContentView(R.layout.layout_view_travel_agency);

            myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)
                    {
                        Object o=dataSnapshot.getValue();
                        userHashMapPoints=(HashMap<String,String>) o;
                        userHashMapPlaces=(HashMap<String,HashMap<String,String>>)o;

                        if(userHashMapPoints.get("points")!=null)
                            userPoints=userHashMapPoints.get("points");
                        else userPoints="0";
                        if(userHashMapPlaces.get("visitedPlaces")!=null) {
                            visitedPlaces = userHashMapPlaces.get("visitedPlaces");
                            if(visitedPlaces.get(placeName)!=null){
                                Switch sw=findViewById(R.id.switchVisitedMonument);
                                sw.setChecked(true);
                            }

                            if(userHashMapPlaces.get("favoritePlaces")!=null) {
                                favoritePlaces = userHashMapPlaces.get("favoritePlaces");
                                if(favoritePlaces.get(placeName)!=null) {
                                    ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
                                    imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                                    imgHeart.setColorFilter(R.color.heartColor);
                                    //imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                                    isFave=true;

                                }
                            }
                        }
                    }
                }
            });

            TextView txtViewMonumentName = findViewById(R.id.textViewMonumentViewName);
            TextView txtViewMonumentAddress = findViewById(R.id.textViewMonumentAddress);
            TextView txtViewMonumentDesc = findViewById(R.id.textViewMonumentViewDescription);
            ImageView imgMonument = findViewById(R.id.imgViewMonumentPhoto);
            RatingBar ratingBar = findViewById(R.id.ratingBarMonument);
            TextView txtViewVisitingHours=findViewById(R.id.textViewVisitingHours);
            TextView txtViewPhoneNumber=findViewById(R.id.textViewPhoneNumber);

            myRef.child("places").child("travel_agencies").child(placeName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                    Object o=dataSnapshot.getValue();
                    HashMap<String,String> hm1=(HashMap<String, String>) o;
                    HashMap<String,Long> hmLong=(HashMap<String, Long>) o;

                    rating=hmLong.get("rating");
                    long l=rating;
                    raters=hmLong.get("numOfRaters");
                    long longRaters=raters;
                    txtViewMonumentName.setText(hm1.get("name"));
                    txtViewMonumentDesc.setText(hm1.get("description"));
                    txtViewMonumentAddress.setText(hm1.get("address"));
                    txtViewVisitingHours.setText(hm1.get("visitingHours"));
                    txtViewPhoneNumber.setText(hm1.get("phoneNumber"));

                    String imgUrl=hm1.get("imgUrl");
                    storageRef=storage.getReference(imgUrl);

                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 800, 600, false);
                            imgMonument.setImageBitmap(smallBitmap);
                        }
                    });

                    ratingBar.setRating((float)l/longRaters);

                    Button btnRate=findViewById(R.id.btnRateMonument);
                    btnRate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            float rez=ratingBar.getRating();
                            raters=raters+1;
                            rating=rating+(long)rez;
                            myRef.child("places").child("travel_agencies").child(placeName).child("numOfRaters").setValue(raters);
                            myRef.child("places").child("travel_agencies").child(placeName).child("rating").setValue(rating);
                            ratingBar.setRating((float)rating/raters);

                            Integer p= Integer.parseInt(userPoints);
                            p+=1;
                            userPoints=p.toString();
                            myRef.child("users").child(userName).child("points").setValue(userPoints).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void aVoid) {
                                    Toast.makeText(ViewPlaceActivity.this, "Thank you for rating!", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });

                }
            });

            Switch sw=findViewById(R.id.switchVisitedMonument);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                    else{
                        myRef.child("users").child(userName).child("visitedPlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });

            ImageView imgHeart = findViewById(R.id.imgViewMonumentIsFavorite);
            imgHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFave){
                        isFave=false;
                        imgHeart.setImageResource(R.drawable.outline_favorite_border_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.black));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });

                    }
                    else{
                        isFave=true;
                        imgHeart.setImageResource(R.drawable.baseline_favorite_black_48dp);
                        imgHeart.setColorFilter(getResources().getColor(R.color.heartColor));
                        myRef.child("users").child(userName).child("favoritePlaces").child(placeName).setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {

                            }
                        });
                    }
                }
            });
        }
    }
}