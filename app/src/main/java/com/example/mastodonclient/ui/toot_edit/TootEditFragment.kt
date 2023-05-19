package com.example.mastodonclient.ui.toot_edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.example.mastodonclient.BuildConfig
import com.example.mastodonclient.R
import com.example.mastodonclient.databinding.FragmentTootEditBinding
import com.example.mastodonclient.ui.login.LoginActivity

class TootEditFragment : Fragment(R.layout.fragment_toot_edit) {

    companion object {
        val TAG = TootEditFragment::class.java.simpleName

        private const val REQUEST_CODE_LOGIN = 0x01
        //画像選択画面からの結果を識別するためのリクエストコードを定義
        private const val REQUEST_CHOOSE_MEDIA = 0x02

        //Fragmentのインスタンス生成用メソッド
        fun newInstance(): TootEditFragment {
            return TootEditFragment()
        }
    }

    private var binding: FragmentTootEditBinding? = null

    private val viewModel: TootEditViewModel by viewModels {
        TootEditViewModelFactory(
            BuildConfig.INSTANCE_URL,
            BuildConfig.USERNAME,
            lifecycleScope,
            requireContext()
        )
    }

    //投稿完了をActivtyに伝えるコールバック
    interface Callback {
        fun onPostComplete()
    }

    //コールバックを保持するプロパティ。nullの場合がある
    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)

        //表示したActivityがCallbackを実装しているか検査してコールバックを保持
        if (context is Callback) {
            callback = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingData: FragmentTootEditBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        //View用のLifecycleOwnerを指定
        bindingData.lifecycleOwner = viewLifecycleOwner
        //DataBindingオブジェクトにviewModelを結びつける
        bindingData.viewModel = viewModel

        //画像の添付ボタンをタップした時のイベントリスナー
        bindingData.addMedia.setOnClickListener {
            openMediaChooser()
        }

        viewModel.loginRequired.observe(viewLifecycleOwner, Observer {
            if (it) {
                launchLoginActivity()
            }
        })
        //投稿完了時にtoastを表示
        viewModel.postComplete.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "投稿完了しました", Toast.LENGTH_LONG).show()
            //コールバックを通じてActivityに投稿完了を伝える
            callback?.onPostComplete()
        })
        //ツールバーのメニューを初期化するためのメソッド
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
        })

    }

    //画像選択画面を呼び出す
    private fun openMediaChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CHOOSE_MEDIA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val uri = data?.data
        if (requestCode == REQUEST_CHOOSE_MEDIA
            && resultCode == Activity.RESULT_OK
            && uri != null) {
            viewModel.addMedia(uri)
        }
    }

    private fun launchLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_LOGIN)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.toot_edit, menu)
    }

    //ツールバー上のメニューが選択された時のイベントを受け取るメソッド
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_post -> {
                //投稿処理を実行
                viewModel.postToot()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }
}
