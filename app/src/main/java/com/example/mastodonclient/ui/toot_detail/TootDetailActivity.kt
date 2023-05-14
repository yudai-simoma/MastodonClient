package com.example.mastodonclient.ui.toot_detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mastodonclient.R
import com.example.mastodonclient.entity.Toot
import com.example.mastodonclient.ui.toot_list.TootListFragment

class TootDetailActivity : AppCompatActivity() {

    companion object {
        private const val KEY_TOOT = "key_toot"

        @JvmStatic
        //Activityの起動をするにはIntentのインスタンスが必要
        fun newIntent(context: Context, toot: Toot): Intent {
            //起動するActivityのClassオブジェクトを指定してIntentのインスタンスを生成
            return Intent(context, TootDetailActivity::class.java).apply {
                putExtra(KEY_TOOT, toot)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toot_detail)

        //IntentオブジェクトにTootオブジェクトをParcelableとして入れる
        val toot = intent?.getParcelableExtra<Toot>(KEY_TOOT) ?: return

        if (savedInstanceState == null) {
            //TootDetailFragmentを表示
            val fragment = TootDetailFragment.newInstance(toot)
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragment,
                    TootListFragment.TAG
                )
                .commit()
        }
    }

}
