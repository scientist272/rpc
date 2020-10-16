package netty;

import common.RpcResponse;

/**
 * 每次调用对应一个独立future，不然会有线程安全问题
 */
public class DefaultFuture {

    private  RpcResponse rpcResponse;

    private volatile boolean isSuccess = false;

    private final Object lock = new Object();

    public RpcResponse getRpcResponse(int timeout){

        synchronized (lock){
            while(!isSuccess){
                try {
                    lock.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rpcResponse;
        }
    }


    public void setResponse(RpcResponse rpcResponse){
        if(isSuccess){
            return;
        }
        synchronized (lock){
            this.rpcResponse = rpcResponse;
            this.isSuccess = true;

            lock.notify();
        }
    }


}
