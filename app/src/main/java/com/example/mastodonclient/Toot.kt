package com.example.mastodonclient

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

//投稿されるデータ用
@Parcelize
data class Toot(
    val id: String,
    @Json(name = "created_at") val createdAt: String,
    val sensitive: Boolean,
    val url: String,
    //Mediaのリストを追加
    @Json(name = "media_attachments") val mediaAttachments: List<Media>,
    val content: String,
    //accountキーの示す連想配列をAccountクラスのオブジェクトに変換する
    val account: Account
):Parcelable {
    //リストから最初のMediaを取得。存在しない場合はnullを返す
    val topMedia: Media?
        get() = mediaAttachments.firstOrNull()
}
