# ThreadPool

* 线程池创建后默认没有线程，当有任务来的时候才会去创建线程。当线程数目达到 `corePoolSize` 后，会把任务放到缓存队列中。

* 当线程池中数量小于 `corePoolSize` 时，`keepAliveTime` 不起作用。只有当线程池中数量大于 `corePoolSize` 时，如果一个线程空闲的时间大于 `keepAliveTime` 才会起作用，直到线程池中的线程数量不大于 `corePoolSize`。

* 核心线程与非核心线程并无本质不同，对线程池来说都只是普通线程，线程池也不知道哪个线程是核心线程，不会对其做什么特殊操作。

* `largestPoolSize`  仅用来记录线程池中同时存在的最大线程数量

* 以下关于线程池状态的内容来自 **Android API 28 Platform**

  > runState 提供主要的生命周期控制，并具有以下值：
  >      RUNNING：接受新任务并处理排队的任务
  >      SHUTDOWN：不接受新任务，但处理排队的任务
  >      STOP：不接受新任务，不处理排队的任务以及中断进行中的任务
  >      TIDYING：所有任务已终止，workerCount 为零，线程转换为 TIDYING 状态将运行 Terminate() 挂钩方法
  >     TERMINATED： terminate() 已完成
  >      这些值之间的数字顺序很重要，可以进行有序的比较。 runState 随时间单调增加，但不必达到每个状态。过渡是：
  >      RUNNING -> SHUTDOWN
  >      在调用 shutdown() 时，可能隐式在 finalize() 中
  >      (RUNNING or SHUTDOWN) -> STOP
  >      在调用shutdownNow()时
  >      SHUTDOWN -> TIDYING
  >      当队列和池都为空时
  >      STOP -> TIDYING
  >      当池为空时
  >      TIDYING -> TERMINATED
  >      当Terminate()挂钩方法完成时
  >      当状态达到 TERMINATED 时，在 awaitTermination() 中等待的线程将返回。
  >      检测从 SHUTDOWN 到 TIDYING 的过渡并不像您想要的那样简单，因为在 SHUTDOWN 状态期间队列可能在非空之后变为空，反之亦然，因此队列可能变为空，但是只有在看到它为空之后，我们才能终止workerCount 为 0（有时需要重新检查-参见下文）。

* `ThreadPoolExecutor` 核心方法 `execute()`：

  ```java
  public void execute(Runnable command) {
      if (command == null)
          throw new NullPointerException();
      int c = ctl.get();
      // 有效的线程数小于核心线程数
      if (workerCountOf(c) < corePoolSize) {
          // 将 command 作为核心任务加入队列
          if (addWorker(command, true))
              return;
          // 加入队列失败，重新读 ctl
          c = ctl.get();
      }
      // ctl 状态为 RUNNING，任务队列中有容量可以插入命令
      if (isRunning(c) && workQueue.offer(command)) {
          // 再次重新读 ctl
          int recheck = ctl.get();
          // 若线程池状态不为 RUNNING，workQueue 中移除任务
          if (!isRunning(recheck) && remove(command)) {
              // 拒绝任务
            reject(command);
              // 线程池有效线程数为 0
          } else if (workerCountOf(recheck) == 0) {
              addWorker(null, false);
          }
          // 二次判断，任务作为非核心任务加入队列，失败时拒绝任务
      } else if (!addWorker(command, false)) {
          // 拒绝任务
          reject(command);
      }
  }
  ```
  
* `ThreadPoolExecutor` 中  `ctl` 变量

  ```java
  private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
  // 低 29 位存储有效线程数
  private static final int COUNT_BITS = Integer.SIZE - 3;
  // 1 左移 29 位 - 1 ==> 2 ^ 29 - 1
  // 00011111111111111111111111111111
  private static final int CAPACITY = (1 << COUNT_BITS) - 1;
  
  // runState is stored in the high-order bits
  // 高 3 位存储线程池状态。
  // 因为有 5 种状态，所以需要 3 位用来存储。
  private static final int RUNNING = -1 << COUNT_BITS;
  private static final int SHUTDOWN = 0 << COUNT_BITS;
  private static final int STOP = 1 << COUNT_BITS;
  private static final int TIDYING = 2 << COUNT_BITS;
  private static final int TERMINATED = 3 << COUNT_BITS;
  
  // Packing and unpacking ctl
  // c 与上 CAPACITY 取反，得到高三位线程池状态
  private static int runStateOf(int c) {
  	return c & ~CAPACITY;
  }
  
  // c 与上 CAPACITY，得到低 29 位有效线程数
  private static int workerCountOf(int c) {
  	return c & CAPACITY;
  }
  ```

* 



















## Before

> [问题来源](https://www.jianshu.com/p/5f7c7c53450c)

### Q1. 线程池如何实现

#### A:

线程任务队列、核心线程数量、最大线程数量；延时、定时策略


### Q2. 非核心线程延迟死亡，如何做到

#### A:

不知道

### Q3. 核心线程为什么不会死

#### A:

不知道 重新唤起？


### Q4. 如何释放核心线程

#### A:

不知道

### Q5. 非核心线程能成为核心线程吗

#### A:

不知道  可以？


### Q6. Runnable在线程池里如何执行

#### A:

execute

### Q7. 线程数如何做选择

#### A:

cpu 核心数  2 - 1  为啥？不知道


### Q8. 常见的不同类型的线程池的功效如何做到

#### A:

控制核心线程数量、最大线程数量、运行策略

  