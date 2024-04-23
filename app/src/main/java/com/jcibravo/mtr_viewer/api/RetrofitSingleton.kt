package com.jcibravo.mtr_viewer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSingleton {
    var isConnectedToHost = false
    var HOST = ""
    val api : ApiService by lazy {
        val hostFinal = HOST
        Retrofit.Builder()
            .baseUrl(hostFinal)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
