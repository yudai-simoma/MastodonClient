package com.example.mastodonclient

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCredentialRepository(
    private val application: Application
) {

    //インスタンスURLとユーザー名で検索してUserCredentialのオブシェくとを返す
    //存在しない場合はnullを返す
    suspend fun find(
        instanceUrl: String,
        username: String
    ): UserCredential? = withContext(Dispatchers.IO) {

        //BuildConfigの定数を元に固定値を返す
        return@withContext UserCredential(
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            BuildConfig.ACCESS_TOKEN
        )
    }

}
