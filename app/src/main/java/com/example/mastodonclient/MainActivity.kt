package com.example.mastodonclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import com.example.mastodonclient.databinding.MainActivityBinding
//import android.widget.TextView
//import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

//        val textView: TextView = findViewById(R.id.textview)
//        textView.text = "Hello XML Layout!"
        val binding: MainActivityBinding = DataBindingUtil.setContentView(
            this,
            R.layout.main_activity
        )
        binding.textview.text = "Hello DataBinding!"
    }
}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MastodonClientTheme {
//        Greeting("Android")
//    }
//}