package com.erickmxav.doacaodemedicamentos.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.erickmxav.doacaodemedicamentos.R;
import com.erickmxav.doacaodemedicamentos.config.FirebaseConfig;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Doação de Medicamentos");
        setSupportActionBar( toolbar );

        authentication = FirebaseConfig.getAuthenticationFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch ( item.getItemId() ){
            case R.id.logout:
                authentication.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openRegisterMedicine(View view) {
        startActivity(new Intent(this, MedicineRegisterActivity.class));
    }

    public void openListMedicine(View view) {
        startActivity(new Intent(this, ListMedicinesActivity.class));
    }

    public void openUserRequestList(View view) {
        startActivity(new Intent(this, ListUserRequestActivity.class));
    }
}