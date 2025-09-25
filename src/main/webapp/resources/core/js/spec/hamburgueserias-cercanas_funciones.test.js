import { obtenerUbicacion } from "../home_funciones.js";

describe("Obtener ubicación", function () {
  let originalGeolocation;

  beforeEach(function () {
    originalGeolocation = navigator.geolocation;

    Object.defineProperty(navigator, "geolocation", {
      value: {
        getCurrentPosition: jasmine.createSpy("getCurrentPosition"),
      },
      configurable: true,
    });
  });

  afterEach(function () {
    if (originalGeolocation) {
      Object.defineProperty(navigator, "geolocation", {
        value: originalGeolocation,
        configurable: true,
      });
    }
  });

  it("debe obtener la ubicación exitosamente", async function () {
    const mockPosition = {
      coords: {
        latitude: -34.6037,
        longitude: -58.3816,
      },
    };

    navigator.geolocation.getCurrentPosition.and.callFake(function (success) {
      success(mockPosition);
    });

    const resultado = await obtenerUbicacion();
    expect(navigator.geolocation.getCurrentPosition).toHaveBeenCalled();
    expect(resultado.latitude).toBe(-34.6037);
    expect(resultado.longitude).toBe(-58.3816);
  });

  it("debe manejar error al obtener ubicación", async function () {
    const mockError = {
      code: 1,
      message: "Permisos denegados por el usuario",
    };

    navigator.geolocation.getCurrentPosition.and.callFake(function (
      success,
      error
    ) {
      error(mockError);
    });

    try {
      await obtenerUbicacion();
      fail("Se esperaba que la función rechazara la promesa");
    } catch (error) {
      expect(navigator.geolocation.getCurrentPosition).toHaveBeenCalled();
      expect(error.code).toBe(1);
      expect(error.message).toBe("Permisos denegados por el usuario");
    }
  });
});
