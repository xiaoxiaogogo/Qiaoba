package com.xiaoxiao.qiaobademo;

import android.net.Uri;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Class.forName("com.xiaoxiao.qiaobademo.TestCallback");
        System.out.println(Demo.class);

        String str = String.format("boolean 类型 : %b", false);
        System.out.println(str);
        assertEquals(4, 2 + 2);
    }

    public interface Demo{

    }
}