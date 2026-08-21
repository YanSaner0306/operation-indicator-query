/**
 * 模块15：安全审计日志查询页面。
 * 功能：按请求号、调用主体、动作和结果筛选只读审计记录，展示耗时与错误码。
 * 技术栈：React 18、Ant Design 查询表单/表格与审计 REST API。
 */
import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, message, Select, Space, Table, Tag } from 'antd';
import { auditLogApi } from '@/api/api-client';
import type { AuditLog } from '@/types/api-client';
import { getErrorMessage } from '@/api/client';

export default function AuditPage(){const [form]=Form.useForm();const [items,setItems]=useState<AuditLog[]>([]);const [loading,setLoading]=useState(false);const load=async(values={})=>{setLoading(true);try{setItems((await auditLogApi.page({...values,page:1,size:100})).items);}catch(e){message.error(getErrorMessage(e));}finally{setLoading(false);}};useEffect(()=>{void load();},[]);return <Card title="审计日志"><Form form={form} layout="inline" onFinish={load} style={{marginBottom:16}}><Form.Item name="requestId" label="请求 ID"><Input allowClear/></Form.Item><Form.Item name="principalId" label="调用主体"><Input allowClear/></Form.Item><Form.Item name="action" label="动作"><Input allowClear/></Form.Item><Form.Item name="result" label="结果"><Select allowClear style={{width:120}} options={[{value:'SUCCESS',label:'成功'},{value:'FAILED',label:'失败'},{value:'DENIED',label:'拒绝'}]}/></Form.Item><Space><Button type="primary" htmlType="submit">查询</Button><Button onClick={()=>{form.resetFields();void load();}}>重置</Button></Space></Form><Table rowKey="id" loading={loading} dataSource={items} pagination={false} scroll={{x:1200}} columns={[{title:'时间',dataIndex:'createdAt',width:180},{title:'Request ID',dataIndex:'requestId',width:220},{title:'主体',render:(_,r)=>`${r.principalType||'-'} / ${r.principalId||'-'}`},{title:'动作',dataIndex:'action'},{title:'方法',dataIndex:'httpMethod',width:80},{title:'路径',dataIndex:'path'},{title:'结果',dataIndex:'result',render:v=><Tag color={v==='SUCCESS'?'green':'red'}>{v}</Tag>},{title:'耗时(ms)',dataIndex:'durationMs',width:100},{title:'错误码',dataIndex:'errorCode',render:v=>v||'-'}]}/></Card>;}
