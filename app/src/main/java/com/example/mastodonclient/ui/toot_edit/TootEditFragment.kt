package com.example.mastodonclient.ui.toot_edit

import android.content.Context
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

class TootEditFragment : Fragment(R.layout.fragment_toot_edit) {

    companion object {
        val TAG = TootEditFragment::class.java.simpleName

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bindingData: FragmentTootEditBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        //View用のLifecycleOwnerを指定
        bindingData.lifecycleOwner = viewLifecycleOwner
        //DataBindingオブジェクトにviewModelを結びつける
        bindingData.viewModel = viewModel

        //投稿完了時にtoastを表示
        viewModel.postComplete.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "投稿完了しました", Toast.LENGTH_LONG).show()
        })
        //ツールバーのメニューを初期化するためのメソッド
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
        })

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
