CREATE USER 'dogfeeder'@'localhost' IDENTIFIED BY 'dogfeeder';
CREATE DATABASE dog_feeder;
GRANT ALL PRIVILEGES ON dog_feeder.* TO 'dogfeeder'@'localhost';
FLUSH PRIVILEGES;