package id.idham.chatapt.network

import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.GenerativeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // gemini
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-2.0-flash",
        @Query("key") apiKey: String,
        @Body request: GenerativeRequest
    ): Response<GenerativeResponse>
}
