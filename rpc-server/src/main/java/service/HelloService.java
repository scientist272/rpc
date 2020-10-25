package service;

import org.springframework.stereotype.Component;
import test.HelloI;

@Component
public class HelloService implements HelloI {

    @Override
    public String sayHello(String arg) {
        return "hello"+arg;
    }
}
