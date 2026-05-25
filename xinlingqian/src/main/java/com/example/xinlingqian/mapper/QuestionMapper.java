package com.example.xinlingqian.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinlingqian.entity.Question;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("SELECT * FROM question WHERE dimension LIKE CONCAT('%', #{dimension}, '%') ORDER BY RAND() LIMIT #{limit}")
    List<Question> selectRandomByDimension(String dimension, Integer limit);

    @Select("SELECT * FROM question WHERE dimension LIKE CONCAT('%', 'REGISTER', '%') ORDER BY sort_order")
    List<Question> selectRegisterQuestions();
}
