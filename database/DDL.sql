/*
=========================================================
 DOGFEEDER DATABASE
=========================================================
*/

-- Query to ceate user table
CREATE TABLE user ( 
  id int NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL, 
  password VARCHAR(255) NOT NULL,
  valid_mail BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id) 
);

-- Query to register the supply food
CREATE TABLE audit_supply_food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    weight DOUBLE,
    FOREIGN KEY (id_user) REFERENCES user(id)
);

-- Query to set a pet
CREATE TABLE pet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) UNIQUE NOT NULL,
    breed VARCHAR(255) NOT NULL,
    birthday DATE DEFAULT CURRENT_DATE,
    weight DOUBLE DEFAULT 10.0,
    type ENUM('small','medium','big') DEFAULT 'small',
    vac_notify BOOLEAN DEFAULT TRUE,
    vac_day INT DEFAULT 1,
    vac_month INT DEFAULT 1,
    CONSTRAINT chk_day_of_month CHECK (vac_day BETWEEN 1 AND 31),
    CONSTRAINT chk_month_of_year CHECK (vac_month BETWEEN 1 AND 12)
);



-- Query to set the settings
CREATE TABLE settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_ration	INT DEFAULT 150,
    led_on BOOLEAN DEFAULT FALSE,
    notify_hopper_low BOOLEAN DEFAULT FALSE,
    notify_feeder_without_food BOOLEAN DEFAULT FALSE	
);

-- Views
DROP VIEW IF EXISTS audit_supply_food_summary;

CREATE VIEW audit_supply_food_summary AS
SELECT 
    YEAR(timestamp) AS year_number,
    MONTH(timestamp) AS month_number,
    MONTHNAME(timestamp) AS month_name,
    COUNT(*) AS count_takes,
    SUM(weight) AS total_weight
FROM 
    audit_supply_food
GROUP BY 
    year_number, month_number, month_name
ORDER BY 
    year_number DESC, month_number DESC;