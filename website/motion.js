'use strict';
(() => {
  const story = document.getElementById('woodland-story');
  const stage = document.getElementById('story-stage');
  const toggle = document.getElementById('motion-toggle');
  if (!story || !stage || !toggle) return;
  const media = window.matchMedia('(prefers-reduced-motion: reduce)');
  const svgs = [...document.querySelectorAll('[data-scrub]')];
  const steps = [...story.querySelectorAll('.story-step')];
  const panels = [...document.querySelectorAll('[data-scroll-art]')];
  const hero = document.querySelector('.hero');
  const clamp = (value, low = 0, high = 1) => Math.min(high, Math.max(low, value));
  let optedOut = false, frame = 0, enabled = false, lastProgress = -1;
  try { optedOut = localStorage.getItem('sproutbook-showcase-motion') === 'off'; } catch {}
  const supported = svgs.every(svg => typeof svg.pauseAnimations === 'function' && typeof svg.setCurrentTime === 'function');
  // Supplied SVG timelines are paused once. Scroll is their only clock.
  svgs.forEach(svg => {
    if (!supported) return;
    svg.pauseAnimations();
    svg.querySelectorAll('animate, animateTransform').forEach(animation => animation.beginElement());
  });
  function pose(svg, progress) {
    if (!svg.setCurrentTime) return;
    const duration = Number(svg.dataset.duration);
    svg.setCurrentTime(clamp(progress) * duration * .999);
  }
  function update() {
    frame = 0;
    if (!enabled || document.hidden) return;
    const rect = story.getBoundingClientRect();
    const viewport = window.innerHeight;
    const travel = Math.max(1, rect.height - stage.offsetHeight - 24);
    const progress = clamp((24 - rect.top) / travel);
    if (rect.bottom >= 0 && rect.top <= viewport && Math.abs(progress - lastProgress) > .0005) {
      lastProgress = progress;
      story.style.setProperty('--journey', progress.toFixed(4));
      story.style.setProperty('--night', clamp((progress - .62) / .3).toFixed(4));
      story.style.setProperty('--tree-scale', (.68 + clamp(progress / .6) * .32).toFixed(4));
      story.style.setProperty('--book-opacity', (1 - clamp((progress - .24) / .15)).toFixed(4));
      story.style.setProperty('--tree-opacity', clamp((progress - .15) / .18).toFixed(4));
      story.style.setProperty('--moon-opacity', clamp((progress - .65) / .22).toFixed(4));
      document.getElementById('story-progress-fill').style.transform = `scaleX(${progress})`;
      steps.forEach((step, index) => {
        const center = [.10, .48, .87][index];
        const opacity = 1 - clamp((Math.abs(progress - center) - .13) / .12);
        step.style.setProperty('--step-opacity', opacity.toFixed(4));
        step.style.setProperty('--step-y', `${(center - progress) * 36}px`);
      });
      svgs.filter(svg => story.contains(svg)).forEach(svg => pose(svg, progress));
    }
    if (hero) {
      const h = hero.getBoundingClientRect();
      if (h.bottom > 0 && h.top < viewport) hero.style.setProperty('--hero-drift', `${clamp(-h.top / h.height) * 42}px`);
    }
    panels.forEach(panel => {
      const r = panel.getBoundingClientRect();
      if (r.bottom <= 0 || r.top >= viewport) return;
      const p = clamp((viewport - r.top) / (viewport + r.height));
      panel.style.setProperty('--art-drift', `${(p - .5) * 32}px`);
      panel.querySelectorAll('[data-scrub]').forEach(svg => pose(svg, p));
    });
  }
  function requestUpdate() {
    if (enabled && !frame && !document.hidden) frame = requestAnimationFrame(update);
  }
  function configure() {
    enabled = supported && !media.matches && !optedOut;
    document.documentElement.classList.toggle('motion-enabled', enabled);
    document.documentElement.classList.toggle('motion-disabled', !enabled);
    toggle.textContent = media.matches ? 'Reduced motion' : enabled ? 'Motion on' : 'Motion off';
    toggle.setAttribute('aria-pressed', String(enabled));
    toggle.disabled = !supported || media.matches;
    toggle.title = media.matches ? 'Following your device’s reduced-motion preference.' : 'Turn scroll animation on or off.';
    lastProgress = -1;
    if (!enabled) {
      if (frame) cancelAnimationFrame(frame);
      frame = 0;
      svgs.forEach(svg => pose(svg, .28));
    } else requestUpdate();
  }
  toggle.addEventListener('click', () => {
    optedOut = !optedOut;
    try { localStorage.setItem('sproutbook-showcase-motion', optedOut ? 'off' : 'on'); } catch {}
    configure();
  });
  window.addEventListener('scroll', requestUpdate, {passive: true});
  window.addEventListener('resize', requestUpdate, {passive: true});
  document.addEventListener('visibilitychange', () => {
    if (document.hidden && frame) { cancelAnimationFrame(frame); frame = 0; }
    else requestUpdate();
  });
  media.addEventListener('change', configure);
  configure();
})();
