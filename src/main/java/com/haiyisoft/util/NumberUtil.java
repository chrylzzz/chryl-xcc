package com.haiyisoft.util;

import com.haiyisoft.constant.XCCConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Chr.yl on 2023/6/4.
 *
 * @author Chr.yl
 */
@Slf4j
public class NumberUtil {

    /**
     * 对比 xcc code = 6xx
     *
     * @param code
     * @return
     */
    public static boolean compareNum(int code) {
        int b = code / 100;
        if (b == XCCConstants.JSONRPC_CODE_SYSTEM_ERROR) {
            log.info("这两位数的首位相等");
            return true;
        } else {
//            System.out.println("这两位数的首位不相等");
            return false;
        }
    }
}
