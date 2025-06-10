package com.example.spotly.network;

import com.example.spotly.model.PlaceResult;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Mencari lokasi berdasarkan query teks
    @GET("search")
    Call<List<PlaceResult>> searchPlaces(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails,
            @Query("extratags") int extraTags
    );
}