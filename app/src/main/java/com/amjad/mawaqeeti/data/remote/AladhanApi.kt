package com.amjad.mawaqeeti.data.remote

import com.amjad.mawaqeeti.data.model.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {
    @GET("timingsByCity/{date}")
    suspend fun getTimings(
        @Path("date") date: String,
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int
    ): AladhanResponse
}
