package com.example.mastodonclient

import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Media
import com.example.mastodonclient.entity.ResponseToken
import com.example.mastodonclient.entity.Toot
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
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

    //Content-Typeを「application/x-www-form-urlencoded」としてstatusをPOSTで送信
    @FormUrlEncoded
    @POST("api/v1/statuses")
    suspend fun postToot(
        @Header("Authorization") accessToken: String,
        @Field("status") status: String,
        //添付するアップロード済みMediaのIDリスト。デフォルト値はnull
        @Field("media_ids[]") mediaIds: List<String>? = null
    ): Toot

    //Multipartで送信
    @Multipart
    @POST("api/v1/media")
    suspend fun postMedia(
        @Header("Authorization") accessToken: String,
        //アップロードする画像データ
        @Part file: MultipartBody.Part
    ): Media

    //Deleteメソッドでリクエストする。{id}の部分が引数idで置き換えられる
    @DELETE("api/v1/statuses/{id}")
    suspend fun deleteToot(
        @Header("Authorization") accessToken: String,
        @Path("id") id: String
    )

    //アクセストークンをリクエスト
    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun token(
        //クライアントキー（ID）
        @Field("client_id") clientId: String,
        //クライアントシークレット
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("scope") scope: String,
        //取得した認証コード
        @Field("code") code: String,
        @Field("grant_type") grantType: String
    ): ResponseToken
}
