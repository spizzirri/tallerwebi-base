/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { validarCamposDeLogin } from "../login_funciones.js";

describe("Login Funciones", function() {
  it("debe devolver true cuando el email y la contraseña son validos", function() {
    expect(validarCamposDeLogin("test@unlam.edu.ar", "test")).toBe(true);
  });

  it("debe devolver false cuando el email es 'test@unlam.edu.ar' y la contraseña es ''", function() {
    expect(validarCamposDeLogin("test@unlam.edu.ar", "")).toBe(false);
  });

  it("debe devolver false cuando el email es '' y la contraseña es 'test'", function() {
    expect(validarCamposDeLogin("", "test")).toBe(false);
  });
  
  it("debe devolver false cuando el email es '' y la contraseña es ''", function() {
    expect(validarCamposDeLogin("", "")).toBe(false);
  });

  it("debe devolver false cuando el email es null y la contraseña es null", function() {
    expect(validarCamposDeLogin(null, null)).toBe(false);
  });
  
  it("debe devolver false cuando el email es null y la contraseña es 'test'", function() {
    expect(validarCamposDeLogin(null, "test")).toBe(false);
  });

  it("debe devolver false cuando el email es 'test@unlam.edu.ar' y la contraseña es null", function() {
    expect(validarCamposDeLogin("test@unlam.edu.ar", null)).toBe(false);
  });
  
  it("debe devolver false cuando el email es '' y la contraseña es null", function() {
    expect(validarCamposDeLogin("", null)).toBe(false);
  });  
});