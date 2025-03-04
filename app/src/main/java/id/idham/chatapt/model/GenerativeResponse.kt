package id.idham.chatapt.model

data class GenerativeResponse(
    val candidates: List<CandidatesItem>,
    val modelVersion: String,
    val usageMetadata: UsageMetadata
)

data class Content(
    val role: String,
    val parts: List<PartsItem>
)

data class CandidatesItem(
    val avgLogprobs: Any,
    val finishReason: String,
    val content: Content
)

data class PromptTokensDetailsItem(
    val modality: String,
    val tokenCount: Int
)

data class UsageMetadata(
    val candidatesTokenCount: Int,
    val promptTokensDetails: List<PromptTokensDetailsItem>,
    val totalTokenCount: Int,
    val promptTokenCount: Int,
    val candidatesTokensDetails: List<CandidatesTokensDetailsItem>
)

data class CandidatesTokensDetailsItem(
    val modality: String,
    val tokenCount: Int
)
