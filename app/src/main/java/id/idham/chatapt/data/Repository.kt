package id.idham.chatapt.data

import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.GenerativeResponse
import id.idham.chatapt.network.ApiService

interface Repository {
    suspend fun generateContent(
        request: GenerativeRequest
    ): Result<GenerativeResponse>
}

class RepositoryImpl(
    private val apiKey: String,
    private val apiService: ApiService
) : Repository {
    override suspend fun generateContent(
        request: GenerativeRequest
    ): Result<GenerativeResponse> {
        return try {
            apiService.generateContent(apiKey = apiKey, request = request).let { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Throwable("Response body is null"))
                } else {
                    Result.failure(Throwable(response.message()))
                }
            }
        } catch (e: Exception) {
            Result.failure(Throwable(e.message.toString()))
        }
    }
}
