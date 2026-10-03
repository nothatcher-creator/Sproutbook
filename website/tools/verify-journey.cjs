/* Real rendered journey and preserved sample regressions.
 * NODE_PATH=<playwright node_modules> node tools/verify-journey.cjs URL OUT [VERSION] [chromium|webkit]
 * Browser engines on Linux are not physical Android/iPhone evidence.
 */
const {chromium,webkit}=require('playwright');
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const url=process.argv[2] || 'http://127.0.0.1:8765/', out=process.argv[3] || path.resolve('journey-evidence');
const version=process.argv[4] || '3.7.0', engine=process.argv[5] || 'chromium';
fs.mkdirSync(out,{recursive:true});
const report={url,version,engine,date:new Date().toISOString(),cases:[]};
const settle=page=>page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))));
const state=page=>page.evaluate(()=>{
 const story=document.getElementById('woodland-story'),stage=document.getElementById('story-stage');
 return {scroll:scrollY,motion:document.documentElement.className,progress:Number(story.style.getPropertyValue('--journey')),
 position:getComputedStyle(stage).position,scene:story.style.cssText,
 times:[...story.querySelectorAll('[data-scrub]')].map(s=>s.getCurrentTime()),paused:[...story.querySelectorAll('[data-scrub]')].every(s=>s.animationsPaused()),
 active:[...story.querySelectorAll('[data-chapter]')].filter(e=>{const r=e.getBoundingClientRect();return r.bottom>0 && r.top<innerHeight;}).map(e=>[e.dataset.chapter,e.style.cssText]),
 steps:[...story.querySelectorAll('.story-step')].map(s=>({opacity:Number(getComputedStyle(s).opacity),top:s.getBoundingClientRect().top+scrollY,bottom:s.getBoundingClientRect().bottom+scrollY})),
 layers:[...story.querySelectorAll('[data-depth]')].map(s=>[s.dataset.depth,getComputedStyle(s).transform]),
 overflow:document.documentElement.scrollWidth-innerWidth,animations:document.getAnimations().filter(a=>a.playState==='running').length};
});
(async()=>{
 const browser=await (engine==='webkit'?webkit:chromium).launch({headless:true,...(engine==='chromium'?{args:['--no-sandbox']}: {})});
 report.browser=browser.version();
 try{
 for(const viewport of [{width:1440,height:900},{width:390,height:844},{width:360,height:640},{width:320,height:700},{width:667,height:375}]){
  const context=await browser.newContext({viewport,reducedMotion:'no-preference'}),page=await context.newPage();
  const item={viewport,errors:[],failed:[],checks:[]};report.cases.push(item);
  page.on('pageerror',e=>item.errors.push(String(e)));page.on('response',r=>{if(r.status()>=400 && new URL(r.url()).origin===new URL(url).origin)item.failed.push([r.url(),r.status()]);});
  await page.addInitScript(()=>{window.__rafCount=0;window.__rafCosts=[];const native=requestAnimationFrame;window.requestAnimationFrame=cb=>native(t=>{const start=performance.now();window.__rafCount++;cb(t);window.__rafCosts.push(performance.now()-start);});});
  await page.goto(url,{waitUntil:'networkidle'});await page.evaluate(()=>document.fonts.ready);await settle(page);
  assert((await page.locator('.site-header .download-link').getAttribute('href')).endsWith(`SproutBook-${version}-debug.apk`));
  assert.equal((await state(page)).overflow,0);assert.equal((await state(page)).motion,'motion-enabled');
  await page.keyboard.press('Tab');assert.equal(await page.locator('.skip').evaluate(e=>{const r=e.getBoundingClientRect();return document.elementFromPoint(r.x+5,r.y+5)===e;}),true,'skip link is visible above header');
  await page.screenshot({path:path.join(out,`top-${viewport.width}.png`)});item.checks.push('APK visible; keyboard skip link; no horizontal overflow');
  const samples=[];
  for(const p of [.1,.35,.68,.9,.68,.35,.1]){
   await page.evaluate(p=>{const s=document.getElementById('woodland-story');scrollTo({top:s.getBoundingClientRect().top+scrollY+(s.offsetHeight-innerHeight)*p,behavior:'instant'});},p);await settle(page);samples.push(await state(page));
   assert(Math.abs(samples.at(-1).progress-p)<.002);assert(samples.at(-1).paused);assert.equal(samples.at(-1).overflow,0);
  }
  for(const [a,b] of [[0,6],[1,5],[2,4]]){assert.equal(samples[a].scene,samples[b].scene,'reverse restores painted scene and camera');assert.deepEqual(samples[a].times,samples[b].times,'reverse restores SVG frames');assert.deepEqual(samples[a].active,samples[b].active,'reverse restores visible phone, plant, cards and animal');}
  assert.notEqual(samples[1].layers[0][1],samples[1].layers[1][1],'background and midground move at different speeds');
  item.checks.push('forward/reverse scene, SVG, plant, phone and animal poses; four depth layers');
  for(const name of ['memory','calendar','feeding','sleep','growth']){
   const chapter=page.locator(`#${name}-chapter`);await chapter.scrollIntoViewIfNeeded();await chapter.locator('.journey-phone img').evaluate(im=>im.decode());await settle(page);
   assert.equal(await chapter.evaluate(e=>Number(getComputedStyle(e).opacity)),1);
   assert(await chapter.locator('h2').isVisible());assert.equal((await state(page)).overflow,0);
   await page.screenshot({path:path.join(out,`${name}-${viewport.width}.png`)});
  }
  const before=await state(page);await page.mouse.wheel(0,100);await page.waitForFunction(y=>scrollY>y,before.scroll);await page.mouse.wheel(0,-100);await page.waitForTimeout(150);
  const idleStart=await page.evaluate(()=>window.__rafCount);await page.waitForTimeout(250);assert.equal(await page.evaluate(()=>window.__rafCount),idleStart,'no idle JavaScript frame loop');
  await page.locator('#explore').scrollIntoViewIfNeeded();await settle(page);assert.equal(await page.locator('#woodland-story').evaluate(e=>e.classList.contains('journey-visible')),false);assert.equal((await state(page)).animations,0,'offscreen ambient animations stop');
  item.checks.push('all chapters readable; native wheel; idle JS stops; offscreen ambient motion stops');
  // rAF cost is local headless CPU evidence, not a hardware FPS claim.
  await page.evaluate(()=>{window.__rafCosts=[];});
  for(let k=0;k<45;k++){await page.evaluate(k=>{const s=document.getElementById('woodland-story');scrollTo({top:s.offsetTop+(s.offsetHeight-innerHeight)*k/44,behavior:'instant'});},k);await settle(page);}
  item.frameCallbacks=await page.evaluate(()=>{const a=window.__rafCosts.toSorted((a,b)=>a-b);return {count:a.length,medianMs:a[Math.floor(a.length*.5)],p95Ms:a[Math.floor(a.length*.95)],maxMs:a.at(-1)};});
  await page.locator('#motion-toggle').click();assert.equal(await page.locator('#motion-toggle').getAttribute('aria-pressed'),'false');assert((await state(page)).steps.every(s=>s.opacity===1));assert.equal((await state(page)).overflow,0);
  await page.reload({waitUntil:'networkidle'});assert.equal(await page.locator('#motion-toggle').textContent(),'Motion off');
  await page.locator('#motion-toggle').click();await page.emulateMedia({reducedMotion:'reduce'});await settle(page);assert(await page.locator('#motion-toggle').isDisabled());assert.equal(await page.locator('#motion-toggle').textContent(),'Reduced motion');assert.equal((await state(page)).overflow,0);assert((await state(page)).steps.every(s=>s.opacity===1));
  await page.locator('#memory-chapter').scrollIntoViewIfNeeded();await page.screenshot({path:path.join(out,`reduced-${viewport.width}.png`)});item.checks.push('motion off persists; live reduced motion preserves every chapter');
      // Rendered clicks/inputs exercise the preserved sample flows.
      await page.locator('#memory-title').fill('Sample woodland walk');
      await page.locator('#memory-form button[type=submit]').click();
      assert.equal(await page.locator('#memory-detail-title').textContent(), 'Sample woodland walk');
      await page.locator('#reset-memory').click();
      assert.match(await page.locator('#memory-count').textContent(), /^4 moments/);
      await page.locator('#tab-feeding').click();
      await page.locator('#feed-amount').fill('80');
      await page.locator('#feeding-form button[type=submit]').click();
      assert.match(await page.locator('#feed-history').textContent(), /Bottle · 80 mL/);
      await page.locator('#reset-feeding').click();
      await page.locator('#tab-teeth').click();
      for (let tooth = 0; tooth < 20; tooth++) {
        await page.locator(`.tooth[data-index="${tooth}"]`).click();
        assert(await page.locator('#tooth-stage').isDisabled(), 'every tap only shows details');
      }
      const tooth = page.locator('.tooth[data-index="0"]');
      await tooth.scrollIntoViewIfNeeded();
      let bounds = await tooth.boundingBox();
      await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
      await page.mouse.down();
      await page.mouse.move(bounds.x + bounds.width / 2 + 20, bounds.y + bounds.height / 2 + 20);
      await page.waitForTimeout(600);
      await page.mouse.up();
      assert(await page.locator('#tooth-stage').isDisabled(), 'movement cancels hold');
      await tooth.click();
      assert(await page.locator('#tooth-stage').isDisabled(), 'tap only shows details');
      bounds = await tooth.boundingBox();
      await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
      await page.mouse.down();
      await page.waitForTimeout(650);
      await page.mouse.up();
      assert(await page.locator('#tooth-stage').isEnabled(), 'long press unlocks editor');
      await tooth.click();
      assert(await page.locator('#tooth-stage').isDisabled(), 'ordinary tap relocks editor');
      await page.locator('#unlock-tooth').click();
      await page.locator('#tooth-stage').selectOption('Observed');
      assert.match(await page.locator('#teeth-summary').textContent(), /2 observed/);
      await page.locator('#reset-teeth').click();
      await page.locator('[data-stage="Teen"]').click();
      assert.match(await page.locator('#stage-title').textContent(), /their own story/);
      // Block popup navigation; verify the reviewable GitHub URL, never submit.
      await page.evaluate(() => { window.open = () => null; });
      await page.locator('#request-title').fill('Sample accessibility idea');
      await page.locator('#request-details').fill('A sample shortcut for easier logging.');
      await page.locator('#request-form button[type=submit]').click();
      const draft = new URL(await page.locator('#request-feedback a').getAttribute('href'));
      assert.equal(draft.origin, 'https://github.com');
      assert.equal(draft.searchParams.get('title'), '[Feature] Sample accessibility idea');
      assert(draft.searchParams.get('body').includes('A sample shortcut for easier logging.'));
      assert(draft.searchParams.get('body').includes(`App preview: ${version}`));
      item.checks.push('memory/feeding/tooth/stage samples and GitHub draft feedback');
      await page.locator('#download').scrollIntoViewIfNeeded();
      assert(await page.locator('#download .download-link').isVisible());
      assert.equal(await page.locator('.arrival-links a').count(),4);
      await page.screenshot({path:path.join(out,`arrival-${viewport.width}.png`)});
      assert.deepEqual(item.failed,[],'local asset HTTP failures');
      assert.deepEqual(item.errors, [], 'browser script errors');
      await context.close();
      console.log(`PASS ${viewport.width}x${viewport.height}: ${item.checks.join('; ')}`);
    }
    for(const reducedMotion of ['no-preference','reduce']){
      const c=await browser.newContext({viewport:{width:390,height:844},javaScriptEnabled:false,reducedMotion});const p=await c.newPage();await p.goto(url,{waitUntil:'networkidle'});
      assert(await p.locator('#motion-toggle').isDisabled());assert.equal(await p.locator('.story-step').count(),5);
      for(const key of ['memory','calendar','feeding','sleep','growth'])assert(await p.locator(`#${key}-journey-title`).isVisible());
      await p.screenshot({path:path.join(out,`no-js-${reducedMotion}.png`)});await c.close();
    }
    report.noJavaScript='PASS: disabled motion control, all five chapters, normal and reduced preference';
    report.result = 'PASS';
  } finally {
    fs.writeFileSync(path.join(out, 'results.json'), JSON.stringify(report, null, 2));
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
