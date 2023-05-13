package com.example.mastodonclient

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TootRepository(
    private val userCredential: UserCredential
) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit: Retrofit
    private val api: MastodonApi

    init {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(userCredential.instanceUrl)
            .client(okHttpClient) // ここでSSL認証無効の設定を適用
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        api = retrofit.create(MastodonApi::class.java)
    }

    suspend fun fetchPublicTimeline(
        maxId: String?,
        onlyMedia: Boolean
    //実行スレッドをIOに指定。呼び出し側は実行スレッドを意識する必要がない
    ) = withContext(Dispatchers.IO) {
        api.fetchPublicTimeline(
            maxId = maxId,
            onlyMedia = onlyMedia
        )
    }

    suspend fun fetchHomeTimeline(
        maxId: String?
    ) = withContext(Dispatchers.IO) {
        api.fetchHomeTimeline(
            //Mastodon APIはアクセストークンの前にBearerを付ける規則になっている
            accessToken = "Bearer ${userCredential.accessToken}",
            maxId = maxId
        )
    }
}
