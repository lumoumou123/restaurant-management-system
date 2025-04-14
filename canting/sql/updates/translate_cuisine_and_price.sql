-- 将菜系类型从中文转为英文
UPDATE `canteen` SET `style` = 'Chinese' WHERE `style` = '中餐';
UPDATE `canteen` SET `style` = 'Western' WHERE `style` = '西餐';
UPDATE `canteen` SET `style` = 'Japanese' WHERE `style` = '日料';
UPDATE `canteen` SET `style` = 'Home Style' WHERE `style` = '家常菜';

-- 将价格范围从中文转为英文
UPDATE `canteen` SET `price` = 'Low' WHERE `price` = '低';
UPDATE `canteen` SET `price` = 'Medium' WHERE `price` = '中';
UPDATE `canteen` SET `price` = 'High' WHERE `price` = '高'; 