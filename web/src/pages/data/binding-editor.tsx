/**
 * 模块10-12：数据绑定分步配置向导。
 * 功能：选择数据源、物理表和本体，配置字段映射及 AND 筛选条件，保存后执行后端校验。
 * 技术栈：React 18、Ant Design Steps/Form.List、元数据 REST API 与 React Router。
 */
import { useEffect, useState } from 'react';
import { Button, Card, Form, Input, message, Select, Space, Steps } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { bindingApi, dataApi } from '@/api/data';
import { ontologyApi } from '@/api/ontology';
import type { DataSourceConfig, TableMetadata, ColumnMetadata } from '@/types/data-source';
import type { Ontology, OntologyProperty } from '@/types/ontology';
import type { BindingPayload } from '@/types/binding';
import { getErrorMessage } from '@/api/client';

export default function BindingEditorPage(){
  const {id}=useParams();const editingId=id?Number(id):undefined;const navigate=useNavigate();const [form]=Form.useForm<BindingPayload>();const [step,setStep]=useState(0);const [sources,setSources]=useState<DataSourceConfig[]>([]);const [tables,setTables]=useState<TableMetadata[]>([]);const [columns,setColumns]=useState<ColumnMetadata[]>([]);const [ontologies,setOntologies]=useState<Ontology[]>([]);const [properties,setProperties]=useState<OntologyProperty[]>([]);const [version,setVersion]=useState<number>();
  const sourceId=Form.useWatch('dataSourceId',form);const ontologyId=Form.useWatch('ontologyId',form);
  useEffect(()=>{Promise.all([dataApi.page({page:1,size:100}),ontologyApi.page({page:0,size:100})]).then(([s,o])=>{setSources(s.items);setOntologies(o.items);});if(editingId)bindingApi.get(editingId).then(r=>{setVersion(r.version);form.setFieldsValue({...r,mappings:r.mappings.map(m=>({sourceColumn:m.sourceColumn,ontologyPropertyId:m.ontologyPropertyId})),filters:r.filters.map(f=>({sourceColumn:f.sourceColumn,operator:f.operator,value:f.value}))});}).catch(e=>message.error(getErrorMessage(e)));},[editingId,form]);
  useEffect(()=>{if(sourceId)dataApi.tables(sourceId).then(setTables).catch(e=>message.error(getErrorMessage(e)));},[sourceId]);
  useEffect(()=>{if(ontologyId)ontologyApi.properties(ontologyId).then(setProperties).catch(e=>message.error(getErrorMessage(e)));},[ontologyId]);
  const table=form.getFieldValue('tableName');const loadColumns=async()=>{if(sourceId&&table)setColumns(await dataApi.columns(sourceId,table));};
  const save=async()=>{try{const values=await form.validateFields();const saved=editingId?await bindingApi.update(editingId,{...values,version}):await bindingApi.create(values);const checked=await bindingApi.validate(saved.id);if(checked.valid){message.success('保存并校验通过');navigate('/data/bindings');}else ModalWarning(checked.messages);}catch(e){message.error(getErrorMessage(e));}};
  return <Card title={editingId?'编辑数据绑定':'新建数据绑定'}><Steps current={step} items={[{title:'基本信息'},{title:'字段映射'},{title:'筛选与确认'}]}/><Form form={form} layout="vertical" style={{marginTop:24}} initialValues={{mappings:[{}],filters:[]}}>
    <div style={{display:step===0?'block':'none'}}><Form.Item name="name" label="绑定名称" rules={[{required:true}]}><Input/></Form.Item><Form.Item name="dataSourceId" label="数据源" rules={[{required:true}]}><Select options={sources.map(x=>({value:x.id,label:x.name}))}/></Form.Item><Form.Item name="schemaName" label="Schema"><Input/></Form.Item><Form.Item name="tableName" label="物理表" rules={[{required:true}]}><Select onChange={()=>setTimeout(()=>void loadColumns(),0)} options={tables.map(x=>({value:x.name,label:x.name}))}/></Form.Item><Form.Item name="ontologyId" label="目标本体" rules={[{required:true}]}><Select options={ontologies.map(x=>({value:x.id,label:x.name}))}/></Form.Item></div>
    <div style={{display:step===1?'block':'none'}}><Form.List name="mappings">{(fields,{add,remove})=><>{fields.map(f=><Space key={f.key} align="baseline"><Form.Item {...f} name={[f.name,'sourceColumn']} rules={[{required:true}]}><Select placeholder="源字段" style={{width:220}} options={columns.map(x=>({value:x.name,label:`${x.name} (${x.typeName})`}))}/></Form.Item><Form.Item {...f} name={[f.name,'ontologyPropertyId']} rules={[{required:true}]}><Select placeholder="本体属性" style={{width:240}} options={properties.map(x=>({value:x.id,label:`${x.name} (${x.code})${x.uniqueFlag?' [唯一]':''}`}))}/></Form.Item><Button danger onClick={()=>remove(f.name)}>删除</Button></Space>)}<Button onClick={()=>add()}>添加映射</Button></>}</Form.List></div>
    <div style={{display:step===2?'block':'none'}}><p>筛选条件之间固定使用 AND，值将通过参数化 SQL 传递。</p><Form.List name="filters">{(fields,{add,remove})=><>{fields.map(f=><Space key={f.key} align="baseline"><Form.Item {...f} name={[f.name,'sourceColumn']} rules={[{required:true}]}><Select placeholder="源字段" style={{width:180}} options={columns.map(x=>({value:x.name,label:x.name}))}/></Form.Item><Form.Item {...f} name={[f.name,'operator']} rules={[{required:true}]}><Select style={{width:130}} options={['EQ','NE','GT','GE','LT','LE','IN','IS_NULL','NOT_NULL'].map(x=>({value:x,label:x}))}/></Form.Item><Form.Item {...f} name={[f.name,'value']}><Input placeholder="IN 用逗号分隔"/></Form.Item><Button danger onClick={()=>remove(f.name)}>删除</Button></Space>)}<Button onClick={()=>add({operator:'EQ'})}>添加筛选条件</Button></>}</Form.List></div>
  </Form><Space><Button onClick={()=>step===0?navigate('/data/bindings'):setStep(step-1)}>{step===0?'取消':'上一步'}</Button>{step<2?<Button type="primary" onClick={async()=>{try{await form.validateFields(step===0?['name','dataSourceId','tableName','ontologyId']:['mappings']);if(step===0)await loadColumns();setStep(step+1);}catch{}}}>下一步</Button>:<Button type="primary" onClick={save}>保存并校验</Button>}</Space></Card>;
}
function ModalWarning(messages:string[]){message.warning(messages.join('；'));}
