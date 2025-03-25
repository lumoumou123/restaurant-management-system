-- 创建餐厅菜单表
CREATE TABLE IF NOT EXISTS menu_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  restaurant_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  category VARCHAR(50),
  image VARCHAR(255),
  is_available BOOLEAN DEFAULT TRUE,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (restaurant_id) REFERENCES canteen(id) ON DELETE CASCADE
); 