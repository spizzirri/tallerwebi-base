import { limitarPorPuntosDeCodigo } from "../comentarios_funciones.js";

describe("Comentarios Funciones", function() {
  it("debe limitar el contenido contando puntos de código Unicode", function() {
    const emoji = "\uD83D\uDE00";
    const contenido = emoji.repeat(2001);
    const contenidoLimitado = limitarPorPuntosDeCodigo(contenido, 2000);

    expect(Array.from(contenidoLimitado).length).toBe(2000);
    expect(contenidoLimitado).toBe(emoji.repeat(2000));
  });
});
