-- 1. 先删除旧数据库
DROP DATABASE IF EXISTS mockmaster;

-- 2. 重建数据库
CREATE DATABASE IF NOT EXISTS mockmaster
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 3. 进入该数据库
USE mockmaster;

-- 4. 创建所有表结构

CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `username` VARCHAR(64) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
                        `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
                        `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `job` (
                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                       `job_name` VARCHAR(100) NOT NULL COMMENT '岗位名称',
                       `job_desc` TEXT COMMENT '岗位描述',
                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       PRIMARY KEY (`id`),
                       UNIQUE KEY `uk_job_name` (`job_name`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

CREATE TABLE `interview` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `user_id` BIGINT NOT NULL COMMENT '用户 ID',
                             `job_id` BIGINT NOT NULL COMMENT '岗位 ID',
                             `difficulty` VARCHAR(20) NOT NULL COMMENT '面试难度 easy/medium/hard',
                             `planned_duration` INT NOT NULL COMMENT '用户配置时长，单位分钟',
                             `actual_duration` INT NOT NULL DEFAULT 0 COMMENT '实际完成时长，单位分钟',
                             `mode` VARCHAR(20) NOT NULL COMMENT '面试模式 voice/text',
                             `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/COMPLETED/ABORTED',
                             `summary` TEXT COMMENT 'AI 面试总结',
                             `last_question` TEXT COMMENT '最后一轮 AI 追问',
                             `total_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
                             `round_count` INT NOT NULL DEFAULT 0 COMMENT '对话轮次',
                             `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
                             `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
                             PRIMARY KEY (`id`),
                             KEY `idx_interview_user_status` (`user_id`, `status`),
                             KEY `idx_interview_job` (`job_id`),
                             CONSTRAINT `fk_interview_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_interview_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE RESTRICT
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='面试会话表';

CREATE TABLE `message` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                           `interview_id` BIGINT NOT NULL COMMENT '面试 ID',
                           `sender_type` VARCHAR(10) NOT NULL COMMENT 'AI/USER',
                           `content` TEXT COMMENT '消息内容',
                           `audio_url` VARCHAR(255) DEFAULT NULL COMMENT '语音资源地址',
                           `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_message_interview` (`interview_id`),
                           CONSTRAINT `fk_message_interview` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`) ON DELETE CASCADE
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='对话记录表';

CREATE TABLE `resource` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型 video/doc/question',
                            `title` VARCHAR(255) NOT NULL COMMENT '标题',
                            `content` TEXT COMMENT '内容',
                            `url` VARCHAR(512) DEFAULT NULL COMMENT '外链地址',
                            `job_id` BIGINT DEFAULT NULL COMMENT '关联岗位',
                            `difficulty` INT NOT NULL DEFAULT 2 COMMENT '推荐难度 1/2/3',
                            PRIMARY KEY (`id`),
                            KEY `idx_resource_job` (`job_id`),
                            CONSTRAINT `fk_resource_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`id`) ON DELETE SET NULL
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='学习资源表';

-- 语音面试详细评分表
CREATE TABLE `interview_detail` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `interview_id` BIGINT NOT NULL COMMENT '面试 ID',
                                    `question_id` BIGINT NOT NULL COMMENT '题目（resource表）ID',
                                    `user_text` TEXT COMMENT '用户回答文字（ASR识别结果）',
                                    `score_accuracy` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Python返回的语义分数',
                                    `score_professional` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Python返回的关键词分数',
                                    `score_logic` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Python返回的流利度分数',
                                    `total_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
                                    `audio_url` VARCHAR(255) DEFAULT NULL COMMENT '上传的音频文件路径',
                                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_interview_detail_interview` (`interview_id`),
                                    KEY `idx_interview_detail_question` (`question_id`),
                                    CONSTRAINT `fk_interview_detail_interview` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_interview_detail_question` FOREIGN KEY (`question_id`) REFERENCES `resource` (`id`) ON DELETE RESTRICT
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='语音面试详细评分表';
-- 增加 answer_mode 列，用来区分是语音还是文本
ALTER TABLE `interview_detail` 
ADD COLUMN `answer_mode` VARCHAR(10) NOT NULL DEFAULT 'voice' COMMENT '回答模式: voice(语音), text(文本)' 
AFTER `question_id`;

-- 插入岗位

INSERT INTO `job` (`job_name`, `job_desc`) VALUES
                                               ('前端开发工程师', '熟悉 Vue / React、工程化、性能优化和组件设计。'),
                                               ('后端开发工程师', '熟悉 Java、Spring Boot、MySQL、缓存与接口设计。'),
                                               ('产品经理', '覆盖需求分析、原型设计、协作推进与结果复盘。'),
                                               ('UI 设计师', '关注视觉规范、交互细节、组件体系与设计表达。'),
                                               ('测试工程师', '关注测试设计、接口自动化、缺陷定位和质量保障。'),
                                               ('云计算工程师', '关注容器、部署、监控、弹性伸缩与稳定性设计。'),
                                               ('移动端开发工程师', '覆盖 iOS / Android / Flutter 与客户端性能优化。'),
                                               ('网络安全工程师', '关注漏洞治理、渗透测试、安全加固和应急响应。'),
                                               ('数据分析师', '覆盖 SQL、指标体系、可视化和业务洞察。'),
                                               ('人工智能工程师', '关注机器学习、模型应用、推理链路和数据闭环。');