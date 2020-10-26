package com.ziggy.rpc.common.protocol;

import java.util.Objects;

public class RpcResponse {
    private String requestID;
    private String error;
    private Object result;

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestID='" + requestID + '\'' +
                ", error='" + error + '\'' +
                ", result=" + result +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcResponse that = (RpcResponse) o;
        return Objects.equals(requestID, that.requestID) &&
                Objects.equals(error, that.error) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {

        return Objects.hash(requestID, error, result);
    }
}
