package utils;

import java.text.NumberFormat;

/**
 * @author 28293
 */
public class UtilAll {

    public static String offset2FileName(final long offset){
        final NumberFormat format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(20);
        format.setMinimumIntegerDigits(20);
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(false);
        return format.format(offset);
    }
}
