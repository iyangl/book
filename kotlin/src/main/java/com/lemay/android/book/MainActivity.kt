package com.lemay.android.book

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var intercept = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchTest()

        Thread {
            while (true) {
                if (!intercept)
                    println("while continue")
            }
        }.start()
    }

    private fun launchTest() {
        GlobalScope.launch(Dispatchers.Main) {
            println("start main scope")
            launchTestIO()
            println("continue main scope")
            intercept = true
        }
    }

    private suspend fun launchTestIO() = withContext(Dispatchers.IO) {
        SystemClock.sleep(4000)
        println("exit sleep")
    }


}
