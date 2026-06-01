import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Tabs, Row, Col, Statistic, Button, InputNumber, Spin, Typography, message } from 'antd';
import {
  BarChartOutlined, TrophyOutlined, SwapOutlined, TeamOutlined, ReloadOutlined, ArrowUpOutlined, ArrowDownOutlined, StarOutlined
} from '@ant-design/icons';
import { statsApi } from '../api/bankApi';

const { Title, Text } = Typography;
const fmt = (v) => v ? Number(v).toLocaleString('vi-VN') : '0';

export default function Statistics() {
  const [activeTab, setActiveTab] = useState('balance');
  const [loading, setLoading] = useState(false);
  const [totalBalance, setTotalBalance] = useState(null);
  const [topCustomers, setTopCustomers] = useState([]);
  const [interTxns, setInterTxns] = useState([]);
  const [intraTxns, setIntraTxns] = useState([]);
  const [depositTxns, setDepositTxns] = useState([]);
  const [withdrawTxns, setWithdrawTxns] = useState([]);
  const [multiBranch, setMultiBranch] = useState([]);
  const [txnSummary, setTxnSummary] = useState([]);
  const [topDepositingCustomers, setTopDepositingCustomers] = useState([]);
  const [topLimit, setTopLimit] = useState(10);

  const loadTab = async (tab) => {
    setLoading(true);
    try {
      switch (tab) {
        case 'balance': {
          const res = await statsApi.getTotalBalance();
          setTotalBalance(res.data.data);
          break;
        }
        case 'top': {
          const res = await statsApi.getTopCustomers(topLimit);
          setTopCustomers(res.data.data || []);
          break;
        }
        case 'inter': {
          const res = await statsApi.getInterBranchTransactions();
          setInterTxns(res.data.data || []);
          break;
        }
        case 'intra': {
          const res = await statsApi.getIntraBranchTransactions();
          setIntraTxns(res.data.data || []);
          break;
        }
        case 'deposit_history': {
          const res = await statsApi.getDepositHistory();
          setDepositTxns(res.data.data || []);
          break;
        }
        case 'withdraw_history': {
          const res = await statsApi.getWithdrawHistory();
          setWithdrawTxns(res.data.data || []);
          break;
        }
        case 'multi': {
          const res = await statsApi.getMultiBranchCustomers();
          setMultiBranch(res.data.data || []);
          break;
        }
        case 'summary': {
          const res = await statsApi.getTransactionSummary();
          setTxnSummary(res.data.data || []);
          break;
        }
        case 'top_depositing': {
          const res = await statsApi.getTopDepositingCustomers(topLimit);
          setTopDepositingCustomers(res.data.data || []);
          break;
        }
      }
    } catch (err) {
      message.error('Lỗi: ' + (err.response?.data?.message || err.message));
    }
    setLoading(false);
  };

  useEffect(() => { loadTab(activeTab); }, [activeTab]);

  const tabItems = [
    {
      key: 'balance',
      label: <><BarChartOutlined /> Tổng số dư</>,
      children: (
        <Spin spinning={loading}>
          {totalBalance && (
            <>
              <Row gutter={16} style={{ marginBottom: 24 }}>
                <Col span={24}>
                  <Card style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: '#fff' }}>
                    <Statistic title={<span style={{ color: 'rgba(255,255,255,0.8)' }}>💰 Tổng số dư toàn hệ thống</span>}
                      value={fmt(totalBalance.systemTotalBalance)}
                      suffix="₫"
                      valueStyle={{ color: '#fff', fontSize: 32, fontWeight: 700 }} />
                  </Card>
                </Col>
              </Row>
              <Row gutter={16}>
                {[
                  { name: 'Hà Nội', balance: totalBalance.hanoiTotalBalance, count: totalBalance.hanoiAccountCount, color: '#1677ff' },
                  { name: 'Đà Nẵng', balance: totalBalance.danangTotalBalance, count: totalBalance.danangAccountCount, color: '#52c41a' },
                  { name: 'TP.HCM', balance: totalBalance.hcmTotalBalance, count: totalBalance.hcmAccountCount, color: '#fa8c16' },
                ].map((b, i) => (
                  <Col span={8} key={i}>
                    <Card>
                      <Statistic title={b.name} value={fmt(b.balance)} suffix="₫"
                        valueStyle={{ color: b.color, fontWeight: 600 }} />
                      <Text type="secondary">{b.count} tài khoản</Text>
                    </Card>
                  </Col>
                ))}
              </Row>
            </>
          )}
        </Spin>
      ),
    },
    {
      key: 'top',
      label: <><TrophyOutlined /> Top KH giàu nhất</>,
      children: (
        <>
          <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
            <Text>Số lượng:</Text>
            <InputNumber min={1} max={50} value={topLimit} onChange={setTopLimit} />
            <Button icon={<ReloadOutlined />} onClick={() => loadTab('top')}>Tải</Button>
          </div>
          <Table loading={loading} dataSource={topCustomers} rowKey="customerId" pagination={false}
            columns={[
              {
                title: '#', key: 'rank', render: (_, __, i) => {
                  return i + 1
                }
              },
              { title: 'ID', dataIndex: 'customerId' },
              { title: 'Họ tên', dataIndex: 'fullName', render: (v) => <strong>{v}</strong> },
              { title: 'Chi nhánh', dataIndex: 'branchId', render: (v) => <Tag color="blue">{v}</Tag> },
              {
                title: 'Tổng số dư', dataIndex: 'totalBalance',
                render: (v) => <span style={{ fontWeight: 700, color: '#52c41a' }}>{fmt(v)} ₫</span>
              },
              { title: 'Số TK', dataIndex: 'accountCount' },
            ]}
          />
        </>
      ),
    },
    {
      key: 'inter',
      label: <><SwapOutlined /> GD liên chi nhánh</>,
      children: (
        <Table loading={loading} dataSource={interTxns} rowKey="transactionId" pagination={{ pageSize: 10 }}
          columns={[
            { title: 'ID', dataIndex: 'transactionId', width: 50 },
            {
              title: 'Loại', dataIndex: 'transactionType',
              render: (v) => <Tag color={v.includes('OUT') ? 'magenta' : 'purple'}>{v}</Tag>
            },
            { title: 'Số tiền', dataIndex: 'amount', render: (v) => `${fmt(v)} ₫` },
            { title: 'TK', dataIndex: 'accountId' },
            { title: 'TK đối ứng', dataIndex: 'relatedAccountId', render: (v) => v || '-' },
            {
              title: 'CN đối ứng', dataIndex: 'relatedBranchId',
              render: (v) => v ? <Tag color="blue">{v}</Tag> : '-'
            },
            {
              title: 'TXN ID', dataIndex: 'distributedTxnId', ellipsis: true,
              render: (v) => v ? <Tag color="orange">{v}</Tag> : '-'
            },
            {
              title: 'Trạng thái', dataIndex: 'status',
              render: (v) => <Tag color={v === 'SUCCESS' ? 'green' : 'red'}>{v}</Tag>
            },
            {
              title: 'Thời gian', dataIndex: 'createdAt',
              render: (v) => v ? new Date(v).toLocaleString('vi-VN') : '-'
            },
          ]}
        />
      ),
    },
    {
      key: 'intra',
      label: <><SwapOutlined /> GD cùng chi nhánh</>,
      children: (
        <Table loading={loading} dataSource={intraTxns} rowKey="transactionId" pagination={{ pageSize: 10 }}
          columns={[
            { title: 'ID', dataIndex: 'transactionId', width: 50 },
            {
              title: 'Loại', dataIndex: 'transactionType',
              render: (v) => <Tag color={v.includes('OUT') ? 'magenta' : 'purple'}>{v}</Tag>
            },
            { title: 'Số tiền', dataIndex: 'amount', render: (v) => `${fmt(v)} ₫` },
            { title: 'TK', dataIndex: 'accountId' },
            { title: 'TK đối ứng', dataIndex: 'relatedAccountId', render: (v) => v || '-' },
            {
              title: 'Trạng thái', dataIndex: 'status',
              render: (v) => <Tag color={v === 'SUCCESS' ? 'green' : 'red'}>{v}</Tag>
            },
            {
              title: 'Thời gian', dataIndex: 'createdAt',
              render: (v) => v ? new Date(v).toLocaleString('vi-VN') : '-'
            },
          ]}
        />
      ),
    },
    {
      key: 'multi',
      label: <><TeamOutlined /> KH nhiều chi nhánh</>,
      children: (
        <Table loading={loading} dataSource={multiBranch} rowKey="phone" pagination={false}
          columns={[
            { title: 'Số điện thoại', dataIndex: 'phone', render: (v) => <strong>{v}</strong> },
            {
              title: 'Số chi nhánh', dataIndex: 'branchCount',
              render: (v) => <Tag color="purple">{v} chi nhánh</Tag>
            },
            {
              title: 'Chi tiết', dataIndex: 'branches',
              render: (branches) => branches?.map((b, i) => (
                <Tag key={i} color="blue">{b.branchId}: {b.fullName}</Tag>
              ))
            },
          ]}
        />
      ),
    },
    {
      key: 'summary',
      label: <><BarChartOutlined /> Thống kê GD</>,
      children: (
        <Table loading={loading} dataSource={txnSummary} rowKey="branchId" pagination={false}
          columns={[
            { title: 'Chi nhánh', dataIndex: 'branchName', render: (v) => <Tag color="blue">{v}</Tag> },
            { title: 'Tổng GD', dataIndex: 'totalTransactions' },
            { title: 'Gửi', dataIndex: 'depositCount', render: (v) => <Tag color="green">{v}</Tag> },
            { title: 'Rút', dataIndex: 'withdrawCount', render: (v) => <Tag color="red">{v}</Tag> },
            { title: 'Chuyển khoản', dataIndex: 'transferCount', render: (v) => <Tag color="purple">{v}</Tag> },
            { title: 'Tổng gửi', dataIndex: 'totalDepositAmount', render: (v) => `${fmt(v)} ₫` },
            { title: 'Tổng rút', dataIndex: 'totalWithdrawAmount', render: (v) => `${fmt(v)} ₫` },
          ]}
        />
      ),
    },
    {
      key: 'deposit_history',
      label: <><ArrowUpOutlined /> Lịch sử gửi tiền</>,
      children: (
        <Table loading={loading} dataSource={depositTxns} rowKey="transactionId" pagination={{ pageSize: 10 }}
          columns={[
            { title: 'ID', dataIndex: 'transactionId', width: 50 },
            {
              title: 'Loại', dataIndex: 'transactionType',
              render: (v) => <Tag color="green">{v}</Tag>
            },
            { title: 'Số tiền', dataIndex: 'amount', render: (v) => <span style={{ fontWeight: 600, color: '#52c41a' }}>+{fmt(v)} ₫</span> },
            { title: 'Tài khoản', dataIndex: 'accountId' },
            {
              title: 'Trạng thái', dataIndex: 'status',
              render: (v) => <Tag color={v === 'SUCCESS' ? 'green' : 'red'}>{v}</Tag>
            },
            {
              title: 'Thời gian', dataIndex: 'createdAt',
              render: (v) => v ? new Date(v).toLocaleString('vi-VN') : '-'
            },
          ]}
        />
      ),
    },
    {
      key: 'withdraw_history',
      label: <><ArrowDownOutlined /> Lịch sử rút tiền</>,
      children: (
        <Table loading={loading} dataSource={withdrawTxns} rowKey="transactionId" pagination={{ pageSize: 10 }}
          columns={[
            { title: 'ID', dataIndex: 'transactionId', width: 50 },
            {
              title: 'Loại', dataIndex: 'transactionType',
              render: (v) => <Tag color="red">{v}</Tag>
            },
            { title: 'Số tiền', dataIndex: 'amount', render: (v) => <span style={{ fontWeight: 600, color: '#ff4d4f' }}>-{fmt(v)} ₫</span> },
            { title: 'Tài khoản', dataIndex: 'accountId' },
            {
              title: 'Trạng thái', dataIndex: 'status',
              render: (v) => <Tag color={v === 'SUCCESS' ? 'green' : 'red'}>{v}</Tag>
            },
            {
              title: 'Thời gian', dataIndex: 'createdAt',
              render: (v) => v ? new Date(v).toLocaleString('vi-VN') : '-'
            },
          ]}
        />
      ),
    },
    {
      key: 'top_depositing',
      label: <><StarOutlined /> Top KH gửi tiền</>,
      children: (
        <>
          <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
            <Text>Số lượng:</Text>
            <InputNumber min={1} max={50} value={topLimit} onChange={setTopLimit} />
            <Button icon={<ReloadOutlined />} onClick={() => loadTab('top_depositing')}>Tải</Button>
          </div>
          <Table loading={loading} dataSource={topDepositingCustomers} rowKey="customerId" pagination={false}
            columns={[
              {
                title: '#', key: 'rank', render: (_, __, i) => {
                  return i + 1
                }
              },
              { title: 'ID', dataIndex: 'customerId' },
              { title: 'Họ tên', dataIndex: 'fullName', render: (v) => <strong>{v}</strong> },
              { title: 'Chi nhánh', dataIndex: 'branchId', render: (v) => <Tag color="blue">{v}</Tag> },
              {
                title: 'Tổng tiền gửi', dataIndex: 'totalAmount',
                render: (v) => <span style={{ fontWeight: 700, color: '#52c41a' }}>{fmt(v)} ₫</span>
              },
              { title: 'Số GD gửi', dataIndex: 'transactionCount' },
            ]}
          />
        </>
      ),
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>Thống kê</h2>
      </div>
      <Card>
        <Tabs items={tabItems} activeKey={activeTab} onChange={setActiveTab} />
      </Card>
    </div>
  );
}
