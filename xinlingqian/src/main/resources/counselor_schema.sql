CREATE TABLE IF NOT EXISTS counselor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '咨询师ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    title VARCHAR(100) COMMENT '头衔',
    specialty VARCHAR(200) COMMENT '专长',
    experience VARCHAR(500) COMMENT '从业经历',
    education VARCHAR(100) COMMENT '学历',
    certificate VARCHAR(500) COMMENT '证书信息',
    description VARCHAR(1000) COMMENT '个人简介',
    hourly_rate DECIMAL(10,2) DEFAULT 0.00 COMMENT '小时费率',
    consultation_count INT DEFAULT 0 COMMENT '咨询次数',
    rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/REJECTED)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询师表';

CREATE TABLE IF NOT EXISTS consultation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '咨询会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    counselor_id BIGINT NOT NULL COMMENT '咨询师ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/IN_PROGRESS/COMPLETED)',
    total_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '总金额',
    paid_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '已支付金额',
    session_count INT DEFAULT 0 COMMENT '会话次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_counselor_id (counselor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询会话表';

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    consultation_id BIGINT NOT NULL COMMENT '咨询会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送者类型(USER/COUNSELOR)',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_consultation_id (consultation_id),
    INDEX idx_sender_id (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
    consultation_id BIGINT NOT NULL COMMENT '咨询会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    counselor_id BIGINT NOT NULL COMMENT '咨询师ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/SUCCESS/FAILED)',
    payment_method VARCHAR(50) COMMENT '支付方式',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_consultation_id (consultation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_counselor_id (counselor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';