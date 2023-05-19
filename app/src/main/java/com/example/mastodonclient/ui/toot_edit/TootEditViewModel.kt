package com.example.mastodonclient.ui.toot_edit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mastodonclient.entity.LocalMedia
import com.example.mastodonclient.repository.MediaFileRepository
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import javax.xml.transform.OutputKeys.MEDIA_TYPE

class TootEditViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val coroutineScope: CoroutineScope,
    application: Application
) : AndroidViewModel(application) {

    private val userCredentialRepository = UserCredentialRepository(
        application
    )
    private val mediaFileRepository = MediaFileRepository(application)

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
                //添付画像があれば1枚ずつ処理
                val uploadedMediaIds = mediaAttachments.value?.map {
                    //添付画像をアップロード
                    tootRepository.postMedia(it.file, it.mediaType)
                    //アップロード済みMediaのIDを取り出してリストに変換
                }?.map { it.id }

                tootRepository.postToot(
                    statusSnapshot,
                    uploadedMediaIds
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
            } catch (e: IOException) {
                errorMessage.postValue(
                    "サーバーに接続できませんでした。${e.message}"
                )
            }

        }
    }

    //添付メディアの更新を伝えるLiveData
    val mediaAttachments = MutableLiveData<ArrayList<LocalMedia>>()

    //添付メディアを追加する。mediaUriはContentProvider上でのデータを指し示す
    fun addMedia(mediaUri: Uri) {
        coroutineScope.launch {
            try {
                //mediaUriが指し示す画像をContentProviderから取得してBitmapオブジェクトを取得
                val bitmap = mediaFileRepository.readBitmap(mediaUri)
                //Bitmapオブジェクトをアプリのファイル領域に一時ファイルとして保存
                val tempFile = mediaFileRepository.saveBitmap(bitmap)

                //添付メディアのリストの新しいインスタンスを生成。現在の添付メディアのリストを追加
                val newMediaAttachments = ArrayList<LocalMedia>()
                mediaAttachments.value?.also {
                    newMediaAttachments.addAll(it)
                }
                //追加されたメディアを新しいリストに追加
                newMediaAttachments.add(LocalMedia(tempFile, MEDIA_TYPE))
                //LiveDataに設定してUIに更新を伝える
                mediaAttachments.postValue(newMediaAttachments)

            } catch (e: IOException) {
                //メディアが読み込めなかった場合のエラー処理
                handleMediaException(mediaUri, e)
            }
        }
    }

    private fun handleMediaException(mediaUri: Uri, e: IOException) {
        errorMessage.postValue("メディアを読み込めません ${e.message} ${mediaUri}")
    }
}

