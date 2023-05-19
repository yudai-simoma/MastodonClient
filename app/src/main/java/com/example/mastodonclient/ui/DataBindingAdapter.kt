package com.example.mastodonclient.ui

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.mastodonclient.entity.Media
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Create a trust manager that does not validate certificate chains
private val trustAllCerts = arrayOf<TrustManager>(
    object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
    }
)

private val okHttpClient: OkHttpClient by lazy {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
    val sslSocketFactory = sslContext.socketFactory

    OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier(HostnameVerifier { _, _ -> true })
        .build()
}

//メソッドをDataBindingからspannedContent属性として利用する
@BindingAdapter("spannedContent")
//TextViewクラスにsetSpannedStringメソッドを追加(Kotlinの拡張関数)
fun TextView.setSpannedString(content: String) {
    //HTMLの文字列をSpannedに変換してTextViewのtextプロパティに設定するメソッド
    text = HtmlCompat.fromHtml(
        content,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
}

//DateBindingからmedia属性として利用
@BindingAdapter("media")
//ImageViewクラスを拡張してsetMediaメソッドを追加
fun ImageView.setMedia(media: Media?) {
    //mediaがnullであれば（添付画像が存在しない）。ImageViewをクリアする
    if (media == null) {
        setImageDrawable(null)
        return
    }

    val glideUrl = GlideUrl(
        media.url,
        LazyHeaders.Builder()
            .addHeader("User-Agent", "your-user-agent")
            .build()
    )

    //Glideによる画像のダウンロードとImageViewへの表示
    GlideApp.with(this)
        .asBitmap()
        .load(glideUrl)
        .into(this)
}
