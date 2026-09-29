import fs from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { Presentation, PresentationFile } from '@oai/artifact-tool';

const workspaceDir = '/Users/aritra/Code/Project/NearHelp';
const SKILL_DIR = '/Users/aritra/.codex/plugins/cache/openai-primary-runtime/presentations/26.905.11957/skills/presentations';
const TMP_DIR = path.join(workspaceDir, '.presentation-build');
const FINAL_PPTX = path.join(workspaceDir, 'presentation', 'NearHelp_AI_Project_Review_White_Background.pptx');
const RUNTIME_PYTHON = '/Users/aritra/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3';
const { finalizePresentation, resolvePresentationFont } = await import(pathToFileURL(path.join(SKILL_DIR, 'container_tools/artifact_tool_utils.mjs')).href);
await fs.mkdir(TMP_DIR, {recursive:true});
await fs.mkdir(path.dirname(FINAL_PPTX), {recursive:true});

const W=1280,H=720;
const C={navy:'#101D34', red:'#D91E36', pale:'#FFF4F5', blue:'#EAF4FE', muted:'#53647A', white:'#FFFFFF', line:'#DCE3EA', green:'#12A870', bg:'#FFFFFF'};
const font=resolvePresentationFont({fontFamily:'Arial'});
const pres=Presentation.create({slideSize:{width:W,height:H}});

