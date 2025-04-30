-- 更新餐厅表中的ownerId，将其设置为14（owner1用户的ID）
UPDATE canteen SET ownerId = 14 WHERE id = 1;

-- 更新其他餐厅，将其也设置为owner1所有
UPDATE canteen SET ownerId = 14 WHERE id IN (2, 3, 4, 5, 6, 7);
 
-- 如果有owner2拥有的餐厅，可以设置为对应ID
UPDATE canteen SET ownerId = 15 WHERE id IN (8, 9, 10); 