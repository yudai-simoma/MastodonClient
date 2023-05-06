package com.example.mastodonclient

import com.squareup.moshi.Json

//dateキーワードを付けてKotlinのdataクラスとして宣言
data class Account(
    val id: String,
    val username: String,
//    KotlinのCamelCaseに合わせる為、マッピング
    @Json(name = "display_name") val displayName: String,
    val url: String
)
