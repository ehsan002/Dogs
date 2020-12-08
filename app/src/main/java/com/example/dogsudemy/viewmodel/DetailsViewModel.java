package com.example.dogsudemy.viewmodel;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dogsudemy.model.DogBreed;
import com.example.dogsudemy.model.DogDatabase;

public class DetailsViewModel extends AndroidViewModel {

    public MutableLiveData<DogBreed> dogLiveData = new MutableLiveData<DogBreed>();

    private RetrieveDogTask retrieveDogTask;

    public DetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetch(int uuid){

        retrieveDogTask = new RetrieveDogTask();
        retrieveDogTask.execute(uuid);

    }

    @Override
    protected void onCleared() {
        if(retrieveDogTask != null){
            retrieveDogTask.cancel(true);
            retrieveDogTask = null;
        }
    }

    private class RetrieveDogTask extends AsyncTask<Integer, Void, DogBreed>{


        @Override
        protected DogBreed doInBackground(Integer... integers) {

            int uuid = integers[0];
            return DogDatabase.getInstance(getApplication()).dogDao().getDog(uuid);
        }

        @Override
        protected void onPostExecute(DogBreed dogBreed) {
            dogLiveData.setValue(dogBreed);
        }
    }

}
