package com.haiyisoft.xcc;

import com.alibaba.fastjson2.JSON;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.model.IVRModel;
import com.haiyisoft.util.DateUtil;
import com.haiyisoft.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ChrylXccApplicationTests {

    @Test
    void contextLoads() {

    }

    /**
     * 保存意图
     */
    @Test
    public void show() {
        String ivrStartTime = DateUtil.getLocalDateTime();
        String cidPhoneNumber = "13287983898";
        String fsCallerId = "55896-0aasffa";
        String icdCallerId = "55526-789962";
        String intent = "#DFYT#TDYT";

        IVRModel ivrModel = new IVRModel(cidPhoneNumber, fsCallerId, icdCallerId, ivrStartTime, intent, "", "", "");
        String jsonParam = JSON.toJSONString(ivrModel);
        log.info("SaveZnIVRLhytForGx,pms接口入参:{}", jsonParam);
        final String url = IVRInit.CHRYL_CONFIG_PROPERTY.getPmsUrl() + XCCConstants.SAVE_INTENT_URL;
        log.info("url:{}", url);
        String postJson = HttpClientUtil.doPostJson(url, jsonParam);
        log.info("SaveZnIVRLhytForGx,pms接口出参:{}", postJson);
    }

}
