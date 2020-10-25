package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import proxy.ProxyFactory;
import test.HelloI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan(basePackages = {"netty","service"})
public class SpringbootApplication{

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args)
    {
        SpringApplication.run(SpringbootApplication.class,args);
        try {
            HelloI helloI = ProxyFactory.getProxy(HelloI.class);
            for (int i = 0; i < 4; i++) {
                executorService.execute(()->{
                    System.out.println(
                            Thread.currentThread().getName()+":"+
                            helloI.sayHello("arg "+Thread.currentThread().getName()));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
