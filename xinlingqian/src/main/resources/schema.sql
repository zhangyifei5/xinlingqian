CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    nickname VARCHAR(50) COMMENT '昵称',
    signature VARCHAR(500) COMMENT '个性签名',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    gender TINYINT COMMENT '性别(0-未知,1-男,2-女)',
    birthday VARCHAR(20) COMMENT '生日',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色(USER/COUNSELOR/ADMIN)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/REJECTED/BANNED)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS counselor_level_up_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    counselor_id BIGINT NOT NULL COMMENT '咨询师ID',
    counselor_name VARCHAR(100) COMMENT '咨询师名称',
    target_level INT NOT NULL COMMENT '目标等级',
    reason VARCHAR(500) COMMENT '申请理由',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询师升级申请表';

CREATE TABLE IF NOT EXISTS customer_service_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送者类型(USER/ADMIN)',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read TINYINT(1) DEFAULT FALSE COMMENT '是否已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服消息表';
