import { limitarPorPuntosDeCodigo } from "./comentarios_funciones.js";

const textarea = document.getElementById("contenido");

if (textarea !== null) {
  const maximo = Number(textarea.dataset.maxCodePoints);
  textarea.addEventListener("input", () => {
    const contenidoLimitado = limitarPorPuntosDeCodigo(textarea.value, maximo);
    if (textarea.value !== contenidoLimitado) {
      textarea.value = contenidoLimitado;
    }
  });
}
