package org.moon.chatutilities.data;

import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public class ImageUtils {

    public static final Pattern MATCH_URL = Pattern.compile("^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@|\\d{1,3}(?:\\.\\d{1,3}){3}|(?:(?:[a-z\\d\\x{00a1}-\\x{ffff}]+-?)*[a-z\\d\\x{00a1}-\\x{ffff}]+)(?:\\.(?:[a-z\\d\\x{00a1}-\\x{ffff}]+-?)*[a-z\\d\\x{00a1}-\\x{ffff}]+)*(?:\\.[a-z\\x{00a1}-\\x{ffff}]{2,6}))(?::\\d+)?(?:[^\\s]*)?$");

    public static BufferedImage resizeImage(BufferedImage input, int width, int height) {
        if (true) return input;

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float xInc = width / input.getWidth();
        float yInc = height / input.getHeight();

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int rgb = input.getRGB(Math.round(x*xInc), Math.round(y*yInc));
                output.setRGB(x, y, rgb);
            }
        }

        return output;
    }

    public static String getImageURL(String s) {
        if (MATCH_URL.matcher(s).matches()) return MATCH_URL.matcher(s).group();
        return null;
    }
}
