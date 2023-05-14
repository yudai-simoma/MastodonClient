package com.example.mastodonclient.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.MainActivityBinding
import com.example.mastodonclient.ui.toot_list.TimelineType
import com.example.mastodonclient.ui.toot_list.TootListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: MainActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.main_activity)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            val fragment = when (it.itemId) {
                //直接インスタンス化せず、newInstanceにタイムラインの種類を指定
                R.id.menu_home -> {
                    TootListFragment.newInstance(TimelineType.HomeTimeline)
                }
                R.id.menu_public -> {
                    TootListFragment.newInstance(TimelineType.PublicTimeline)
                }

                else -> null
            }
            fragment ?: return@setOnNavigationItemSelectedListener false

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    fragment,
                    TootListFragment.TAG
                )
                .commit()

            return@setOnNavigationItemSelectedListener true
        }

        if (savedInstanceState == null) {
            //初期状態で表示するFragmentはホームタイムラインとする
            val fragment = TootListFragment.newInstance(
                TimelineType.HomeTimeline
            )
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
