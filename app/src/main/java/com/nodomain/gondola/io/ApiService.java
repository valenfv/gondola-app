package com.nodomain.gondola.io;

import com.nodomain.gondola.model.Producto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/productos/barcode/{barCode}/lista/{lista}")
    Call<Producto> getProducto(@Path("barCode") String barCode, @Path("lista") String lista);
}

