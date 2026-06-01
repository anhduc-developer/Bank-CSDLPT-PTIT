import React, { useState } from "react";
import {
  Card,
  Form,
  InputNumber,
  Select,
  Button,
  message,
  Descriptions,
  Tag,
  Row,
  Col,
  Result,
  Switch,
  Timeline,
  Typography,
  Alert,
  Space,
  Divider,
} from "antd";
import {
  DollarOutlined,
  MinusCircleOutlined,
  CheckCircleOutlined,
  ThunderboltOutlined,
  ClockCircleOutlined,
  LockOutlined,
  UnlockOutlined,
} from "@ant-design/icons";
import { transactionApi } from "../api/bankApi";

const { Text } = Typography;
const fmt = (v) => (v ? Number(v).toLocaleString("vi-VN") : "0");

export default function Transactions() {
  const [depositForm] = Form.useForm();
  const [singleWithdrawForm] = Form.useForm();
  const [withdrawForm] = Form.useForm();
  const [depositResult, setDepositResult] = useState(null);
  const [singleWithdrawResult, setSingleWithdrawResult] = useState(null);
  const [concurrentResult, setConcurrentResult] = useState(null);
  const [depositLoading, setDepositLoading] = useState(false);
  const [singleWithdrawLoading, setSingleWithdrawLoading] = useState(false);
  const [withdrawLoading, setWithdrawLoading] = useState(false);
  const [useLock, setUseLock] = useState(true);

  const handleDeposit = async (values) => {
    setDepositLoading(true);
    setDepositResult(null);
    try {
      const res = await transactionApi.deposit(values);
      if (res.data.success) {
        setDepositResult(res.data.data);
        message.success("Nạp tiền thành công!");
      } else {
        message.error(res.data.message);
      }
    } catch (err) {
      message.error("Lỗi: " + (err.response?.data?.message || err.message));
    }
    setDepositLoading(false);
  };

  const handleSingleWithdraw = async (values) => {
    setSingleWithdrawLoading(true);
    setSingleWithdrawResult(null);
    try {
      const res = await transactionApi.withdraw(values);
      if (res.data.success) {
        setSingleWithdrawResult(res.data.data);
        message.success("Rút tiền thành công!");
      } else {
        message.error(res.data.message);
      }
    } catch (err) {
      message.error("Lỗi: " + (err.response?.data?.message || err.message));
    }
    setSingleWithdrawLoading(false);
  };

  const handleConcurrentWithdraw = async (values) => {
    setWithdrawLoading(true);
    setConcurrentResult(null);
    try {
      const payload = {
        branchId: values.branchId,
        accountId: values.accountId,
        amountThread1: values.amountThread1,
        amountThread2: values.amountThread2,
        useLock,
      };
      const res = await transactionApi.concurrentWithdraw(payload);
      if (res.data.success) {
        setConcurrentResult(res.data.data);
        if (useLock) {
          message.success("Rút tiền hoàn tất (có Pessimistic Lock)");
        } else {
          message.warning(
            "Rút tiền hoàn tất (KHÔNG lock — kiểm tra Lost Update!)",
          );
        }
      } else {
        message.error(res.data.message);
      }
    } catch (err) {
      message.error("Lỗi: " + (err.response?.data?.message || err.message));
    }
    setWithdrawLoading(false);
  };

  const getLogIcon = (log) => {
    if (
      log.includes("SUCCESS") ||
      log.includes("✅") ||
      log.includes("COMMITTED")
    ) {
      return <CheckCircleOutlined style={{ color: "#52c41a" }} />;
    }
    if (
      log.includes("FAILED") ||
      log.includes("❌") ||
      log.includes("ERROR") ||
      log.includes("LOST UPDATE")
    ) {
      return <MinusCircleOutlined style={{ color: "#ff4d4f" }} />;
    }
    if (log.includes("lock") || log.includes("Lock") || log.includes("🔒")) {
      return <LockOutlined style={{ color: "#faad14" }} />;
    }
    if (log.includes("NO LOCK") || log.includes("⚠️")) {
      return <UnlockOutlined style={{ color: "#ff7a45" }} />;
    }
    return <ThunderboltOutlined style={{ color: "#1677ff" }} />;
  };

  const getLogColor = (log) => {
    if (
      log.includes("SUCCESS") ||
      log.includes("✅") ||
      log.includes("COMMITTED") ||
      log.includes("chính xác")
    )
      return "green";
    if (
      log.includes("FAILED") ||
      log.includes("❌") ||
      log.includes("ERROR") ||
      log.includes("LOST UPDATE")
    )
      return "red";
    if (log.includes("[T1]")) return "blue";
    if (log.includes("[T2]")) return "purple";
    return "gray";
  };

  const branchOptions = [
    { value: "HN", label: "Hà Nội" },
    { value: "DN", label: "Đà Nẵng" },
    { value: "HCM", label: "TP.HCM" },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>Gửi tiền / Rút tiền</h2>
      </div>

      {/* ROW 1: Deposit + Single Withdraw */}
      <Row gutter={24}>
        {/* DEPOSIT */}
        <Col xs={24} lg={12}>
          <Card
            title={
              <>
                <div style={{ color: "#52c41a" }} /> NẠP TIỀN
              </>
            }
            style={{ borderTop: "3px solid #52c41a", marginBottom: 24 }}
          >
            <Form form={depositForm} layout="vertical" onFinish={handleDeposit}>
              <Form.Item
                name="branchId"
                label="Chi nhánh"
                rules={[{ required: true }]}
              >
                <Select placeholder="Chọn chi nhánh" options={branchOptions} />
              </Form.Item>
              <Form.Item
                name="accountId"
                label="Tài khoản"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1}
                  placeholder="Nhập ID tài khoản"
                />
              </Form.Item>
              <Form.Item
                name="amount"
                label="Số tiền (VND)"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1000}
                  step={100000}
                  placeholder="1,000,000"
                  formatter={(v) =>
                    `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                  }
                />
              </Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={depositLoading}
                block
                style={{ background: "#52c41a", borderColor: "#52c41a" }}
              >
                <div /> Nạp tiền
              </Button>
            </Form>

            {depositResult && (
              <div style={{ marginTop: 16 }}>
                <Result
                  status="success"
                  title="Nạp tiền thành công!"
                  subTitle={`Số dư mới: ${fmt(depositResult.balance)} ₫`}
                />
              </div>
            )}
          </Card>
        </Col>

        {/* SINGLE WITHDRAW */}
        <Col xs={24} lg={12}>
          <Card
            title={
              <>
                <div style={{ color: "#fa8c16" }} /> RÚT TIỀN
              </>
            }
            style={{ borderTop: "3px solid #fa8c16", marginBottom: 24 }}
          >
            <Form
              form={singleWithdrawForm}
              layout="vertical"
              onFinish={handleSingleWithdraw}
            >
              <Form.Item
                name="branchId"
                label="Chi nhánh"
                rules={[{ required: true }]}
              >
                <Select placeholder="Chọn chi nhánh" options={branchOptions} />
              </Form.Item>
              <Form.Item
                name="accountId"
                label="Tài khoản"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1}
                  placeholder="Nhập ID tài khoản"
                />
              </Form.Item>
              <Form.Item
                name="amount"
                label="Số tiền rút (VND)"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1000}
                  step={100000}
                  placeholder="1,000,000"
                  formatter={(v) =>
                    `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                  }
                />
              </Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={singleWithdrawLoading}
                block
                style={{ background: "#fa8c16", borderColor: "#fa8c16" }}
              >
                <div /> Rút tiền
              </Button>
            </Form>

            {singleWithdrawResult && (
              <div style={{ marginTop: 16 }}>
                <Result
                  status="success"
                  title="Rút tiền thành công!"
                  subTitle={`Số dư mới: ${fmt(singleWithdrawResult.balance)} ₫`}
                />
              </div>
            )}
          </Card>
        </Col>
      </Row>

      {/* ROW 2: Concurrent Withdraw (full width) */}
      <Divider style={{ margin: "8px 0 24px" }}>
        <ThunderboltOutlined />
      </Divider>

      <Card
        title={
          <>
            <div style={{ color: "#ff4d4f" }} /> RÚT TIỀN ĐỒNG
            THỜI
          </>
        }
        style={{ borderTop: "3px solid #ff4d4f", marginBottom: 24 }}
      >
        <Form
          form={withdrawForm}
          layout="vertical"
          onFinish={handleConcurrentWithdraw}
        >
          <Row gutter={24}>
            <Col xs={24} md={6}>
              <Form.Item
                name="branchId"
                label="Chi nhánh"
                rules={[{ required: true }]}
              >
                <Select placeholder="Chọn chi nhánh" options={branchOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item
                name="accountId"
                label="Tài khoản"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1}
                  placeholder="ID"
                />
              </Form.Item>
            </Col>
            <Col xs={12} md={6}>
              <Form.Item
                name="amountThread1"
                label={
                  <Space>
                    <Tag color="blue">T1</Tag> Giao dịch 1
                  </Space>
                }
                rules={[{ required: true, message: "Nhập số tiền T1" }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1000}
                  step={100000}
                  placeholder="600,000"
                  formatter={(v) =>
                    `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                  }
                />
              </Form.Item>
            </Col>
            <Col xs={12} md={6}>
              <Form.Item
                name="amountThread2"
                label={
                  <Space>
                    <Tag color="purple">T2</Tag> Giao dịch 2
                  </Space>
                }
                rules={[{ required: true, message: "Nhập số tiền T2" }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1000}
                  step={100000}
                  placeholder="600,000"
                  formatter={(v) =>
                    `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                  }
                />
              </Form.Item>
            </Col>
          </Row>

          {/* PESSIMISTIC LOCK TOGGLE */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "12px 16px",
              background: useLock ? "#f6ffed" : "#fff2f0",
              borderRadius: 8,
              border: `1px solid ${useLock ? "#b7eb8f" : "#ffccc7"}`,
              transition: "all 0.3s",
              marginBottom: 16,
            }}
          >
            <div>
              <Text
                strong
                style={{ color: useLock ? "#389e0d" : "#cf1322", fontSize: 14 }}
              >
                {useLock ? (
                  <>
                    <LockOutlined /> LOCK ON — PESSIMISTIC LOCK
                  </>
                ) : (
                  <>
                    <UnlockOutlined /> LOCK OFF
                  </>
                )}
              </Text>
              <br />
            </div>
            <Switch
              checked={useLock}
              onChange={setUseLock}
              checkedChildren={<LockOutlined />}
              unCheckedChildren={<UnlockOutlined />}
              style={{ background: useLock ? "#52c41a" : "#ff4d4f" }}
            />
          </div>

          <Button
            type="primary"
            htmlType="submit"
            loading={withdrawLoading}
            block
            danger
            size="large"
          >
            {useLock ? (
              <>
                <LockOutlined /> Rút tiền
              </>
            ) : (
              <>
                <UnlockOutlined /> Rút tiền
              </>
            )}
          </Button>
        </Form>

        {/* Concurrent result */}
        {concurrentResult && (
          <div style={{ marginTop: 24 }}>
            <Row gutter={24}>
              <Col xs={24} md={12}>
                <Alert
                  message={
                    concurrentResult.lostUpdate
                      ? "LOST UPDATE DETECTED!"
                      : "Kết quả chính xác"
                  }
                  description={
                    <Descriptions column={1} size="small" bordered>
                      <Descriptions.Item label="Balance ban đầu">
                        <Text strong>
                          {fmt(concurrentResult.initialBalance)} ₫
                        </Text>
                      </Descriptions.Item>
                      <Descriptions.Item label="Thread 1">
                        <Tag
                          color={
                            concurrentResult.thread1Result === "SUCCESS"
                              ? "green"
                              : "red"
                          }
                        >
                          {concurrentResult.thread1Result}
                        </Tag>
                      </Descriptions.Item>
                      <Descriptions.Item label="Thread 2">
                        <Tag
                          color={
                            concurrentResult.thread2Result === "SUCCESS"
                              ? "green"
                              : "red"
                          }
                        >
                          {concurrentResult.thread2Result}
                        </Tag>
                      </Descriptions.Item>
                      <Descriptions.Item label="Expected balance">
                        <Text strong style={{ color: "#1677ff", fontSize: 15 }}>
                          {fmt(concurrentResult.expectedBalance)} ₫
                        </Text>
                      </Descriptions.Item>
                      <Descriptions.Item label="Actual balance">
                        <Text
                          strong
                          style={{
                            color: concurrentResult.lostUpdate
                              ? "#ff4d4f"
                              : "#52c41a",
                            fontSize: 15,
                          }}
                        >
                          {fmt(concurrentResult.finalBalance)} ₫
                        </Text>
                      </Descriptions.Item>
                    </Descriptions>
                  }
                  type={concurrentResult.lostUpdate ? "error" : "success"}
                  showIcon
                  style={{ marginBottom: 16 }}
                />

                {concurrentResult.lostUpdate && (
                  <Alert
                    type="error"
                    showIcon
                    message={`Chênh lệch: ${fmt(concurrentResult.finalBalance - concurrentResult.expectedBalance)} ₫`}
                    description="Một giao dịch bị GHI ĐÈ! Cả 2 thread đọc cùng balance => cả 2 pass check => thread sau ghi đè thread trước => Lost Update"
                    style={{ background: "#fff2f0", marginBottom: 16 }}
                  />
                )}
                {!concurrentResult.lostUpdate &&
                  concurrentResult.thread1Result === "SUCCESS" &&
                  concurrentResult.thread2Result !== "SUCCESS" && (
                    <Alert
                      type="success"
                      showIcon
                      message="Pessimistic Lock hoạt động đúng!"
                      description="Thread 2 phải đợi Thread 1 COMMIT → đọc balance đúng → từ chối khi không đủ tiền."
                      style={{ marginBottom: 16 }}
                    />
                  )}
              </Col>

              {/* Logs timeline */}
              <Col xs={24} md={12}>
                <Card
                  size="small"
                  title={
                    <>
                      <ClockCircleOutlined /> MÔ TẢ
                    </>
                  }
                  style={{ maxHeight: 400, overflow: "auto" }}
                >
                  <Timeline
                    items={(concurrentResult.logs || []).map((log, idx) => ({
                      key: idx,
                      dot: getLogIcon(log),
                      color: getLogColor(log),
                      children: (
                        <Text
                          style={{
                            fontSize: 12,
                            fontFamily: "monospace",
                            color: log.includes("[T1]")
                              ? "#1677ff"
                              : log.includes("[T2]")
                                ? "#722ed1"
                                : log.includes("✅") ||
                                  log.includes("chính xác")
                                  ? "#52c41a"
                                  : log.includes("❌") || log.includes("LOST")
                                    ? "#ff4d4f"
                                    : "#333",
                          }}
                        >
                          {log}
                        </Text>
                      ),
                    }))}
                  />
                </Card>
              </Col>
            </Row>
          </div>
        )}
      </Card>
    </div>
  );
}
