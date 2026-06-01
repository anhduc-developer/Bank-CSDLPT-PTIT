import { Layout, Menu } from "antd";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import {
  DashboardOutlined,
  BankOutlined,
  UserOutlined,
  CreditCardOutlined,
  DollarOutlined,
  SwapOutlined,
  BarChartOutlined,
  StarOutlined,
  ThunderboltOutlined,
  CloudSyncOutlined,
} from "@ant-design/icons";

const { Sider, Content } = Layout;

const menuItems = [
  { key: "/dashboard", icon: <DashboardOutlined />, label: "Dashboard" },
  { key: "/branches", icon: <BankOutlined />, label: "Chi nhánh" },
  { key: "/customers", icon: <UserOutlined />, label: "Khách hàng" },
  { key: "/accounts", icon: <CreditCardOutlined />, label: "Tài khoản" },
  { key: "/transactions", icon: <DollarOutlined />, label: "Gửi / Rút tiền" },
  { key: "/transfers", icon: <SwapOutlined />, label: "Chuyển tiền" },
  { key: "/statistics", icon: <BarChartOutlined />, label: "Thống kê" },
  { key: "/deadlock-demo", icon: <ThunderboltOutlined />, label: "Demo Deadlock" },
  { key: "/replication", icon: <CloudSyncOutlined />, label: "Nhân bản dữ liệu" },
];
export default function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider
        width={240}
        style={{ position: "fixed", left: 0, top: 0, bottom: 0, zIndex: 100 }}
      >
        <div className="sidebar-logo">
          <StarOutlined className="logo-icon" />
          <span>MB BANK</span>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0, marginTop: 8 }}
        />
        <div
          style={{
            position: "absolute",
            bottom: 16,
            left: 0,
            right: 0,
            textAlign: "center",
            color: "rgba(255,255,255,0.3)",
            fontSize: 11,
          }}
        >
          Distributed Banking v1.0
        </div>
      </Sider>
      <Layout style={{ marginLeft: 240 }}>
        <Content className="content-wrapper">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
