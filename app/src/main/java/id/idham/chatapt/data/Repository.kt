package id.idham.chatapt.data

import id.idham.chatapt.database.ChatMessage
import id.idham.chatapt.database.ChatMessageDao
import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.GenerativeResponse
import id.idham.chatapt.network.ApiService
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun generateContent(request: GenerativeRequest): Result<GenerativeResponse>
    fun getAllMessages(): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage)
}

class RepositoryImpl(
    private val apiKey: String,
    private val apiService: ApiService,
    private val chatMessageDao: ChatMessageDao
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

    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatMessageDao.getAllMessages()
    }

    override suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }
}
