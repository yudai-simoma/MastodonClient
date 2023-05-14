package com.example.mastodonclient.ui

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mastodonclient.entity.Media

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
    //Glideによる画像のダウンロードとImageViewへの表示
    Glide.with(this)
        .load(media.url)
        .into(this)
}
