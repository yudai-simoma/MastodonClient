package com.example.mastodonclient.ui.toot_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Toot
import com.example.mastodonclient.entity.UserCredential
import com.example.mastodonclient.repository.AccountRepository
import com.example.mastodonclient.repository.TootRepository
import com.example.mastodonclient.repository.UserCredentialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TootListViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val timelineType: TimelineType,
    private val coroutineScope: CoroutineScope,
    application: Application
    //AndroidViewModelを継承、LifecycleObserverを実装
) : AndroidViewModel(application), LifecycleObserver {

    private val userCredentialRepository = UserCredentialRepository(
        application
    )
    private lateinit var tootRepository: TootRepository
    private lateinit var accountRepository: AccountRepository

    private lateinit var userCredential: UserCredential

    val isLoading = MutableLiveData<Boolean>()
    val accountInfo = MutableLiveData<Account>()
    var hasNext = true

    val tootList = MutableLiveData<ArrayList<Toot>>()

    //onCreateで実行する指定
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        coroutineScope.launch {
            //UserCredentialオブジェクトを取得、UserCredentialが存在しなければメソッドを終了する
            userCredential = userCredentialRepository
                .find(instanceUrl, username) ?: return@launch
            //取得したuserCredentialオブジェクトを使ってTootRepositoryをインスタンス化
            tootRepository = TootRepository(userCredential)
            accountRepository = AccountRepository(userCredential)

            loadNext()
        }
    }

    fun clear() {
        val tootListSnapshot = tootList.value ?: return
        tootListSnapshot.clear()
    }

    fun loadNext() {
        coroutineScope.launch {
            //アカウント情報の取得を実行
            updateAccountInfo()

            isLoading.postValue(true)

            val tootListSnapshot = tootList.value ?: ArrayList()

            val maxId = tootListSnapshot.lastOrNull()?.id
            //タイムラインの種類に応じてRepositoryのメソッドを呼び分ける
            val tootListResponse = when (timelineType) {
                TimelineType.PublicTimeline -> {
                    tootRepository.fetchPublicTimeline(
                        maxId = maxId,
                        onlyMedia = true
                    )
                }
                TimelineType.HomeTimeline -> {
                    tootRepository.fetchHomeTimeline(
                        maxId = maxId
                    )
                }
            }

            tootListSnapshot.addAll(tootListResponse)
            tootList.postValue(tootListSnapshot)

            hasNext = tootListResponse.isNotEmpty()
            isLoading.postValue(false)
        }
    }

    private suspend fun updateAccountInfo() {
        //取得済みのアカウント情報が無ければAPIアクセスを実行
        val accountInfoSnapshot = accountInfo.value
            ?: accountRepository.verifyAccountCredential()

        accountInfo.postValue(accountInfoSnapshot)
    }
}
