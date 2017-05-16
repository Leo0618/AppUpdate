package com.leo618.appupdate;

/**
 * function:
 *
 * <p></p>
 * Created by lzj on 2016/12/13.
 */

@SuppressWarnings("ALL")
public final class UpdateConst {
    public static final int STATE_RESULT_OK = 0x0000;
    public static final int STATE_RESULT_ERROR = 0x0001;
    public static final int STATE_RESULT_NULL = STATE_RESULT_ERROR + 1;
    public static final int STATE_RESULT_SERVER_RESP_ERROR = STATE_RESULT_ERROR + 2;

}
