package com.uangel.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void camelToSnakeTest() {
        String name = "DialogStartReq";
        System.out.println(StringUtil.camelToSnake(name));

        Assert.assertEquals(StringUtil.snakeToCamel(name), name);
    }

    @Test
    public void snakeToCamelTest() {
        String name = "incoming_call_req";
        System.out.println(StringUtil.snakeToCamel(name));

        Assert.assertEquals(StringUtil.camelToSnake(name), name);

        name = "INCOMING_CALL_REQ";
        System.out.println(StringUtil.snakeToCamel(name));
        System.out.println(StringUtil.camelToSnake(name));
    }
}
