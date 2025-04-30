-- 移除ownerId的默认值
ALTER TABLE canteen MODIFY COLUMN ownerId BIGINT NOT NULL;
 
-- 更新现有记录中ownerId为7的记录
-- 注意：在生产环境中执行此操作前需要确认数据正确性
UPDATE canteen SET ownerId = 14 WHERE ownerId = 7; 