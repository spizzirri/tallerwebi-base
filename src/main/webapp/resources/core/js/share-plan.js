document.addEventListener('click', (event) => {
  const button = event.target.closest('[data-share-plan]');
  if (!button) return;
  event.preventDefault();
  const url = button.dataset.shareUrl;
  if (!url) return;
  // La URL publica sale de <meta name="public-base-url"> (PUBLIC_BASE_URL en el servidor):
  // detras de ngrok el link que hay que compartir no es el host que ve el navegador.
  const base =
    document.querySelector('meta[name="public-base-url"]')?.content || window.location.origin;
  const absoluteUrl = new URL(url, base).href;
  // Solo la URL: con title o text en el payload, WhatsApp y compañía concatenan ese texto
  // antes del link en el mensaje. La descripcion del plan ya la muestra la tarjeta OG del
  // preview, asi que cualquier texto extra es redundancia.
  if (navigator.share) {
    navigator.share({ url: absoluteUrl }).catch((error) => {
      if (error.name !== 'AbortError') copyLink(absoluteUrl, button);
    });
    return;
  }
  copyLink(absoluteUrl, button);
});

async function copyLink(url, button) {
  try {
    await navigator.clipboard.writeText(url);
  } catch {
    const input = document.createElement('input');
    input.value = url;
    input.style.position = 'fixed';
    input.style.opacity = '0';
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    input.remove();
  }
  const original = button.textContent;
  button.textContent = 'Link copiado';
  setTimeout(() => {
    button.textContent = original;
  }, 2000);
}
