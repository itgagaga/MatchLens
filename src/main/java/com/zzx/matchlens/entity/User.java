package com.zzx.matchlens.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类，代表系统中的一个用户账号。
 * 包含用户名、密码、昵称、角色（admin/client）等信息，
 * 用于系统认证和权限控制。
 * 对应数据库表 t_user。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User {

    /** 用户唯一标识（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /** 登录用户名 */
    private String username;
    /** 登录密码 */
    private String password;
    /** 用户昵称 */
    private String nickname;
    /** 用户角色（admin-管理员 / client-客户端用户） */
    private String role;
    /** 账号创建时间 */
    private LocalDateTime createTime;

    public User(String username, String password, String nickname, String role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.createTime = LocalDateTime.now();
    }
}
