export const CATALOG = 'https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json';
const allowed = new Set(['Text', 'Column', 'Row', 'Card', 'Button', 'CheckBox', 'Divider', 'Image']);
const actions = new Set(['venue', 'prepare', 'website', 'directions', 'save', 'refine']);
export function validateSurface(reply, surfaceId, eventIds, previousChecklist = []) {
 if (!reply || typeof reply.text !== 'string' || reply.text.length > 2000) throw Error('Invalid assistant text');
 if (!Array.isArray(reply.components) || !reply.components.length || reply.components.length > 70) throw Error('Invalid component count');
 const ids = new Set();
 for (const c of reply.components) {
  if (!c || typeof c.id !== 'string' || !/^[a-zA-Z0-9_-]{1,60}$/.test(c.id) || ids.has(c.id) || !allowed.has(c.component)) throw Error('Invalid component');
  ids.add(c.id);
  const props = {Text:['text','variant'],Column:['children','align'],Row:['children','align'],Card:['child'],Button:['child','variant','action'],CheckBox:['label','value'],Divider:[],Image:['url','description','variant']}[c.component];
  if(Object.keys(c).some(k=>!['id','component',...props].includes(k))) throw Error('Unexpected property');
  if(c.component==='Image' && (c.url!=='asset://community' || typeof c.description!=='string'))throw Error('Invalid image');
  if(c.component==='Text' && (typeof c.text!=='string' || c.text.length>2500)) throw Error('Invalid text');
  if(['Column','Row'].includes(c.component) && (!Array.isArray(c.children) || c.children.length>20 || c.children.some(x=>typeof x!=='string')))throw Error('Invalid children');
  if(['Card','Button'].includes(c.component) && typeof c.child!=='string')throw Error('Missing child');
  if(c.component==='Button') {
   const e=c.action?.event;
   if(!e || Object.keys(c.action).length!==1 || !actions.has(e.name) || !eventIds.has(e.context?.eventId) || Object.keys(e.context).some(k=>k!=='eventId')) throw Error('Unauthorized action');
  }
  if(c.component==='CheckBox' && (typeof c.label!=='string' || c.label.length>160 || !/^\/ready[0-9]{1,2}$/.test(c.value?.path) || Object.keys(c.value).length!==1))throw Error('Invalid checkbox binding');
 }
 if(!ids.has('root'))throw Error('Missing root');
 const byId=new Map(reply.components.map(c=>[c.id,c]));
 const seen=new Set();
 function walk(id,parents=new Set()) {
  if(!ids.has(id)||parents.has(id)||parents.size>10)throw Error('Invalid component graph');
  seen.add(id);const c=byId.get(id);const next=new Set([...parents,id]);
  for(const child of c.children??(c.child?[c.child]:[]))walk(child,next);
 }
 walk('root');if(seen.size!==ids.size)throw Error('Unreachable component');
 const prior=new Map(previousChecklist.map(c=>[c.label,c.checked]));
 const value={};for(const c of reply.components)if(c.component==='CheckBox')value[c.value.path.slice(1)]=prior.get(c.label)===true;
 const message=(type,body)=>({version:'v0.9',[type]:{surfaceId,...body}});
 return {text:reply.text,messages:[message('createSurface',{catalogId:CATALOG,sendDataModel:true}),message('updateDataModel',{path:'/',value}),message('updateComponents',{components:reply.components})]};
}
export function normalizeDiscovery(data) {
 const cities=new Map(data.cities.map(c=>[c.id,c.name]));
 const sponsored=new Set((data.sponsors??[]).map(s=>s.sponsor_campaigns?.event_id));
 return data.events.slice(0,100).map(e=>({id:e.id,title:e.title,description:e.description,city:cities.get(e.city_id),date:e.starts_at,timezone:e.timezone,venue:e.venue??'Not listed',address:e.address??'Not listed',format:e.format,topics:e.event_topics?.map(t=>t.topics?.name),entry:e.is_free===true?'Free':e.is_free===false?'Paid; check official pricing':'Not listed',sponsored:sponsored.has(e.id)}));
}
