package com.example.mastodonclient.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.example.mastodonclient.entity.UserCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCredentialRepository(
    private val application: Application
) {

    companion object {
        //SharedPreferencesに値を出し入れするときに使うキーを定義
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    //Mastodonインスタンス毎のSharedPreferencesオブジェクトを返す
    private fun getPreference(instanceUrl: String): SharedPreferences? {
        //instanceUrlからホスト名を抜き出す
        val hostname = Uri.parse(instanceUrl).host
            ?: return null
        //ホスト名を元にSharedPreferencesのファイル名を構築
        val filename = "{$hostname}.dat"
        //SharedPreferencesオブジェクトを作成。アクセス権はこのアプリに限定
        return application.getSharedPreferences(
            filename,
            Context.MODE_PRIVATE)
    }

    //アクセストークンをSharedPreferencesオブジェクトに保存
    suspend fun set(
        userCredential: UserCredential
    ) = withContext(Dispatchers.IO) {
        val pref = getPreference(userCredential.instanceUrl)
        pref?.edit {
            putString(KEY_ACCESS_TOKEN, userCredential.accessToken)
        }
    }

    //インスタンスURLとユーザー名で検索してUserCredentialのオブシェくとを返す
    //存在しない場合はnullを返す
    suspend fun find(
        instanceUrl: String,
        username: String
    //SharedPreferencesにはMainスレッドでアクセスする
    ): UserCredential? = withContext(Dispatchers.Main) {
        //SharedPreferencesオブジェクトからアクセストークンを取り出して、UserCredentialオブジェクトとして返す
        val pref = getPreference(instanceUrl)
            ?: return@withContext null

        val accessToken = pref.getString(KEY_ACCESS_TOKEN, null)
            ?: return@withContext null

        return@withContext UserCredential(instanceUrl, username, accessToken)
    }
}
