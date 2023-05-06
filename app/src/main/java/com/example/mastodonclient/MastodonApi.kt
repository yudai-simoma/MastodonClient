package com.example.mastodonclient

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface MastodonApi {

    @GET("api/v1/timelines/public")
    //suspendキーワードをつかて中断関数として定義
    suspend fun fetchPublicTimeline(
        //クエリonly_mediaとして送るパラメータを定義。デフォルト値はfalse
        @Query("only_media") onlyMedia: Boolean = false
    ): List<Toot>   //Tootオブジェクトを直接取得
}
