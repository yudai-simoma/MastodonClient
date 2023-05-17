package com.example.mastodonclient.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.entity.UserCredential
import com.example.mastodonclient.repository.AuthRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginViewModel(
    private val instanceUrl: String,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private val TAG = AndroidViewModel::class.java.simpleName
    }

    private val authRepository = AuthRepository(instanceUrl)
    private val userCredentialRepository = UserCredentialRepository(
        application
    )

    //アクセストークンが保存されたことをUIに伝えるLiveData
    val accessTokenSaved = MutableLiveData<UserCredential>()

    fun requestAccessToken(
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        scopes: String,
        code: String
    ) {
        //アクセストークンのリクエストを実行
        coroutineScope.launch {
            val responseToken = authRepository.token(
                clientId,
                clientSecret,
                redirectUri,
                scopes,
                code
            )

            //取得したアクセストークンをログ表示
            Log.d(TAG, responseToken.accessToken)

            //UserCredentialオブジェクトのインスタンス化
            val userCredential = UserCredential(
                instanceUrl = instanceUrl,
                accessToken = responseToken.accessToken
            )
            //アクセストークンを保存
            userCredentialRepository.set(userCredential)

            //アクセストークン保存をUIに伝える
            accessTokenSaved.postValue(userCredential)
        }
    }
}
