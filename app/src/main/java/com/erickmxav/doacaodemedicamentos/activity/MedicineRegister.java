package com.erickmxav.doacaodemedicamentos.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.erickmxav.doacaodemedicamentos.R;
import com.erickmxav.doacaodemedicamentos.config.FirebaseConfig;
import com.erickmxav.doacaodemedicamentos.helper.Permission;
import com.erickmxav.doacaodemedicamentos.helper.UserFirebase;
import com.erickmxav.doacaodemedicamentos.model.Medicine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MedicineRegister extends AppCompatActivity {

    private TextView mTextView; //bottomsheet
    private static final int SELECTION_CAMERA  = 100;
    private static final int SELECTION_GALERY = 200;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private EditText fieldName, fieldCategory, fieldValidity;
    private CircleImageView imageMedicine;
    private Button addImageMedicine;
    private Button chooseCamera;
    private Button chooseGallery;
    private Medicine medicine;
    private Uri urlImage;
    private String userId;

    private FirebaseAuth authentication;
    private StorageReference storageReference;
    private DatabaseReference medicineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_medicine);

        //Validation permissions
        Permission.validatePermissions(permissoesNecessarias, this, 1);
        storageReference = FirebaseConfig.getFirebaseStorage();
        authentication = FirebaseConfig.getAuthenticationFirebase();

        userId = UserFirebase.getUserId();
        fieldName = findViewById(R.id.editName);
        fieldCategory = findViewById(R.id.editCategory);
        fieldValidity = findViewById(R.id.editValidity);
        imageMedicine = findViewById(R.id.imageMedic);
        addImageMedicine = findViewById(R.id.addImageMedic);
        chooseCamera = findViewById(R.id.openCamera);
        chooseGallery = findViewById(R.id.openGallery);

        //Recover user data
        FirebaseUser user = UserFirebase.getActualUser();
        Uri url = user.getPhotoUrl();

        Button buttonShow = addImageMedicine;
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        MedicineRegister.this, R.style.BottomSheetTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                        .inflate(
                                R.layout.layout_bottom_sheet,
                                (LinearLayout)findViewById(R.id.bottomSheetContainer)
                        );

                bottomSheetView.findViewById(R.id.openCamera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if ( i.resolveActivity(getPackageManager()) != null ) {
                            startActivityForResult(i, SELECTION_CAMERA);

                        }
                    }
                });

                bottomSheetView.findViewById(R.id.openGallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                        if ( i.resolveActivity(getPackageManager()) != null ){
                            startActivityForResult(i, SELECTION_GALERY );
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;

            try {

                switch ( requestCode ){
                    case SELECTION_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                    case SELECTION_GALERY:
                        Uri selectedImage = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage );
                        break;
                }

                if ( imagem != null ){

                    imageMedicine.setImageBitmap( imagem );

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("images")
                            .child("medicines")
                            .child(userId + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MedicineRegister.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    urlImage = url;
                                }
                            });

                            Toast.makeText(MedicineRegister.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    //Register medicine
    public void registerMedicine(View view){

        medicine = new Medicine();
        medicine.setName( fieldName.getText().toString() );
        medicine.setCategory( fieldCategory.getText().toString() );
        medicine.setValidity( fieldValidity.getText().toString() );
        medicine.setPhoto( urlImage.toString());

        medicine.RegisterMedicine();

        Toast.makeText(MedicineRegister.this,
                "Sucesso ao cadastrar medicamento",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    //Permissões galeria/camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for ( int resultPermission : grantResults){
            if ( resultPermission == PackageManager.PERMISSION_DENIED ){
                validatePermissionAlert();
            }
        }
    }

    private void validatePermissionAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}