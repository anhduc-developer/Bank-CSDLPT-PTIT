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
  Typography,
} from "antd";

import { transferApi } from "../api/bankApi";
import { useState, useEffect } from "react";

const { Text } = Typography;

const fmt = (v) => (v != null ? Number(v).toLocaleString("vi-VN") : "—");

export default function Transfers() {
  const [internalForm] = Form.useForm();

  const [interForm] = Form.useForm();

  const [internalResult, setInternalResult] = useState(null);

  const [interResult, setInterResult] = useState(null);

  const [internalLoading, setInternalLoading] = useState(false);

  const [interLoading, setInterLoading] = useState(false);

  const [simulateCrash, setSimulateCrash] = useState(false);
  const handleInternal = async (values) => {
    setInternalLoading(true);

    setInternalResult(null);

    try {
      const res = await transferApi.internal(values);

      setInternalResult(
        res.data.success
          ? res.data.data
          : {
            status: "FAILED",
            message: res.data.message,
          },
      );

      if (res.data.success) {
        message.success("Chuyển tiền thành công");
      } else {
        message.error(res.data.message);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message;

      setInternalResult({
        status: "FAILED",
        message: msg,
      });

      message.error(msg);
    }

    setInternalLoading(false);
  };

  const handleInterBranch = async (values) => {
    setInterLoading(true);

    setInterResult(null);

    try {
      const res = await transferApi.interBranch({
        ...values,
        simulateCrash,
      });
      if (res.data.data) {
        setInterResult(res.data.data);
      } else if (res.data.success) {
        setInterResult(res.data.data);
      } else {
        setInterResult({
          status: "FAILED",
          message: res.data.message,
        });
      }

      if (res.data.success) {
        message.success("Transaction COMMITTED");
      } else {
        message.error(res.data.message);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message;
      const data = err.response?.data?.data;

      if (data) {
        setInterResult(data);
      } else {
        setInterResult({
          status: "FAILED",
          message: msg,
        });
      }

      message.error(msg);
    }

    setInterLoading(false);
  };

  const BalanceStepTimeline = ({ result }) => {
    const [visibleSteps, setVisibleSteps] = useState(0);

    const isFailed = result.status === "FAILED";
    const hasBalanceData = result.sourceBalanceBefore != null;

    useEffect(() => {
      if (!hasBalanceData) return;
      setVisibleSteps(0);

      const totalSteps = isFailed ? 4 : 3;
      let step = 0;

      const timer = setInterval(() => {
        step++;
        setVisibleSteps(step);
        if (step >= totalSteps) clearInterval(timer);
      }, 800);

      return () => clearInterval(timer);
    }, [result, hasBalanceData, isFailed]);

    if (!hasBalanceData) return null;

    const steps = [
      {
        icon: "",
        title: "SỐ DƯ BAN ĐẦU",
        subtitle: "Đọc số dư từ cả 2 chi nhánh",
        color: "#667eea",
        bgGradient: "linear-gradient(135deg, #667eea22, #764ba222)",
        borderColor: "#667eea",
        items: [
          {
            label: `${result.fromBranch} (TK #${result.fromAccountId})`,
            value: fmt(result.sourceBalanceBefore),
            tag: "SOURCE",
            tagColor: "blue",
          },
          {
            label: `${result.toBranch} (TK #${result.toAccountId})`,
            value: fmt(result.destBalanceBefore),
            tag: "DEST",
            tagColor: "cyan",
          },
        ],
      },
      {
        icon: "",
        title: "SOURCE TRỪ TIỀN",
        subtitle: `Trừ ${fmt(result.amount)} ₫ từ tài khoản nguồn`,
        color: "#f5a623",
        bgGradient: "linear-gradient(135deg, #f5a62322, #f7971e22)",
        borderColor: "#f5a623",
        items: [
          {
            label: `${result.fromBranch} (TK #${result.fromAccountId})`,
            value: fmt(result.sourceBalanceAfterDebit),
            tag: "ĐÃ TRỪ",
            tagColor: "orange",
            highlight: true,
            oldValue: fmt(result.sourceBalanceBefore),
          },
          {
            label: `${result.toBranch} (TK #${result.toAccountId})`,
            value: fmt(result.destBalanceBefore),
            tag: "CHƯA ĐỔI",
            tagColor: "default",
          },
        ],
      },
    ];

    if (isFailed) {
      // Step 3: CRASH
      steps.push({
        icon: "",
        title: "DESTINATION SERVER CRASH!",
        subtitle: `${result.toBranch} server bị crash — không thể cộng tiền cho tài khoản đích`,
        color: "#ff4d4f",
        bgGradient: "linear-gradient(135deg, #ff4d4f22, #ff616622)",
        borderColor: "#ff4d4f",
        isCrash: true,
        items: [
          {
            label: `${result.fromBranch} — Đã trừ ${fmt(result.amount)} ₫`,
            value: fmt(result.sourceBalanceAfterDebit),
            tag: "MẤT TIỀN",
            tagColor: "red",
          },
          {
            label: `${result.toBranch} — Chưa nhận được tiền`,
            value: fmt(result.destBalanceBefore),
            tag: "CRASH",
            tagColor: "red",
          },
        ],
      });

      // Step 4: ROLLBACK
      steps.push({
        icon: "",
        title: "ROLLBACK — HOÀN TIỀN",
        subtitle: "2PC phát hiện lỗi → ABORT → Rollback tất cả thay đổi",
        color: "#52c41a",
        bgGradient: "linear-gradient(135deg, #52c41a22, #73d13d22)",
        borderColor: "#52c41a",
        isRollback: true,
        items: [
          {
            label: `${result.fromBranch} (TK #${result.fromAccountId})`,
            value: fmt(result.sourceBalanceAfter),
            tag: "HOÀN LẠI",
            tagColor: "green",
            highlight: true,
            oldValue: fmt(result.sourceBalanceAfterDebit),
          },
          {
            label: `${result.toBranch} (TK #${result.toAccountId})`,
            value: fmt(result.destBalanceAfter),
            tag: "NGUYÊN VẸN",
            tagColor: "green",
          },
        ],
      });
    } else {
      // Step 3: Commit thành công
      steps.push({
        icon: "",
        title: "COMMIT THÀNH CÔNG",
        subtitle: "Cả 2 site đã COMMIT — giao dịch hoàn tất",
        color: "#52c41a",
        bgGradient: "linear-gradient(135deg, #52c41a22, #73d13d22)",
        borderColor: "#52c41a",
        items: [
          {
            label: `${result.fromBranch} (TK #${result.fromAccountId})`,
            value: fmt(result.sourceBalanceAfter),
            tag: "FINAL",
            tagColor: "green",
          },
          {
            label: `${result.toBranch} (TK #${result.toAccountId})`,
            value: fmt(result.destBalanceAfter),
            tag: "FINAL",
            tagColor: "green",
          },
        ],
      });
    }

    return (
      <div style={{ marginTop: 24 }}>
        <div
          style={{
            marginBottom: 20,
            fontWeight: 700,
            fontSize: 18,
            display: "flex",
            alignItems: "center",
            gap: 8,
          }}
        >
          MÔ TẢ
          {isFailed && (
            <Tag color="red" style={{ fontSize: 12, fontWeight: 600 }}>
              CRASH → ROLLBACK
            </Tag>
          )}
        </div>

        <div style={{ position: "relative" }}>
          <div
            style={{
              position: "absolute",
              left: 24,
              top: 0,
              bottom: 0,
              width: 3,
              background: "linear-gradient(to bottom, #667eea, #f5a623, #ff4d4f, #52c41a)",
              borderRadius: 3,
              zIndex: 0,
            }}
          />

          {steps.map((step, idx) => (
            <div
              key={idx}
              style={{
                position: "relative",
                marginBottom: idx < steps.length - 1 ? 16 : 0,
                paddingLeft: 56,
                opacity: visibleSteps > idx ? 1 : 0.15,
                transform: visibleSteps > idx ? "translateX(0)" : "translateX(20px)",
                transition: "all 0.6s cubic-bezier(0.4, 0, 0.2, 1)",
              }}
            >
              {/* Circle node */}
              <div
                style={{
                  position: "absolute",
                  left: 12,
                  top: 16,
                  width: 28,
                  height: 28,
                  borderRadius: "50%",
                  background: visibleSteps > idx ? step.borderColor : "#d9d9d9",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: 14,
                  zIndex: 1,
                  boxShadow: visibleSteps > idx
                    ? `0 0 12px ${step.borderColor}66`
                    : "none",
                  transition: "all 0.6s ease",
                }}
              >
                {step.icon}
              </div>

              {/* Step card */}
              <div
                style={{
                  background: step.bgGradient,
                  borderRadius: 12,
                  border: `1px solid ${step.borderColor}44`,
                  padding: "16px 20px",
                  backdropFilter: "blur(8px)",
                  ...(step.isCrash && visibleSteps > idx
                    ? {
                      animation: "shake 0.5s ease-in-out",
                      boxShadow: `0 0 20px ${step.borderColor}33`,
                    }
                    : {}),
                  ...(step.isRollback && visibleSteps > idx
                    ? {
                      boxShadow: `0 0 20px ${step.borderColor}33`,
                    }
                    : {}),
                }}
              >
                {/* Step header */}
                <div style={{ marginBottom: 12 }}>
                  <div
                    style={{
                      fontWeight: 700,
                      fontSize: 15,
                      color: step.color,
                      marginBottom: 2,
                      display: "flex",
                      alignItems: "center",
                      gap: 8,
                    }}
                  >
                    <span
                      style={{
                        background: step.borderColor,
                        color: "#fff",
                        borderRadius: 6,
                        padding: "1px 8px",
                        fontSize: 11,
                        fontWeight: 700,
                      }}
                    >
                      STEP {idx + 1}
                    </span>
                    {step.title}
                  </div>
                  <div style={{ fontSize: 12, color: "#8c8c8c" }}>
                    {step.subtitle}
                  </div>
                </div>

                {/* Balance items */}
                {step.items.map((item, iIdx) => (
                  <div
                    key={iIdx}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      padding: "8px 12px",
                      borderRadius: 8,
                      background: item.highlight
                        ? `${step.borderColor}11`
                        : "rgba(255,255,255,0.5)",
                      marginBottom: iIdx < step.items.length - 1 ? 6 : 0,
                      border: item.highlight
                        ? `1px dashed ${step.borderColor}55`
                        : "1px solid transparent",
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      <Tag
                        color={item.tagColor}
                        style={{ margin: 0, fontSize: 11, fontWeight: 600 }}
                      >
                        {item.tag}
                      </Tag>
                      <span style={{ fontSize: 13, color: "#595959" }}>
                        {item.label}
                      </span>
                    </div>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                      {item.oldValue && (
                        <>
                          <span
                            style={{
                              textDecoration: "line-through",
                              color: "#bfbfbf",
                              fontSize: 13,
                            }}
                          >
                            {item.oldValue} ₫
                          </span>
                          <span style={{ color: step.borderColor }}>→</span>
                        </>
                      )}
                      <span
                        style={{
                          fontWeight: 700,
                          fontSize: 15,
                          color: item.highlight ? step.borderColor : "#262626",
                          fontFamily: "'JetBrains Mono', monospace",
                        }}
                      >
                        {item.value} ₫
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Kết luận */}
        {visibleSteps >= steps.length && (
          <div
            style={{
              marginTop: 20,
              padding: "14px 20px",
              borderRadius: 12,
              background: isFailed
                ? "linear-gradient(135deg, #52c41a11, #73d13d11)"
                : "linear-gradient(135deg, #52c41a11, #73d13d11)",
              border: `1px solid ${isFailed ? "#52c41a" : "#52c41a"}44`,
              textAlign: "center",
              animation: "fadeInUp 0.5s ease",
            }}
          >
            <span style={{ fontSize: 20, marginRight: 8 }}>

            </span>
            <Text
              strong
              style={{
                color: isFailed ? "#52c41a" : "#52c41a",
                fontSize: 14,
              }}
            >
              {isFailed
                ? "2PC đã bảo vệ tính toàn vẹn dữ liệu — Số dư đã được khôi phục về ban đầu!"
                : "Giao dịch 2PC hoàn tất thành công — Dữ liệu nhất quán trên tất cả các site!"}
            </Text>
          </div>
        )}
      </div>
    );
  };

  // =========================
  // RESULT UI
  // =========================
  const renderResult = (result, showBalanceSteps = false) => {
    if (!result) return null;

    const isSuccess = result.status === "SUCCESS";

    return (
      <div style={{ marginTop: 16 }}>
        <Card
          style={{
            borderLeft: `4px solid ${isSuccess ? "#52c41a" : "#ff4d4f"}`,
          }}
        >
          <Result
            status={isSuccess ? "success" : "error"}
            title={
              isSuccess ? "Chuyển tiền thành công" : "Chuyển tiền thất bại"
            }
            subTitle={
              result.transactionId
                ? `Transaction ID: ${result.transactionId}`
                : result.message
            }
          />

          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="Từ tài khoản">
              #{result.fromAccountId}
            </Descriptions.Item>

            <Descriptions.Item label="Đến tài khoản">
              #{result.toAccountId}
            </Descriptions.Item>

            <Descriptions.Item label="Từ chi nhánh">
              <Tag color="blue">{result.fromBranch}</Tag>
            </Descriptions.Item>

            <Descriptions.Item label="Đến chi nhánh">
              <Tag color="green">{result.toBranch}</Tag>
            </Descriptions.Item>

            <Descriptions.Item label="Số tiền">
              <strong>{fmt(result.amount)} ₫</strong>
            </Descriptions.Item>

            <Descriptions.Item label="Trạng thái">
              <Tag color={isSuccess ? "green" : "red"}>{result.status}</Tag>
            </Descriptions.Item>

            <Descriptions.Item label="Số dư nguồn">
              {fmt(result.sourceBalanceAfter)} ₫
            </Descriptions.Item>

            <Descriptions.Item label="Số dư đích">
              {fmt(result.destBalanceAfter)} ₫
            </Descriptions.Item>
          </Descriptions>
          {showBalanceSteps && <BalanceStepTimeline result={result} />}

          {result.logs && result.logs.length > 0 && (
            <div style={{ marginTop: 24 }}>
              <div
                style={{
                  marginBottom: 16,
                  fontWeight: 700,
                  fontSize: 18,
                }}
              >
                Chi tiết giao dịch 2PC
              </div>

              <div
                style={{
                  background: "#0d1117",
                  borderRadius: 12,
                  padding: 20,
                  border: "1px solid #30363d",
                }}
              >
                {result.logs.map((log, index) => {
                  let color = "#00ff90";

                  if (
                    log.includes("CRASH") ||
                    log.includes("ABORT") ||
                    log.includes("ROLLBACK") ||
                    log.includes("LỖI")
                  ) {
                    color = "#ff7875";
                  }

                  if (log.includes("COMMIT")) {
                    color = "#69c0ff";
                  }

                  if (log.includes("STEP")) {
                    color = "#ffd666";
                  }

                  return (
                    <div
                      key={index}
                      style={{
                        marginBottom: 10,
                        color,
                        fontFamily: "monospace",
                        fontSize: 14,
                        lineHeight: 1.8,
                        borderLeft: `3px solid ${color}`,
                        paddingLeft: 12,
                      }}
                    >
                      <strong>[{String(index + 1).padStart(2, "0")}]</strong>{" "}
                      {log}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </Card>
      </div>
    );
  };

  return (
    <div className="fade-in">
      {/* Inline keyframe styles */}
      <style>{`
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
          20%, 40%, 60%, 80% { transform: translateX(4px); }
        }
        @keyframes fadeInUp {
          from { opacity: 0; transform: translateY(12px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      <div className="page-header">
        <h2>Chuyển tiền</h2>
      </div>

      <Row gutter={24}>
        {/* =========================
            INTERNAL TRANSFER
        ========================= */}
        <Col xs={24} lg={12}>
          <Card
            title="Chuyển cùng chi nhánh"
            style={{
              borderTop: "3px solid #1677ff",
              marginBottom: 24,
            }}
          >
            <Form
              form={internalForm}
              layout="vertical"
              onFinish={handleInternal}
            >
              <Form.Item
                name="branchId"
                label="Chi nhánh"
                rules={[{ required: true }]}
              >
                <Select
                  placeholder="Chọn chi nhánh"
                  options={[
                    {
                      value: "HN",
                      label: "Hà Nội",
                    },
                    {
                      value: "DN",
                      label: "Đà Nẵng",
                    },
                    {
                      value: "HCM",
                      label: "TP.HCM",
                    },
                  ]}
                />
              </Form.Item>

              <Row gutter={12}>
                <Col span={12}>
                  <Form.Item
                    name="fromAccountId"
                    label="Từ TK"
                    rules={[{ required: true }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID nguồn"
                    />
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    name="toAccountId"
                    label="Đến TK"
                    rules={[{ required: true }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID đích"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="amount"
                label="Số tiền"
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
              <div style={{ height: 74, marginBottom: 16, }} />
              <Button
                type="primary"
                htmlType="submit"
                loading={internalLoading}
                block
              >
                Chuyển tiền nội bộ
              </Button>
            </Form>

            {renderResult(internalResult)}
          </Card>
        </Col>

        {/* =========================
            INTER BRANCH TRANSFER
        ========================= */}
        <Col xs={24} lg={12}>
          <Card
            title="Chuyển liên chi nhánh"
            style={{
              borderTop: "3px solid #722ed1",
              marginBottom: 24,
            }}
          >
            <Form
              form={interForm}
              layout="vertical"
              onFinish={handleInterBranch}
            >
              <Row gutter={12}>
                <Col span={12}>
                  <Form.Item
                    name="fromBranch"
                    label="Từ CN"
                    rules={[{ required: true }]}
                  >
                    <Select
                      placeholder="CN nguồn"
                      options={[
                        {
                          value: "HN",
                          label: "Hà Nội",
                        },
                        {
                          value: "DN",
                          label: "Đà Nẵng",
                        },
                        {
                          value: "HCM",
                          label: "TP.HCM",
                        },
                      ]}
                    />
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    name="fromAccountId"
                    label="Từ TK"
                    rules={[{ required: true }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID nguồn"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={12}>
                <Col span={12}>
                  <Form.Item
                    name="toBranch"
                    label="Đến CN"
                    rules={[{ required: true }]}
                  >
                    <Select
                      placeholder="CN đích"
                      options={[
                        {
                          value: "HN",
                          label: "Hà Nội",
                        },
                        {
                          value: "DN",
                          label: "Đà Nẵng",
                        },
                        {
                          value: "HCM",
                          label: "TP.HCM",
                        },
                      ]}
                    />
                  </Form.Item>
                </Col>

                <Col span={12}>
                  <Form.Item
                    name="toAccountId"
                    label="Đến TK"
                    rules={[{ required: true }]}
                  >
                    <InputNumber
                      style={{ width: "100%" }}
                      min={1}
                      placeholder="ID đích"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="amount"
                label="Số tiền"
                rules={[{ required: true }]}
              >
                <InputNumber
                  style={{ width: "100%" }}
                  min={1000}
                  step={100000}
                  placeholder="5,000,000"
                  formatter={(v) =>
                    `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                  }
                />
              </Form.Item>

              {/* =========================
                  SIMULATE CRASH
              ========================= */}
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  padding: "12px 16px",
                  background: simulateCrash ? "#fff2f0" : "#f6ffed",
                  borderRadius: 8,
                  border: `1px solid ${simulateCrash ? "#ffccc7" : "#b7eb8f"}`,
                  marginBottom: 16,
                }}
              >
                <div>
                  <Text
                    strong
                    style={{
                      color: simulateCrash ? "#cf1322" : "#389e0d",
                    }}
                  >
                    {simulateCrash
                      ? "SERVER CRASH"
                      : "HOẠT ĐỘNG BÌNH THƯỜNG"}
                  </Text>

                  <br />

                  <Text type="secondary" style={{ fontSize: 11 }}>
                    {simulateCrash
                      ? "Source trừ tiền => Dest crash => Rollback"
                      : "Chuyển tiền bình thường bằng 2PC"}
                  </Text>
                </div>

                <Switch
                  checked={simulateCrash}
                  onChange={setSimulateCrash}
                  checkedChildren="CRASH"
                  unCheckedChildren="OK"
                />
              </div>

              <Button
                type="primary"
                htmlType="submit"
                loading={interLoading}
                block
                style={{
                  background: simulateCrash ? "#ff4d4f" : "#722ed1",

                  borderColor: simulateCrash ? "#ff4d4f" : "#722ed1",
                }}
              >
                {simulateCrash
                  ? "Chuyển tiền (Giả lập CRASH)"
                  : "Chuyển liên chi nhánh (2PC)"}
              </Button>
            </Form>

            {renderResult(interResult, true)}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
