package com.example.mm.firebasestoragedatabase.view.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mm.firebasestoragedatabase.R;
import com.example.mm.firebasestoragedatabase.models.UploadObject;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
       private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton mButtonChooseImage;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextViewShowUpload;
    private Button mButtonUpload;

    private Uri mImageUri;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();

        mStorageReference = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
    }

    private void initializeView(){
        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUpload = findViewById(R.id.text_view_show_upload);

        mButtonChooseImage.setOnClickListener(this);
        mButtonUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.button_choose_image:
                openFileChooser();
                break;

            case R.id.button_upload:
                if (mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(this, "Upload in Progress", Toast.LENGTH_SHORT).show();
                }else {
                    uploadsFile();
                }
                break;

            case R.id.text_view_show_upload:
                startActivity(new Intent(MainActivity.this, ImagesActivity.class));
                break;
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadsFile() {

        if (mImageUri != null){
            StorageReference fileReference = mStorageReference
                    .child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                            UploadObject uploadObject = new UploadObject(mEditTextFileName.getText().toString()
                                    , taskSnapshot.getDownloadUrl().toString());

                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(uploadObject);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress =
                                    (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                            mProgressBar.setProgress((int)progress);
                        }
                    });

        }else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            mImageUri = data.getData();

            Picasso.with(MainActivity.this)
                    .load(mImageUri)
                    .into(mImageView);

        }
    }
}
