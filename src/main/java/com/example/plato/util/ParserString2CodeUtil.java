package com.example.plato.util;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 10:08 上午
 */
@Slf4j
public class ParserString2CodeUtil {

    public static Object parserString2Code(String varParam, Map<String, Object> varMap) {
        if (StringUtils.isBlank(varParam) || MapUtils.isEmpty(varMap)) {
            return null;
        }
        JexlEngine jexlEngine = new JexlEngine();
        JexlContext jexlContext = new MapContext();
        varMap.forEach((k, v) -> jexlContext.set(k, v));
        try {
            Expression expression = jexlEngine.createExpression(varParam);
            return expression.evaluate(jexlContext);
        } catch (Exception e) {
            log.error("parserString2Code解析异常varParam:{},varMap:{}",
                    varParam,
                    PlatoJsonUtil.toJson(varMap));
        }
        return null;
    }

    /*public static void main(String[] args) {
        String expression = "count >= 11 && count <= 3000";
        Map<String, Object> map = new HashMap<>();
        map.put("count", 25100);
        Object code = parserString2Code(expression, map);
        System.out.println(code);
    }*/
}
