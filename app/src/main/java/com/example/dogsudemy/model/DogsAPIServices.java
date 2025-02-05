package com.example.dogsudemy.model;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DogsAPIServices {

    private static final String BASE_URL = "https://raw.githubusercontent.com/";

    private DogsApi api;

    public DogsAPIServices(){

        api = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(DogsApi.class);

    }

    public Single<List<DogBreed>> getDogs(){

        return api.getDogs();

    }

}
