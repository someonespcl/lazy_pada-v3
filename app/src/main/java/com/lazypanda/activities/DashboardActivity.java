package com.lazypanda.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import com.lazypanda.databinding.ActivityDashboardBinding;
import com.lazypanda.R;
import me.ibrahimsn.lib.OnItemSelectedListener;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        fAuth = FirebaseAuth.getInstance();
        replaceFrag(new HomeFragment());
        
        FragmentManager fManager = getSupportFragmentManager();
        binding.bottomBar.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public boolean onItemSelect(int item) {
                        switch (item) {
                            case 0:
                                replaceFrag(new HomeFragment());
                                break;
                            case 1:
                                replaceFrag(new ProfileFragment());
                                break;
                            case 2:
                                replaceFrag(new UsersFragment());
                                break;
                            case 3:
                                replaceFrag(new HomeFragment());
                                break;
                        }
                        return true;
                    }
                });
    }
    
    
    private void replaceFrag(Fragment fragmet) {
        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
        fragTrans.replace(R.id.content, fragmet);
        fragTrans.commit();
    }
    
    // check user
    private void checkUserStatus() {
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
        } else {
            startActivity(new Intent(DashboardActivity.this, WelcomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
    
}
