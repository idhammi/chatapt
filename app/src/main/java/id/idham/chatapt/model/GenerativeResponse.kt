package id.idham.chatapt.model

import com.google.gson.annotations.SerializedName

data class GenerativeResponse(
    @SerializedName("candidates")
    val candidates: List<CandidatesItem>,

    @SerializedName("modelVersion")
    val modelVersion: String,

    @SerializedName("usageMetadata")
    val usageMetadata: UsageMetadata
)

data class Content(
    @SerializedName("role")
    val role: String,

    @SerializedName("parts")
    val parts: List<PartsItem>
)

data class CandidatesItem(
    @SerializedName("avgLogprobs")
    val avgLogprobs: Any,

    @SerializedName("finishReason")
    val finishReason: String,

    @SerializedName("content")
    val content: Content
)

data class PromptTokensDetailsItem(
    @SerializedName("modality")
    val modality: String,

    @SerializedName("tokenCount")
    val tokenCount: Int
)

data class UsageMetadata(
    @SerializedName("candidatesTokenCount")
    val candidatesTokenCount: Int,

    @SerializedName("promptTokensDetails")
    val promptTokensDetails: List<PromptTokensDetailsItem>,

    @SerializedName("totalTokenCount")
    val totalTokenCount: Int,

    @SerializedName("promptTokenCount")
    val promptTokenCount: Int,

    @SerializedName("candidatesTokensDetails")
    val candidatesTokensDetails: List<CandidatesTokensDetailsItem>
)

data class CandidatesTokensDetailsItem(
    @SerializedName("modality")
    val modality: String,

    @SerializedName("tokenCount")
    val tokenCount: Int
)
