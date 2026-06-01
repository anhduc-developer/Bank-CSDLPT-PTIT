import { useState, useEffect, useRef } from "react";
import {
  Card,
  Form,
  InputNumber,
  Select,
  Button,
  message,
  Tag,
  Row,
  Col,
  Typography,
  Divider,
  Alert,
} from "antd";
import {
  ThunderboltOutlined,
  LockOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
} from "@ant-design/icons";
import { demoApi } from "../api/bankApi";

const { Text, Title } = Typography;

const fmt = (v) => (v != null ? Number(v).toLocaleString("vi-VN") : "—");

// ============================================================
// Color Palette
// ============================================================
const COLORS = {
  thread1: "#00d4ff",
  thread2: "#ff6b6b",
  system: "#a78bfa",
  deadlock: "#ff4d4f",
  success: "#52c41a",
  warning: "#faad14",
  bg: "#0d1117",
  bgCard: "#161b22",
  border: "#30363d",
  text: "#e6edf3",
  textMuted: "#8b949e",
};

export default function DeadlockDemo() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [visibleLogs, setVisibleLogs] = useState(0);
  const logContainerRef = useRef(null);

  // Animate logs appearing one by one
  useEffect(() => {
    if (!result?.combinedLogs?.length) return;
    setVisibleLogs(0);

    let step = 0;
    const total = result.combinedLogs.length;
    const timer = setInterval(() => {
      step++;
      setVisibleLogs(step);
      if (step >= total) clearInterval(timer);
    }, 400);

    return () => clearInterval(timer);
  }, [result]);

  // Auto-scroll logs
  useEffect(() => {
    if (logContainerRef.current) {
      logContainerRef.current.scrollTop = logContainerRef.current.scrollHeight;
    }
  }, [visibleLogs]);

  const handleSubmit = async (values) => {
    setLoading(true);
    setResult(null);

    try {
      const res = await demoApi.deadlock(values);
      if (res.data?.data) {
        setResult(res.data.data);
        message.success("Demo deadlock hoàn tất!");
      } else {
        message.error(res.data?.message || "Lỗi không xác định");
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message;
      message.error(msg);
    }

    setLoading(false);
  };

  // ============================================================
  // Get log color based on content
  // ============================================================
  const getLogColor = (log) => {
    if (log.includes("THREAD-1")) return COLORS.thread1;
    if (log.includes("THREAD-2")) return COLORS.thread2;
    if (log.includes("SYSTEM")) return COLORS.system;
    return COLORS.text;
  };

  const isDeadlockLog = (log) =>
    log.includes("DEADLOCK") || log.includes("VICTIM");
  const isSuccessLog = (log) =>
    log.includes("COMMIT") ||
    log.includes("WINNER") ||
    log.includes("thành công");
  const isErrorLog = (log) =>
    log.includes("ROLLBACK") || log.includes("Error");

  // ============================================================
  // Render
  // ============================================================
  return (
    <div className="fade-in">
      <style>{`
        @keyframes deadlockPulse {
          0%, 100% { box-shadow: 0 0 8px rgba(255, 77, 79, 0.3); }
          50% { box-shadow: 0 0 24px rgba(255, 77, 79, 0.8); }
        }
        @keyframes logSlideIn {
          from { opacity: 0; transform: translateX(-20px); }
          to { opacity: 1; transform: translateX(0); }
        }
        @keyframes shimmer {
          0% { background-position: -200% 0; }
          100% { background-position: 200% 0; }
        }
        @keyframes winnerGlow {
          0%, 100% { box-shadow: 0 0 8px rgba(82, 196, 26, 0.3); }
          50% { box-shadow: 0 0 20px rgba(82, 196, 26, 0.6); }
        }
        .deadlock-terminal::-webkit-scrollbar {
          width: 6px;
        }
        .deadlock-terminal::-webkit-scrollbar-track {
          background: ${COLORS.bg};
        }
        .deadlock-terminal::-webkit-scrollbar-thumb {
          background: ${COLORS.border};
          border-radius: 3px;
        }
      `}</style>

      <div className="page-header">
        <h2>
          <div style={{ color: "#faad14", marginRight: 8 }} />
          DEADLOCK
        </h2>
      </div>

      <Row gutter={24}>
        {/* ============================================================ */}
        {/* INPUT FORM */}
        {/* ============================================================ */}
        <Col xs={24} lg={8}>
          <Card
            title={
              <span>
                <div style={{ marginRight: 8 }} />
                GIAO DỊCH
              </span>
            }
            style={{
              borderTop: "3px solid #faad14",
              marginBottom: 24,
            }}
          >


            <Form form={form} layout="vertical" onFinish={handleSubmit}>
              <Form.Item
                name="branchId"
                label="Chi nhánh"
                rules={[{ required: true, message: "Chọn chi nhánh" }]}
              >
                <Select
                  placeholder="Chọn chi nhánh"
                  options={[
                    { value: "HN", label: "Hà Nội" },
                    { value: "DN", label: "Đà Nẵng" },
                    { value: "HCM", label: "TP.HCM" },
                  ]}
                />
              </Form.Item>

              <Row gutter={12}>
                <Col span={12}>
                  <Form.Item
                    name="accountAId"
                    label="Account A"
                    rules={[{ required: true, message: "Nhập ID" }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID tài khoản A"
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="accountBId"
                    label="Account B"
                    rules={[{ required: true, message: "Nhập ID" }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID tài khoản B"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={12}>
                <Col span={12}>
                  <Form.Item
                    name="amountAtoB"
                    label={
                      <span>
                        <span style={{ color: COLORS.thread1 }}>T1</span>{" "}
                        (A→B)
                      </span>
                    }
                    rules={[{ required: true, message: "Nhập số tiền" }]}
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
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="amountBtoA"
                    label={
                      <span>
                        <span style={{ color: COLORS.thread2 }}>T2</span>{" "}
                        (B→A)
                      </span>
                    }
                    rules={[{ required: true, message: "Nhập số tiền" }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1000}
                      step={100000}
                      placeholder="2,000,000"
                      formatter={(v) =>
                        `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                      }
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                block
                size="large"
                style={{
                  background: "linear-gradient(135deg, #1890ff, #096dd9)",
                  borderColor: "#1890ff",
                  height: 48,
                  fontSize: 16,
                  fontWeight: 600,
                }}
              >
                {loading ? "Đang chạy demo..." : "RUN"}
              </Button>
            </Form>

          </Card>
        </Col>

        {/* ============================================================ */}
        {/* RESULT AREA */}
        {/* ============================================================ */}
        <Col xs={24} lg={16}>
          {!result && !loading && (
            <Card
              style={{
                textAlign: "center",
                padding: "60px 20px",
                background: "#fafafa",
                border: "2px dashed #d9d9d9",
              }}
            >
              <ThunderboltOutlined
                style={{ fontSize: 64, color: "#d9d9d9", marginBottom: 16 }}
              />
              <Title level={4} style={{ color: "#bfbfbf" }}>
                Chưa có kết quả
              </Title>
              <Text type="secondary">
                Nhập thông tin và bấm &quot;Bắt đầu Demo Deadlock&quot; để xem kết quả
              </Text>
            </Card>
          )}

          {loading && (
            <Card
              style={{
                textAlign: "center",
                padding: "60px 20px",
                background: COLORS.bg,
                border: `1px solid ${COLORS.border}`,
              }}
            >
              <Title level={4} style={{ color: COLORS.deadlock }}>
                Đang thực thi giao dịch...
              </Title>
              <Text style={{ color: COLORS.textMuted }}>
                2 Thread đang chạy song song — chờ phản hồi từ hệ thống
              </Text>
              <div
                style={{
                  marginTop: 20,
                  height: 4,
                  borderRadius: 2,
                  background: `linear-gradient(90deg, ${COLORS.thread1}, ${COLORS.deadlock}, ${COLORS.thread2}, ${COLORS.deadlock}, ${COLORS.thread1})`,
                  backgroundSize: "200% 100%",
                  animation: "shimmer 1.5s ease-in-out infinite",
                }}
              />
            </Card>
          )}

          {result && (
            <>
              {/* Winner / Victim Summary */}
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={12}>
                  <Card
                    style={{
                      borderRadius: 12,
                      border: `2px solid ${COLORS.success}`,
                      background: `${COLORS.success}08`,
                      animation: "winnerGlow 2s ease-in-out infinite",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: 12,
                      }}
                    >
                      <CheckCircleOutlined
                        style={{ fontSize: 32, color: COLORS.success }}
                      />
                      <div>
                        <Tag color="green" style={{ fontWeight: 700, marginBottom: 4 }}>
                          WINNER
                        </Tag>
                        <div
                          style={{
                            fontWeight: 700,
                            fontSize: 16,
                            color: "#262626",
                          }}
                        >
                          {result.winnerThread}
                        </div>
                        <div style={{ fontSize: 12, color: "#8c8c8c" }}>
                          {result.winnerDirection}
                        </div>
                        <div
                          style={{
                            fontSize: 12,
                            color: COLORS.success,
                            marginTop: 4,
                          }}
                        >
                          COMMITTED thành công
                        </div>
                      </div>
                    </div>
                  </Card>
                </Col>
                <Col span={12}>
                  <Card
                    style={{
                      borderRadius: 12,
                      border: `2px solid ${COLORS.deadlock}`,
                      background: `${COLORS.deadlock}08`,
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: 12,
                      }}
                    >
                      <CloseCircleOutlined
                        style={{ fontSize: 32, color: COLORS.deadlock }}
                      />
                      <div>
                        <Tag color="red" style={{ fontWeight: 700, marginBottom: 4 }}>
                          VICTIM
                        </Tag>
                        <div
                          style={{
                            fontWeight: 700,
                            fontSize: 16,
                            color: "#262626",
                          }}
                        >
                          {result.victimThread}
                        </div>
                        <div style={{ fontSize: 12, color: "#8c8c8c" }}>
                          {result.victimDirection}
                        </div>
                        <div
                          style={{
                            fontSize: 12,
                            color: COLORS.deadlock,
                            marginTop: 4,
                          }}
                        >
                          MySQL ROLLBACK
                        </div>
                      </div>
                    </div>
                  </Card>
                </Col>
              </Row>

              {/* Balance Changes */}
              <Card
                style={{
                  marginBottom: 16,
                  borderRadius: 12,
                  borderTop: "3px solid #667eea",
                }}
              >
                <Title level={5} style={{ marginBottom: 16 }}>
                  Thay đổi số dư
                </Title>
                <Row gutter={16}>
                  <Col span={12}>
                    <div
                      style={{
                        padding: 16,
                        borderRadius: 10,
                        background:
                          "linear-gradient(135deg, #667eea10, #764ba210)",
                        border: "1px solid #667eea33",
                      }}
                    >
                      <div
                        style={{
                          fontSize: 12,
                          color: "#8c8c8c",
                          marginBottom: 4,
                        }}
                      >
                        Account #{result.accountAId}
                      </div>
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        <span
                          style={{
                            fontSize: 18,
                            fontWeight: 700,
                            fontFamily: "'JetBrains Mono', monospace",
                            color:
                              result.balanceABefore !== result.balanceAAfter
                                ? "#667eea"
                                : "#262626",
                          }}
                        >
                          {fmt(result.balanceABefore)} ₫
                        </span>
                        <span style={{ color: "#8c8c8c" }}>→</span>
                        <span
                          style={{
                            fontSize: 18,
                            fontWeight: 700,
                            fontFamily: "'JetBrains Mono', monospace",
                            color:
                              result.balanceABefore !== result.balanceAAfter
                                ? "#52c41a"
                                : "#262626",
                          }}
                        >
                          {fmt(result.balanceAAfter)} ₫
                        </span>
                      </div>
                    </div>
                  </Col>
                  <Col span={12}>
                    <div
                      style={{
                        padding: 16,
                        borderRadius: 10,
                        background:
                          "linear-gradient(135deg, #f5a62310, #f7971e10)",
                        border: "1px solid #f5a62333",
                      }}
                    >
                      <div
                        style={{
                          fontSize: 12,
                          color: "#8c8c8c",
                          marginBottom: 4,
                        }}
                      >
                        Account #{result.accountBId}
                      </div>
                      <div
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 8,
                        }}
                      >
                        <span
                          style={{
                            fontSize: 18,
                            fontWeight: 700,
                            fontFamily: "'JetBrains Mono', monospace",
                            color:
                              result.balanceBBefore !== result.balanceBAfter
                                ? "#f5a623"
                                : "#262626",
                          }}
                        >
                          {fmt(result.balanceBBefore)} ₫
                        </span>
                        <span style={{ color: "#8c8c8c" }}> → </span>
                        <span
                          style={{
                            fontSize: 18,
                            fontWeight: 700,
                            fontFamily: "'JetBrains Mono', monospace",
                            color:
                              result.balanceBBefore !== result.balanceBAfter
                                ? "#52c41a"
                                : "#262626",
                          }}
                        >
                          {fmt(result.balanceBAfter)} ₫
                        </span>
                      </div>
                    </div>
                  </Col>
                </Row>
              </Card>

              {/* Combined Terminal Logs */}
              <Card
                title={
                  <span style={{ color: COLORS.text }}>
                    Terminal Log — Chi tiết từng bước
                    <Tag
                      color="blue"
                      style={{ marginLeft: 12, fontSize: 11, fontWeight: 600 }}
                    >
                      {visibleLogs} / {result.combinedLogs.length}
                    </Tag>
                  </span>
                }
                style={{
                  marginBottom: 16,
                  background: COLORS.bg,
                  border: `1px solid ${COLORS.border}`,
                  borderRadius: 12,
                }}
                headStyle={{
                  background: COLORS.bgCard,
                  borderBottom: `1px solid ${COLORS.border}`,
                  borderRadius: "12px 12px 0 0",
                }}
                bodyStyle={{ padding: 0 }}
              >
                <div
                  ref={logContainerRef}
                  className="deadlock-terminal"
                  style={{
                    maxHeight: 500,
                    overflowY: "auto",
                    padding: 20,
                  }}
                >
                  {result.combinedLogs.map((log, idx) => {
                    if (idx >= visibleLogs) return null;

                    const color = getLogColor(log);
                    const isDeadlock = isDeadlockLog(log);
                    const isSuccess = isSuccessLog(log);
                    const isError = isErrorLog(log);

                    return (
                      <div
                        key={idx}
                        style={{
                          marginBottom: 8,
                          fontFamily:
                            "'JetBrains Mono', 'Fira Code', monospace",
                          fontSize: 13,
                          lineHeight: 1.8,
                          animation: "logSlideIn 0.3s ease-out",
                          padding: "6px 12px",
                          borderRadius: 6,
                          borderLeft: `3px solid ${color}`,
                          background: isDeadlock
                            ? `${COLORS.deadlock}15`
                            : isSuccess
                              ? `${COLORS.success}10`
                              : isError
                                ? `${COLORS.deadlock}08`
                                : `${color}08`,
                          ...(isDeadlock
                            ? {
                              animation:
                                "logSlideIn 0.3s ease-out, deadlockPulse 1.5s ease-in-out infinite",
                            }
                            : {}),
                        }}
                      >
                        <span style={{ color: COLORS.textMuted, marginRight: 12 }}>
                          {String(idx + 1).padStart(2, "0")}
                        </span>
                        <span
                          style={{
                            color,
                            fontWeight: isDeadlock || isSuccess ? 700 : 400,
                          }}
                        >
                          {log}
                        </span>
                      </div>
                    );
                  })}

                  {/* Blinking cursor */}
                  {visibleLogs < result.combinedLogs.length && (
                    <div
                      style={{
                        display: "inline-block",
                        width: 8,
                        height: 16,
                        background: COLORS.text,
                        marginLeft: 32,
                        animation: "deadlockPulse 1s step-end infinite",
                      }}
                    />
                  )}
                </div>
              </Card>


            </>
          )}
        </Col>
      </Row>
    </div>
  );
}