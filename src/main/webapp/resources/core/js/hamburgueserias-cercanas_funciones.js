export function obtenerUbicacion() {
  return new Promise((resolve, reject) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        function (position) {
          const { latitude, longitude } = position.coords;
          resolve({ latitude, longitude });
        },
        function (error) {
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        }
      );
    } else {
      reject(new Error("Geolocalización no soportada por este navegador"));
    }
  });
}

export async function obtenerHamburgueseriasCercanas(latitud, longitud) {
  console.log("Latitud:", latitud, "Longitud:", longitud);
  const url = `/spring/hamburgueserias-cercanas/${latitud}/${longitud}/lista`;
  console.log("URL de la API:", url);
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error("Error al obtener hamburgueserías");
    }
    return await response.json();
  } catch (error) {
    console.error(error);
    return [];
  }
}

export async function renderizarHamburgueserias() {
  try {
    const ubicacion = await obtenerUbicacion();
    const hamburgueserias = await obtenerHamburgueseriasCercanas(
      ubicacion.latitude,
      ubicacion.longitude
    );

    const container = document.querySelector("#hamburgueserias-list");
    if (!container) return;

    // Elimina los items existentes excepto el título
    container.querySelectorAll("li:not(.active)").forEach((li) => li.remove());
    console.log("length", hamburgueserias.length);

    if (hamburgueserias.length === 0) {
      const li = document.createElement("li");
      li.className = "list-group-item";
      li.textContent = "No se encontraron hamburgueserías cercanas.";
      container.appendChild(li);
      return;
    }

    hamburgueserias.forEach((h) => {
      const li = document.createElement("li");
      li.className = "list-group-item item-hamburgueseria";
      li.innerHTML = `<strong>${h.nombre}</strong><br>Puntuación: ${
        h.puntuacion
      }<br>Dirección: ${h.direccion}<br>Es comercio adherido: ${
        h.esComercioAdherido ? "Si" : "No"
      }<br>Latitud: ${h.latitud} | Longitud: ${
        h.longitud
      }<br><a href="" target="_blank">Ver mas</a>`;
      container.appendChild(li);
    });
  } catch (error) {
    console.error("Error al renderizar hamburgueserías:", error);
    renderizarError("Error al obtener ubicación: " + error.message);
  }
}

export function renderizarError(mensaje) {
  const container = document.querySelector(".container ul.list-group");
  if (!container) return;
  container.querySelectorAll("li:not(.active)").forEach((li) => li.remove());
  const li = document.createElement("li");
  li.className = "list-group-item item-hamburgueseria list-group-item-danger";
  li.textContent = mensaje;
  container.appendChild(li);

  // Hide loader when error is displayed
  if (window.LoaderUtils) {
    window.LoaderUtils.hide();
  }
}
