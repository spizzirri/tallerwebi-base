import {
  obtenerUbicacion,
  obtenerHamburgueseriasCercanas,
} from "./hamburgueserias-cercanas_funciones.js";

document.addEventListener("DOMContentLoaded", async function () {
  try {
    const ubicacion = await obtenerUbicacion();
    const hamburgueserias = await obtenerHamburgueseriasCercanas(
      ubicacion.latitude,
      ubicacion.longitude
    );
    renderizarHamburgueserias(hamburgueserias);
  } catch (error) {
    renderizarError(
      "No se pudo obtener la ubicación o las hamburgueser&iacute;as."
    );
  }
});

function renderizarHamburgueserias(hamburgueserias) {
  const container = document.querySelector(".container ul.list-group");
  if (!container) return;
  // Elimina los items existentes excepto el título
  container.querySelectorAll("li:not(.active)").forEach((li) => li.remove());
  console.log("length", hamburgueserias.length);
  if (hamburgueserias.length === 0) {
    const li = document.createElement("li");
    li.className = "list-group-item";
    li.textContent = "No se encontraron hamburgueser&iacute;as cercanas.";
    container.appendChild(li);
    return;
  }
  hamburgueserias.forEach((h) => {
    const li = document.createElement("li");
    li.className = "list-group-item";
    li.innerHTML = `<strong>${h.nombre}</strong><br>Puntuaci&oacute;n: ${h.puntuacion}<br>Latitud: ${h.latitud} | Longitud: ${h.longitud}`;
    container.appendChild(li);
  });
}

function renderizarError(mensaje) {
  const container = document.querySelector(".container ul.list-group");
  if (!container) return;
  container.querySelectorAll("li:not(.active)").forEach((li) => li.remove());
  const li = document.createElement("li");
  li.className = "list-group-item list-group-item-danger";
  li.textContent = mensaje;
  container.appendChild(li);
}
