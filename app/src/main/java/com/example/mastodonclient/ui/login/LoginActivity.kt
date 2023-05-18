package com.example.mastodonclient.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mastodonclient.R

class LoginActivity : AppCompatActivity(R.layout.activity_login),
    LoginFragment.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, LoginFragment.TAG)
                .commit()
        }
    }

    //singleTaskが指定されていて、Activityがすでに起動されている場合に呼ばれる
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent ?: return

        //URIからcodeのクエリを取得
        val code = intent.data?.getQueryParameter("code") ?: return
        val loginFragment = supportFragmentManager.findFragmentByTag(LoginFragment.TAG)
        if (loginFragment is LoginFragment) {
            //LoginFragmentを通じてアクセストークンをリクエスト
            loginFragment.requestAccessToken(code)
        }
    }

    override fun onAuthCompleted() {
        //認証完了のコールバック
        Toast.makeText(this, "ログイン完了しました", Toast.LENGTH_LONG).show()
        //呼び出し元へ返す結果を設定してActivityを終了
        setResult(Activity.RESULT_OK)
        finish()
    }
}
