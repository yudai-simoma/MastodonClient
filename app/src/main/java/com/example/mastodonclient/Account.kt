package com.example.mastodonclient

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

//dateキーワードを付けてKotlinのdataクラスとして宣言
@Parcelize
data class Account(
    val id: String,
    val username: String,
//    KotlinのCamelCaseに合わせる為、マッピング
    @Json(name = "display_name") val displayName: String,
    val url: String
):Parcelable
