-- 如果表不存在，添加likes字段
ALTER TABLE menu_item ADD COLUMN IF NOT EXISTS likes INT DEFAULT 0;

-- 确保所有记录都有初始值
UPDATE menu_item SET likes = 0 WHERE likes IS NULL; 