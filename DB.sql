CREATE DATABASE IF NOT EXISTS rbpo_db CHARACTER SET utf8mb4;
USE rbpo_db;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS commercial_offers;
DROP TABLE IF EXISTS purchase_requests;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    username VARCHAR(30)  NOT NULL,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL,
    enabled  TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT uq_user_username UNIQUE (username),
    CONSTRAINT ck_user_role CHECK (role IN ('EMPLOYEE', 'MANAGER', 'ADMIN'))
);

CREATE TABLE suppliers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    category    VARCHAR(100) NOT NULL,
    is_approved TINYINT(1)   NOT NULL DEFAULT 0,
    CONSTRAINT uq_supplier_name UNIQUE (name)
);

CREATE TABLE purchase_requests (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    description VARCHAR(500) NOT NULL,
    max_budget  DOUBLE       NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_request_budget CHECK (max_budget > 0),
    CONSTRAINT ck_request_status CHECK (status IN ('OPEN', 'ORDERED', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE commercial_offers (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_request_id BIGINT      NOT NULL,
    supplier_id         BIGINT      NOT NULL,
    price               DOUBLE      NOT NULL,
    delivery_term       VARCHAR(100),
    valid_until         DATE        NOT NULL,
    status              VARCHAR(20) NOT NULL,
    CONSTRAINT fk_offer_request FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests (id),
    CONSTRAINT fk_offer_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT uq_offer_request_supplier UNIQUE (purchase_request_id, supplier_id),
    CONSTRAINT ck_offer_price CHECK (price > 0),
    CONSTRAINT ck_offer_status CHECK (status IN ('NEW', 'SELECTED', 'REJECTED'))
);

CREATE TABLE purchase_orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_request_id BIGINT      NOT NULL,
    commercial_offer_id BIGINT      NOT NULL,
    execution_status    VARCHAR(20) NOT NULL,
    CONSTRAINT fk_order_request FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests (id),
    CONSTRAINT fk_order_offer FOREIGN KEY (commercial_offer_id) REFERENCES commercial_offers (id),
    CONSTRAINT uq_order_request UNIQUE (purchase_request_id),
    CONSTRAINT uq_order_offer UNIQUE (commercial_offer_id),
    CONSTRAINT ck_order_status CHECK (execution_status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

INSERT INTO users (id, name, username, password, role, enabled) VALUES
    (1, 'Иван Иванов',   'ivanov',  '$2b$10$yoUCU.q8sHVDo0DDJBmwCu5SBnr0mNHb2xYCOXWKPiHhdXYnw9N2S', 'EMPLOYEE', 1),
    (2, 'Анна Петрова',  'petrova', '$2b$10$zEEQZVIZQ0ks.aiYBNddzew9MytnxEaiIT4rSZoVIwjmhvhPHtNbG', 'MANAGER',  1),
    (3, 'Пётр Сидоров',  'sidorov', '$2b$10$m6kPwRdaOzGN02cAyPAzaeCSxXhOHK3eVUHO5wQZq2QxOHpJeuhme', 'ADMIN',    1),
    (4, 'Заблокированный Пользователь', 'blocked', '$2b$10$yoUCU.q8sHVDo0DDJBmwCu5SBnr0mNHb2xYCOXWKPiHhdXYnw9N2S', 'EMPLOYEE', 0);

INSERT INTO suppliers (id, name, category, is_approved) VALUES
    (1, 'ООО Техноснаб', 'Оборудование', 1),
    (2, 'ИП Кузнецов', 'Канцелярия', 1),
    (3, 'ООО СтройМир', 'Мебель', 1),
    (4, 'ООО Сомнительный поставщик', 'Разное', 0);

INSERT INTO purchase_requests (id, user_id, description, max_budget, status, created_at) VALUES
    (1, 1, 'Ноутбуки для отдела разработки (10 шт.)', 1000000, 'OPEN', NOW()),
    (2, 2, 'Канцелярия на квартал', 50000, 'OPEN', NOW()),
    (3, 1, 'Офисные кресла (20 шт.)', 300000, 'ORDERED', NOW());

INSERT INTO commercial_offers (id, purchase_request_id, supplier_id, price, delivery_term, valid_until, status) VALUES
    (1, 1, 1, 950000, '14 дней', '2027-12-31', 'NEW'),
    (2, 2, 2, 45000, '3 дня', '2027-12-31', 'NEW'),
    (3, 3, 3, 280000, '21 день', '2027-12-31', 'SELECTED'),
    (4, 3, 1, 310000, '10 дней', '2027-12-31', 'REJECTED');

INSERT INTO purchase_orders (id, purchase_request_id, commercial_offer_id, execution_status) VALUES
    (1, 3, 3, 'IN_PROGRESS');

CREATE USER IF NOT EXISTS 'rbpo_app'@'localhost' IDENTIFIED BY 'App12345';
GRANT SELECT, INSERT, UPDATE, DELETE ON rbpo_db.* TO 'rbpo_app'@'localhost';
FLUSH PRIVILEGES;