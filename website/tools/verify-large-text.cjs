const {chromium}=require('playwright');
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const url=process.argv[2]||'http://127.0.0.1:8765/',out=process.argv[3]||'large-text-evidence';
fs.mkdirSync(out,{recursive:true});
(async()=>{
 const browser=await chromium.launch({headless:true,args:['--no-sandbox']});
 const report={url,browser:browser.version(),date:new Date().toISOString(),fontScale:'200%',cases:[]};
 try{
  for(const viewport of [{width:390,height:844},{width:1440,height:900}]){
   const page=await browser.newPage({viewport,reducedMotion:'reduce'});
   await page.goto(url,{waitUntil:'networkidle'});await page.evaluate(()=>document.fonts.ready);
   await page.evaluate(()=>document.documentElement.style.fontSize='200%');
   const item={viewport,checks:[]};report.cases.push(item);
   for(const selector of ['.hero','#memory-chapter','#calendar-chapter','#explore','#requests','#download']){
    await page.locator(selector).scrollIntoViewIfNeeded();
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth-innerWidth),0,`${selector} overflow`);
    assert(await page.locator(selector).isVisible());item.checks.push(selector);
    await page.screenshot({path:path.join(out,`${viewport.width}-${selector.replace(/[.#]/g,'')}.png`)});
   }
   console.log(`PASS ${viewport.width}: 200% text; opening, memory, calendar, demos, requests, arrival; no horizontal overflow`);
   await page.close();
  }
  report.result='PASS';
 }finally{fs.writeFileSync(path.join(out,'results.json'),JSON.stringify(report,null,2));await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
