import { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  Row,
  Col,
  Select,
  Spin,
  Alert,
  Divider,
  Timeline,
  Badge,
  Statistic,
  message,
  Descriptions,
} from "antd";
import {
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  DatabaseOutlined,
  CloudSyncOutlined,
  ExperimentOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
} from "@ant-design/icons";
import { replicationApi } from "../api/bankApi";

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

export default function Replication() {
  const [statusData, setStatusData] = useState([]);
  const [compareData, setCompareData] = useState(null);
  const [lagResult, setLagResult] = useState(null);
  const [loading, setLoading] = useState({
    status: false,
    compare: false,
    lag: false,
  });
  const [selectedBranch, setSelectedBranch] = useState("HN");

  const fetchStatus = async () => {
    setLoading((prev) => ({ ...prev, status: true }));
    try {
      const res = await replicationApi.getStatus();
      setStatusData(res.data);
    } catch (err) {
      message.error("Không thể lấy trạng thái Replication: " + err.message);
    } finally {
      setLoading((prev) => ({ ...prev, status: false }));
    }
  };

  // Load replication status on mount
  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchCompare = async (branch) => {
    setLoading((prev) => ({ ...prev, compare: true }));
    try {
      const res = await replicationApi.compare(branch);
      setCompareData(res.data);
    } catch (err) {
      message.error("Không thể so sánh dữ liệu: " + err.message);
    } finally {
      setLoading((prev) => ({ ...prev, compare: false }));
    }
  };

  const demoLag = async (branch) => {
    setLoading((prev) => ({ ...prev, lag: true }));
    setLagResult(null);
    try {
      const res = await replicationApi.demoLag(branch);
      setLagResult(res.data);
      message.success("Demo Replication Lag hoàn tất!");
    } catch (err) {
      message.error("Lỗi demo Replication Lag: " + err.message);
    } finally {
      setLoading((prev) => ({ ...prev, lag: false }));
    }
  };

  // ============================================================
  // SECTION 1: Trạng thái Replication
  // ============================================================

  const statusColumns = [
    {
      title: "Chi nhánh",
      dataIndex: "branchName",
      key: "branchName",
      render: (text, record) => (
        <Space>
          <DatabaseOutlined />
          <Text strong>{text}</Text>
          <Tag color="blue">{record.branchId}</Tag>
        </Space>
      ),
    },
    {
      title: "Trạng thái",
      key: "healthy",
      render: (_, record) => (
        <Tag
          icon={
            record.healthy ? (
              <CheckCircleOutlined />
            ) : (
              <CloseCircleOutlined />
            )
          }
          color={record.healthy ? "success" : "error"}
        >
          {record.healthy ? "ĐANG HOẠT ĐỘNG" : "CÓ VẤN ĐỀ"}
        </Tag>
      ),
    },
    {
      title: "IO Thread",
      dataIndex: "slaveIORunning",
      key: "slaveIORunning",
      render: (val) => (
        <Tag color={val === "Yes" ? "green" : "red"}>{val || "N/A"}</Tag>
      ),
    },
    {
      title: "SQL Thread",
      dataIndex: "slaveSQLRunning",
      key: "slaveSQLRunning",
      render: (val) => (
        <Tag color={val === "Yes" ? "green" : "red"}>{val || "N/A"}</Tag>
      ),
    },
    {
      title: "Độ trễ (giây)",
      dataIndex: "secondsBehindMaster",
      key: "secondsBehindMaster",
      render: (val) => {
        if (val === null || val === undefined) return <Tag>N/A</Tag>;
        const num = Number(val);
        return (
          <Tag color={num === 0 ? "green" : num < 5 ? "orange" : "red"}>
            {num}s
          </Tag>
        );
      },
    },
    {
      title: "Master Host",
      dataIndex: "masterHost",
      key: "masterHost",
      render: (val) => <Text code>{val || "N/A"}</Text>,
    },
  ];

  // ============================================================
  // SECTION 2: So sánh dữ liệu
  // ============================================================

  const compareColumns = [
    {
      title: "Bảng",
      dataIndex: "table",
      key: "table",
      render: (val) => <Text code>{val}</Text>,
    },
    {
      title: "Master (bản ghi)",
      dataIndex: "masterCount",
      key: "masterCount",
      render: (val) => <Statistic value={val} valueStyle={{ fontSize: 16 }} />,
    },
    {
      title: "Slave (bản ghi)",
      dataIndex: "slaveCount",
      key: "slaveCount",
      render: (val) => <Statistic value={val} valueStyle={{ fontSize: 16 }} />,
    },
    {
      title: "Đồng bộ?",
      dataIndex: "inSync",
      key: "inSync",
      render: (val) => (
        <Tag
          icon={
            val ? <CheckCircleOutlined /> : <CloseCircleOutlined />
          }
          color={val ? "success" : "error"}
        >
          {val ? "ĐỒNG BỘ" : "CHƯA ĐỒNG BỘ"}
        </Tag>
      ),
    },
    {
      title: "Chênh lệch",
      dataIndex: "difference",
      key: "difference",
      render: (val) => (
        <Tag color={val === 0 ? "green" : "red"}>
          {val === 0 ? "Không" : `${val} bản ghi`}
        </Tag>
      ),
    },
  ];

  // ============================================================
  // RENDER
  // ============================================================

  return (
    <div>
      <Title level={2}>
        <div style={{ marginRight: 12 }} />
        DATA REPLICATION
      </Title>


      {/* ============================================================ */}
      {/* SECTION 1: Trạng thái Replication */}
      {/* ============================================================ */}
      <Card
        title={
          <Space>
            <div spin={loading.status} />
            <span>TRẠNG THÁI REPLICATION</span>
          </Space>
        }
        extra={
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchStatus}
            loading={loading.status}
          >
            Làm mới
          </Button>
        }
        style={{ marginBottom: 24 }}
      >


        <Table
          columns={statusColumns}
          dataSource={statusData}
          rowKey="branchId"
          pagination={false}
          loading={loading.status}
          expandable={{
            expandedRowRender: (record) => (
              <Descriptions size="small" column={2} bordered>
                <Descriptions.Item label="IO State">
                  {record.slaveIOState || "N/A"}
                </Descriptions.Item>
                <Descriptions.Item label="Master Log File">
                  {record.masterLogFile || "N/A"}
                </Descriptions.Item>
                <Descriptions.Item label="Relay Log File">
                  {record.relayLogFile || "N/A"}
                </Descriptions.Item>
                <Descriptions.Item label="Read Master Log Pos">
                  {record.readMasterLogPos || "N/A"}
                </Descriptions.Item>
                <Descriptions.Item label="Exec Master Log Pos">
                  {record.execMasterLogPos || "N/A"}
                </Descriptions.Item>
                <Descriptions.Item label="Master Port">
                  {record.masterPort || "N/A"}
                </Descriptions.Item>
                {record.lastIOError && (
                  <Descriptions.Item label="Last IO Error" span={2}>
                    <Text type="danger">{record.lastIOError}</Text>
                  </Descriptions.Item>
                )}
                {record.lastSQLError && (
                  <Descriptions.Item label="Last SQL Error" span={2}>
                    <Text type="danger">{record.lastSQLError}</Text>
                  </Descriptions.Item>
                )}
                {record.error && (
                  <Descriptions.Item label="Lỗi" span={2}>
                    <Text type="danger">{record.error}</Text>
                  </Descriptions.Item>
                )}
              </Descriptions>
            ),
          }}
        />
      </Card>

      {/* ============================================================ */}
      {/* SECTION 2: So sánh dữ liệu Master vs Slave */}
      {/* ============================================================ */}
      <Card
        title={
          <Space>
            <DatabaseOutlined />
            <span>So sánh dữ liệu Master vs Slave</span>
          </Space>
        }
        style={{ marginBottom: 24 }}
      >

        <Space style={{ marginBottom: 16 }}>
          <Select
            value={selectedBranch}
            onChange={setSelectedBranch}
            style={{ width: 200 }}
          >
            <Option value="HN">Hà Nội</Option>
            <Option value="DN">Đà Nẵng</Option>
            <Option value="HCM">TP.HCM</Option>
          </Select>
          <Button
            type="primary"
            icon={<SyncOutlined />}
            onClick={() => fetchCompare(selectedBranch)}
            loading={loading.compare}
          >
            So sánh
          </Button>
        </Space>

        {compareData && (
          <>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Chi nhánh"
                    value={compareData.branchId}
                    prefix={<DatabaseOutlined />}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Trạng thái"
                    value={compareData.allInSync ? "ĐỒNG BỘ" : "CHƯA ĐỒNG BỘ"}
                    valueStyle={{
                      color: compareData.allInSync ? "#3f8600" : "#cf1322",
                    }}
                    prefix={
                      compareData.allInSync ? (
                        <CheckCircleOutlined />
                      ) : (
                        <CloseCircleOutlined />
                      )
                    }
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Thời điểm kiểm tra"
                    value={
                      compareData.timestamp
                        ? new Date(compareData.timestamp).toLocaleTimeString(
                          "vi-VN"
                        )
                        : "N/A"
                    }
                    prefix={<ClockCircleOutlined />}
                  />
                </Card>
              </Col>
            </Row>

            <Table
              columns={compareColumns}
              dataSource={compareData.tables}
              rowKey="table"
              pagination={false}
              loading={loading.compare}
            />
          </>
        )}
      </Card>

      {/* ============================================================ */}
      {/* SECTION 3: Demo Replication Lag */}
      {/* ============================================================ */}
      <Card
        title={
          <Space>
            <div />
            <span>Replication Lag</span>
          </Space>
        }
      >


        <Space style={{ marginBottom: 16 }}>
          <Select
            value={selectedBranch}
            onChange={setSelectedBranch}
            style={{ width: 200 }}
          >
            <Option value="HN">Hà Nội</Option>
            <Option value="DN">Đà Nẵng</Option>
            <Option value="HCM">TP.HCM</Option>
          </Select>
          <Button
            type="primary"
            icon={<ExperimentOutlined />}
            onClick={() => demoLag(selectedBranch)}
            loading={loading.lag}
            danger
          >
            RUN
          </Button>
        </Space>

        {loading.lag && (
          <div style={{ textAlign: "center", padding: 24 }}>
            <Spin size="large" />
            <div style={{ marginTop: 8 }}>
              Đang ghi vào Master và đo thời gian đồng bộ...
            </div>
          </div>
        )}

        {lagResult && !loading.lag && (
          <>
            {/* Kết quả tổng quan */}
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Chi nhánh"
                    value={lagResult.branchId}
                    prefix={<DatabaseOutlined />}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Replication Lag"
                    value={
                      lagResult.replicationLagMs !== undefined
                        ? lagResult.replicationLagMs + " ms"
                        : "N/A"
                    }
                    valueStyle={{
                      color:
                        lagResult.replicationLagMs === 0
                          ? "#3f8600"
                          : "#faad14",
                    }}
                    prefix={<ClockCircleOutlined />}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Statistic
                    title="Kết quả"
                    value={lagResult.error ? "LỖI" : "THÀNH CÔNG"}
                    valueStyle={{
                      color: lagResult.error ? "#cf1322" : "#3f8600",
                    }}
                    prefix={
                      lagResult.error ? (
                        <CloseCircleOutlined />
                      ) : (
                        <CheckCircleOutlined />
                      )
                    }
                  />
                </Card>
              </Col>
            </Row>

            {/* Timeline các bước */}
            {lagResult.steps && (
              <Timeline
                items={lagResult.steps.map((step) => ({
                  color:
                    step.replicated === false
                      ? "orange"
                      : step.replicated === true ||
                        step.action?.includes("ĐÃ ĐỒNG BỘ")
                        ? "green"
                        : "blue",
                  children: (
                    <div>
                      <Text strong>
                        Bước {step.step}: {step.action}
                      </Text>
                      <br />
                      {step.masterCount !== undefined && (
                        <Text type="secondary">
                          Master: {step.masterCount} bản ghi
                        </Text>
                      )}
                      {step.slaveCount !== undefined && (
                        <>
                          <br />
                          <Text type="secondary">
                            Slave: {step.slaveCount} bản ghi
                          </Text>
                        </>
                      )}
                      {step.writeTimeMs !== undefined && (
                        <>
                          <br />
                          <Text type="secondary">
                            Thời gian ghi: {step.writeTimeMs}ms
                          </Text>
                        </>
                      )}
                      {step.replicationLagMs !== undefined && (
                        <>
                          <br />
                          <Tag
                            color={step.replicationLagMs === 0 ? "green" : "orange"}
                          >
                            Replication lag: {step.replicationLagMs}ms
                          </Tag>
                        </>
                      )}
                      {step.replicated !== undefined && (
                        <>
                          <br />
                          <Tag color={step.replicated ? "success" : "warning"}>
                            {step.replicated ? "ĐÃ ĐỒNG BỘ ✅" : "CHƯA ĐỒNG BỘ ⏳"}
                          </Tag>
                        </>
                      )}
                    </div>
                  ),
                }))}
              />
            )}

            {lagResult.error && (
              <Alert
                message="Lỗi"
                description={lagResult.error}
                type="error"
                showIcon
              />
            )}
          </>
        )}
      </Card>
    </div>
  );
}
