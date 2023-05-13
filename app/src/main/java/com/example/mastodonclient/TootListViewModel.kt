package com.example.mastodonclient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TootListViewModel(
    private val instanceUrl: String,
    private val username: String,
    private val coroutineScope: CoroutineScope,
    application: Application
    //AndroidViewModelを継承、LifecycleObserverを実装
) : AndroidViewModel(application), LifecycleObserver {

    private val userCredentialRepository = UserCredentialRepository(
        application
    )
    private lateinit var tootRepository: TootRepository

    private lateinit var userCredential: UserCredential

    val isLoading = MutableLiveData<Boolean>()
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

            loadNext()
        }
    }

    fun clear() {
        val tootListSnapshot = tootList.value ?: return
        tootListSnapshot.clear()
    }

    fun loadNext() {
        coroutineScope.launch {
            isLoading.postValue(true)

            val tootListSnapshot = tootList.value ?: ArrayList()

            val maxId = tootListSnapshot.lastOrNull()?.id
            //公開タイムライン取得を、ホームタイムライン取得に変更
            val tootListResponse = tootRepository.fetchHomeTimeline(
                maxId = maxId
            )
            tootListSnapshot.addAll(tootListResponse)
            tootList.postValue(tootListSnapshot)

            hasNext = tootListResponse.isNotEmpty()
            isLoading.postValue(false)
        }
    }
}
