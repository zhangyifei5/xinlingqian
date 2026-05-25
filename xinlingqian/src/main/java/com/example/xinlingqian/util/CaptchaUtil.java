package com.example.xinlingqian.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaUtil {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final Random RANDOM = new Random();
    
    public static class CaptchaResult {
        private final String code;
        private final BufferedImage image;
        
        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }
        
        public String getCode() {
            return code;
        }
        
        public BufferedImage getImage() {
            return image;
        }
    }
    
    public static CaptchaResult generateCaptcha() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setColor(new Color(240, 248, 255));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String charStr = String.valueOf(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
            code.append(charStr);
            
            g2d.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 28));
            g2d.setColor(new Color(
                RANDOM.nextInt(100) + 50,
                RANDOM.nextInt(100) + 50,
                RANDOM.nextInt(100) + 50
            ));
            
            int x = i * 28 + 10;
            int y = 28 + RANDOM.nextInt(6) - 3;
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            g2d.rotate(angle, x, y);
            g2d.drawString(charStr, x, y);
            g2d.rotate(-angle, x, y);
        }
        
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(
                RANDOM.nextInt(200) + 50,
                RANDOM.nextInt(200) + 50,
                RANDOM.nextInt(200) + 50,
                100
            ));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        for (int i = 0; i < 80; i++) {
            g2d.setColor(new Color(
                RANDOM.nextInt(255),
                RANDOM.nextInt(255),
                RANDOM.nextInt(255),
                80
            ));
            g2d.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }
        
        g2d.dispose();
        return new CaptchaResult(code.toString(), image);
    }
}
