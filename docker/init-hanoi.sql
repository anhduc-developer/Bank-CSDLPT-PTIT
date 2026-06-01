
USE bank_hanoi;
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS branch (
    branch_id   VARCHAR(10)  PRIMARY KEY
    branch_name VARCHAR(100) NOT NULL
    city        VARCHAR(50)  NOT NULL
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer (
    customer_id BIGINT       AUTO_INCREMENT PRIMARY KEY
    full_name   VARCHAR(100) NOT NULL   
    phone       VARCHAR(20)  UNIQUE    
    email       VARCHAR(100)         
    address     VARCHAR(200)            
    branch_id   VARCHAR(10)  NOT NULL    
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

CREATE TABLE IF NOT EXISTS account (
    account_id  BIGINT        AUTO_INCREMENT PRIMARY KEY
    customer_id BIGINT        NOT NULL    
    branch_id   VARCHAR(10)   NOT NULL    
    balance     DECIMAL(15,2) NOT NULL DEFAULT 0.00 
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' 
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (branch_id)   REFERENCES branch(branch_id),
    CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS transaction_history (
    transaction_id     BIGINT        AUTO_INCREMENT PRIMARY KEY
    transaction_type   VARCHAR(30)   NOT NULL
    amount             DECIMAL(15,2) NOT NULL 
    account_id         BIGINT        NOT NULL 
    related_account_id BIGINT                 
    related_branch_id  VARCHAR(10)            
    balance_after      DECIMAL(15,2)         
    status             VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS'
    distributed_txn_id VARCHAR(50)            
    description        VARCHAR(200)  
    created_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS distributed_transaction_log (
    txn_id             VARCHAR(50)   PRIMARY KEY
    txn_type           VARCHAR(30)   NOT NULL 
    status             VARCHAR(20)   NOT NULL DEFAULT 'STARTED' 
    source_branch      VARCHAR(10)   NOT NULL 
    dest_branch        VARCHAR(10)   NOT NULL 
    amount             DECIMAL(15,2) NOT NULL 
    source_account_id  BIGINT        NOT NULL 
    dest_account_id    BIGINT        NOT NULL 
    error_message      VARCHAR(500)           
    created_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transaction_participant (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    txn_id     VARCHAR(50) NOT NULL 
    branch_id  VARCHAR(10) NOT NULL 
    role       VARCHAR(20) NOT NULL 
    status     VARCHAR(20) NOT NULL DEFAULT 'PREPARING'
    action     VARCHAR(30)         
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (txn_id) REFERENCES distributed_transaction_log(txn_id)
);


INSERT INTO branch (branch_id, branch_name, city) VALUES
('HN', 'Chi nhánh Hà Nội', 'Hà Nội');


INSERT INTO customer (full_name, phone, email, address, branch_id) VALUES
('Nguyễn Văn An',    '0901000001', 'an.nguyen@email.com',   '12 Phố Huế, Hai Bà Trưng, Hà Nội',       'HN'),
('Trần Thị Bình',    '0901000002', 'binh.tran@email.com',   '45 Láng Hạ, Đống Đa, Hà Nội',             'HN'),
('Lê Hoàng Cường',   '0901000003', 'cuong.le@email.com',    '78 Nguyễn Trãi, Thanh Xuân, Hà Nội',      'HN'),
('Phạm Minh Dũng',   '0901000004', 'dung.pham@email.com',   '23 Kim Mã, Ba Đình, Hà Nội',              'HN'),
('Hoàng Thị Ánh',     '0901000005', 'em.hoang@email.com',    '56 Trần Duy Hưng, Cầu Giấy, Hà Nội',     'HN');


INSERT INTO account (customer_id, branch_id, balance, status) VALUES
(1, 'HN', 50000000.00, 'ACTIVE'),     
(1, 'HN', 20000000.00, 'ACTIVE'),    
(2, 'HN', 75000000.00, 'ACTIVE'),    
(3, 'HN', 30000000.00, 'ACTIVE'),   
(4, 'HN', 100000000.00, 'ACTIVE'),    
(5, 'HN', 15000000.00, 'ACTIVE');   


INSERT INTO transaction_history (transaction_type, amount, account_id, balance_after, status, description) VALUES
('DEPOSIT',  50000000.00, 1, 50000000.00, 'SUCCESS', 'Nạp tiền ban đầu'),
('DEPOSIT',  20000000.00, 2, 20000000.00, 'SUCCESS', 'Nạp tiền ban đầu'),
('DEPOSIT',  75000000.00, 3, 75000000.00, 'SUCCESS', 'Nạp tiền ban đầu'),
('DEPOSIT',  30000000.00, 4, 30000000.00, 'SUCCESS', 'Nạp tiền ban đầu'),
('DEPOSIT', 100000000.00, 5, 100000000.00, 'SUCCESS', 'Nạp tiền ban đầu'),
('DEPOSIT',  15000000.00, 6, 15000000.00, 'SUCCESS', 'Nạp tiền ban đầu');
