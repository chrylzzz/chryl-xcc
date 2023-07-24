package com.haiyisoft.advice;

import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.entry.XCCEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Chr.yl on 2023/6/3.
 *
 * @author Chr.yl
 */
@Slf4j
public class IVRExceptionAdvice {

    /**
     * handle invoke xcc exception:
     *
     * @param method
     * @param e
     * @return
     */
    public static XCCEvent handleException(String method, Exception e) {
        e.printStackTrace();
        log.error("你的代码报错啦: method: {} , exception: {} ", method, e);
        /**
         * 报错直接转人工
         */
        XCCEvent xccEvent = new XCCEvent();
        xccEvent.setCode(XCCConstants.CHRYL_ERROR_CODE);
        xccEvent.setMessage("你的代码报错啦: " + method + e);
//        xccEvent.setXccRecognitionResult("你的代码报错啦: " + xccEvent.getXccRecognitionResult());
//        xccEvent.setType("你的代码报错啦: " + xccEvent.getType());
//        xccEvent.setError("你的代码报错啦: " + xccEvent.getError());
        log.error("handleException 异常处理 xccEvent: {}", xccEvent);
        return xccEvent;
    }
}
