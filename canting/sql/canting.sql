/*
 Navicat Premium Dump SQL

 Source Server         : gs2
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : 10.0.0.8:3306
 Source Schema         : canting

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 11/12/2024 10:27:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for canteen
-- ----------------------------
DROP TABLE IF EXISTS `canteen`;
CREATE TABLE `canteen`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `create_time` datetime NULL DEFAULT NULL,
  `score` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评分',
  `style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜系',
  `price` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '价格',
  `lat` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '纬度',
  `lng` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经度',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '餐厅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of canteen
-- ----------------------------
INSERT INTO `canteen` VALUES (1, '1', '1', '2024-12-06 11:11:35', '5', '家常菜', '低', '53.45294023589251', '-7.0997907040498065', '早上八点到晚上六点', 'https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1ux23m.img?w=718&h=404&m=6');

-- ----------------------------
-- Table structure for score_list
-- ----------------------------
DROP TABLE IF EXISTS `score_list`;
CREATE TABLE `score_list`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `score` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评分1-5',
  `create_time` datetime NULL DEFAULT NULL,
  `canting_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2112258051 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of score_list
-- ----------------------------
INSERT INTO `score_list` VALUES (-1998159871, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-1750695934, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-1738113023, '1', NULL, 1);
INSERT INTO `score_list` VALUES (-1700364287, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-1675149310, '5', NULL, 1);
INSERT INTO `score_list` VALUES (-1553514494, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-916029438, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-878280703, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-840531966, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-643399679, '1', NULL, 1);
INSERT INTO `score_list` VALUES (-584679422, '3', NULL, 1);
INSERT INTO `score_list` VALUES (-433692670, '4', NULL, 1);
INSERT INTO `score_list` VALUES (-291086334, '5', NULL, 1);
INSERT INTO `score_list` VALUES (338067458, '5', NULL, 1);
INSERT INTO `score_list` VALUES (417808385, '5', NULL, 1);
INSERT INTO `score_list` VALUES (484868097, '4', NULL, NULL);
INSERT INTO `score_list` VALUES (484868098, '4', NULL, 1);
INSERT INTO `score_list` VALUES (908492801, '4', NULL, 1);
INSERT INTO `score_list` VALUES (1038565378, '5', NULL, 1);
INSERT INTO `score_list` VALUES (1495695361, '4', NULL, 1);
INSERT INTO `score_list` VALUES (1697021953, '4', NULL, NULL);
INSERT INTO `score_list` VALUES (1722187777, '4', NULL, NULL);
INSERT INTO `score_list` VALUES (1785102338, '4', NULL, 1);
INSERT INTO `score_list` VALUES (2112258050, '4', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
