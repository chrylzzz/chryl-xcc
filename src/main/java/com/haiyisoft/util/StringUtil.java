package com.haiyisoft.util;

import com.haiyisoft.constant.XCCConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created By Chryl on 2023-12-22.
 */
@Slf4j
public class StringUtil {

    /**
     * 去除业务数据有非法符号
     * 例如: YYSR#查询到您号码关联了地址为秀厢大道东段20#宝海公寓4#楼1单元4701房,的用电户，请问您是查询这一户吗？您可以说“是”或者“不是”,您请说
     *
     * @param todoText
     * @return
     */
    public static String removeAnswerIllegalCharacter(String todoText) {
        String removeAnswerIllegalCharacter = todoText.substring(0, 5).concat(
                todoText.substring(5).replaceAll(XCCConstants.NGD_SEPARATOR, StringUtils.EMPTY)
        );
        log.info("removeAnswerIllegalCharacter: {}", removeAnswerIllegalCharacter);
        return removeAnswerIllegalCharacter;
    }

    /**
     * 处理数字为中文汉字
     *
     * @param queryText
     * @return
     */
    public static String convertNumber2Ch(String queryText) {
        if ("0".equals(queryText)) {
            queryText = "零";
        } else if ("1".equals(queryText)) {
            queryText = "一";
        } else if ("2".equals(queryText)) {
            queryText = "二";
        } else if ("3".equals(queryText)) {
            queryText = "三";
        } else if ("4".equals(queryText)) {
            queryText = "四";
        } else if ("5".equals(queryText)) {
            queryText = "五";
        } else if ("6".equals(queryText)) {
            queryText = "六";
        } else if ("7".equals(queryText)) {
            queryText = "七";
        } else if ("8".equals(queryText)) {
            queryText = "八";
        } else if ("9".equals(queryText)) {
            queryText = "九";
        }
        return queryText;
    }

}
