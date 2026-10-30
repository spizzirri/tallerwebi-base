export function limitarPorPuntosDeCodigo(contenido, maximo) {
  return Array.from(contenido).slice(0, maximo).join("");
}
