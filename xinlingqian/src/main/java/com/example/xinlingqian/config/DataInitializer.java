package com.example.xinlingqian.config;

import com.example.xinlingqian.entity.User;
import com.example.xinlingqian.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
    }

    private void createAdminAccount() {
        String adminUsername = "admin";
        String adminPassword = "admin123";
        String adminNickname = "管理员";

        User existingAdmin = userMapper.findByUsername(adminUsername);
        
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNickname(adminNickname);
            admin.setRole("ADMIN");
            admin.setStatus("APPROVED");
            admin.setBalance(0.0);
            
            userMapper.insert(admin);
            System.out.println("✅ 管理员账号创建成功: username=" + adminUsername + ", password=" + adminPassword);
        } else {
            System.out.println("ℹ️ 管理员账号已存在: username=" + adminUsername);
        }
    }
}