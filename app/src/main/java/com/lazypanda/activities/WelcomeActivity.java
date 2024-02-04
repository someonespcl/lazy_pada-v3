package com.lazypanda.activities;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.lazypanda.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
    
    //going to login screen
    public void goToLoginScreen(View v) {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
    }
    
    //going to register activity
    public void goToRegisterA(View v) {
        startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
