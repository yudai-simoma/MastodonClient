package com.example.mastodonclient.repository

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaFileRepository(application: Application) {

    //ContentProviderが管理するデータへアクセスするためのオブジェクト
    private val contentResolver = application.contentResolver

    //アプリケーション固有のデータ領域を示すFileオブジェクト
    private val saveDir = application.filesDir

    //mediaUriが示す画像データを読み込んでBitmapオブジェクトを返す
    suspend fun readBitmap(
        mediaUri: Uri
    ): Bitmap = withContext(Dispatchers.IO) {
        @Suppress("DEPRECATION")
        //メディアを管理するContentProviderへのアクセスを提供するユーティリティクラス
        return@withContext MediaStore.Images.Media.getBitmap(
            contentResolver,
            mediaUri
        )
    }

    //Bitmapオブジェクトを一時ファイルとして保存してFileオブジェクトを返す
    suspend fun saveBitmap(
        bitmap: Bitmap
    ): File = withContext(Dispatchers.IO) {
        //ファイル名がmediaから始まり、.jpgで終わる一時ファイルを作成
        val tempFile = createTempFile(
            directory = saveDir,
            prefix = "media",
            suffix = ".jpg"
        )
        //一時ファイルを書き込み用に開く
        FileOutputStream(tempFile).use {
            //BitmapオブジェクトをJPEGでエンコードして保存
            bitmap.compress(
                Bitmap.CompressFormat.JPEG, 100, it)
        }
        return@withContext tempFile
    }
}
