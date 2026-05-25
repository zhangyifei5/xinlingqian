package com.example.xinlingqian.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByUsername(String username);
    
    User findByPhone(String phone);
    
    User findByEmail(String email);
    
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{username}, '%')")
    List<User> searchByUsername(String username);
}
