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

-- Query to set a food
CREATE TABLE food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    trademark VARCHAR(255) UNIQUE NOT NULL,
    grain_type ENUM('small','medium','big')
);