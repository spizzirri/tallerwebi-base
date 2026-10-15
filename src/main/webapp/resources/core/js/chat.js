// Convierte markdown del bot a HTML seguro
function htmlDesdeMarkdown(texto) {
  let html = marked.parse(texto, { breaks: true }); // https://marked.js.org/
  return DOMPurify.sanitize(html); // https://github.com/cure53/DOMPurify
}

// Mensaje del usuario: solo texto, sin HTML
function mostrarMensajeUsuario(contenedor, texto) {
  let parrafo = document.createElement("p");
  parrafo.textContent = "Vos: " + texto;
  contenedor.appendChild(parrafo);
  parrafo.className = "chat-mensaje-usuario";
  contenedor.scrollTop = contenedor.scrollHeight;
}

// Mensaje del asistente: con markdown
function mostrarMensajeAsistente(contenedor, texto) {
  let parrafo = document.createElement("div");
  parrafo.innerHTML = "<strong>Asistente:</strong> " + htmlDesdeMarkdown(texto);
  parrafo.className = "chat-mensaje-asistente";
  contenedor.appendChild(parrafo);
  contenedor.scrollTop = contenedor.scrollHeight;
}

function inicializarChat() {
  let form = document.getElementById("form-chat");
  if (!form) return;

  let url = form.dataset.endpoint;
  let contenedor = document.getElementById("mensajes");
  let input = document.getElementById("input-mensaje");

  form.addEventListener("submit", function (evento) {
    evento.preventDefault();

    let texto = input.value.trim();
    if (!texto) return;

    mostrarMensajeUsuario(contenedor, texto);
    input.value = "";

    let body = new URLSearchParams({ mensaje: texto });

    // llamamos a al api de gemini en java
    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: body,
      credentials: "same-origin",
    })
      .then(function (res) {
        if (!res.ok) throw new Error("Error " + res.status);
        return res.json();
      })
      .then(function (data) {
        mostrarMensajeAsistente(contenedor, data.respuesta);
      })
      .catch(function () {
        mostrarMensajeUsuario(contenedor, "(no se pudo enviar el mensaje)");
      });
  });
}

document.addEventListener("DOMContentLoaded", inicializarChat);
