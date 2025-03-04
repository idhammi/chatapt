package id.idham.chatapt.model

import com.google.gson.annotations.SerializedName

data class GenerativeRequest(
    @SerializedName("contents")
    val contents: List<ContentsItem>
)

data class PartsItem(
    @SerializedName("text")
    val text: String
)

data class ContentsItem(
    @SerializedName("parts")
    val parts: List<PartsItem>
)
