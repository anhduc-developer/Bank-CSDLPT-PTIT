# Distributed Banking System

Hệ thống ngân hàng phân tán mô phỏng môi trường quản lý đa chi nhánh trên nền tảng cơ sở dữ liệu phân tán. Dự án tập trung vào các kỹ thuật cốt lõi của hệ phân tán như phân mảnh dữ liệu, giao dịch phân tán, kiểm soát tương tranh, xử lý deadlock và nhân bản dữ liệu.

---

## Tổng quan

Trong mô hình ngân hàng thực tế, mỗi chi nhánh quản lý dữ liệu khách hàng và tài khoản riêng nhưng vẫn phải hỗ trợ các nghiệp vụ xuyên chi nhánh. Hệ thống được thiết kế với ba site độc lập, mỗi site sở hữu cơ sở dữ liệu riêng và có khả năng phối hợp xử lý giao dịch thông qua giao thức Two-Phase Commit (2PC).

### Các chức năng chính

* Quản lý khách hàng và tài khoản theo chi nhánh
* Nạp tiền, rút tiền
* Chuyển tiền nội bộ chi nhánh
* Chuyển tiền liên chi nhánh bằng giao thức Two-Phase Commit (2PC)
* Kiểm soát tương tranh bằng Row-Level Locking
* Mô phỏng Deadlock và Lost Update
* Truy vấn phân tán trên nhiều site
* Nhân bản dữ liệu Master–Slave Replication
* Giám sát trạng thái đồng bộ dữ liệu

---

## Kiến trúc hệ thống

### Mô hình triển khai


Frontend (React)
        │
        ▼
Backend (Spring Boot)
Transaction Coordinator
        │
 ┌──────┼──────┐
 ▼      ▼      ▼
HN     DN     HCM
Master Master Master
 │       │       │
 ▼       ▼       ▼
Slave   Slave   Slave


### Các Site

| Site   | Chi nhánh | Database    | Master | Slave |
| ------ | --------- | ----------- | ------ | ----- |
| Site 1 | Hà Nội    | bank_hanoi  | 3307   | 3317  |
| Site 2 | Đà Nẵng   | bank_danang | 3308   | 3318  |
| Site 3 | TP.HCM    | bank_hcm    | 3309   | 3319  |

### Kiến trúc dữ liệu

* Phân mảnh ngang (Horizontal Fragmentation) theo chi nhánh
* Mỗi site quản lý dữ liệu cục bộ của mình
* Các giao dịch liên chi nhánh được điều phối bởi Transaction Coordinator
* Dữ liệu báo cáo được đọc từ Slave nhằm giảm tải cho Master

---

## Công nghệ sử dụng

### Backend

| Công nghệ   | Phiên bản |
| ----------- | --------- |
| Java        | 17+       |
| Spring Boot | 3.x       |
| Spring JDBC | Latest    |
| HikariCP    | Latest    |
| Gradle      | 8.x       |

### Frontend

| Công nghệ  |
| ---------- |
| React 18   |
| Vite       |
| Ant Design |
| Axios      |

### Database & Infrastructure

| Công nghệ        |
| ---------------- |
| MySQL 8          |
| Docker           |
| Docker Compose   |
| GTID Replication |

---

## Các kỹ thuật CSDL phân tán được áp dụng

### 1. Horizontal Fragmentation

Dữ liệu được phân chia theo chi nhánh:

* Hà Nội → bank_hanoi
* Đà Nẵng → bank_danang
* TP.HCM → bank_hcm

Mỗi site chỉ quản lý dữ liệu của chi nhánh tương ứng.

---

### 2. Distributed Transactions (2PC)

Chuyển tiền liên chi nhánh sử dụng giao thức Two-Phase Commit.

#### Phase 1 – Prepare

* Khóa tài khoản nguồn và đích bằng `SELECT ... FOR UPDATE`
* Kiểm tra điều kiện giao dịch
* Ghi log trạng thái PREPARED

#### Phase 2 – Commit

* Nếu tất cả participant thành công → COMMIT
* Nếu bất kỳ participant thất bại → ROLLBACK toàn bộ

Đảm bảo tính:

* Atomicity
* Consistency
* Durability

cho giao dịch phân tán.

