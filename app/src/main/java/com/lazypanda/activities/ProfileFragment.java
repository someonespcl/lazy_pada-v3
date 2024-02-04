package com.lazypanda.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lazypanda.R;
import com.lazypanda.utils.CustomLoadingDialog;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference dataRefer;
    StorageReference storageReference;
    String storagePath = "Users_Profile/";
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 145;
    private static final int PERMISSION_REQUEST_CODE = 141;
    private static final int PICK_IMAGE_REQUEST_CODE = 142;
    Uri croppedImageUri;
    ImageView profilePicV;
    TextView user_name_profile, user_email_profile, user_phoneN_profile;
    String image;

    ActivityResultLauncher<Intent> resultLauncher;
    private ActivityResultLauncher<Intent> uCropLauncher;
    private Uri croppedMediaImageUri;
    DrawableCrossFadeFactory dcf;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dataRefer = database.getReference("Users");
        dataRefer.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference();
        resultRegister();
        
        dcf = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        //header_Profile_Pic = getActivity().findViewById(R.id.header_profile_pic);
        ImageView profileMenuV = view.findViewById(R.id.logout);
        profilePicV = view.findViewById(R.id.profilePic);
        user_email_profile = view.findViewById(R.id.userProfileEmail);
        user_name_profile = view.findViewById(R.id.userProfileName);
        user_phoneN_profile = view.findViewById(R.id.userProfilePhoneN);

        // profile menu view
        profileMenuV.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupMenu(v);
                        // checkAndRequestPermissions();
                        // pickImageFromGallery();
                    }
                });

        profilePicV.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProfileImageDialog dia = new ProfileImageDialog(getActivity(), image);
                        dia.show();
                    }
                });

        Query query = dataRefer.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String email = "" + ds.child("email").getValue();
                            String name = "" + ds.child("name").getValue();
                            String phoneNumber = "" + ds.child("phoneN").getValue();
                            image = "" + ds.child("image").getValue();

                            user_email_profile.setText(email);
                            user_name_profile.setText(name);
                            user_phoneN_profile.setText(phoneNumber);
                            try {
                                Glide.with(requireActivity())
                                        .load(image)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .transition(DrawableTransitionOptions.withCrossFade(dcf))
                                        .circleCrop()
                                        .into(profilePicV);
                            } catch (Exception e) {
                                Glide.with(requireActivity())
                                        .load(R.drawable.p)
                                        .circleCrop()
                                        .into(profilePicV);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError arg0) {}
                });
        uCropLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                // The cropped image URI will be in the 'result.getData()' intent
                                Uri croppedUri = UCrop.getOutput(result.getData());
                                if (croppedUri != null) {
                                    // Handle the cropped image URI here
                                    uploadProfile(croppedUri);
                                }
                            } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                                Throwable cropError = UCrop.getError(result.getData());
                                // Handle the error
                            }
                        });

        return view;
    }

    // Check and request camera and gallery permissions
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Camera");
            permissionsList.add(Manifest.permission.CAMERA);
        }

        if (ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Read Storage");
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Write Storage");
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Read Media Images");
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Read Media Video");
            permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO);
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Read Media Audio");
            permissionsList.add(Manifest.permission.READ_MEDIA_AUDIO);
        }
        
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add("Allow Notifications");
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    getActivity(), permissionsList.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void launchUCrop(Uri sourceUri) {
        UCrop uCrop =
                UCrop.of(
                                sourceUri,
                                Uri.fromFile(
                                        new File(getActivity().getCacheDir(), "croppedImage.jpg")))
                        .withAspectRatio(
                                1, 1); // Set the desired aspect ratio (e.g., 1:1 for a square)
        Intent uCropIntent = uCrop.getIntent(requireActivity());
        startActivityForResult(
                uCropIntent, UCrop.REQUEST_CROP); // 'this' should be your activity context
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    // You can now proceed to pick an image from the gallery or use the camera
                    // pickImageFromGallery();
                } else {
                    // Permission denied
                    // Handle the case where the user denied permission
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void pickImageFromGallery() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            resultLauncher.launch(intent);
        }
    }

    private void resultRegister() {
        resultLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    Uri imageFromMedia = result.getData().getData();
                                    // uploadProfile(imageFromMedia);
                                    launchUCrop(imageFromMedia);
                                }
                            }
                        });
    }

    // Handle the result of picking an image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && data != null) {
            Uri selectedImageUri = data.getData();

            // Use uCrop to crop the selected image
            UCrop.of(
                            selectedImageUri,
                            Uri.fromFile(
                                    new File(getActivity().getCacheDir(), "cropped_image.jpg")))
                    .withAspectRatio(1, 1) // Set the desired aspect ratio (e.g., 1:1 for a square)
                    .start(getActivity(), this);
        }

        // Handle uCrop result
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            croppedImageUri = UCrop.getOutput(data);
            uploadProfile(croppedImageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // uploading image
    private void uploadProfile(Uri uri) {
        CustomLoadingDialog.showLoadingDialog(requireActivity());
        String filePathAndName = storagePath + "_" + user.getUid();
        StorageReference storageRefer2 = storageReference.child(filePathAndName);

        // Compress the image before uploading
        Bitmap compressedBitmap = compressImage(uri);

        if (compressedBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedBitmap.compress(
                    Bitmap.CompressFormat.JPEG, 60, baos); // Adjust compression quality as needed
            byte[] data = baos.toByteArray();

            // Upload the compressed image
            storageRefer2
                    .putBytes(data)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapShot) {
                                    Task<Uri> uriTask = taskSnapShot.getStorage().getDownloadUrl();
                                    while (!uriTask.isSuccessful())
                                        ;
                                    Uri downloadUri = uriTask.getResult();

                                    if (uriTask.isSuccessful()) {
                                        CustomLoadingDialog.hideLoadingDialog();
                                        HashMap<String, Object> results = new HashMap<>();
                                        results.put("image", downloadUri.toString());

                                        dataRefer
                                                .child(user.getUid())
                                                .updateChildren(results)
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(
                                                                                getActivity(),
                                                                                "Image Uploaded",
                                                                                Toast.LENGTH_LONG)
                                                                        .show();
                                                            }
                                                        })
                                                .addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(
                                                                    @NonNull Exception e) {
                                                                Toast.makeText(
                                                                                getActivity(),
                                                                                e.getMessage(),
                                                                                Toast.LENGTH_LONG)
                                                                        .show();
                                                            }
                                                        });
                                    } else {
                                        CustomLoadingDialog.hideLoadingDialog();
                                        Toast.makeText(
                                                        getActivity(),
                                                        "Some Error Occurred",
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });
        }
    }

    // compress image
    private Bitmap compressImage(Uri imageUri) {
        try {
            // Load the selected image into a Bitmap
            Bitmap originalBitmap =
                    MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

            // Compress the Bitmap
            Bitmap compressedBitmap =
                    Bitmap.createScaledBitmap(
                            originalBitmap,
                            originalBitmap.getWidth() / 2,
                            originalBitmap.getHeight() / 2,
                            true);

            return compressedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // showing popup menu
    public void showPopupMenu(View view) {
        PopupMenu popupMenu =
                new PopupMenu(
                        getActivity(),
                        view); // 'this' refers to the current activity, and 'view' is the view that
        // triggers the popup
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(
                R.menu.profile_menu, popupMenu.getMenu()); // Use the XML menu resource you created

        // Set an item click listener for the menu items
        // Show the popup menu
        popupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.logOutUser) {
                            mAuth.signOut();
                            startActivity(new Intent(getActivity(), WelcomeActivity.class));
                            getActivity().finish();
                            return true;
                        }
                        if (itemId == R.id.changeProfilePic) {
                            checkAndRequestPermissions();
                            pickImageFromGallery();
                        }
                        return false;
                    }
                });
        popupMenu.show();
    }
}
