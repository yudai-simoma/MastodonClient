package com.example.mastodonclient

import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter

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