function shape(slide, geometry, x,y,w,h,fill='none',stroke='none',sw=0,r=0){
  return slide.shapes.add({geometry,position:{left:x,top:y,width:w,height:h},fill,
    line:{style:'solid',fill:stroke,width:sw},...(r?{borderRadius:r}:{})});
}
function txt(slide,s,x,y,w,h,size=24,color=C.navy,bold=false,align='left'){
  const q=shape(slide,'textbox',x,y,w,h);
  q.text=s;
  q.text.style={typeface:font,fontSize:size,color,bold,alignment:align,verticalAlignment:'middle',autoFit:'shrinkText',wrap:'word',insets:{left:0,right:0,top:0,bottom:0}};
  return q;
}
function rule(slide,x,y,w,color=C.line,sw=2){shape(slide,'line',x,y,w,0,'none',color,sw);}
function base(title,num){
  const s=pres.slides.add(); s.background.fill=C.bg;
  shape(s,'rect',0,0,W,H,C.white);
  shape(s,'rect',0,0,10,H,C.red);
  txt(s,title,58,33,1160,76,title.length>43?32:title.length>33?36:42,C.navy,true);
  rule(s,58,118,1160);
  txt(s,'NearHelp AI  /  Project review',58,678,800,20,16,C.muted);
  txt(s,String(num).padStart(2,'0'),1160,674,60,26,17,C.muted,true,'right');
  return s;
}
function item(slide,n,head,body,x,y,w=510){
 txt(slide,String(n).padStart(2,'0'),x,y,65,45,26,C.red,true);
 txt(slide,head,x+70,y,w-70,42,25,C.navy,true);
 txt(slide,body,x+70,y+43,w-70,73,21,C.muted);
}
function pill(slide,label,x,y,w,h,fill=C.white,border=C.line,fs=21,color=C.navy){
 const q=shape(slide,'roundRect',x,y,w,h,fill,border,1.5,14);
 q.text=label; q.text.style={typeface:font,fontSize:fs,bold:true,color,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',insets:{left:10,right:10,top:4,bottom:4}};
 return q;
}
function arrow(slide,x,y,w=52){txt(slide,'→',x,y,w,50,33,C.red,true,'center');}
async function screenshot(slide,file,x,y,w,h,alt,opts={}){
 const bytes=await fs.readFile(path.join(workspaceDir,'SS',file));
 const ext=path.extname(file).toLowerCase();
 slide.images.add({blob:bytes,contentType:ext==='.png'?'image/png':'image/jpeg',alt,fit:opts.fit??'contain',position:{left:x,top:y,width:w,height:h},...(opts.crop?{crop:opts.crop}:{})});
}
function notes(slide,text){slide.speakerNotes.textFrame.setText(text);}

// 1 — introduction and problem statement
{
 const s=pres.slides.add(); s.background.fill=C.bg;
 shape(s,'rect',0,0,W,H,C.white);
 shape(s,'rect',0,0,10,H,C.red);
 txt(s,'Project Introduction ( Problem Statement)',60,32,1120,64,39,C.navy,true);
 rule(s,60,112,1147);
 txt(s,'NearHelp AI',72,144,690,82,59,C.red,true);
 txt(s,'Community emergency assistance on Android',74,224,690,52,30,C.navy,true);
 txt(s,'When an emergency occurs, bystanders need quick guidance and clear location context while professional responders are on the way.',74,306,680,110,27,C.navy);
 txt(s,'The current prototype brings an SOS entry point, a location map, emergency protocols and AI chat into one mobile experience.',74,437,680,102,25,C.muted);
 rule(s,74,561,650,C.line,2);
 txt(s,'Team: Aritra, Adil, Dishari, Abhisikta, Plaban and Sayantan',74,581,700,54,20,C.muted);
 await screenshot(s,'29917.jpg',865,144,272,500,'NearHelp home screen with live map and SOS control');
 notes(s,'Project team names and roles come from README.md. Screenshot is from the SS folder in the repository. College, department and faculty mentor were not supplied, so they are omitted.');
}
// 2 — challenges
{
 const s=base('Existing Emergency Response Challenges',2);
 txt(s,'The critical gap is what a person can do before organized help arrives.',65,149,1100,53,29,C.navy,true);
 item(s,1,'Time and distance','Professional responders may be several minutes away when an incident begins.',70,245,520);
 item(s,2,'Bystander uncertainty','People nearby may be willing to help but may not know a safe first step.',665,245,520);
 item(s,3,'Location friction','Describing an exact location during stress can slow a request for help.',70,427,520);
 item(s,4,'Fragmented tools','Calling, maps and guidance are often separate interactions for the user.',665,427,520);
 notes(s,'WHO emphasizes the need for timely emergency care: https://www.who.int/health-topics/emergency-care . 112 India provides a location-enabled SOS system, so this slide does not claim location sharing is absent from existing systems: https://112.gov.in/ . The challenges frame the user experience and are not measured outcomes for NearHelp AI.');
}
// 3 — proposed solution
{
 const s=base('Proposed Solution',3);
 txt(s,'NearHelp AI places the first emergency actions in a single Android app.',66,153,1110,61,30,C.navy,true);
 const obs=[
  ['SOS access','A prominent control starts the emergency journey.'],
  ['Location context','GPS and the map help users understand where they are.'],
  ['Immediate guidance','Protocol screens and AI chat provide information while help is sought.'],
  ['Community foundation','The design can later connect users with verified nearby responders.']
 ];
 obs.forEach((o,i)=>{let y=254+i*99;txt(s,String(i+1).padStart(2,'0'),70,y,85,54,36,C.red,true);txt(s,o[0],169,y,316,53,27,C.navy,true);txt(s,o[1],511,y,670,65,23,C.muted);rule(s,169,y+78,1002,C.line,1.2)});
 notes(s,'The first three solution elements are visible in screenshots from the repository. Responder connection is future scope, not demonstrated here.');
}
// 4 — workflow and methodology
{
 const s=base('System Workflow and Methodology',4);
 txt(s,'The prototype follows a short path from opening the app to choosing an informed next action.',63,145,1130,72,27,C.navy);
 const labels=['Open app','Select help','Use GPS','View map','Ask AI','Act'];
 labels.forEach((l,i)=>{const x=58+i*198;pill(s,l,x,251,163,70,i===5?C.pale:C.white,i===5?'#F5A5AE':C.line,21,i===5?C.red:C.navy);if(i<5)arrow(s,x+162,260,36)});
 item(s,1,'Input','User opens the app, chooses a topic or starts an SOS action.',71,398,540);
 item(s,2,'Processing','Device location informs the map; the chat sends a question to the AI service.',664,398,540);
 txt(s,'Output: the screen presents local context, a protocol step or an AI response for the user to review.',70,573,1115,69,24,C.navy,true);
 notes(s,'This is a simplified user workflow. The SOS control, location map and assistant screens are visible in repository screenshots. It does not represent a verified end-to-end dispatch.');
}
// 5 — implemented features
{
 const s=base('Implemented Features of NearHelp AI',5);
 const lines=[
  ['Emergency entry','The home screen presents a hold-to-activate SOS control.'],
  ['Location and map','The app shows the current GPS position and a live map view.'],
  ['Emergency protocols','The interface displays topic-based steps for common emergencies.'],
  ['AI conversation','The assistant accepts a question and displays a response.']
 ];
 lines.forEach((a,i)=>{const y=153+i*119;shape(s,'rect',67,y+6,7,92,i===3?C.red:'#C8D9EA');txt(s,a[0],99,y,600,42,27,C.navy,true);txt(s,a[1],99,y+44,603,67,22,C.muted);});
 await screenshot(s,'screen_test.png',842,150,260,455,'NearHelp protocol and assistant screen');
 txt(s,'Current Android build',836,616,278,29,17,C.muted,false,'center');
 notes(s,'Features are supported by project screenshots in SS/. The visible SOS control does not prove an alert was dispatched. The protocol and assistant screen show UI behavior, not clinical validation.');
}
// 6 — integrations
{
 const s=base('AI Chatbot & Google Maps Integration',6);
 txt(s,'Gemini-assisted chat',70,150,495,50,30,C.red,true);
 txt(s,'The user enters an emergency question. The app sends it to the AI service and presents the returned answer.',70,211,490,113,22,C.navy);
 txt(s,'The captured reply includes a source label. Medical reliability still requires systematic validation.',70,315,490,73,20,C.muted);
 await screenshot(s,'screen_check2.png',137,407,288,207,'AI assistant response in the NearHelp app',{fit:'cover',crop:{left:0,top:0.21,right:0,bottom:0.30}});
 shape(s,'line',635,155,0,470,'none',C.line,2);
 txt(s,'Location map',695,150,495,50,31,C.red,true);
 txt(s,'Device GPS provides position context. Google Maps displays the area around the user.',695,211,490,113,22,C.navy);
 txt(s,'The map supports orientation before an SOS action or a request for guidance.',695,315,490,73,20,C.muted);
 await screenshot(s,'29917.jpg',774,407,320,207,'NearHelp location and map screen',{fit:'cover',crop:{left:0,top:0.18,right:0,bottom:0.34}});
 notes(s,'Screenshots are from the project SS folder. The repository includes a Google Maps view and an AI agent integration. The screenshot demonstrates a reply; it does not establish clinical accuracy or production readiness.');
}
// 7 — architecture and stack
{
 const s=base('System Architecture and Technology Stack',7);
 txt(s,'The Android client is the user-facing layer. It connects the location map and AI interaction.',67,144,1120,57,25,C.navy);
 const app=pill(s,'Android app\nKotlin + Jetpack Compose',477,229,327,88,C.pale,'#F5A5AE',25,C.navy);
 const a=pill(s,'Google Maps SDK',111,422,267,84,C.white,C.line,24);
 const b=pill(s,'Gemini AI service',505,422,267,84,C.white,C.line,24);
 const c=pill(s,'Backend API foundation',899,422,267,84,C.white,C.line,23);
 s.shapes.connect(app,a,{kind:'straight',fromSide:'bottom',toSide:'top',line:{style:'solid',fill:C.muted,width:2}});
 s.shapes.connect(app,b,{kind:'straight',fromSide:'bottom',toSide:'top',line:{style:'solid',fill:C.muted,width:2}});
 s.shapes.connect(app,c,{kind:'straight',fromSide:'bottom',toSide:'top',line:{style:'solid',fill:C.muted,width:2}});
 txt(s,'Implemented app layer',109,527,505,37,23,C.navy,true);
 txt(s,'Maps and AI are visible in the current demo.',109,571,505,49,21,C.muted);
 txt(s,'Integration path',700,527,470,37,23,C.navy,true);
 txt(s,'FastAPI and data services support the wider project design; connected rescue flow still needs validation.',700,571,470,66,20,C.muted);
 notes(s,'Architecture is simplified from README.md and the Android source. FastAPI is in the repository, but this slide does not claim an end-to-end rescue workflow or production database integration.');
}
// 8 — demo, testing and results
{
 const s=base('Application Demo, Testing & Current Results',8);
 txt(s,'Current evidence comes from device screenshots of the working Android interface.',67,143,1135,65,25,C.navy);
 await screenshot(s,'29917.jpg',83,230,166,305,'Home screen capture');
 await screenshot(s,'screen_test.png',285,230,166,305,'Protocol screen capture');
 await screenshot(s,'screen_check2.png',487,230,166,305,'Chat response capture');
 const statuses=[['Launch + navigation','Home and major tabs appear'],['Location map','GPS area and SOS control shown'],['Emergency content','Protocol topic and steps shown'],['AI conversation','Question and response captured']];
 txt(s,'Demo check',718,205,260,37,21,C.muted,true);txt(s,'Observed result',990,205,210,37,21,C.muted,true);
 rule(s,713,246,491,C.navy,2);
 statuses.forEach((a,i)=>{const y=256+i*79;txt(s,a[0],719,y,268,63,21,C.navy,true);txt(s,a[1],990,y,208,63,19,C.muted);rule(s,713,y+69,491,C.line,1)});
 txt(s,'These checks confirm visible interface behavior. Dispatch reliability, response time and medical accuracy need separate evaluation.',70,568,1135,76,21,C.muted);
 notes(s,'Evidence comes from project screenshots 29917.jpg, screen_test.png and screen_check2.png. The checks summarize what is visible, not formal automated test results or real-world rescue performance.');
}
// 9 — research contribution
{
 const s=base('Research Contribution',9);
 const cols=[
  {n:'01',h:'Emergency care',b:'WHO emphasizes access to quality, timely emergency care. This motivates support during the early response period.'},
  {n:'02',h:'Existing digital systems',b:'India’s 112 system already combines SOS requests with location data. NearHelp studies the user-facing guidance layer.'},
  {n:'03',h:'Community response',b:'A randomized trial found that phone alerts increased CPR started by nearby trained volunteers before EMS arrival.'}
 ];
 cols.forEach((a,i)=>{let x=68+i*405;txt(s,a.n,x,158,100,48,36,C.red,true);txt(s,a.h,x,228,365,64,28,C.navy,true);rule(s,x,311,351,C.line,2);txt(s,a.b,x,337,351,166,22,C.muted);});
 shape(s,'rect',69,537,1138,92,C.pale);
 txt(s,'Project contribution: a prototype that places map context, emergency content and AI interaction in one mobile journey.',95,549,1080,72,24,C.navy,true,'center');
 txt(s,'Sources: WHO emergency care; 112 India; Ringh et al., NEJM (2015)',72,647,1100,25,16,C.muted);
 notes(s,'[1] WHO, Emergency care, https://www.who.int/health-topics/emergency-care\n[2] Government of India, Emergency Response Support System, https://112.gov.in/\n[3] M. Ringh et al., Mobile-Phone Dispatch of Laypersons for CPR in Out-of-Hospital Cardiac Arrest, New England Journal of Medicine 372 (2015), 2316–2325, https://www.nejm.org/doi/full/10.1056/NEJMoa1406038 . The project contribution is a prototype integration and research direction, not a measured improvement over existing systems. 112 India also offers a SHOUT volunteer feature for women and children.');
}
// 10 — limitations, future and conclusion
{
 const s=base('Limitations, Future Scope & Conclusion',10);
 txt(s,'Current limitations',69,155,500,48,30,C.red,true);
 txt(s,'The screenshots do not establish successful real-world dispatch or improved response time.',70,221,502,80,22,C.navy);
 txt(s,'AI answers need systematic checks against verified medical sources before wider use.',70,321,502,80,22,C.navy);
 txt(s,'The current demo does not evaluate responder availability or field performance.',70,421,502,80,22,C.navy);
 shape(s,'line',636,153,0,413,'none',C.line,2);
 txt(s,'Future scope',695,155,480,48,30,C.red,true);
 txt(s,'Validate protocol retrieval and medical safety guardrails.',697,221,478,66,22,C.navy);
 txt(s,'Connect and verify nearby responders, then add live rescue tracking.',697,315,478,76,22,C.navy);
 txt(s,'Expand access with Bengali, Hindi and English support.',697,421,478,66,22,C.navy);
 shape(s,'rect',69,558,1138,84,C.pale);
 txt(s,'Conclusion: NearHelp AI now demonstrates the mobile foundation for location-aware emergency assistance.',95,566,1080,71,24,C.navy,true,'center');
 notes(s,'Limitations are framed around the evidence available in repository screenshots. Future scope follows ppt_instruction.md. No claim of clinical validation or real-world rescue performance is made.');
}

const candidatePath=path.join(TMP_DIR,'candidate.pptx');
await (await PresentationFile.exportPptx(pres)).save(candidatePath);
const result=await finalizePresentation({
  workspaceDir,candidatePath,finalPath:FINAL_PPTX,pythonExecutable:RUNTIME_PYTHON,
  integrityValidatorPath:path.join(SKILL_DIR,'container_tools/inspect_presentation_package_integrity.py'),
  layoutValidatorPath:path.join(SKILL_DIR,'container_tools/inspect_presentation_layout_geometry.py'),
  layoutArgs:['--expected-slide-size-emu','12192000,6858000','--validate-heading-fit'],
  explicitTotalSlideCount:10,requiredNativeTableOwnerSlides:[],requiredNativeChartOwnerSlides:[],
  fontPolicy:{basis:'design',families:[font]},verifyArtifactToolImport:true,
  receiptPath:path.join(TMP_DIR,'validation-white-background.json')
});
console.log(JSON.stringify({font,final:FINAL_PPTX,result},null,2));
