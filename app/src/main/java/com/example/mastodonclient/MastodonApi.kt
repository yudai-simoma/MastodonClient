package com.example.mastodonclient

import okhttp3.ResponseBody
import retrofit2.http.GET

interface MastodonApi {

    @GET("api/v1/timelines/public")
    //suspendキーワードをつかて中断関数として定義
    suspend fun fetchPublicTimeline(
    ): ResponseBody
}
