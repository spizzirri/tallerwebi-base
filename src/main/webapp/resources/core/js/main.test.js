/**
DOCU DE JASMINE: https://jasmine.github.io/api/3.10/index.html
**/

describe("Main", function() {
  it("debe devoler 2 cuando envio 1 y 1", function() {
    expect(suma(1, 1)).toBe(2);
  });

  it("debe devoler 4 cuando envio 2 y 2", function() {
    expect(multiplicar(2, 2)).toBe(4);
  });
});