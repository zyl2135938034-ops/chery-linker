export default function(t,_,e,i,o){var s,n,c,a,l,r=$app_require$;
s=t.setTimeout,n=t.setInterval,c=t.clearTimeout,a=t.clearInterval,l=t.$app_require$||r,function(){(()=>{var t={},
o={};
function r(_){var e=o[_];
if(void 0!==e)return e.exports;
var i=o[_]={exports:{}};
return t[_](i,i.exports,r),i.exports}r.g=(()=>{if("object"==typeof _)return _;
try{return this||Function("return this")()}catch(t){if("object"==typeof e)return e}})(),r.rv=()=>"1.7.12",r.ruid="bundler=rspack@1.7.12";
var p=[[[[0,"page"]],{width:"216px",height:"257px",flexDirection:"column",backgroundColor:"#000000"}],[[[0,"p"]],{width:"216px",height:"242px",flexDirection:"column"}],[[[0,"hd"]],{width:"216px",height:"28px",flexDirection:"row",alignItems:"center",justifyContent:"space-between",paddingLeft:"20px",paddingRight:"20px",marginTop:"5px"}],[[[0,"model"]],{height:"15px",fontSize:"12px",color:"#7E8A9C"}],[[[0,"clock"]],{height:"17px",fontSize:"15px",fontWeight:"bold",color:"#FFFFFF"}],[[[0,"r1"]],{width:"216px",height:"42px",flexDirection:"row",alignItems:"flex-end",justifyContent:"space-between",paddingLeft:"20px",paddingRight:"20px"}],[[[0,"r1l"]],{height:"36px",flexDirection:"row",alignItems:"flex-end"}],[[[0,"num"]],{height:"34px",fontSize:"32px",fontWeight:"bold",color:"#FFFFFF"}],[[[0,"km"]],{height:"16px",fontSize:"14px",color:"#8B97A8",marginLeft:"4px",marginBottom:"2px"}],[[[0,"std"]],{height:"11px",fontSize:"9px",fontWeight:"bold",color:"#5C6675",marginLeft:"3px",marginBottom:"4px"}],[[[0,"pill"]],{height:"20px",flexDirection:"row",alignItems:"center",paddingLeft:"7px",paddingRight:"10px",marginBottom:"6px",borderRadius:"10px",backgroundColor:"#171C22"}],[[[0,"dot"]],{width:"6px",height:"6px",borderRadius:"3px"}],[[[0,"pill-t"]],{height:"12px",fontSize:"10px",color:"#C6D0DC",marginLeft:"5px"}],[[[0,"r2"]],{width:"216px",height:"24px",flexDirection:"row",alignItems:"center",paddingLeft:"20px",marginTop:"6px"}],[[[0,"ic-bolt"]],{width:"11px",height:"18px",objectFit:"contain"}],[[[0,"pct"]],{height:"17px",fontSize:"16px",fontWeight:"bold",color:"#FFFFFF",marginLeft:"5px"}],[[[0,"chg"]],{height:"12px",fontSize:"10px",color:"#2FD07A",marginLeft:"6px"}],[[[0,"r3"]],{width:"216px",height:"10px",flexDirection:"row",alignItems:"center",paddingLeft:"20px",marginTop:"2px"}],[[[0,"bar"]],{width:"136px",height:"8px",flexDirection:"row",backgroundColor:"#1B2128",borderRadius:"4px"}],[[[0,"fill"]],{height:"8px",borderRadius:"4px"}],[[[0,"carr"]],{width:"216px",height:"70px",flexDirection:"row",alignItems:"center",justifyContent:"center"}],[[[0,"carimg"]],{width:"178px",height:"68px",objectFit:"contain"}],[[[0,"acts"]],{width:"216px",height:"54px",flexDirection:"row",alignItems:"flex-start",justifyContent:"space-between",paddingLeft:"11px",paddingRight:"11px"}],[[[0,"acol"]],{width:"46px",height:"54px",flexDirection:"column",alignItems:"center"}],[[[0,"ab"]],{width:"38px",height:"38px",flexDirection:"row",alignItems:"center",justifyContent:"center",borderRadius:"19px",backgroundColor:"#191E24"}],[[[0,"i20"]],{width:"18px",height:"18px",objectFit:"contain"}],[[[0,"al"]],{height:"12px",fontSize:"10px",color:"#9AA6B6",marginTop:"4px"}],[[[0,"msg"]],{width:"186px",height:"14px",fontSize:"10px",textAlign:"center",color:"#8B97A8"}],[[[0,"g1"]],{width:"216px",height:"88px",flexDirection:"row",alignItems:"flex-start",justifyContent:"space-between",paddingLeft:"20px",paddingRight:"20px",marginTop:"6px"}],[[[0,"g2"]],{width:"216px",height:"88px",flexDirection:"row",alignItems:"flex-start",justifyContent:"space-between",paddingLeft:"20px",paddingRight:"20px",marginTop:"6px"}],[[[0,"gcol"]],{width:"78px",height:"88px",flexDirection:"column",alignItems:"center"}],[[[0,"gb"]],{width:"62px",height:"62px",flexDirection:"row",alignItems:"center",justifyContent:"center",borderRadius:"31px",backgroundColor:"#191E24"}],[[[0,"i30"]],{width:"27px",height:"27px",objectFit:"contain"}],[[[0,"gl"]],{height:"15px",fontSize:"12px",color:"#C6D0DC",marginTop:"7px"}],[[[0,"stat"]],{width:"216px",height:"143px",flexDirection:"column",justifyContent:"center",paddingLeft:"18px",paddingRight:"18px"}],[[[0,"srow"]],{width:"180px",height:"22px",flexDirection:"row",alignItems:"center",justifyContent:"space-between"}],[[[0,"sk"]],{height:"12px",fontSize:"11px",color:"#7E8A9C"}],[[[0,"sv"]],{height:"12px",fontSize:"11px",fontWeight:"bold",color:"#FFFFFF"}],[[[0,"sep"]],{width:"180px",height:"1px",backgroundColor:"#1C2229"}],[[[0,"tools"]],{width:"216px",height:"60px",flexDirection:"row",alignItems:"flex-start",justifyContent:"space-between",paddingLeft:"34px",paddingRight:"34px"}],[[[0,"tcol"]],{width:"44px",height:"60px",flexDirection:"column",alignItems:"center"}],[[[0,"tb"]],{width:"44px",height:"44px",flexDirection:"row",alignItems:"center",justifyContent:"center",borderRadius:"22px",backgroundColor:"#191E24"}],[[[0,"i20s"]],{width:"18px",height:"18px",objectFit:"contain"}],[[[0,"tl"]],{height:"12px",fontSize:"10px",color:"#9AA6B6",marginTop:"4px"}],[[[0,"dots"]],{width:"216px",height:"15px",flexDirection:"row",alignItems:"center",justifyContent:"center"}],[[[0,"di"]],{width:"6px",height:"6px",marginLeft:"3px",marginRight:"3px",borderRadius:"3px"}]],u=function(t,_,e){"use strict";
Object.defineProperty(_,"__esModule",{value:!0}),_.default=void 0;
var i=r(e("@app-module/system.interconnect")),o=r(e("@app-module/system.storage")),l=r(e("@app-module/system.router"));
function r(t){return t&&t.__esModule?t:{default:t}}let p="#2FD07A",u="#FFB020",d="#FF6B6B",g="#8FA3B8",h="#3D7EF7",f="#FFFFFF",m="#33383F",x="#1E3557",v="#3A2412",k={204:"连接超时",1e3:"手机侧未知错误",1001:"手机没装车控桥",1006:"蓝牙已断开"},
b=null,L=0,y="",w=null,T=null,F={},nSh=null,sOn=!1,sPk=0,sLast=0,sBase=0,sCool=0,sHint=!1,dUntil=0,dSeq=0,dBuf=[],dFlush=0,gT=0,sX=0,sY=0,sZ=0,sV=0,kG=0,kGx=0,kGy=0,kGz=0,kAv=0,kPx=0,kPy=0,kPz=0,kQ=0,kEmit=0,kSupF=0,kSupR=0,bOn=0,bT0=0,bN=0,bGap=0,bSx=0,bSy=0,bSz=0,bSa=0,bPk=0,rOn=0,rT0=0,rT=0,rn=0,rx=0,ry=0,rz=0,qN=0,fP=0,pP=0,gEn="fist";
function C(t){return t<10?"0"+t:String(t)}function S(){let t=new Date;
return C(t.getHours())+":"+C(t.getMinutes())}function j(t,_){try{o.default.set({key:t,value:String(_)})}catch(t){}}function D(t,_){return k[t]||(_||"通路异常")+(null==t?"":" "+t)}function R(t){let _=F[t];
return _?(delete F[t],c(_.timer),_):null}function P(t){y=String(t).slice(0,26);
let _=Object.keys(F);
for(let t=0;
t<_.length;
t++){let e=R(_[t]);
e&&e.reject(Error(y))}}_.default={private:{page:1,d1:"#FFFFFF",d2:"#33383F",d3:"#33383F",clockText:"--:--",rangeNum:"—",socText:"—",chargeText:"",barWidth:"0%",barColor:"#2FD07A",linkText:"启动中",dotColor:"#5C6675",linkColor:"#8FA3B8",lockText:"未知",lockBg:"#191E24",lockColor:"#C6D0DC",updText:"—",freshText:"暂无数据",freshColor:"#8FA3B8",msg:"正在通过蓝牙找手机…",msgColor:"#8FA3B8",unlockBg:"#1E3557",refreshLabel:"刷新",busy:!1,probing:!1,pend:!1},
onInit(){this.clockText=S();
let t=this;
T=n(()=>{t.clockText=S()},
1e4),this.openChannel(),this.startShake(),new Promise(_=>{let e=!1,i=t=>{e||(e=!0,_(t))};
s(()=>i(null),1200),o.default.get({key:"gEn",success:t=>i(t),fail:()=>i(null)})}).then(_=>{null!=_&&(gEn=String(_))}),this.syncGcfg(),new Promise(t=>{let _=!1,e=e=>{_||(_=!0,t(e||""))};
s(()=>e(""),1500),o.default.get({key:"carSnap",success:t=>e(t),fail:()=>e("")})}).then(_=>{if(_)try{t.apply(JSON.parse(_),!0)}catch(t){}})},
onShow(){this.doRefresh(),this.startPoll(),this.startShake(),this.syncGcfg()},
onHide(){this.stopShake(),this.stopPoll(),this.pend=!1,this.unlockBg=x},
onDestroy(){this.stopShake(),this.stopPoll(),T&&(a(T),T=null)},
onSwipe(t){let _=t&&t.direction||"";
"left"===_&&this.page<3?this.toPage(this.page+1):"right"===_&&this.page>1&&this.toPage(this.page-1)},
toPage(t){this.page=t,this.d1=1===t?f:m,this.d2=2===t?f:m,this.d3=3===t?f:m},
toPage1(){this.toPage(1)},
toPage2(){this.toPage(2)},
toPage3(){this.toPage(3)},
startPoll(){this.stopPoll();
let t=this;
w=n(()=>{t.doRefresh()},
6e4)},
stopPoll(){w&&(a(w),w=null)},
startShake(){if(sOn)return;
let m=null;
try{m=$app_require$("@app-module/system.sensor")}catch(t){m=null}
if(!m)return;
let vm=this,onS=t=>{if(!t||!sOn)return;
let _=Number(t.x)||0,e=Number(t.y)||0,o=Number(t.z)||0,n=Math.sqrt(_*_+e*e+o*o);
if(!(n>0))return;
let i=Date.now(),dt=sV?i-gT:999,dx=_-(sV?sX:_),dy=e-(sV?sY:e),dz=o-(sV?sZ:o),dj=Math.sqrt(dx*dx+dy*dy+dz*dz);
sX=_,sY=e,sZ=o,sV=1,gT=i;
kG||(kGx=_,kGy=e,kGz=o,kG=1),kGx=.82*kGx+.18*_,kGy=.82*kGy+.18*e,kGz=.82*kGz+.18*o;
let ax=_-kGx,ay=e-kGy,az=o-kGz,am=Math.sqrt(ax*ax+ay*ay+az*az),qp=qN;
qN=am<3?qN+1:0;
am>=12&&!bOn&&(bOn=1,bT0=i,bN=0,bGap=0,bSx=0,bSy=0,bSz=0,bSa=0,bPk=0);
if(bOn){am>=12?(bN++,bGap=0):bGap++;
bGap<=3&&(bSx+=ax,bSy+=ay,bSz+=az,bSa+=am,am>bPk&&(bPk=am));
if(bGap>3||i-bT0>500){let du=i-bT0,ch=bSa>0?Math.sqrt(bSx*bSx+bSy*bSy+bSz*bSz)/bSa:0;
bN>=4&&bPk>=20&&du<=580&&ch>=.22&&(kEmit=i,kSupF=i+700,kSupR=i+1200,fP=0,
vm.onGesture(bSx>0&&bSx>Math.abs(bSz)?"push":"pull")),bOn=0,bN=0,bGap=0}}
if(fP){if(bOn||am>45)fP=0;else if(i-fP>=220){let q=i-kEmit>1000;fP=0,q&&(kEmit=i,vm.onGesture("fist"))}}
if(pP){if(kEmit>pP||bOn)pP=0;else if(i-pP>=150){let q=am<5;pP=0,q&&(kEmit=i,vm.onGesture("pinch"))}}
i-kEmit>1000&&i>kSupF&&!bOn&&dj>=5&&dj<14&&n<21&&am>=3&&am<=8.5&&qp>=6&&!pP&&(pP=i);
if(!bOn&&am>=5&&am<=22){
if(!rOn||i-rT>260)rOn=1,rT0=i,rn=0,rx=0,ry=0,rz=0;
rT=i;
if(kAv){let q1=Math.sqrt(kPx*kPx+kPy*kPy+kPz*kPz)||1,q2=Math.sqrt(ax*ax+ay*ay+az*az)||1,
ux=kPx/q1,uy=kPy/q1,uz=kPz/q1,vx=ax/q2,vy=ay/q2,vz=az/q2;
rx+=uy*vz-uz*vy,ry+=uz*vx-ux*vz,rz+=ux*vy-uy*vx}
if(++rn>=12&&i-rT0<=1400){let mg=Math.sqrt(rx*rx+ry*ry+rz*rz),
A=[Math.abs(rx),Math.abs(ry),Math.abs(rz)].sort((a,b)=>b-a);
if(mg>2.6&&A[0]/(A[1]+1e-6)>=1.4&&i-kEmit>1000){
let sg=A[0]===Math.abs(rx)?rx:A[0]===Math.abs(ry)?ry:rz;
kEmit=i,rOn=0,rn=0,rx=0,ry=0,rz=0,vm.onGesture(sg>0?"ccw":"cw")}}
}else if(rOn&&i-rT>260)rOn=0,rn=0,rx=0,ry=0,rz=0;
i-kEmit>1000&&i>kSupF&&!bOn&&!rOn&&dj>=14&&n>=18&&n<45&&am>=8&&!fP&&(fP=i);
dj>4&&(kQ=i),kPx=ax,kPy=ay,kPz=az,kAv=1;
dUntil&&(i<dUntil?(dBuf.push(i,_,e,o,n,dj),(dBuf.length>=60||i-dFlush>400)&&vm.dbgFlush()):(vm.dbgFlush(),dUntil=0))},onF=()=>{sOn=!1};
try{if("function"==typeof m.subscribe)m.subscribe({type:"accelerometer",interval:"game",success:onS,callback:onS,fail:onF});
else{if("function"!=typeof m.subscribeAccelerometer)return;
m.subscribeAccelerometer({interval:"game",callback:onS,fail:onF})}nSh=m,sOn=!0,sPk=0,sLast=0,sBase=0,sV=0,sCool=0,dUntil=0,dBuf=[],kG=0,kAv=0,kEmit=0,kQ=0,kSupF=0,kSupR=0,bOn=0,bN=0,bGap=0,bPk=0,rOn=0,rn=0,qN=0,fP=0,pP=0}catch(t){sOn=!1,nSh=null}},
stopShake(){if(!sOn)return;
sOn=!1,sPk=0,kG=0,kAv=0,bOn=0,bN=0,bGap=0,bPk=0,rOn=0,rn=0,qN=0,fP=0,pP=0;
let t=nSh;
nSh=null;
if(!t)return;
try{if("function"==typeof t.unsubscribe)t.unsubscribe({type:"accelerometer"});
else"function"==typeof t.unsubscribeAccelerometer&&t.unsubscribeAccelerometer()}catch(t){}},
onGesture(t){if(!this.gOk(t)||this.busy)return;
let _={fist:"攥拳",pinch:"捏手指",cw:"顺时针转手",ccw:"逆时针转手",push:"向前推",pull:"向后拉"}[t]||t;
this.hint(_+" · 已识别",u),this.buzz(),this.gSend(t)},
gOk(t){return(","+gEn+",").indexOf(","+t+",")>=0},
syncGcfg(){this.request("/gcfg","GET",null,4e3).then(t=>{t&&0===Number(t.code)&&t.data&&(gEn=String(t.data.list||""),j("gEn",gEn))}).catch(()=>{})},
gSend(t){try{b&&b.send({data:{id:8e5+dSeq++,path:"/gesture",method:"POST",body:{g:t},token:"b33d986be4e94e59"}})}catch(_){}},
buzz(){let t=null;
try{t=$app_require$("@app-module/system.vibrator")}catch(_){t=null}
if(!t)try{t=$app_require$("@system.vibrator")}catch(_){t=null}
try{t&&"function"==typeof t.vibrate&&t.vibrate({mode:"long"})}catch(t){}},
hint(t,_){let e=this;
this.flashT&&(a(this.flashT),this.flashT=null),this.toPage(2),this.say(t,_),this.flashT=s(()=>{e.flashT=null,e.toPage(1)},5e3)},
dbgFlush(){if(!dBuf.length)return;let t=dBuf;
dBuf=[],dFlush=Date.now();try{b&&b.send({data:{id:9e5+dSeq++,path:"/dbg",method:"POST",body:{b:t},token:"b33d986be4e94e59"}})}catch(_){}},
toast(t){try{let m=$app_require$("@app-module/system.prompt");
m&&"function"==typeof m.showToast&&m.showToast({message:t})}catch(t){}},
openChannel(){if(b)return;
let t=this,_=null;
try{_=i.default.instance()}catch(t){this.link("不支持",d),this.say("这台表不支持蓝牙通路",d);
return}b=_,_.onopen=_=>{t.link("已连接",p),j("carLink",_&&_.isReconnected?"蓝牙已重连":"蓝牙已连接")},
_.onclose=_=>{t.link("蓝牙断开",d),P(D(_&&_.code,"蓝牙断开"))},
_.onerror=_=>{let e=D(_&&_.code,_&&_.data||"通路错误");
t.link(e.slice(0,4),d),j("carLastErr",e),P(e)},
_.onmessage=t=>{let _=function(t){if(null==t)return null;
if("object"==typeof t)return t;
try{return JSON.parse(String(t))}catch(t){return null}}(t&&void 0!==t.data?t.data:t);
if(_&&"gcfg"===_.cmd){gEn=String(_.list||""),j("gEn",gEn);
return}
if(_&&"dbg"===_.cmd){let n=Number(_.ms)||15000;_.on?(dUntil=Date.now()+n,t.say("采集中 · 请做动作",u)):(t.dbgFlush(),dUntil=0,t.say("采集结束",g));
return}
if(!_||void 0===_.id)return;
let e=R(_.id);
if(e){if(401===Number(_.http)){y="密码不符",e.resolve(null);
return}e.resolve(_)}}},
request(t,_,e,i){return new Promise((o,n)=>{if(this.openChannel(),!b)return void n(Error(y="蓝牙通路不可用"));
let c=++L;
F[c]={resolve:o,reject:n,timer:s(()=>{let t=R(c);
t&&(y="手机无应答",t.reject(Error(y)))},
i||9e3)};
try{b.send({data:{id:c,path:t,method:_,body:e||null,token:"b33d986be4e94e59"},
fail:(t,_)=>{let e=R(c);
if(!e)return;
let i=t&&t.code||_;
y=String(D(i,t&&t.data||"发送失败")).slice(0,26),e.reject(Error(y))}})}catch(_){let t=R(c);
if(!t)return;
y="发送异常",t.reject(Error(y))}})},
link(t,_){this.linkText=t,this.dotColor=_,this.linkColor=_},
say(t,_){this.msg=t,this.msgColor=_},
onFind(){this.command("find")},
onWindow(){this.command("window")},
onTrunk(){this.command("trunk")},
onAc(){this.command("ac")},
doProbe(){if(this.probing)return;
this.probing=!0,this.link("自检中",u),this.say("正在经蓝牙问手机…",g);
let t=this;
this.request("/ping","GET",null,7e3).then(_=>{if(t.probing=!1,!_){t.link("密码不对",u),t.say("手机已收到，但两端密码不一致",u);
return}t.link("已连接",p),t.say("蓝牙通路正常",p),t.doRefresh()}).catch(()=>{t.probing=!1,t.link("未连接",d),t.say("不通："+(y||"未知"),u)})},
apply(t,_){if(!t)return;
let e=t.rangeKm,i=t.soc;
this.rangeNum=null==e?"—":String(e),this.socText=null==i?"—":i+"%",this.barWidth=(null==i?0:i)+"%",this.barColor=null!=i&&i<20?u:p,this.chargeText=t.charging?"充电中":"",this.lockText=!0===t.locked?"已锁车":!1===t.locked?"未锁车":"状态未知",this.lockBg=!0===t.locked?"#14351F":!1===t.locked?v:"#191E24",this.lockColor=!0===t.locked?"#7FE0A8":!1===t.locked?"#FFC46B":"#C6D0DC",this.updText=(_?"上次 ":"")+(t.updatedAt||"—"),this.freshText=t.stale?"已过期":"新鲜",this.freshColor=t.stale?u:p,_||j("carSnap",JSON.stringify(t))},
doRefresh(){if(this.busy||this.probing)return;
this.busy=!0,this.refreshLabel="读取中";
let t=this;
this.request("/status","GET").then(_=>{(t.busy=!1,t.refreshLabel="刷新",_)?0===_.code?(t.apply(_.data,!1),t.link("已连接",p)):t.say("桥说："+(_.message||"未就绪"),u):t.link("密码不对",u)}).catch(()=>{t.busy=!1,t.refreshLabel="刷新",t.link("未连接",d),t.say("不通："+(y||"未知"),u)})},
onUnlock(){if(!this.pend){this.pend=!0,this.unlockBg=v,this.link("再点确认",u);
let t=this;
s(()=>{t.pend=!1,t.unlockBg=x,"再点确认"===t.linkText&&t.link("已连接",p)},
5e3);
return}this.pend=!1,this.unlockBg=x,this.command("unlock")},
onLock(){this.command("lock")},
command(t){if(this.busy)return;
let _={lock:["正在上锁…","已上锁"],unlock:["正在解锁…","已解锁"],find:["正在寻车…","已鸣笛闪灯"],window:["正在通风…","已通风"],trunk:["正在开后备箱…","后备箱已开"],ac:["正在开空调…","空调已开启"]},e=_[t]||["正在执行…","已完成"];
this.busy=!0,this.link("下发中",u),this.say(e[0],g);
let i=this,c={action:t,ts:Date.now(),nonce:String(Date.now())+Math.random().toString(36).slice(2,8)};
this.request("/command","POST",c,25e3).then(c=>{if(i.busy=!1,!c){i.link("密码不对",u),i.say("失败：两端密码不一致",d);
return}0===c.code?(i.apply(c.data,!1),i.link("已连接",p),i.say(e[1],p)):(i.link("已连接",p),i.say("失败："+(c.message||"车辆未响应"),d))}).catch(()=>{i.busy=!1,i.link("未连接",d),i.say("失败："+(y||"断连")+"，请到车旁确认",d)})},
goDiag(){j("carLastErr",y||""),l.default.push({uri:"/pages/settings"})}};
let $=_.default||t.exports,B=["public","protected","private"];
if($.data&&B.some(function(t){return $[t]}))throw Error('页面VM对象中的属性data不可与"'+B.join(",")+'"同时存在，请使用private替换data名称');
$.data||($.data={},
$._descriptor={},
B.forEach(function(t){let _=typeof $[t];
if("object"===_)for(let _ in $.data=Object.assign($.data,$[t]),$[t])$._descriptor[_]={access:t};
else"function"===_&&console.warn("页面VM对象中的属性"+t+"的值不能是函数，请使用对象")}))},
d=function(t){let _=t||this;
return aiot.__ce__("div",{__vm__:_,__opts__:{classList:["page"],events:{swipe:function(t){return _.onSwipe(t)}}}},
[aiot.__ci__({__vm__:_,__opts__:{shown:function(){return 1===_.page}}},
function(){return[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["p","p1"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["hd"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["model"],value:"风云 A9"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["clock"],value:function(){return _.clockText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["r1"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["r1l"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["num"],value:function(){return _.rangeNum}}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["km"],value:"km"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["std"],value:"CLTC"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["pill"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["dot"],style:function(){return r.g.$translateStyle$("background-color:"+_.dotColor)}}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["pill-t"],value:function(){return _.linkText}}},
[])])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["r2"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["ic-bolt"],src:"/common/ic-bolt.png",alt:"blank"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["pct"],value:function(){return _.socText}}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["chg"],value:function(){return _.chargeText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["r3"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["bar"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["fill"],style:function(){return r.g.$translateStyle$("width:"+_.barWidth+";background-color:"+_.barColor)}}},
[])])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["carr"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["carimg"],src:"/common/car-a9.png",alt:"blank"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["acts"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["acol"],events:{click:function(t){return _.onUnlock(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["ab"],style:function(){return r.g.$translateStyle$("background-color:"+_.unlockBg)}}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20"],src:"/common/ic-unlock.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["al"],value:"解锁"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["acol"],events:{click:function(t){return _.onFind(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["ab"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20"],src:"/common/ic-horn.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["al"],value:"寻车"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["acol"],events:{click:function(t){return _.onWindow(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["ab"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20"],src:"/common/ic-window.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["al"],value:"通风"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["acol"],events:{click:function(t){return _.onTrunk(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["ab"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20"],src:"/common/ic-trunk.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["al"],value:"后备箱"}},
[])])])])]}),aiot.__ci__({__vm__:_,__opts__:{shown:function(){return 2===_.page}}},
function(){return[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["p","p2"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["hd"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["model"],value:"风云 A9"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["clock"],value:function(){return _.clockText}}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{lines:"1",classList:["msg"],style:function(){return r.g.$translateStyle$("color:"+_.msgColor)},
value:function(){return _.msg}}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["g1"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gcol"],events:{click:function(t){return _.onLock(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gb"],style:function(){return r.g.$translateStyle$("background-color:"+_.lockBg)}}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i30"],src:"/common/ic-lock.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["gl"],style:function(){return r.g.$translateStyle$("color:"+_.lockColor)},
value:function(){return _.lockText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gcol"],events:{click:function(t){return _.onFind(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i30"],src:"/common/ic-horn.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["gl"],value:"寻车"}},
[])])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["g2"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gcol"],events:{click:function(t){return _.onWindow(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i30"],src:"/common/ic-window.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["gl"],value:"通风"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gcol"],events:{click:function(t){return _.onAc(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["gb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i30"],src:"/common/ic-ac.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["gl"],value:"空调"}},
[])])])])]}),aiot.__ci__({__vm__:_,__opts__:{shown:function(){return 3===_.page}}},
function(){return[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["p","p3"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["hd"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["model"],value:"风云 A9"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["clock"],value:function(){return _.clockText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["stat"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["srow"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sk"],value:"蓝牙通路"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sv"],style:function(){return r.g.$translateStyle$("color:"+_.linkColor)},
value:function(){return _.linkText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["sep"]}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["srow"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sk"],value:"上次更新"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sv"],value:function(){return _.updText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["sep"]}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["srow"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sk"],value:"数据状态"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sv"],style:function(){return r.g.$translateStyle$("color:"+_.freshColor)},
value:function(){return _.freshText}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["sep"]}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["srow"]}},
[aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sk"],value:"锁状态"}},
[]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["sv"],value:function(){return _.lockText}}},
[])])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tools"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tcol"],events:{click:function(t){return _.doRefresh(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20s"],src:"/common/ic-refresh.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["tl"],value:function(){return _.refreshLabel}}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tcol"],events:{click:function(t){return _.doProbe(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20s"],src:"/common/ic-search.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["tl"],value:"自检"}},
[])]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tcol"],events:{click:function(t){return _.goDiag(t)}}}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["tb"]}},
[aiot.__ce__("image",{__vm__:_,__opts__:{classList:["i20s"],src:"/common/ic-wrench.png",alt:"blank"}},
[])]),aiot.__ce__("text",{__vm__:_,__opts__:{classList:["tl"],value:"诊断"}},
[])])])])]}),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["dots"]}},
[aiot.__ce__("div",{__vm__:_,__opts__:{classList:["di"],style:function(){return r.g.$translateStyle$("background-color:"+_.d1)},
events:{click:function(t){return _.toPage1(t)}}}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["di"],style:function(){return r.g.$translateStyle$("background-color:"+_.d2)},
events:{click:function(t){return _.toPage2(t)}}}},
[]),aiot.__ce__("div",{__vm__:_,__opts__:{classList:["di"],style:function(){return r.g.$translateStyle$("background-color:"+_.d3)},
events:{click:function(t){return _.toPage3(t)}}}},
[])])])};
i.entry=function(t){u({},
t,l),t.default.template=d,t.default.style=p}})()}()}
