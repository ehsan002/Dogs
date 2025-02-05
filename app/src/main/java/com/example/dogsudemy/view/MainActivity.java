package com.example.dogsudemy.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.dogsudemy.R;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_SMS = 123;

    private NavController navController;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

        navController = Navigation.findNavController(this, R.id.fragment);

        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (DrawerLayout)null);
    }

    public void checkSMSPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                new AlertDialog.Builder(this)
                        .setTitle("Send SMS Permission")
                        .setMessage("This app requires access to send an sms")
                        .setPositiveButton("Ask me", (dialog, which) -> requestSMSPermission())
                        .setNegativeButton("No", (dialog, which) -> notifyDetailFragment(false))
                        .show();

            }else {
                requestSMSPermission();
            }

        }else {
            notifyDetailFragment(true);
        }

    }

    private void requestSMSPermission() {
        String[] permissions = {Manifest.permission.SEND_SMS};
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_SMS:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    notifyDetailFragment(true);
                }
                else {
                    notifyDetailFragment(false);
                }
                break;
            }
        }
    }

    private void notifyDetailFragment(Boolean permissionGranted) {
        Fragment activeFragment = fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (activeFragment instanceof DetailsFragment){
            ((DetailsFragment) activeFragment).onPermissionResult(permissionGranted);
        }

    }
}