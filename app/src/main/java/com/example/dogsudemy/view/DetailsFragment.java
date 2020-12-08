package com.example.dogsudemy.view;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.palette.graphics.Palette;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dogsudemy.R;
import com.example.dogsudemy.databinding.FragmentDetailsBinding;
import com.example.dogsudemy.databinding.SendSmsDialogBinding;
import com.example.dogsudemy.model.DogBreed;
import com.example.dogsudemy.model.DogPalette;
import com.example.dogsudemy.model.SmsInfo;
import com.example.dogsudemy.util.Util;
import com.example.dogsudemy.viewmodel.DetailsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsFragment extends Fragment {

    private int dogsId;
    private DetailsViewModel viewModel;

    private FragmentDetailsBinding binding;

    private boolean sendSmsStarted = false;

    private DogBreed currentDog;

//    @BindView(R.id.imageDetails)
//    ImageView dogImage;
//
//    @BindView(R.id.textviewDogName)
//    TextView dogName;
//
//    @BindView(R.id.textviewDogPurpose)
//    TextView dogPurpose;
//
//    @BindView(R.id.textviewDogLifespan)
//    TextView lifespan;
//
//    @BindView(R.id.textviewDogTemperment)
//    TextView dogTemperament;

    public DetailsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null){
            dogsId = DetailsFragmentArgs.fromBundle(getArguments()).getDogsId();
        }

        viewModel = ViewModelProviders.of(this).get(DetailsViewModel.class);
        viewModel.fetch(dogsId);

        Log.i("Test", "after fetch calling");

        observeViewModel();

    }

    private void observeViewModel() {

        viewModel.dogLiveData.observe(getViewLifecycleOwner(), dogBreed -> {

            if (dogBreed != null && dogBreed instanceof DogBreed && getContext() != null){

                currentDog = dogBreed;
                binding.setSingleDog(dogBreed);

                if (dogBreed.imageUrl != null){
                    setUpBackgroundColor(dogBreed.imageUrl);
                }


//                Log.i("Test", "inside if");

//                dogName.setText(dogBreed.dogBreed);
//                dogPurpose.setText(dogBreed.breedFor);
//                dogTemperament.setText(dogBreed.temperament);
//                lifespan.setText(dogBreed.lifeSpan);

//                if(dogBreed.imageUrl != null){
//                    Util.loadImage(dogImage, dogBreed.imageUrl, new CircularProgressDrawable(getContext()));
//                }

            }

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false);
        this.binding = binding;
        //View view = inflater.inflate(R.layout.fragment_details, container, false);
        //ButterKnife.bind(this, view);

        return binding.getRoot();
    }

    private void setUpBackgroundColor(String url){

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@Nullable Palette palette) {
                                int color = palette.getLightMutedSwatch().getRgb();
                                DogPalette myPalette = new DogPalette(color);
                                binding.setPalette(myPalette);
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_send:{

                if (!sendSmsStarted){
                    sendSmsStarted = true;
                    ((MainActivity)getActivity()).checkSMSPermission();
                }

                break;
            }
            case R.id.action_share: {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check this dog");
                intent.putExtra(Intent.EXTRA_TEXT, currentDog.dogBreed+ " bred for "+ currentDog.breedFor);
                intent.putExtra(Intent.EXTRA_STREAM, currentDog.imageUrl);
                startActivity(Intent.createChooser(intent, "Share with"));
                break;
            }
        }

        return super.onOptionsItemSelected(item);


    }

    public void onPermissionResult(Boolean permissionGranted) {

        if (isAdded() && sendSmsStarted && permissionGranted){
            SmsInfo smsInfo = new SmsInfo("", currentDog.dogBreed+ " bred for "+ currentDog.breedFor, currentDog.imageUrl);

            SendSmsDialogBinding dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                    R.layout.send_sms_dialog,
                    null,
                    false);
            new AlertDialog.Builder(getContext())
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton("Send SMS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!dialogBinding.smsDestination.getText().toString().isEmpty()){
                                smsInfo.to = dialogBinding.smsDestination.getText().toString();
                                sendSms(smsInfo);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            sendSmsStarted = false;

            dialogBinding.setSmsInfo(smsInfo);
        }

    }

    private void sendSms(SmsInfo smsInfo) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(smsInfo.to, null, smsInfo.text, pendingIntent, null);
    }

}