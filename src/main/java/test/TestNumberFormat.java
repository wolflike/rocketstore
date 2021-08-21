package test;

import org.junit.Test;

import java.text.NumberFormat;

/**
 * @author 28293
 */
public class TestNumberFormat {

    @Test
    public void test(){
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(20);
        format.setMinimumIntegerDigits(20);
        format.setMinimumFractionDigits(0);
        System.out.println(format.format(0));
    }
}
