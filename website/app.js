'use strict';
(() => {
  const $ = id => document.getElementById(id);
  const all = selector => [...document.querySelectorAll(selector)];
  const positions = [[34,27],[60,22],[22,47],[76,39],[15,57],[85,52],[44,13],[37,61],[63,58],[53,41],[25,31],[74,24]];
  const initialMemories = [{title:'That first little smile',category:'FIRST',note:'A sleepy smile after breakfast. One to keep.'},{title:'A walk in the woods',category:'FAMILY',note:'Fresh air, little footsteps, and nowhere to rush.'},{title:'A wonderfully messy lunch',category:'FUNNY MOMENT',note:'More avocado on the cheeks than on the spoon.'},{title:'A new sound',category:'MILESTONE',note:'A little babble that stopped us both in our tracks.'}];
  const initialFeeds = [{type:'Bottle',amountMl:120,duration:0,time:'9:15 am'},{type:'Pump',amountMl:90,duration:0,time:'7:40 am'}];
  let memories = structuredClone(initialMemories), selectedMemory = 0;
  let feeds = structuredClone(initialFeeds), feedType = 'Bottle', feedUnit = 'mL';
  let teeth = Array(20).fill('Not seen'); teeth[4]='Erupted'; teeth[5]='Observed';
  let selectedTooth=4, toothUnlocked=false, activeDemo='memory', stage='Pregnancy';
  const stageData={
    Pregnancy:['01','Getting ready for a new beginning.','Due dates, appointments, kick sessions, contraction timing, preparation lists, and the questions you want to remember.',['Appointments','Pregnancy tools','Birth preparation']],
    Baby:['02','Finding your own little rhythm.','Quick feeding and sleep logs, native soothing sounds, diapers, solids, teeth, and the moments that seem to arrive every day.',['Feeding + sleep','Solids + teeth','Memories']],
    Toddler:['03','Little steps. Big discoveries.','Meals, sleep, potty observations, teeth, milestones, routines, and a place to keep the wonderfully unexpected moments.',['Meals','Potty journal','Routines']],
    Child:['04','A bigger world to explore.','Appointments, activities, health notes, everyday responsibilities, milestones, and a memory tree that keeps growing.',['Schedule','Health records','Responsibilities']],
    Teen:['05','Growing into their own story.',"Schedules, activities, responsibilities, health records, and family memories, with earlier care records kept safely in the same child's profile.",['Activities','Responsibilities','Health notes']]
  };
  const message = text => { $('demo-message').textContent=text; };
  function selectDemo(name) {
    if(!['memory','feeding','teeth'].includes(name)) throw new Error('Choose memory, feeding, or teeth.');
    activeDemo=name;
    all('[data-demo]').forEach(b=>{const selected=b.dataset.demo===name;b.classList.toggle('active',selected);b.setAttribute('aria-selected',String(selected));b.tabIndex=selected?0:-1;});
    ['memory','feeding','teeth'].forEach(n=>{$('panel-'+n).hidden=n!==name;});
    message('');return {activeDemo};
  }
  function renderMemories() {
    $('memory-count').textContent=`${memories.length} moments, growing together`;
    $('tree-leaves').replaceChildren();
    memories.forEach((m,i)=>{const button=document.createElement('button');button.type='button';button.className='tree-leaf'+(i===selectedMemory?' selected':'');button.style.left=positions[i][0]+'%';button.style.top=positions[i][1]+'%';button.textContent=String(i+1);button.setAttribute('aria-label','Open memory: '+m.title);button.setAttribute('aria-pressed',String(i===selectedMemory));button.addEventListener('click',()=>{selectedMemory=i;renderMemories();});$('tree-leaves').append(button);});
    const m=memories[selectedMemory];$('memory-category').textContent=m.category;$('memory-detail-title').textContent=m.title;$('memory-note').textContent=m.note;
  }
  function addMemory(title) {
    if(typeof title!=='string'||!title.trim()||title.trim().length>65) throw new Error('Use a memory title from 1 to 65 characters.');
    if(memories.length>=positions.length) throw new Error('This demo tree has room for 12 memories. Reset it to try again.');
    memories.push({title:title.trim(),category:'YOUR SAMPLE MEMORY',note:'A little moment, added to the demo tree.'});selectedMemory=memories.length-1;renderMemories();message('A new leaf for your sample memory.');return {memoryCount:memories.length,title:memories[selectedMemory].title};
  }
  function configureFeed(type) {
    if(!['Bottle','Breast','Pump'].includes(type))throw new Error('Choose Bottle, Breast, or Pump.');
    feedType=type;all('[data-feed]').forEach(b=>{const selected=b.dataset.feed===type;b.classList.toggle('selected',selected);b.setAttribute('aria-pressed',String(selected));});
    const breast=type==='Breast';$('amount-label').textContent=breast?'Duration (minutes)':`Amount (${feedUnit})`;$('feed-unit-label').textContent=breast?'min':feedUnit;$('feed-hint').textContent=breast?'Both sides · sample session':type==='Pump'?'Total pumped amount':'Bottle amount';$('unit-toggle').hidden=breast;$('feed-amount').max=breast?'180':feedUnit==='mL'?'1000':'34';$('feed-amount').min=breast?'1':feedUnit==='mL'?'1':'0.1';$('feed-amount').step=breast||feedUnit==='mL'?'1':'0.1';$('feed-amount').value=breast?'15':feedUnit==='mL'?'120':'4';
  }
  function volume(ml){return feedUnit==='mL'?`${Math.round(ml)} mL`:`${(ml/29.5735295625).toFixed(1)} fl oz`;}
  function renderFeeds() {
    $('daily-total').textContent=volume(feeds.reduce((n,f)=>n+f.amountMl,0));$('feed-count').textContent=String(feeds.length);$('feed-history').replaceChildren();
    feeds.slice(0,4).forEach(f=>{const li=document.createElement('li'),label=document.createElement('span'),time=document.createElement('small');label.textContent=`${f.type} · ${f.type==='Breast'?f.duration+' min':volume(f.amountMl)}`;time.textContent=f.time;li.append(label,time);$('feed-history').append(li);});
  }
  function logFeed(type,amount,unit='mL') {
    if(!['Bottle','Breast','Pump'].includes(type)||!['mL','fl oz'].includes(unit)||typeof amount!=='number'||!Number.isFinite(amount)||amount<=0)throw new Error('Enter a valid sample feed.');
    const ml=unit==='fl oz'?amount*29.5735295625:amount;
    if(type==='Breast'?(amount>180||!Number.isInteger(amount)):ml>1006)throw new Error('This amount is outside the demo range.');
    feeds.unshift({type,amountMl:type==='Breast'?0:ml,duration:type==='Breast'?amount:0,time:'Just now'});renderFeeds();message('Sample feeding saved. Your app records are unchanged.');return {feedCount:feeds.length,totalMl:Math.round(feeds.reduce((n,f)=>n+f.amountMl,0))};
  }
  function toothName(i){const names=['Second molar','First molar','Canine','Lateral incisor','Central incisor','Central incisor','Lateral incisor','Canine','First molar','Second molar'];return `${i<10?'Upper':'Lower'} ${names[i%10].toLowerCase()} · ${i%10<5?'right':'left'}`;}
  function showTooth(i,unlock=false){selectedTooth=i;toothUnlocked=unlock;$('tooth-title').textContent=toothName(i);$('tooth-state').textContent=teeth[i];$('tooth-stage').value=teeth[i];$('tooth-stage').disabled=!unlock;$('unlock-tooth').hidden=unlock;all('.tooth').forEach(b=>b.classList.toggle('selected',Number(b.dataset.index)===i));if(unlock)message('Tooth stage unlocked. Choose a stage below.');}
  function renderTeeth() {
    ['upper-teeth','lower-teeth'].forEach(id=>$(id).replaceChildren());
    teeth.forEach((state,i)=>{const b=document.createElement('button');b.type='button';b.className='tooth '+(state==='Erupted'?'erupted':state==='Observed'?'observed':'');b.dataset.index=i;b.textContent=state==='Erupted'?'✓':state==='Observed'?'•':'–';b.setAttribute('aria-label',`${toothName(i)}, ${state}. Hold to edit.`);
      const a=(18+(i%10)*16)*Math.PI/180;b.style.left=(50+Math.cos(a)*41-5)+'%';b.style.top=((105+(i<10?-1:1)*Math.sin(a)*83)-17)+'px';
      let timer=null,start=null,held=false;
      const cancel=()=>{if(timer)clearTimeout(timer);timer=null;};
      b.addEventListener('pointerdown',e=>{if(e.button!==0)return;held=false;start={x:e.clientX,y:e.clientY};cancel();timer=setTimeout(()=>{held=true;showTooth(i,true);},550);});
      b.addEventListener('pointermove',e=>{if(start&&Math.hypot(e.clientX-start.x,e.clientY-start.y)>10){cancel();start=null;}});
      ['pointerup','pointercancel','pointerleave'].forEach(event=>b.addEventListener(event,cancel));
      b.addEventListener('click',()=>{if(!held)showTooth(i,false);held=false;});$(i<10?'upper-teeth':'lower-teeth').append(b);
    });
    const erupted=teeth.filter(x=>x==='Erupted').length,observed=teeth.filter(x=>x==='Observed').length;$('teeth-summary').textContent=`${erupted} erupted · ${observed} observed · ${20-erupted-observed} not seen`;showTooth(selectedTooth,toothUnlocked);
  }
  function selectStage(name){if(!Object.hasOwn(stageData,name))throw new Error('Choose a supported life stage.');stage=name;all('[data-stage]').forEach(b=>{const yes=b.dataset.stage===name;b.classList.toggle('selected',yes);b.setAttribute('aria-pressed',String(yes));});const data=stageData[name];$('stage-number').textContent='CHAPTER '+data[0];$('stage-title').textContent=data[1];$('stage-copy').textContent=data[2];$('stage-tags').replaceChildren(...data[3].map(text=>{const span=document.createElement('span');span.textContent=text;return span;}));return {stage};}
  function buildRequest(title,area,details){if(typeof title!=='string'||title.trim().length<5||title.length>100||typeof details!=='string'||details.trim().length<10||details.length>1500)throw new Error('Add a short title and explain how this would help.');const body=`## Feature idea\n${details.trim()}\n\n## Area\n${area}\n\n## Context\nRequested through the SproutBook showcase website.\nApp preview: 3.7.0\n\nPlease use sample data and keep family information private.`;const url=new URL('https://github.com/nothatcher-creator/Sproutbook/issues/new');url.searchParams.set('title','[Feature] '+title.trim());url.searchParams.set('body',body);return url.href;}
  all('[data-demo]').forEach((b,index)=>{b.addEventListener('click',()=>selectDemo(b.dataset.demo));b.addEventListener('keydown',e=>{if(!['ArrowDown','ArrowUp','Home','End'].includes(e.key))return;e.preventDefault();const tabs=all('[data-demo]');const next=e.key==='Home'?0:e.key==='End'?tabs.length-1:(index+(e.key==='ArrowDown'?1:-1)+tabs.length)%tabs.length;selectDemo(tabs[next].dataset.demo);tabs[next].focus();});});
  $('memory-form').addEventListener('submit',e=>{e.preventDefault();try{addMemory($('memory-title').value);$('memory-title').value='';}catch(error){message(error.message);}});
  $('reset-memory').addEventListener('click',()=>{memories=structuredClone(initialMemories);selectedMemory=0;renderMemories();message('Sample memories reset.');});
  all('[data-feed]').forEach(b=>b.addEventListener('click',()=>configureFeed(b.dataset.feed)));
  $('unit-toggle').addEventListener('click',()=>{const old=Number($('feed-amount').value);feedUnit=feedUnit==='mL'?'fl oz':'mL';configureFeed(feedType);$('feed-amount').value=feedUnit==='fl oz'?(old/29.5735295625).toFixed(1):String(Math.round(old*29.5735295625));$('unit-toggle').textContent=feedUnit==='mL'?'Use fl oz':'Use mL';renderFeeds();});
  $('feeding-form').addEventListener('submit',e=>{e.preventDefault();try{logFeed(feedType,Number($('feed-amount').value),feedUnit);}catch(error){message(error.message);}});
  $('reset-feeding').addEventListener('click',()=>{feeds=structuredClone(initialFeeds);renderFeeds();message('Sample feeds reset.');});
  $('unlock-tooth').addEventListener('click',()=>showTooth(selectedTooth,true));
  $('tooth-stage').addEventListener('change',()=>{if(!toothUnlocked)return;const v=$('tooth-stage').value;if(!['Not seen','Observed','Erupted'].includes(v))return;teeth[selectedTooth]=v;renderTeeth();message('Sample tooth stage updated.');});
  $('reset-teeth').addEventListener('click',()=>{teeth=Array(20).fill('Not seen');teeth[4]='Erupted';teeth[5]='Observed';selectedTooth=4;toothUnlocked=false;renderTeeth();message('Sample teeth reset.');});
  all('[data-stage]').forEach(b=>b.addEventListener('click',()=>selectStage(b.dataset.stage)));
  $('request-form').addEventListener('submit',e=>{e.preventDefault();try{const url=buildRequest($('request-title').value,$('request-area').value,$('request-details').value);const opened=window.open(url,'_blank','noopener,noreferrer');$('request-feedback').replaceChildren();const link=document.createElement('a');link.href=url;link.target='_blank';link.rel='noopener noreferrer';link.textContent='Open your feature request on GitHub';$('request-feedback').append('Your draft is ready. Review and submit it on GitHub. ',link);}catch(error){$('request-feedback').textContent=error.message;}});
  renderMemories();renderFeeds();renderTeeth();selectStage(stage);
  const state=()=>({activeDemo,stage,memoryCount:memories.length,feedCount:feeds.length,totalMl:Math.round(feeds.reduce((n,f)=>n+f.amountMl,0)),eruptedTeeth:teeth.filter(t=>t==='Erupted').length});
  window.SproutBookShowcase=Object.freeze({state,selectDemo,addMemory,logFeed,selectStage,buildRequest});
  if(document.modelContext?.registerTool){const life=new AbortController();const tools=[
    {name:'get_sproutbook_demo_state',description:'Read the temporary sample state of the SproutBook website demos. This contains no family app records.',inputSchema:{type:'object',properties:{},additionalProperties:false},annotations:{readOnlyHint:true},execute:()=>state()},
    {name:'select_sproutbook_demo',description:'Show one website sample demo: memory, feeding, or teeth.',inputSchema:{type:'object',properties:{demo:{type:'string',enum:['memory','feeding','teeth']}},required:['demo'],additionalProperties:false},annotations:{readOnlyHint:false},execute:input=>selectDemo(input.demo)},
    {name:'add_sproutbook_sample_memory',description:'Add a temporary memory leaf to the website sample tree. Does not save to the Android app.',inputSchema:{type:'object',properties:{title:{type:'string',minLength:1,maxLength:65}},required:['title'],additionalProperties:false},annotations:{readOnlyHint:false},execute:input=>addMemory(input.title)}
  ];for(const tool of tools){try{Promise.resolve(document.modelContext.registerTool(tool,{signal:life.signal})).catch(()=>{});}catch{}}window.addEventListener('pagehide',()=>life.abort(),{once:true});}
})();
