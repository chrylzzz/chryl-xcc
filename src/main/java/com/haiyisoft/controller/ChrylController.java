package com.haiyisoft.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiyisoft.boot.IVRInit;
import com.haiyisoft.config.ThreadPoolConfig;
import com.haiyisoft.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Chr.yl on 2023/3/6.
 *
 * @author Chr.yl
 */
@Slf4j
@RestController
@RequestMapping("xcc")
public class ChrylController {

    /**
     * 查看配置信息
     *
     * @param response
     * @throws InterruptedException
     * @throws IOException
     */
    @GetMapping("chryl")
    public void chryl(HttpServletResponse response) throws InterruptedException, IOException {
//        String loadYml = JSON.toJSONString(IVRInit.CHRYL_CONFIG_PROPERTY, JSONWriter.Feature.PrettyFormat);
        ObjectMapper mapper = new ObjectMapper();
        String jackLoadYml = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(IVRInit.CHRYL_CONFIG_PROPERTY);

        log.info("loadYml : {}", jackLoadYml);
        response.getWriter().write(jackLoadYml);
        response.flushBuffer();
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * info
     *
     * @return
     */
    @GetMapping("info")
    public JSONObject info() {
        JSONObject infoJsonData = new JSONObject();
        infoJsonData.put("cpu_num", ThreadPoolConfig.CPU_NUM);
        infoJsonData.put("nei_ip", IpUtil.INTRANET_IP);
        infoJsonData.put("wai_ip", IpUtil.INTERNET_IP);
        log.info("info : {}", infoJsonData);
        return infoJsonData;
    }

}
