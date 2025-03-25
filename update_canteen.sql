-- 检查列是否存在，如果不存在则添加
ALTER TABLE canteen ADD COLUMN IF NOT EXISTS ownerId BIGINT DEFAULT 7;

-- 更新任何可能的现有空值
UPDATE canteen SET ownerId = 7 WHERE ownerId IS NULL;
