package com.example.xinlingqian.controller;

import com.example.xinlingqian.util.CaptchaUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    
    private static final String CAPTCHA_SESSION_KEY = "captcha_code";
    
    @GetMapping("/generate")
    public Map<String, String> generateCaptcha(HttpSession session) {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generateCaptcha();
        
        session.setAttribute(CAPTCHA_SESSION_KEY, result.getCode().toLowerCase());
        
        String base64Image = imageToBase64(result.getImage());
        
        Map<String, String> response = new HashMap<>();
        response.put("image", "data:image/png;base64," + base64Image);
        response.put("sessionId", session.getId());
        return response;
    }
    
    @PostMapping("/verify")
    public Map<String, Object> verifyCaptcha(@RequestBody Map<String, String> request, HttpSession session) {
        String captcha = request.get("captcha");
        Map<String, Object> response = new HashMap<>();
        
        String storedCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        
        if (storedCaptcha == null) {
            response.put("success", false);
            response.put("message", "验证码已过期，请刷新重试");
            return response;
        }
        
        if (captcha == null || captcha.toLowerCase().equals(storedCaptcha)) {
            response.put("success", true);
            response.put("message", "验证成功");
            session.removeAttribute(CAPTCHA_SESSION_KEY);
        } else {
            response.put("success", false);
            response.put("message", "验证码错误");
        }
        
        return response;
    }
    
    public static boolean verifyCaptchaDirect(String captcha, HttpSession session) {
        String storedCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        if (storedCaptcha == null || captcha == null) {
            return false;
        }
        boolean valid = captcha.toLowerCase().equals(storedCaptcha);
        if (valid) {
            session.removeAttribute(CAPTCHA_SESSION_KEY);
        }
        return valid;
    }
    
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("转换图片失败", e);
        }
    }
}
