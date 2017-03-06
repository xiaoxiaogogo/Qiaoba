package com.xiaoxiao.qiaoba;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Class p = Person.class;
        Class s = Student.class;
        Field name = Person.class.getDeclaredField("name");
        name.setAccessible(true);
        System.out.println(name.getType());

        assertEquals(4, 2 + 2);
    }
}

class Person{
    String name;
}

class Student extends Person{

}