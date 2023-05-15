package com.example.mastodonclient.repository

import android.app.Application
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.entity.UserCredential
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

        return@withContext null
    }

}
