const agregarLinea = (contenedor, quien, texto) => {
  const p = document.createElement("p");
  p.textContent = quien + ": " + texto;
  contenedor.appendChild(p);
  contenedor.scrollTop = contenedor.scrollHeight;
};

const inicializarChat = () => {
  const form = document.getElementById("form-chat");
  if (!form) return;

  const urlMensaje = form.dataset.endpoint;
  const contenedor = document.getElementById("mensajes");
  const input = document.getElementById("input-mensaje");
  if (!urlMensaje || !contenedor || !input) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const texto = input.value.trim();
    if (!texto) return;

    agregarLinea(contenedor, "Vos", texto);
    input.value = "";

    const body = new URLSearchParams({ mensaje: texto });
    const res = await fetch(urlMensaje, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
      credentials: "same-origin",
    });

    if (!res.ok) {
      agregarLinea(contenedor, "Sistema", "Error " + res.status);
      return;
    }

    const data = await res.json();
    agregarLinea(contenedor, "Asistente", data.respuesta);
  });
};

document.addEventListener("DOMContentLoaded", inicializarChat);
