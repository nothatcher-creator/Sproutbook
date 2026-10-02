'use strict';
(() => {
  const story = document.getElementById('woodland-story'), stage = document.getElementById('story-stage'), toggle = document.getElementById('motion-toggle');
  if (!story || !stage || !toggle) return;
  const media = matchMedia('(prefers-reduced-motion: reduce)');
  const chapters = [...story.querySelectorAll('[data-chapter]')], supplied = [...story.querySelectorAll('[data-scrub]')];
  const arrival = document.getElementById('download'), progressFill = document.getElementById('story-progress-fill'), location = document.getElementById('journey-location');
  const names = ['THE CANOPY','THE MEMORY TREE','THE NOTICEBOARD','THE PICNIC','A QUIET NIGHT','ROOM TO GROW'];
  const clamp = n => Math.min(1, Math.max(0, n));
  const smooth = (a,b,n) => { const p = clamp((n-a)/(b-a)); return p*p*(3-2*p); };
  const bell = (a,b,n) => Math.sin(clamp((n-a)/(b-a))*Math.PI);
  let optedOut = false, enabled = false, pending = 0, bounds = [], storyBounds, arrivalBounds;
  try { optedOut = localStorage.getItem('sproutbook-showcase-motion') === 'off'; } catch {}
  // Supplied SVG clocks stay paused. Scrolling is their only clock.
  supplied.forEach(svg => {
    if (typeof svg.pauseAnimations === 'function' && typeof svg.setCurrentTime === 'function') {
      svg.pauseAnimations();
      svg.querySelectorAll('animate,animateTransform').forEach(a => { try { a.beginElement(); } catch {} });
    } else svg.querySelectorAll('animate,animateTransform').forEach(a => a.remove());
  });
  function pose(svg,p) { if (typeof svg.setCurrentTime === 'function') svg.setCurrentTime(clamp(p)*Number(svg.dataset.duration)*.999); }
  function style(node,values) {
    for (const [key,value] of Object.entries(values)) {
      const formatted = typeof value === 'number' ? value.toFixed(5) : value;
      if (node.style.getPropertyValue(key) !== formatted) node.style.setProperty(key,formatted);
    }
  }
  function measure() {
    const rect = story.getBoundingClientRect(); storyBounds = {top:rect.top+scrollY,height:rect.height};
    bounds = chapters.map(chapter => { const r=chapter.getBoundingClientRect(); return {top:r.top+scrollY,height:r.height}; });
    const a=arrival.getBoundingClientRect(); arrivalBounds={top:a.top+scrollY,height:a.height}; requestUpdate();
  }
  function update() {
    pending=0;
    if (!enabled || document.hidden || !storyBounds) return;
    const y=scrollY, viewport=innerHeight;
    const visible=storyBounds.top < y+viewport && storyBounds.top+storyBounds.height > y;
    story.classList.toggle('journey-visible',visible);
    const arrivalVisible=arrivalBounds.top < y+viewport && arrivalBounds.top+arrivalBounds.height > y;
    arrival.classList.toggle('arrival-visible',arrivalVisible);
    if(arrivalVisible) style(arrival,{'--arrival-growth':.84+clamp((y+viewport-arrivalBounds.top)/(viewport+arrivalBounds.height))*.16});
    if(!visible) return;
    const p=clamp((y-storyBounds.top)/Math.max(1,storyBounds.height-viewport));
    const sky=1-smooth(.015,.17,p), path=smooth(.19,.36,p), evening=smooth(.50,.72,p), night=smooth(.62,.9,p);
    style(story,{
      '--journey':p,'--sky':sky,'--morning':(1-sky)*(1-path),'--path':path*(1-evening),'--evening':evening,'--night':night,
      '--shade':smooth(.035,.18,p),'--moon':night,'--moon-y':`${(1-night)*130}px`,
      '--back-y':`${-p*42}px`,'--mid-y':`${-p*90}px`,'--front-y':`${-p*230}px`,'--branch-x':`${p*100}px`,'--fern-y':`${p*140}px`,
      '--canopy':bell(.015,.65,p),'--canopy-y':`${(p-.10)*-650}px`,'--birds-x':`${p*700}px`,'--birds-y':`${-p*180}px`,
      '--fox':bell(.27,.61,p),'--fox-x':`${(p-.34)*580}px`,
      '--butterfly':bell(.08,.75,p),'--butterfly-x':`${Math.sin(p*8)*90}px`,'--butterfly-y':`${(p-.25)*-230}px`,'--butterfly-turn':`${Math.sin(p*8)*12}deg`,
      '--leaf':bell(.12,.88,p),'--leaf-x':`${Math.sin(p*9)*90}px`,'--leaf-y':`${p*550-170}px`,'--leaf-turn':`${p*280}deg`,'--particles':.25+night*.45
    });
    progressFill.style.transform=`scaleX(${p.toFixed(5)})`;
    let active=0;
    bounds.forEach((b,index)=>{
      const chapter=chapters[index];
      if(y+viewport < b.top-100 || y > b.top+b.height+100) return;
      const q=clamp((y+viewport-b.top)/(viewport+b.height)), assembly=smooth(.10,.56,q), turn=Math.sin(q*Math.PI*2)*2;
      style(chapter,{
        '--exhibit-y':`${(q-.5)*-55}px`,'--phone-x':`${(q-.5)*16}px`,'--phone-y':`${(1-assembly)*100-q*12}px`,'--phone-turn':`${turn}deg`,
        '--plant-scale':index===5 ? .35+assembly*.65 : index===1 ? .68+assembly*.32 : .96+assembly*.04,
        '--reveal':smooth(.12,.45,q),'--card-y':`${(1-assembly)*70}px`,'--card-turn':`${-12+assembly*8}deg`,
        '--animal-x':`${(q-.5)*110}px`,'--animal-y':`${-bell(.25,.7,q)*(index===2 ? 32:6)}px`,'--animal-turn':`${index===4 ? (q-.5)*5:turn}deg`,
        '--grass-y':`${(q-.5)*-28}px`,'--wave-scale':.82+assembly*.18,'--sprout-scale':.55+smooth(0,.55,q)*.45
      });
      chapter.querySelectorAll('[data-scrub]').forEach(svg=>pose(svg,q));
      if(b.top<=y+viewport*.5) active=index;
    });
    location.textContent=names[active]; stage.querySelectorAll('[data-scrub]').forEach(svg=>pose(svg,p));
  }
  function requestUpdate(){if(enabled && !pending && !document.hidden) pending=requestAnimationFrame(update);}
  function configure(){
    enabled=!media.matches && !optedOut;
    document.documentElement.classList.toggle('motion-enabled',enabled); document.documentElement.classList.toggle('motion-disabled',!enabled);
    toggle.textContent=media.matches ? 'Reduced motion':enabled ? 'Motion on':'Motion off'; toggle.setAttribute('aria-pressed',String(enabled)); toggle.disabled=media.matches;
    toggle.title=media.matches ? 'Following your device’s reduced-motion preference.':'Turn woodland animation on or off.';
    if(!enabled){if(pending) cancelAnimationFrame(pending);pending=0;story.classList.remove('journey-visible');arrival.classList.remove('arrival-visible');supplied.forEach(svg=>pose(svg,.28));}
    measure();
  }
  toggle.addEventListener('click',()=>{optedOut=!optedOut;try{localStorage.setItem('sproutbook-showcase-motion',optedOut ? 'off':'on');}catch{} configure();});
  addEventListener('scroll',requestUpdate,{passive:true}); addEventListener('resize',measure,{passive:true});
  document.addEventListener('visibilitychange',()=>{
    if(document.hidden){if(pending)cancelAnimationFrame(pending);pending=0;story.classList.remove('journey-visible');arrival.classList.remove('arrival-visible');}else requestUpdate();
  });
  media.addEventListener('change',configure); new ResizeObserver(measure).observe(document.getElementById('main')); document.fonts.ready.then(measure); configure();
})();