---

### 3. Concurrency Control

Hệ thống sử dụng:


SELECT ... FOR UPDATE


để khóa bản ghi ở mức hàng (Row-Level Lock).

Các giao dịch chuyển tiền luôn khóa tài khoản theo thứ tự tăng dần của ID nhằm ngăn chặn Circular Wait và giảm nguy cơ Deadlock.

---

### 4. Deadlock Simulation

Mô phỏng tình huống:


Thread 1:
Account A → Account B

Thread 2:
Account B → Account A


MySQL InnoDB sẽ:

* Phát hiện vòng chờ
* Chọn Deadlock Victim
* Rollback giao dịch bị chọn

---

### 5. Master–Slave Replication

Mỗi chi nhánh gồm:


Master (Read/Write)
        │
        ▼
Slave (Read Only)


Sử dụng:

* GTID Replication
* Binary Log
* Relay Log

Thông tin giám sát:

* Replica_IO_Running
* Replica_SQL_Running
* Seconds_Behind_Source

---

### 6. Distributed Queries

Các truy vấn thống kê được thực hiện trên toàn bộ Slave:

* Tổng số dư toàn hệ thống
* Top khách hàng
* Lịch sử giao dịch
* Giao dịch liên chi nhánh
* Thống kê theo chi nhánh

Kết quả được tổng hợp tại tầng ứng dụng.

---

## Cấu trúc cơ sở dữ liệu

Mỗi site sử dụng cùng một schema.

| Bảng                        | Chức năng                     |
| --------------------------- | ----------------------------- |
| branch                      | Thông tin chi nhánh           |
| customer                    | Thông tin khách hàng          |
| account                     | Thông tin tài khoản           |
| transaction_history         | Lịch sử giao dịch             |
| distributed_transaction_log | Log giao dịch phân tán        |
| transaction_participant     | Thông tin participant của 2PC |

---

## Hướng dẫn chạy hệ thống

### 1. Khởi động Database

bash
docker compose up -d


Kiểm tra trạng thái:

bash
docker ps


---

### 2. Thiết lập Replication


chmod +x docker/replication/setup-replication.sh

./docker/replication/setup-replication.sh


Kết quả mong đợi:


Replica_IO_Running: Yes
Replica_SQL_Running: Yes


---

### 3. Chạy Backend

bash
cd backend

./gradlew bootRun


Backend:


http://localhost:8080


---

### 4. Chạy Frontend

bash
cd frontend

npm install
npm run dev


Frontend:


http://localhost:3000


---

## Các kịch bản demo

### Demo 2PC

* Chuyển tiền Hà Nội → Đà Nẵng
* Chuyển tiền Đà Nẵng → TP.HCM
* Giả lập lỗi giữa Prepare và Commit

### Demo Lost Update

* Hai luồng rút tiền đồng thời
* So sánh có và không sử dụng locking

### Demo Deadlock

* Hai transaction khóa tài nguyên theo thứ tự ngược nhau
* Quan sát Deadlock Victim

### Demo Replication

* Ghi dữ liệu vào Master
* Đọc dữ liệu từ Slave
* Theo dõi Replication Lag

---

## API chính

| Nhóm chức năng | Endpoint           |
| -------------- | ------------------ |
| Customers      | /api/customers     |
| Accounts       | /api/accounts      |
| Transactions   | /api/transactions  |
| Transfers      | /api/transfers     |
| Statistics     | /api/stats         |
| Replication    | /api/replication   |
| Deadlock Demo  | /api/demo/deadlock |

---

## Kết quả đạt được

* Xây dựng thành công mô hình ngân hàng phân tán 3 site
* Triển khai giao dịch phân tán bằng Two-Phase Commit
* Hỗ trợ Replication Master–Slave
* Kiểm soát tương tranh bằng Pessimistic Locking
* Mô phỏng Lost Update và Deadlock
* Hỗ trợ truy vấn phân tán trên nhiều site
* Xây dựng giao diện quản lý trực quan bằng React

---

## Thành viên thực hiện

|          Họ và tên               |
| ---------------------------------|
|     Mai Anh Đức (Leader)         |
|     Trần Đăng Dương              |
|     Trịnh Anh Tú                 |
