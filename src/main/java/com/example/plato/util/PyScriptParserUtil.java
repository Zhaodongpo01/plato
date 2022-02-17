package com.example.plato.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.example.plato.exception.PlatoException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/13 7:29 下午
 */
@Slf4j
public class PyScriptParserUtil {

    /**
     * @param args = new String[] {"python", "/Users/zhaodongpo/plato/plato/src/main/resources/python", "快手",
     * "wb_liuyanmei"};
     */
    public static boolean runPyScript(String[] args) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            throw new PlatoException(e, "runPyScript exec error");
        }
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
        } catch (UnsupportedEncodingException e) {
            throw new PlatoException(e, "runPyScript UnsupportedEncodingException error");
        }
        String line;
        while (true) {
            try {
                if (!((line = in.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                throw new PlatoException(e, "");
            }
            log.info("runPyScript read line:{}", line);
        }
        try {
            in.close();
        } catch (IOException e) {
            throw new PlatoException(e, "close error");
        }
        try {
            return process.waitFor() == 1;
        } catch (InterruptedException e) {
            throw new PlatoException(e, "runPyScript InterruptedException");
        }
    }
}
