/**
 * 模块5：平台登录页面。
 * 功能：提交用户名密码、展示认证错误，并在成功后返回原目标页面。
 * 技术栈：React、Ant Design Form、React Router 与认证上下文。
 */
import { Alert, Button, Card, Form, Input, Typography } from 'antd';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getErrorMessage } from '@/api/client';
import { useAuth } from '@/auth/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async (values: { username: string; password: string }) => {
    setSubmitting(true);
    setError('');
    try {
      await login(values.username, values.password);
      const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
      navigate(from, { replace: true });
    } catch (reason) {
      setError(getErrorMessage(reason));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', background: '#f0f2f5' }}>
      <Card style={{ width: 380 }}>
        <Typography.Title level={3}>业务本体推理平台</Typography.Title>
        <Typography.Paragraph type="secondary">请使用平台账号登录</Typography.Paragraph>
        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}
        <Form layout="vertical" onFinish={submit}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input autoComplete="username" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting} block>登录</Button>
        </Form>
      </Card>
    </div>
  );
}
