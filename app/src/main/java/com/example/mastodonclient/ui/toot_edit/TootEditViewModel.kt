package com.example.mastodonclient.ui.toot_edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection

class TootEditViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application) {

    private val userCredentialRepository = UserCredentialRepository(
        application
    )

    //投稿内容のLiveDataを保持
    val status = MutableLiveData<String>()

    val loginRequired = MutableLiveData<Boolean>()

    //投稿完了をUIに伝えるLiveData
    val postComplete = MutableLiveData<Boolean>()
    //エラーメッセージをUIに伝えるLiveData
    val errorMessage = MutableLiveData<String>()

    fun postToot() {
        //statusがBlankにエラーの内容をUIに伝えて処理を抜ける
        val statusSnapshot = status.value ?: return
        //UserCredentialオブジェクトを取得、UserCredentialが存在しなければメソッドを終了する
        if (statusSnapshot.isBlank()) {
            errorMessage.postValue("投稿内容がありません")
            return
        }

        coroutineScope.launch {
            val credential = userCredentialRepository.find(instanceUrl, username)
            if (credential == null) {
                loginRequired.postValue(true)
                return@launch
            }

            //Tootの投稿を実行
            val tootRepository = TootRepository(credential)
            try {
                tootRepository.postToot(
                statusSnapshot
                )
                //投稿完了をUIに伝える
                postComplete.postValue(true)
              //サーバー側でエラーが発生した場合、RetrofitはHttpExceptionの例外が発生
            } catch (e: HttpException) {
                when (e.code()) {
                    //スコープを超える操作をした場合、サーバーはステータスコード403を返す
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        //エラーメッセージを表示
                        errorMessage.postValue("必要な権限がありません")
                    }
                }
            }
        }
    }
}

