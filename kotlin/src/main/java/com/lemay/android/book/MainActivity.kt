package com.lemay.android.book

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var intercept = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*launchTest()

        Thread {
            while (true) {
                if (!intercept)
                // println("while continue")
                    SystemClock.sleep(1)
            }
        }.start()*/

        /*launchFunction()

        asyncFunction()*/

        allCoroutineSuspendFunction()
    }

    //*************************************launch*********************************************

    private fun launchTest() {
        //TODO 这里的打印顺序是反的 为啥？
        // Dispatchers.Main 是以给主线程 handle 添加任务的方式现实在主线程上的运行的
        GlobalScope.launch {
            GlobalScope.launch(Dispatchers.Main) {
                println("start main scope")
                println(launchTestIO())
            }
            GlobalScope.launch(Dispatchers.Main) {
                println("continue main scope")
                println(launchTestIO())
                intercept = true
            }
            println("-------------")
        }
    }

    // withContext 不创建新的协程，在指定协程上运行代码块
    private suspend fun launchTestIO(): Int = withContext(Dispatchers.IO) {
        delay(4000)
        println("exit sleep")
        return@withContext 1
    }

    /**
     * {@link CoroutineScope.launch} 的三个入参
     * <p>
     *  CoroutineContext：这里的 context 是协程的上下文，
     *  <strong>自己传入的协程上下文就是线程池，可以自己创建</strong>
     *  有四种模式：
     *      Dispatchers.Default - 默认值，不传就是 Default。
     *      Dispatchers.IO - IO线程
     *      Dispatchers.Main - 主线程
     *      Dispatchers.Unconfined - 没指定，就是在当前线程
     * </p>
     * <p>
     *  CoroutineStart：启动模式。默认为 DEFAULT
     *      DEFAULT - 默认模式，创建就启动
     *      LAZY - 当你需要的时候才会启动
     *      ATOMIC - 自动的根据上下文调动协程执行，与 DEFAULT 类似，但在开始前不能取消协程
     *      UNDISPATCHED - 实验 API，使用此模式时，协程语义可能发生变化。
     * </p>
     * <p>
     *   block：闭包方法体，协程内执行的代码
     * </p>
     */
    private fun launchFunction() {
        // 协程构建函数的返回值，可以看做协程对象
        val job = GlobalScope.launch(Dispatchers.Default, CoroutineStart.LAZY) {
            // 这里是 block
            println("这是一个 LAZY 模式启动的协程")
        }
        // 启动协程，只有 LAZY 模式需要手动启动
        job.start()
        // 取消协程
        job.cancel()
    }

    /**
     * async 是有返回值的 launch
     * <strong>async 会阻塞外部协程，但不会阻塞线程</strong>
     */
    private fun asyncFunction() {
        GlobalScope.launch {
            // 有返回值的协程
            val deferred = GlobalScope.async(Dispatchers.Main, CoroutineStart.LAZY) {
                println("这是一个 LAZY 模式有返回值的 async 协程")
                delay(500)
                return@async "asyncFunction"
            }
            println("这里会在协程之前执行")
            // await 为 deferred 协程闭包的返回值
            val await = deferred.await()
            println("async 协程闭包返回值 await: $await")
        }
    }

    //******************线程内所有协程被挂起，且无可执行代码，线程处于什么状态********************

    /**
     * 1.协程不会抢占 cpu，只会在 cpu 空闲的时候执行，从 suspend 状态 resume 后如果有正在执行的协程，会等待
     * 2.Dispatchers.Main 是利用主线程中 handler 发消息，会等待主线程中消息处理完成后执行，所以导致执行顺序落后
     * 3.所有协程都被挂起时，线程处于存活状态
     *
     *
     * *****************************协程被挂起后恢复在哪个线程*****************************
     *
     *                   Default    |    Main    |    IO         <-- 外层协程上下文模式
     *               ==========================================
     *  Default    ||    Default    |    Main    |   Default   ||
     *  -------    || -----------------------------------------||
     *    Main     ||    Default    |    Main    |   Default   ||
     *  -------    || -----------------------------------------||
     *     IO      ||    Default    |    Main    |   Default   ||
     *               ==========================================
     *     ↑
     * 内层协程上下文模式
     *
     * 注：表格内 Default 代表 DefaultDispatcher 默认线程池。
     *     当前测试未指定线程池。
     */
    private fun allCoroutineSuspendFunction() {
        val thread = Thread {

        }
        thread.start()
        GlobalScope.launch(Dispatchers.Default) {
            println("allCoroutineSuspendFunction 协程开始")
            GlobalScope.launch(Dispatchers.Main) {
                suspendFunction()
            }
            GlobalScope.launch(Dispatchers.IO) {
                suspendFunction2()
            }
            GlobalScope.launch(Dispatchers.Default) {
                commonFunction()
            }
            GlobalScope.launch(Dispatchers.Unconfined) {
                println("Unconfined end  thread：${Thread.currentThread().name}")
            }
        }
        for (i in 0..10) {
            println("thread 的状态：${thread.isAlive}")
        }
    }

    private suspend fun suspendFunction() {
        println("suspendFunction AAAAAAAA start thread：${Thread.currentThread().name}")
        delay(1)
        println("suspendFunction AAAAAAAA end  thread：${Thread.currentThread().name}")
    }

    private suspend fun suspendFunction2() {
        println("suspendFunction BBBBBBBB start thread：${Thread.currentThread().name}")
        delay(2)
        println("suspendFunction BBBBBBBB end  thread：${Thread.currentThread().name}")
    }

    private fun commonFunction() {
        println("common end  thread：${Thread.currentThread().name}")
    }

}
