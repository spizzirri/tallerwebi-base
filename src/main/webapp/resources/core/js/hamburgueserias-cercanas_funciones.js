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
  const url = `/spring//hamburgueserias-cercanas/${latitud}/${longitud}/lista`;
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
