package test;

import org.junit.Test;
import com.ziggy.rpc.client.proxy.ProxyFactory;

import java.util.concurrent.CompletableFuture;


public class CommonTest {

    private final Object lock = new Object();

    @Test
    public void testGenDelay() {

        CompletableFuture<String> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("done");
        });

//        try {
//            System.out.println("wait to complete");
//            System.out.println(future.get());
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }


        future.whenCompleteAsync((message, e) -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println(message + " " + Thread.currentThread().getName());
                    synchronized (lock){
                        lock.notify();
                    }
            }
        );


        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void testClient(){
        try {
            test.Test test = ProxyFactory.getProxy(test.Test.class);
            test.Test("test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
