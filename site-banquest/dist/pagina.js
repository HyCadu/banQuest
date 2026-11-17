(() => {
  "use strict";

  const chapters = [
    { name: "Trilha", title: ["Seu estudo,", "em modo jogo."], copy: "Aprenda Banco de Dados em lições curtas, encare questões de provas anteriores e ganhe XP com um guaiamum do Espírito Santo." },
    { name: "Lição", title: ["Um conceito,", "uma jogada."], copy: "Leia o conceito, pratique e confira a explicação. Nas questões de prova, o cronômetro ajuda você a descobrir o seu ritmo." },
    { name: "Revisão", title: ["Errou hoje?", "Pratique de novo."], copy: "Na aba Praticar, os erros das lições voltam para revisão. Retome o assunto no seu tempo, inclusive sem internet depois de entrar no app." }
  ];
  const $ = selector => document.querySelector(selector);
  const stops = [...document.querySelectorAll("[data-stop]")];
  const shots = [...document.querySelectorAll("[data-screen]")];
  const journey = $("#tour");
  const stage = $(".stage");
  const lightbox = $("#lightbox");
  const motion = matchMedia("(prefers-reduced-motion: reduce)");
  let active = -1;
  let pending = false;
  let stageHeight = 0;
  let draggedFrom = null;
  let handledSwipe = false;
  let destination = null;

  function selectChapter(index) {
    if (index === active) return;
    active = index;
    const chapter = chapters[index];
    const emphasis = document.createElement("em");
    emphasis.textContent = chapter.title[1];
    $("#chapter-title").replaceChildren(chapter.title[0], document.createElement("br"), emphasis);
    $("#chapter-copy").textContent = chapter.copy;
    shots.forEach((shot, i) => { shot.hidden = i !== index; });
    stops.forEach((stop, i) => stop.setAttribute("aria-pressed", String(index === i)));
    $("#phone").setAttribute("aria-label", "Ampliar tela de " + chapter.name);
    $("#phone").classList.add("screen-change");
    if (!motion.matches) {
      const elements = [$("#chapter-title"), $("#chapter-copy")];
      for (const element of elements) {
        element.getAnimations().forEach(animation => animation.cancel());
        element.animate([{ opacity: 0.5, transform: "translateY(8px)" }, { opacity: 1, transform: "none" }], { duration: 220 });
      }
    }
  }

  function renderScroll() {
    pending = false;
    if (motion.matches || lightbox.open) return;
    const travel = journey.offsetHeight - stageHeight;
    const offset = Math.min(1, Math.max(0, (-journey.getBoundingClientRect().top + Math.min(0, innerHeight - stageHeight)) / Math.max(1, travel)));
    stage.style.setProperty("--track", offset.toFixed(4));
    stage.style.setProperty("--camera", (-11 * Math.sin(offset * Math.PI)).toFixed(2) + "deg");
    stage.style.setProperty("--pan", (Math.sin(offset * Math.PI * 2) * 15).toFixed(2) + "px");
    stage.style.setProperty("--hop", (-Math.abs(Math.sin(offset * 2 * Math.PI)) * 30).toFixed(2) + "px");
    if (destination !== null && Math.abs(window.scrollY - destination) > 2) return;
    destination = null;
    selectChapter(Math.min(2, Math.floor(offset * 3)));
  }
  function schedule() {
    if (!pending) { pending = true; requestAnimationFrame(renderScroll); }
  }
  function measure() {
    stageHeight = stage.offsetHeight;
    journey.style.setProperty("--stage-height", stageHeight + "px");
    schedule();
  }
  function jump(index) {
    index = Math.max(0, Math.min(2, index));
    selectChapter(index);
    if (motion.matches) return;
    const start = journey.getBoundingClientRect().top + scrollY - Math.min(0, innerHeight - stageHeight);
    const travel = journey.offsetHeight - stageHeight;
    destination = Math.round(start + (index === 0 ? 0 : (index + 0.5) / 3) * travel);
    window.scrollTo({ top: destination, behavior: "instant" });
    schedule();
  }
  stops.forEach((stop, index) => {
    stop.addEventListener("click", () => jump(index));
    stop.addEventListener("keydown", e => {
      if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(e.key)) return;
      e.preventDefault();
      const next = e.key === "Home" ? 0 : e.key === "End" ? 2 : (index + (e.key === "ArrowRight" ? 1 : 2)) % 3;
      jump(next);
      stops[next].focus({ preventScroll: true });
    });
  });
  function openScreen() {
    if (handledSwipe) { handledSwipe = false; return; }
    $("#full-screen").src = shots[active].src;
    $("#full-screen").alt = shots[active].alt;
    $("#lightbox-title").textContent = "BanQuest · " + chapters[active].name;
    lightbox.showModal();
  }
  const showcase = $(".showcase");
  showcase.addEventListener("pointerdown", e => { handledSwipe = false; draggedFrom = [e.clientX, e.clientY]; });
  showcase.addEventListener("pointerup", e => {
    if (!draggedFrom) return;
    const dx = e.clientX - draggedFrom[0], dy = e.clientY - draggedFrom[1];
    draggedFrom = null;
    if (Math.abs(dx) > 38 && Math.abs(dx) > Math.abs(dy) * 1.4) {
      handledSwipe = true;
      jump(active + (dx < 0 ? 1 : -1));
    }
  });
  showcase.addEventListener("pointercancel", () => { draggedFrom = null; });
  $("#phone").addEventListener("click", openScreen);
  $("#enlarge").addEventListener("click", openScreen);
  $("#dismiss").addEventListener("click", () => lightbox.close());
  lightbox.addEventListener("click", e => { if (e.target === lightbox) lightbox.close(); });
  lightbox.addEventListener("close", schedule);
  window.addEventListener("scroll", schedule, { passive: true });
  window.addEventListener("wheel", () => { destination = null; }, { passive: true });
  function updateMode() {
    journey.classList.toggle("enhanced", !motion.matches);
    $(".scroll-cue").lastChild.textContent = motion.matches ? " Escolha uma tela do app" : " Role para conhecer o app";
    destination = null;
    measure();
  }
  selectChapter(0);
  updateMode();
  motion.addEventListener("change", updateMode);
  if (window.ResizeObserver) new ResizeObserver(measure).observe(stage);
  window.addEventListener("resize", measure);
  document.fonts.ready.then(measure);

  const { stem, options, answer, explanation } = window.BANQUEST_EXERCISE;
  $("#question").textContent = stem;
  const answers = options.map((label, i) => {
    const button = document.createElement("button");
    button.textContent = label;
    button.addEventListener("click", () => {
      answers.forEach((item, n) => { item.disabled = true; item.classList.toggle("right", n === answer); });
      if (i !== answer) button.classList.add("wrong");
      $(".feedback").textContent = (i === answer ? "Acertou! " : "Resposta correta: " + options[answer] + ". ") + explanation;
      $(".retry").hidden = false;
    });
    $("#answers").append(button);
    return button;
  });
  $(".retry").addEventListener("click", () => {
    answers.forEach(button => { button.disabled = false; button.classList.remove("wrong", "right"); });
    $(".feedback").textContent = "";
    $(".retry").hidden = true;
    answers[0].focus();
  });

  function updateCountdown() {
    const date = new Intl.DateTimeFormat("sv-SE", { timeZone: "America/Sao_Paulo", year:"numeric", month:"2-digit", day:"2-digit" }).format(new Date());
    const days = Math.round((Date.UTC(2027, 1, 21) - Date.parse(date + "T00:00:00Z")) / 86400000);
    $("#countdown").textContent = days < 0 ? "Prova encerrada" : days === 0 ? "A prova é hoje" : days === 1 ? "Falta 1 dia" : "Faltam " + days + " dias";
  }
  updateCountdown();
  document.addEventListener("visibilitychange", () => { if (!document.hidden) updateCountdown(); });
  setInterval(updateCountdown, 60000);

  const links = [...document.querySelectorAll("[data-apk]")];
  function https(value) {
    try { const url = new URL(value); return url.protocol === "https:" && !url.username && !url.password ? url : null; } catch { return null; }
  }
  function setDownload(url) { links.forEach(link => { link.href = url.href; }); }
  async function resolveRelease() {
    const override = https(window.BANQUEST_APK_URL);
    if (override) { setDownload(override); return; }
    const { origin, publishableKey } = window.BANQUEST_RELEASES;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 4000);
    try {
      const rpc = await fetch(origin + "/rest/v1/rpc/latest_release", {
        method: "POST", headers: { apikey: publishableKey, "Content-Type": "application/json" },
        body: "{}", signal: controller.signal, credentials: "omit", referrerPolicy: "no-referrer"
      });
      if (!rpc.ok) return;
      const release = await rpc.json();
      const url = https(release?.apk_url);
      if (url && url.origin === origin && url.pathname.startsWith("/storage/v1/object/public/apk/") && url.pathname.endsWith(".apk")) setDownload(url);
    } catch { /* Os links no HTML já apontam para o APK publicado. */ }
    finally { clearTimeout(timeout); }
  }
  resolveRelease();
})();
