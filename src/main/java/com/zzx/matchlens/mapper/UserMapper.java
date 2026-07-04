package com.zzx.matchlens.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzx.matchlens.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
