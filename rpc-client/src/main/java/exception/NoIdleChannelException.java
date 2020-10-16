package exception;

public class NoIdleChannelException extends Exception {
    public NoIdleChannelException() {
        super("No Idle Channels");
    }
}
