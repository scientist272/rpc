package test;

import com.sun.xml.internal.ws.util.CompletedFuture;
import io.netty.util.concurrent.CompleteFuture;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class CommonTest {

    @Test
    public void testGenDelay(){

        CompletableFuture<String> future = new CompletableFuture<>();

        CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("done");
        });

        try {
            System.out.println("wait to complete");
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
