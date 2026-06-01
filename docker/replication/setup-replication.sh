#!/bin/bash

set -e

MYSQL_ROOT_PASSWORD="root"
REPL_USER="repl_user"
REPL_PASSWORD="repl_pass"

MASTERS=("mysql-hanoi" "mysql-danang" "mysql-hcm")
SLAVES=("mysql-hanoi-slave" "mysql-danang-slave" "mysql-hcm-slave")
DATABASES=("bank_hanoi" "bank_danang" "bank_hcm")

echo "THIẾT LẬP MYSQL REPLICATION - 3 CẶP M-S"
echo ""

wait_for_mysql() {
    local container=$1
    local max_attempts=30
    local attempt=0

    echo "Đợi $container sẵn sàng..."
    while [ $attempt -lt $max_attempts ]; do
        if docker exec $container mysqladmin ping -uroot -p$MYSQL_ROOT_PASSWORD --silent 2>/dev/null; then
            echo "$container đã sẵn sàng!"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done

    echo "$container không khởi động được sau $((max_attempts * 2)) giây"
    return 1
}

setup_replication() {
    local master=$1
    local slave=$2
    local db=$3

    echo ""
    echo "Thiết lập Replication: $master -> $slave"

    echo "Bước 1: Tạo user replication trên Master..."
    docker exec $master mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "
        DROP USER IF EXISTS '$REPL_USER'@'%';
        CREATE USER '$REPL_USER'@'%' IDENTIFIED BY '$REPL_PASSWORD';
        GRANT REPLICATION SLAVE ON *.* TO '$REPL_USER'@'%';
        FLUSH PRIVILEGES;
    " 2>/dev/null
    echo "User '$REPL_USER' đã được tạo trên $master"

    echo "Bước 2: Lấy Master Status..."
    MASTER_STATUS=$(docker exec $master mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "SHOW BINARY LOG STATUS\G" 2>/dev/null)
    MASTER_FILE=$(echo "$MASTER_STATUS" | grep "File:" | awk '{print $2}')
    MASTER_POS=$(echo "$MASTER_STATUS" | grep "Position:" | awk '{print $2}')
    echo "File: $MASTER_FILE"
    echo "Position: $MASTER_POS"

    echo "Bước 3: Cấu hình Slave..."
    docker exec $slave mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "
        STOP REPLICA;
        RESET REPLICA ALL;
        CHANGE REPLICATION SOURCE TO
            SOURCE_HOST='$master',
            SOURCE_USER='$REPL_USER',
            SOURCE_PASSWORD='$REPL_PASSWORD',
            SOURCE_AUTO_POSITION=1,
            GET_SOURCE_PUBLIC_KEY=1,
            SOURCE_SSL=0;
        START REPLICA;
    " 2>/dev/null
    echo "Slave đã được cấu hình và bắt đầu nhân bản"

    echo "Bước 4: Kiểm tra trạng thái Slave..."
    sleep 5

    SLAVE_STATUS=$(docker exec $slave mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "SHOW REPLICA STATUS\G" 2>/dev/null)
    IO_RUNNING=$(echo "$SLAVE_STATUS" | grep "Replica_IO_Running:" | awk '{print $2}')
    SQL_RUNNING=$(echo "$SLAVE_STATUS" | grep "Replica_SQL_Running:" | head -1 | awk '{print $2}')
    SECONDS_BEHIND=$(echo "$SLAVE_STATUS" | grep "Seconds_Behind_Source:" | awk '{print $2}')

    echo "Replica_IO_Running:  $IO_RUNNING"
    echo "Replica_SQL_Running: $SQL_RUNNING"
    echo "Seconds_Behind:      $SECONDS_BEHIND"

    if [ "$IO_RUNNING" = "Yes" ] && [ "$SQL_RUNNING" = "Yes" ]; then
        echo "REPLICATION THÀNH CÔNG: $master -> $slave"
    else
        echo "REPLICATION CÓ VẤN ĐỀ - Kiểm tra lại cấu hình"
        LAST_ERROR=$(echo "$SLAVE_STATUS" | grep "Last_IO_Error:" | cut -d':' -f2-)
        if [ -n "$LAST_ERROR" ] && [ "$LAST_ERROR" != " " ]; then
            echo "Last IO Error: $LAST_ERROR"
        fi
        LAST_SQL_ERROR=$(echo "$SLAVE_STATUS" | grep "Last_SQL_Error:" | cut -d':' -f2-)
        if [ -n "$LAST_SQL_ERROR" ] && [ "$LAST_SQL_ERROR" != " " ]; then
            echo "Last SQL Error: $LAST_SQL_ERROR"
        fi
    fi
}

for master in "${MASTERS[@]}"; do
    wait_for_mysql $master
done

for slave in "${SLAVES[@]}"; do
    wait_for_mysql $slave
done

echo ""
echo "Tất cả containers đã sẵn sàng. Bắt đầu thiết lập..."

for i in "${!MASTERS[@]}"; do
    setup_replication "${MASTERS[$i]}" "${SLAVES[$i]}" "${DATABASES[$i]}"
done

echo ""
echo "TỔNG KẾT REPLICATION"
echo ""
echo "mysql-hanoi  (3307) -> mysql-hanoi-slave  (3317)"
echo "mysql-danang (3308) -> mysql-danang-slave (3318)"
echo "mysql-hcm    (3309) -> mysql-hcm-slave    (3319)"
echo ""
echo "Dữ liệu nhân bản: TẤT CẢ 6 bảng mỗi site"
echo "Phương thức: MySQL GTID-based Async Replication"
echo "Chiều: Master -> Slave (một chiều)"
echo ""
echo "Kiểm tra trạng thái qua API:"
echo "curl http://localhost:8080/api/replication/status"
echo ""
