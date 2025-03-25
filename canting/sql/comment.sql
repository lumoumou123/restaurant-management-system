CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `canting_id` bigint(20) NOT NULL COMMENT '餐厅ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-正常，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_canting_id` (`canting_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐厅评论表'; 