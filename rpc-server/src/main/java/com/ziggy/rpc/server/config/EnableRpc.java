package com.ziggy.rpc.server.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RpcConfig.class})
public @interface EnableRpc {
}
