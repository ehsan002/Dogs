package com.example.dogsudemy.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.dogsudemy.model.DogBreed;
import com.example.dogsudemy.model.DogDao;
import com.example.dogsudemy.model.DogDatabase;
import com.example.dogsudemy.model.DogsAPIServices;
import com.example.dogsudemy.util.NotificationHelper;
import com.example.dogsudemy.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ListViewModel extends AndroidViewModel {

    public MutableLiveData<List<DogBreed>> dogs = new MutableLiveData<List<DogBreed>>();
    public MutableLiveData<Boolean> dogLoadError = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<Boolean>();

    private DogsAPIServices apiServices = new DogsAPIServices();

    private CompositeDisposable disposable = new CompositeDisposable();

    private SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getApplication());
    private long refreshTime = 5*60*1000*1000*1000L;

    private AsyncTask<List<DogBreed>, Void, List<DogBreed>> insertTask;
    private AsyncTask<Void, Void, List<DogBreed>> retrieveTask;

    public ListViewModel(@NonNull Application application) {
        super(application);
    }

    public void refresh(){

        checkCacheDuration();

        long updateTime = preferencesHelper.getUpdateTime();

        long currentTime = System.nanoTime();

        if (updateTime != 0 && currentTime-updateTime < refreshTime){

            fetchFromDatabase();

        }else {
            fetchFromRemote();
        }

    }

    public void refreshBypassCache(){
        fetchFromRemote();
    }

    private void fetchFromDatabase(){

        loading.setValue(true);
        retrieveTask = new RetrieveDogsTask();
        retrieveTask.execute();

    }

    private void fetchFromRemote(){

        loading.setValue(true);
        disposable.add(

                apiServices.getDogs()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<DogBreed>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<DogBreed> dogBreeds) {

                                insertTask = new InsertDogTask();
                                insertTask.execute(dogBreeds);

                                Toast.makeText(getApplication(), "Dogs retrieved", Toast.LENGTH_SHORT).show();

                                NotificationHelper.getInstance(getApplication()).createNotification();

                                //dogsRetrieved();

                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                dogLoadError.setValue(true);
                                loading.setValue(false);
                                e.printStackTrace();
                            }
                        })

        );
    }

    private void dogsRetrieved(List<DogBreed> dogList) {
        dogs.setValue(dogList);
        dogLoadError.setValue(false);
        loading.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();

        if (insertTask != null){

            insertTask.cancel(true);
            insertTask = null;

        }

        if (retrieveTask != null){

            retrieveTask.cancel(true);
            retrieveTask = null;

        }
    }

    private class InsertDogTask extends AsyncTask<List<DogBreed>, Void, List<DogBreed>>{


        @Override
        protected List<DogBreed> doInBackground(List<DogBreed>... lists) {

            List<DogBreed> list = lists[0];
            DogDao dogDao = DogDatabase.getInstance(getApplication()).dogDao();
            dogDao.deleteAllDogs();

            ArrayList<DogBreed> newList = new ArrayList<>(list);
            List<Long> result = dogDao.insertAll(newList.toArray(new DogBreed[0]));

            int i = 0;
            while (i<list.size()){

                list.get(i).uuid = result.get(i).intValue();
                i++;

            }

            return list;
        }

        @Override
        protected void onPostExecute(List<DogBreed> dogBreeds) {

            dogsRetrieved(dogBreeds);
            preferencesHelper.saveUpdateTime(System.nanoTime());

        }
    }

    private class RetrieveDogsTask extends AsyncTask<Void, Void, List<DogBreed>>{


        @Override
        protected List<DogBreed> doInBackground(Void... voids) {

            return DogDatabase.getInstance(getApplication()).dogDao().getAllDogs();
        }

        @Override
        protected void onPostExecute(List<DogBreed> dogBreeds) {
            dogsRetrieved(dogBreeds);
            Toast.makeText(getApplication(), "Dogs retrieved from Database", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCacheDuration(){

        String cachePreference = preferencesHelper.getCacheDuration();
        if(!cachePreference.equals("")){

            try{
                int cachePreferenceInt = Integer.parseInt(cachePreference);
                refreshTime = cachePreferenceInt * 1000 * 1000 * 1000L;

            }catch (NumberFormatException e){
                e.printStackTrace();
            }

        }

    }
}
