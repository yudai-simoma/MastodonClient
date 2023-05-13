package com.example.mastodonclient

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MastodonApi {

    @GET("api/v1/timelines/public")
    //suspendキーワードをつかて中断関数として定義
    suspend fun fetchPublicTimeline(
        //クエリmax_idとして送るパラメーターを定義。デフォルト値はnull
        @Query("max_id") maxId: String? = null,
        //クエリonly_mediaとして送るパラメータを定義。デフォルト値はfalse
        @Query("only_media") onlyMedia: Boolean = false
    ): List<Toot>   //Tootオブジェクトを直接取得

    //HTTPアクセスのAuthorizationヘッダーにアクセストークンを設定
    @GET("api/v1/timelines/home")
    suspend fun fetchHomeTimeline(
        @Header("Authorization") accessToken: String,
        @Query("max_id") maxId: String? = null,
        @Query("limit") limit: Int? = null
    ): List<Toot>

    //APIの定義
    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyAccountCredential (
        @Header("Authorization") accessToken: String
    ): Account
}
