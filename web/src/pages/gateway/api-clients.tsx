/**
 * 模块14：API 客户端与机器凭证管理页面。
 * 功能：维护客户端直接权限，轮换或撤销 API Key，并强制完整密钥只展示一次。
 * 技术栈：React 18、Ant Design Form/Table/Modal、Clipboard API 与 REST API。
 */
import { useEffect, useState } from 'react';
import { Alert, Button, Card, Form, Input, message, Modal, Select, Space, Switch, Table, Tag, Typography } from 'antd';
import { machineClientApi } from '@/api/api-client';
import type { Permission } from '@/types/auth';
import type { ApiClient, ApiClientPayload, CreatedApiKey } from '@/types/api-client';
import { getErrorMessage } from '@/api/client';

export default function ApiClientPage(){
  const [items,setItems]=useState<ApiClient[]>([]);const [permissions,setPermissions]=useState<Permission[]>([]);const [editing,setEditing]=useState<ApiClient>();const [open,setOpen]=useState(false);const [createdKey,setCreatedKey]=useState<CreatedApiKey>();const [form]=Form.useForm<ApiClientPayload>();
  const load=async()=>{try{const [p,ps]=await Promise.all([machineClientApi.page({page:1,size:100}),machineClientApi.permissions()]);setItems(p.items);setPermissions(ps);}catch(e){message.error(getErrorMessage(e));}};useEffect(()=>{void load();},[]);
  const edit=(item?:ApiClient)=>{setEditing(item);form.setFieldsValue(item?{clientId:item.clientId,name:item.name,permissionCodes:item.permissionCodes,version:item.version}:{permissionCodes:[]});setOpen(true);};
  const save=async()=>{try{const v=await form.validateFields();editing?await machineClientApi.update(editing.id,{...v,version:editing.version}):await machineClientApi.create(v);setOpen(false);await load();message.success('保存成功');}catch(e){message.error(getErrorMessage(e));}};
  const issue=async(item:ApiClient)=>{try{setCreatedKey(await machineClientApi.createCredential(item.id,undefined,true));await load();}catch(e){message.error(getErrorMessage(e));}};
  return <Card title="API 客户端" extra={<Button type="primary" onClick={()=>edit()}>新建客户端</Button>}><Table rowKey="id" dataSource={items} pagination={false} expandable={{expandedRowRender:r=><Table rowKey="keyId" size="small" pagination={false} dataSource={r.credentials} columns={[{title:'Key ID',dataIndex:'keyId'},{title:'前缀',dataIndex:'keyPrefix'},{title:'状态',dataIndex:'status',render:v=><Tag>{v}</Tag>},{title:'到期时间',dataIndex:'expiresAt',render:v=>v||'-'},{title:'操作',render:(_,k)=>k.status==='ACTIVE'?<Button danger size="small" onClick={async()=>{await machineClientApi.revoke(r.id,k.keyId);await load();}}>撤销</Button>:null}]}/>}} columns={[
    {title:'Client ID',dataIndex:'clientId'},{title:'名称',dataIndex:'name'},{title:'权限',dataIndex:'permissionCodes',render:(v:string[])=><Space wrap>{v.map(x=><Tag key={x}>{x}</Tag>)}</Space>},{title:'启用',render:(_,r)=><Switch checked={r.status==='ENABLED'} onChange={async c=>{try{await machineClientApi.setStatus(r,c?'ENABLED':'DISABLED');await load();}catch(e){message.error(getErrorMessage(e));}}}/>} ,{title:'操作',render:(_,r)=><Space><Button size="small" onClick={()=>edit(r)}>编辑</Button><Button size="small" type="primary" onClick={()=>issue(r)}>生成/轮换 Key</Button></Space>}
  ]}/><Modal open={open} title={editing?'编辑 API 客户端':'新建 API 客户端'} onCancel={()=>setOpen(false)} onOk={save}><Form form={form} layout="vertical"><Form.Item name="clientId" label="Client ID" rules={[{required:true},{pattern:/^[a-z][a-z0-9_.-]{2,99}$/}]}><Input disabled={!!editing}/></Form.Item><Form.Item name="name" label="名称" rules={[{required:true}]}><Input/></Form.Item><Form.Item name="permissionCodes" label="直接权限"><Select mode="multiple" options={permissions.map(x=>({value:x.code,label:`${x.name} (${x.code})`}))}/></Form.Item></Form></Modal>
  <Modal open={!!createdKey} title="请立即保存 API Key" closable={false} maskClosable={false} onOk={()=>setCreatedKey(undefined)} cancelButtonProps={{style:{display:'none'}}}>{createdKey&&<><Alert type="warning" showIcon message="完整密钥仅本次显示，关闭后无法再次查询。"/><Typography.Paragraph copyable style={{marginTop:16,wordBreak:'break-all'}}>{createdKey.apiKey}</Typography.Paragraph><Typography.Text type="secondary">Key ID：{createdKey.keyId}，到期：{createdKey.expiresAt}</Typography.Text></>}</Modal></Card>;
}
