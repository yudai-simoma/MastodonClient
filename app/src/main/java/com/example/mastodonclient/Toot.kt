package com.example.mastodonclient

import com.squareup.moshi.Json

//投稿されるデータ用
data class Toot(
    val id: String,
    @Json(name = "created_at") val createdAt: String,
    val sensitive: Boolean,
    val url: String,
    val content: String,
    //accountキーの示す連想配列をAccountクラスのオブジェクトに変換する
    val account: Account
)
