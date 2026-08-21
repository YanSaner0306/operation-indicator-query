/**
 * 模块9-12：数据绑定列表与生命周期操作页面。
 * 功能：查询、校验、预览、启停和删除绑定，并进入分步配置向导。
 * 技术栈：React 18、Ant Design Table/Modal、React Router 与 RBAC 组件。
 */
import { useEffect, useState } from 'react';
import { Button, Card, Descriptions, message, Modal, Space, Switch, Table, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import { bindingApi } from '@/api/data';
import Permission from '@/auth/Permission';
import type { Binding, BindingPreview } from '@/types/binding';
import { getErrorMessage } from '@/api/client';

export default function BindingListPage() {
  const navigate = useNavigate(); const [items,setItems]=useState<Binding[]>([]); const [loading,setLoading]=useState(false); const [preview,setPreview]=useState<BindingPreview>();
  const load=async()=>{setLoading(true);try{setItems((await bindingApi.page({page:1,size:100})).items);}catch(e){message.error(getErrorMessage(e));}finally{setLoading(false);}};
  useEffect(()=>{void load();},[]);
  const validate=async(id:number)=>{try{const r=await bindingApi.validate(id);r.valid?message.success('绑定校验通过'):Modal.warning({title:'绑定校验未通过',content:r.messages.join('；')});await load();}catch(e){message.error(getErrorMessage(e));}};
  return <Card title="本体数据绑定" extra={<Permission code="BINDING_MANAGE"><Button type="primary" onClick={()=>navigate('/data/bindings/new')}>新建绑定</Button></Permission>}>
    <Table rowKey="id" loading={loading} dataSource={items} pagination={false} columns={[
      {title:'名称',dataIndex:'name'},{title:'本体',dataIndex:'ontologyName'},{title:'数据源',dataIndex:'dataSourceName'},{title:'表',dataIndex:'tableName'},
      {title:'状态',dataIndex:'status',render:(v)=><Tag color={v==='ENABLED'?'green':'default'}>{v==='ENABLED'?'启用':'停用'}</Tag>},
      {title:'最近测试',dataIndex:'lastTestStatus',render:(v)=><Tag color={v==='SUCCESS'?'green':v==='FAILED'?'red':'default'}>{v}</Tag>},
      {title:'操作',render:(_,r)=><Space wrap><Button size="small" onClick={()=>navigate(`/data/bindings/${r.id}/edit`)}>查看/编辑</Button><Permission code="BINDING_MANAGE"><Button size="small" onClick={()=>validate(r.id)}>校验</Button><Button size="small" onClick={async()=>{try{setPreview(await bindingApi.preview(r.id));await load();}catch(e){message.error(getErrorMessage(e));}}}>预览</Button><Switch size="small" checked={r.status==='ENABLED'} onChange={async checked=>{try{await bindingApi.setStatus(r,checked?'ENABLED':'DISABLED');await load();}catch(e){message.error(getErrorMessage(e));}}}/><Button size="small" danger onClick={()=>Modal.confirm({title:'确认删除该绑定？',content:'启用中的绑定不可删除。',onOk:async()=>{try{await bindingApi.remove(r);await load();}catch(e){message.error(getErrorMessage(e));}}})}>删除</Button></Permission></Space>}
    ]}/>
    <Modal open={!!preview} title="绑定预览（最多一条）" footer={null} onCancel={()=>setPreview(undefined)} width={760}><Descriptions column={1} bordered size="small" items={preview?[{key:'key',label:'业务唯一键',children:String(preview.externalKey??'-')},{key:'source',label:'源字段',children:<pre>{JSON.stringify(preview.sourceValues,null,2)}</pre>},{key:'property',label:'本体属性',children:<pre>{JSON.stringify(preview.properties,null,2)}</pre>},{key:'time',label:'耗时',children:`${preview.durationMs} ms`}]:[]}/></Modal>
  </Card>;
}
