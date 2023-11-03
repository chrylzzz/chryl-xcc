package com.haiyisoft.model;

import com.alibaba.fastjson2.JSONArray;
import com.haiyisoft.constant.XCCConstants;
import com.haiyisoft.util.DateUtil;
import lombok.Data;

/**
 * ngd node metadata
 * Created by Chr.yl on 2023/6/15.
 *
 * @author Chr.yl
 */
@Data
public class NGDNodeMetaData {
    //queryText
    private String query;
    // query time
    private String queryTime;
    //suggestAnswer 处理之后的
    private String answer;
    // answer time
    private String answerTime;
    //source
    private String source;
    //solved
    private boolean solved;
    //节点内容
    private JSONArray dialogArray;

    public NGDNodeMetaData() {
        this.dialogArray = new JSONArray();
    }

    public NGDNodeMetaData(String query, String answer) {
        this.dialogArray = new JSONArray();
        this.query = query;
        this.queryTime = DateUtil.getLocalDateTime();
        this.answer = answer;
        this.answerTime = DateUtil.getLocalDateTime();
    }

    public void setAnswer(String answer) {
        //去除#
        if (answer.contains(XCCConstants.NGD_SEPARATOR)) {//有#
            String[] split = answer.split(XCCConstants.NGD_SEPARATOR);
            //AJSR#
            if (split.length > 1) {//有#有内容
                this.answer = answer.split(XCCConstants.NGD_SEPARATOR)[1];
            } else {//有#无内容
                this.answer = answer;
            }
        } else {//无#
            this.answer = answer;
        }
    }

}
