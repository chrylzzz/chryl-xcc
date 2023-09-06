package com.haiyisoft.util;

import com.haiyisoft.constant.XCCConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Chr.yl on 2023/6/2.
 *
 * @author Chr.yl
 */
@Slf4j
public class Dom4jUtil {

    /**
     * 广西智能IVR专用
     * 解析ASR返回的XML
     * XSwitch目前只将阿里/迅飞返回的JSON解析,XML只透传了结果
     *
     * @param xmlStr
     * @return
     */
    public static String parseAsrResXml(String xmlStr) {
        /*
         <?xml version="1.0"?>
         <result>
             <interpretation grammar="builtin:grammar/boolean?language=zh-CN;y=1;n=2 builtin" confidence="1.0">
             <instance>广西</instance>
             <input mode="speech">广西</input>
             </interpretation>
         </result>
       */

        String parseText = "";
        if (StringUtils.isBlank(xmlStr)) {
            log.info("parseAsrResXml 解析 xmlStr 数据为空");
        } else {
            log.info("parseAsrResXml 解析 xmlStr 开始 : [{}]", xmlStr);
            try {
                xmlStr = xmlStr.replace(XCCConstants.NL, "");
                System.out.println(xmlStr);
                xmlStr = xmlStr.replace(XCCConstants.ESCAPE_CHARACTER, "");
                System.out.println(xmlStr);
                Document document = DocumentHelper.parseText(xmlStr);
                Element root = document.getRootElement();
                List<Element> elements = root.elements();
                a:
                for (Element element : elements) {
                    for (Iterator<Element> it = element.elementIterator(); it.hasNext(); ) {
                        Element e = it.next();
                        String name = e.getName();
                        //<input></input>里就是识别结果
                        if (XCCConstants.INPUT.equals(name)) {
                            parseText = e.getTextTrim();
                            break a;
                        }
                    }
                }
                log.info("parseAsrResXml 解析 XML 完成: {}", parseText);
            } catch (Exception e) {
                log.error("解析 XML 失败: {}" , e);
                e.printStackTrace();
            }
        }
        return parseText;
    }

    public static void main(String[] args) {
        String str = null;
//        String str = "<?xml version=\\\"1.0\\\"?>\\n<result>\\n <interpretation grammar=\\\"builtin:grammar/boolean?language=zh-CN;y=1;n=2 builtin\\\" confidence=\\\"1.0\\\">\\n    <instance>广西</instance>\\n    <input mode=\\\"speech\\\">广西</input>\\n  </interpretation>\\n</result>";
//        String str = "<?xml version=\\\"1.0\\\"?>\\n<result>\\n <interpretation grammar=\\\"builtin:grammar/boolean?language=zh-CN;y=1;n=2 builtin\\\" confidence=\\\"1.0\\\">\\n    <instance></instance>\\n    <input mode=\\\"speech\\\"></input>\\n  </interpretation>\\n</result>";

        String s = parseAsrResXml(str);
        System.out.println(s);


    }
}
