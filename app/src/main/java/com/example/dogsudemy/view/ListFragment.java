package com.example.dogsudemy.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dogsudemy.R;
import com.example.dogsudemy.model.DogBreed;
import com.example.dogsudemy.viewmodel.ListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListFragment extends Fragment {

    private ListViewModel listViewModel;
    private DogListAdapter adapter = new DogListAdapter(new ArrayList<>());

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout refreshLayout;

    @BindView(R.id.dogsList)
    RecyclerView dogsList;

    @BindView(R.id.textViewErrorMessage)
    TextView tvErrorMessage;

    @BindView(R.id.progressBar)
    ProgressBar progress;




    public ListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        Log.i("Test", "Working");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        ListFragmentDirections.ActionDetails actionDetails = ListFragmentDirections.actionDetails();
//        Navigation.findNavController(view).navigate(actionDetails);

        listViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        listViewModel.refresh();

        dogsList.setLayoutManager(new LinearLayoutManager(getContext()));
        dogsList.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dogsList.setVisibility(View.GONE);
                tvErrorMessage.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                listViewModel.refreshBypassCache();
                refreshLayout.setRefreshing(false);
            }
        });

        observeViewModel();

    }

    private void observeViewModel() {

        listViewModel.dogs.observe(getViewLifecycleOwner(), dogBreeds -> {
            if (dogBreeds != null && dogBreeds instanceof List){
                dogsList.setVisibility(View.VISIBLE);
                adapter.updateDogsList(dogBreeds);
            }
        });

        listViewModel.dogLoadError.observe(getViewLifecycleOwner(), isError -> {

            if(isError != null && isError instanceof Boolean){
                if (isError){
                    tvErrorMessage.setVisibility(View.VISIBLE);
                }else{
                    tvErrorMessage.setVisibility(View.GONE);
                }
            }

        });

        listViewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {

            if (isLoading != null && isLoading instanceof Boolean){

                if (isLoading){

                    progress.setVisibility(View.VISIBLE);
                    dogsList.setVisibility(View.GONE);
                    tvErrorMessage.setVisibility(View.GONE);

                } else progress.setVisibility(View.GONE);

            }

        } );

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.actionMenuSettings:{
                if (isAdded()){
                    NavDirections actionSettings = ListFragmentDirections.actionSettings();
                    Navigation.findNavController(getView()).navigate(actionSettings);
                }
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}