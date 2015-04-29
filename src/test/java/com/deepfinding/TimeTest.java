package com.deepfinding;
import java.util.concurrent.*;

/**
* Created by LKJie on 2015/4/25.
*/
public class TimeTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        System.out.println("不告诉你。");
                        Thread.sleep(1000 * 3);
                        System.out.println("8888888888");
                        return false;
                    }
                });
        try {
            executorService.submit(futureTask).get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("超时了吧~~~");
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }
}
