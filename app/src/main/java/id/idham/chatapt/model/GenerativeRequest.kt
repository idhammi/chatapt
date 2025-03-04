package id.idham.chatapt.model

data class GenerativeRequest(
    val contents: List<ContentsItem>,
    val generationConfig: GenerationConfig? = null
)

data class PartsItem(
    val text: String
)

data class ContentsItem(
    val role: String? = null,
    val parts: List<PartsItem>
)

data class GenerationConfig(
    val maxOutputTokens: Int? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null
)
