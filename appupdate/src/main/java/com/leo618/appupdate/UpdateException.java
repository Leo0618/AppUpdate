package com.leo618.appupdate;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

@SuppressWarnings("ALL")
public class UpdateException extends Exception {

    private int state = -1;

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(String message, int state) {
        super(message);
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "UpdateException{" +
                "state=" + state + "\n" +
                "message=" + getMessage() + "\n" +
                ((getLocalizedMessage() != null) ? (getClass().getName() + ": " + getLocalizedMessage()) : getClass().getName()) +
                '}';
    }
}

