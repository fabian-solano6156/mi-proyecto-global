package com.example.cordiprueba.api

import com.example.cordiprueba.Inicio.composables.PracticaRequest
import com.example.cordiprueba.Inicio.composables.PracticaResponse // IMPORTANTE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.PUT

interface ApiService {
    @POST("practicas")
    suspend fun guardarPractica(
        @Body practica: PracticaRequest
    ): Response<PracticaResponse>

    @GET("practicas")
    suspend fun obtenerPracticas(): Response<List<PracticaResponse>>

    @GET("practicas")
    suspend fun getPracticas(): Response<List<PracticaResponse>>
    @Multipart
    @POST("/practicas/{practica_id}/subir-archivo")
    suspend fun subirArchivo(
        @Path("practica_id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>
    @DELETE("practicas/{id}")
    suspend fun eliminarPractica(@Path("id") id: Int): Response<Unit>
    @GET("practicas/{id}")
    suspend fun getPracticaById(@Path("id") id: Int): Response<PracticaResponse>
    @PUT("practicas/{id}")
    suspend fun actualizarPractica(
        @Path("id") id: Int,
        @Body practica: PracticaRequest
    ): Response<PracticaResponse>
    @DELETE("archivos/{id}")
    suspend fun eliminarArchivo(@Path("id") id: Int): Response<Unit>
}