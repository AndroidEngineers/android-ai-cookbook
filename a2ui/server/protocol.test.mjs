import test from 'node:test';import assert from 'node:assert/strict';
import {validateSurface} from './protocol.mjs';
const valid=()=>({text:'An event',components:[{id:'root',component:'Column',children:['title','button']},{id:'title',component:'Text',text:'Event'},{id:'button',component:'Button',child:'label',action:{event:{name:'venue',context:{eventId:'event-1'}}}},{id:'label',component:'Text',text:'Venue'}]});
const check=r=>validateSurface(r,'surface-1',new Set(['event-1']));
test('creates three actual A2UI messages',()=>{const r=check(valid());assert.equal(r.messages[2].updateComponents.components.length,4);assert.equal(r.messages[0].version,'v0.9');});
test('rejects fabricated event action',()=>{const r=valid();r.components[2].action.event.context.eventId='fake';assert.throws(()=>check(r));});
test('rejects function side effects',()=>{const r=valid();r.components[2].action={functionCall:{call:'openUrl',args:{url:'https://evil.test'}}};assert.throws(()=>check(r));});
test('rejects cycles',()=>{const r=valid();r.components[0].children=['root'];assert.throws(()=>check(r));});
test('rejects missing references',()=>{const r=valid();r.components[0].children=['missing'];assert.throws(()=>check(r));});
test('rejects invented components',()=>{const r=valid();r.components[1].component='WebView';assert.throws(()=>check(r));});
test('initializes checkbox paths',()=>{const r={text:'Prepare',components:[{id:'root',component:'CheckBox',label:'Check details',value:{path:'/ready1'}}]};assert.equal(check(r).messages[1].updateDataModel.value.ready1,false);});
test('rejects arbitrary data bindings',()=>{assert.throws(()=>check({text:'x',components:[{id:'root',component:'CheckBox',label:'x',value:{path:'/admin'}}]}));});

test('preserves checked state by exact label, not reassigned binding path',()=>{const reply={text:'Updated',components:[{id:'root',component:'CheckBox',label:'Pack laptop',value:{path:'/ready4'}}]};const result=validateSurface(reply,'next',new Set(),[{label:'Pack laptop',checked:true}]);assert.equal(result.messages[1].updateDataModel.value.ready4,true);assert.equal(result.messages[0].createSurface.sendDataModel,true);});
test('new checklist item starts unchecked',()=>{const reply={text:'Updated',components:[{id:'root',component:'CheckBox',label:'New task',value:{path:'/ready0'}}]};const result=validateSurface(reply,'next',new Set(),[{label:'Old task',checked:true}]);assert.equal(result.messages[1].updateDataModel.value.ready0,false);});
