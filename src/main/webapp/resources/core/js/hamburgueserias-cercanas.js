import {
  obtenerUbicacion,
  obtenerHamburgueseriasCercanas,
} from "./hamburgueserias-cercanas_funciones";

document.addEventListener("DOMContentLoaded", async function () {
  try {
    const ubicacion = await obtenerUbicacion();
    const hamburgueserias = await obtenerHamburgueseriasCercanas(
      ubicacion.latitude,
      ubicacion.longitude
    );
    renderizarHamburgueserias(hamburgueserias);
  } catch (error) {
    renderizarError("No se pudo obtener la ubicación o las hamburgueserías.");
  }
});

function renderizarHamburgueserias(hamburgueserias) {
  const container = document.querySelector(".container ul.list-group");
  if (!container) return;
  // Elimina los items existentes excepto el título
  container.querySelectorAll("li:not(.active)").forEach((li) => li.remove());
  if (hamburgueserias.length === 0) {
    const li = document.createElement("li");
    li.className = "list-group-item";
    li.textContent = "No se encontraron hamburgueserías cercanas.";
    container.appendChild(li);
    return;
  }
  hamburgueserias.forEach((h) => {
    const li = document.createElement("li");
    li.className = "list-group-item";
    li.innerHTML = `<strong>${h.nombre}</strong><br>Puntuación: ${h.puntuacion}<br>Latitud: ${h.latitud} | Longitud: ${h.longitud}`;
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
