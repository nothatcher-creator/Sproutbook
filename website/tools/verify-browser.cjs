/* Real Chromium checks. Install playwright 1.63.0 and its Chromium browser.
 * Usage: NODE_PATH=/path/to/node_modules node tools/verify-browser.cjs URL OUT [VERSION]
 * Only synthetic website samples are changed; GitHub drafts are never submitted.
 */
const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const url = process.argv[2] || 'https://sproutbook-woodland.nothatch.chatgpt.site';
const out = process.argv[3] || path.resolve('browser-evidence');
const version = process.argv[4] || '3.5.1';
fs.mkdirSync(out, { recursive: true });
const report = { url, version, date: new Date().toISOString(), cases: [] };
const settle = page => page.evaluate(() => new Promise(resolve =>
  requestAnimationFrame(() => requestAnimationFrame(resolve))));
const state = page => page.evaluate(() => {
  const story = document.getElementById('woodland-story');
  const stage = document.getElementById('story-stage');
  return {
    scroll: scrollY,
    motion: document.documentElement.className,
    progress: Number(story.style.getPropertyValue('--journey')),
    position: getComputedStyle(stage).position,
    times: [...story.querySelectorAll('[data-scrub]')].map(s => s.getCurrentTime()),
    paused: [...story.querySelectorAll('[data-scrub]')].every(s => s.animationsPaused()),
    steps: [...story.querySelectorAll('.story-step')].map(s => ({
      opacity: Number(getComputedStyle(s).opacity),
      top: s.getBoundingClientRect().top + scrollY,
      bottom: s.getBoundingClientRect().bottom + scrollY,
    })),
    overflow: document.documentElement.scrollWidth - innerWidth,
  };
});

(async () => {
  const browser = await chromium.launch({ headless: true, args: ['--no-sandbox'] });
  report.browser = browser.version();
  try {
    for (const viewport of [
      { width: 1440, height: 900 }, { width: 390, height: 844 },
      { width: 360, height: 640 }, { width: 667, height: 375 },
    ]) {
      const context = await browser.newContext({ viewport, reducedMotion: 'no-preference' });
      const page = await context.newPage();
      const item = { viewport, errors: [], checks: [] };
      report.cases.push(item);
      page.on('pageerror', error => item.errors.push(String(error)));
      await page.goto(url, { waitUntil: 'networkidle' });
      await page.locator('.site-header .download-link').waitFor({ state: 'visible' });
      const download = page.locator('.site-header .download-link');
      assert((await download.getAttribute('href')).endsWith(`SproutBook-${version}-debug.apk`));
      await page.screenshot({ path: path.join(out, `top-${viewport.width}x${viewport.height}.png`) });
      item.checks.push('APK visible at top');
      assert.equal((await state(page)).overflow, 0, 'horizontal overflow');
      assert.equal((await state(page)).motion, 'motion-enabled');

      if ((await state(page)).position === 'sticky') {
        const samples = [];
        for (const progress of [.10, .48, .87, .48, .10]) {
          await page.evaluate(p => {
            const story = document.getElementById('woodland-story');
            const stage = document.getElementById('story-stage');
            const travel = story.offsetHeight - stage.offsetHeight - 24;
            scrollTo({ top: story.getBoundingClientRect().top + scrollY - 24 + travel * p, behavior: 'instant' });
          }, progress);
          await settle(page);
          const sample = await state(page);
          assert(Math.abs(sample.progress - progress) < .002, 'rendered scroll progress');
          assert(sample.paused, 'SVG clocks must remain paused');
          samples.push(sample);
          await page.screenshot({ path: path.join(out, `story-${viewport.width}-${progress}-${samples.length}.png`) });
        }
        assert.deepEqual(samples[1].times, samples[3].times, 'reverse restores SVG frame');
        assert.deepEqual(samples[0].times, samples[4].times, 'reverse restores starting frame');
        for (const [index, sample] of samples.slice(0, 3).entries()) {
          assert(sample.steps[index].opacity > .99, 'active chapter readable');
        }
        const before = await state(page);
        await page.waitForTimeout(250);
        assert.deepEqual((await state(page)).times, before.times, 'idle must not autoplay');
        await page.mouse.wheel(0, 120);
        await page.waitForFunction(y => scrollY > y, before.scroll);
        await page.waitForTimeout(200);
        assert((await state(page)).progress > before.progress, 'normal wheel advances story');
        await page.mouse.wheel(0, -120);
        await page.waitForTimeout(200);
        assert((await state(page)).progress < .13, 'normal wheel reverses story');
        item.samples = samples;
        item.checks.push('forward/reverse SVG frames, readable chapters, native wheel, paused idle');
      } else {
        const stacked = await state(page);
        assert(stacked.steps.every(s => s.opacity === 1), 'short-height chapters visible');
        assert(stacked.steps[1].top >= stacked.steps[0].bottom, 'stacked chapters do not overlap');
        item.checks.push('short-height stacked story');
      }

      await page.locator('#motion-toggle').click();
      assert.equal(await page.locator('#motion-toggle').getAttribute('aria-pressed'), 'false');
      let still = await state(page);
      assert(still.steps.every(s => s.opacity === 1), 'motion-off keeps every chapter');
      assert(still.steps[1].top >= still.steps[0].bottom, 'still chapters do not overlap');
      await page.reload({ waitUntil: 'networkidle' });
      assert.equal(await page.locator('#motion-toggle').textContent(), 'Motion off');
      await page.locator('#motion-toggle').click();
      await page.emulateMedia({ reducedMotion: 'reduce' });
      await settle(page);
      assert(await page.locator('#motion-toggle').isDisabled());
      assert.equal(await page.locator('#motion-toggle').textContent(), 'Reduced motion');
      assert((await state(page)).steps.every(s => s.opacity === 1));
      await page.locator('#woodland-story').scrollIntoViewIfNeeded();
      await page.screenshot({ path: path.join(out, `reduced-${viewport.width}.png`) });
      item.checks.push('motion off persists; live reduced-motion preference stacks chapters');

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
      assert.deepEqual(item.errors, [], 'browser script errors');
      await context.close();
      console.log(`PASS ${viewport.width}x${viewport.height}: ${item.checks.join('; ')}`);
    }
    report.result = 'PASS';
  } finally {
    fs.writeFileSync(path.join(out, 'results.json'), JSON.stringify(report, null, 2));
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
