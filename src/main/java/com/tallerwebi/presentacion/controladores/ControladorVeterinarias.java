package com.tallerwebi.presentacion.controladores;

import com.tallerwebi.dominio.entidades.Especialidad;
import com.tallerwebi.dominio.entidades.Veterinaria;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorVeterinarias {

  @RequestMapping(path = "/veterinarias", method = RequestMethod.GET)
  public ModelAndView listarVeterinarias() {
    List<Veterinaria> veterinarias = obtenerVeterinariasHardcodeadas();

    Map<String, Object> modelo = new ModelMap();
    modelo.put("veterinarias", veterinarias);
    modelo.put("sinPrestadores", veterinarias.isEmpty());

    return new ModelAndView("veterinarias", modelo);
  }

  private List<Veterinaria> obtenerVeterinariasHardcodeadas() {
    List<Veterinaria> lista = new ArrayList<>();
    /* Harcodeado con mock hecho por ia (eliminar despues) */
    lista.add(
      new Veterinaria(
        1,
        "Veterinaria San Roque",
        "Mitre 450, San Justo",
        "4444-1111",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        2,
        "Pet Care Norte",
        "Av. Rivadavia 1200, Ramos Mejía",
        "4444-2222",
        false,
        List.of(Especialidad.CIRUGIA_CARDIACA)
      )
    );

    lista.add(
      new Veterinaria(
        3,
        "Clínica Animal Sur",
        "Brigadier López 890",
        "4444-3333",
        true,
        List.of(Especialidad.CIRUGIA_ORTOPEDICA, Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        4,
        "Centro Veterinario Casanova",
        "República de Portugal 2300, Isidro Casanova",
        "4485-1234",
        false,
        List.of(Especialidad.CIRUGIA_PLASTICA, Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        5,
        "Hospital Veterinario La Matanza",
        "Av. Illia 2500, San Justo",
        "4484-9876",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_CARDIACA, Especialidad.CIRUGIA_NEUROSICA)
      )
    );

    lista.add(
      new Veterinaria(
        6,
        "Patitas Felices",
        "Almirante Brown 600, Morón",
        "4629-5555",
        false,
        List.of(Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        7,
        "Veterinaria El Arca",
        "Av. de Mayo 1400, Ramos Mejía",
        "4658-3210",
        true,
        List.of(Especialidad.CIRUGIA_ESTETICA, Especialidad.CIRUGIA_PLASTICA)
      )
    );

    lista.add(
      new Veterinaria(
        8,
        "Clínica Vet Haedo",
        "Fasola 320, Haedo",
        "4659-1122",
        false,
        List.of(Especialidad.CIRUGIA_ORTOPEDICA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        9,
        "Centro Integral Animal",
        "Brandsen 1100, Ituzaingó",
        "4661-8899",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        10,
        "Huellas y Colas",
        "Arieta 2800, San Justo",
        "4441-2345",
        false,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        11,
        "Veterinaria Castelar",
        "Carlos Casares 950, Castelar",
        "4627-7744",
        true,
        List.of(Especialidad.CIRUGIA_CARDIACA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        12,
        "VetLife Morón",
        "Nuestra Señora del Buen Viaje 800, Morón",
        "4628-9900",
        false,
        List.of(Especialidad.CIRUGIA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        13,
        "Amigos Fieles",
        "Luro 5500, Gregorio de Laferrere",
        "4626-3333",
        true,
        List.of(Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    lista.add(
      new Veterinaria(
        14,
        "Clínica Veterinaria Lomas",
        "Av. San Martín 3200, Lomas del Mirador",
        "4699-5566",
        false,
        List.of(Especialidad.CIRUGIA_CARDIACA, Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        15,
        "Vital Pet",
        "Av. Crovara 1200, La Tablada",
        "4652-7788",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        16,
        "Veterinaria Los Álamos",
        "Ruta 3 Km 22, Isidro Casanova",
        "4625-1212",
        false,
        List.of(Especialidad.CIRUGIA_PLASTICA)
      )
    );

    lista.add(
      new Veterinaria(
        17,
        "Centro de Especialidades Veterinarias",
        "Pueyrredón 400, Ramos Mejía",
        "4654-2233",
        true,
        List.of(
          Especialidad.CIRUGIA_NEUROSICA,
          Especialidad.CIRUGIA_CARDIACA,
          Especialidad.CIRUGIA_TRAUMATOLOGICA
        )
      )
    );

    lista.add(
      new Veterinaria(
        18,
        "Mundo Animal",
        "Av. Gaona 2100, Ciudadela",
        "4653-4411",
        false,
        List.of(Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        19,
        "Hospital Veterinario Oeste",
        "Santa Rosa 1500, Castelar",
        "4623-8877",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    lista.add(
      new Veterinaria(
        20,
        "Veterinaria San Martín",
        "San Martín 4500, Florida",
        "4760-5555",
        false,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_PLASTICA)
      )
    );

    lista.add(
      new Veterinaria(
        21,
        "Clínica Mascotas",
        "Av. Maipú 2300, Olivos",
        "4799-2211",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        22,
        "Veterinaria del Bosque",
        "Av. Márquez 1100, San Isidro",
        "4743-1122",
        false,
        List.of(Especialidad.CIRUGIA_CARDIACA, Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        23,
        "Centro Vet Pasteur",
        "Pasteur 600, Martínez",
        "4792-8833",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_CARDIACA)
      )
    );

    lista.add(
      new Veterinaria(
        24,
        "Amor de Huellas",
        "Av. Centenario 850, San Isidro",
        "4747-9900",
        false,
        List.of(Especialidad.CIRUGIA_ORTOPEDICA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        25,
        "Veterinaria Belgrano",
        "Av. Cabildo 3100, CABA",
        "4701-3344",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_NEUROSICA)
      )
    );

    lista.add(
      new Veterinaria(
        26,
        "Clínica Animal Palermo",
        "Scalabrini Ortiz 1500, CABA",
        "4832-7766",
        false,
        List.of(Especialidad.CIRUGIA_PLASTICA, Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        27,
        "Hospital Veterinario Central",
        "Av. Córdoba 4200, CABA",
        "4864-1111",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_NEUROSICA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        28,
        "Vet Center Caballito",
        "Av. Rivadavia 5400, CABA",
        "4901-5555",
        false,
        List.of(Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA_CARDIACA)
      )
    );

    lista.add(
      new Veterinaria(
        29,
        "Veterinaria Flores",
        "Av. Carabobo 200, CABA",
        "4631-8888",
        true,
        List.of(Especialidad.CIRUGIA_PLASTICA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    lista.add(
      new Veterinaria(
        30,
        "Peludos y Cía",
        "Av. San Juan 3200, CABA",
        "4932-4411",
        false,
        List.of(Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        31,
        "Clínica Vet del Sur",
        "Av. Mitre 1500, Avellaneda",
        "4201-9999",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        32,
        "Veterinaria Lanús",
        "Av. 9 de Julio 1200, Lanús",
        "4241-7733",
        false,
        List.of(Especialidad.CIRUGIA_ORTOPEDICA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        33,
        "Centro Mascotero Quilmes",
        "Peatonal Rivadavia 200, Quilmes",
        "4253-1122",
        true,
        List.of(Especialidad.CIRUGIA_ESTETICA, Especialidad.CIRUGIA_CARDIACA)
      )
    );

    lista.add(
      new Veterinaria(
        34,
        "Hospital Animal Bernal",
        "Av. San Martín 800, Bernal",
        "4251-5588",
        false,
        List.of(Especialidad.CIRUGIA_PLASTICA, Especialidad.CIRUGIA_NEUROSICA)
      )
    );

    lista.add(
      new Veterinaria(
        35,
        "Veterinaria Berazategui",
        "Av. 14 4500, Berazategui",
        "4256-3344",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        36,
        "Vet Solano",
        "Av. 844 2100, San Francisco Solano",
        "4271-9988",
        false,
        List.of(Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        37,
        "Clínica Animal Varela",
        "Av. San Martín 3000, Florencio Varela",
        "4237-1111",
        true,
        List.of(Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    lista.add(
      new Veterinaria(
        38,
        "Veterinaria Canning",
        "Mariano Castex 1200, Canning",
        "4295-6677",
        false,
        List.of(Especialidad.CIRUGIA_CARDIACA, Especialidad.CIRUGIA_PLASTICA)
      )
    );

    lista.add(
      new Veterinaria(
        39,
        "Centro Vet Lomas",
        "Boedo 500, Lomas de Zamora",
        "4243-2211",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        40,
        "Hospital de Mascotas Banfield",
        "Maipú 800, Banfield",
        "4202-5544",
        false,
        List.of(Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA_ESTETICA)
      )
    );

    lista.add(
      new Veterinaria(
        41,
        "Veterinaria Temperley",
        "Meeks 1100, Temperley",
        "4244-8899",
        true,
        List.of(Especialidad.CIRUGIA_NEUROSICA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    lista.add(
      new Veterinaria(
        42,
        "Sanidad Animal Adrogué",
        "Spiro 900, Adrogué",
        "4293-1122",
        false,
        List.of(Especialidad.CIRUGIA_PLASTICA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        43,
        "Veterinaria Burzaco",
        "Espora 2500, Burzaco",
        "4299-5566",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_CARDIACA)
      )
    );

    lista.add(
      new Veterinaria(
        44,
        "Vet Center Ezeiza",
        "Av. French 300, Ezeiza",
        "4295-1111",
        false,
        List.of(Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        45,
        "Clínica Vet Monte Grande",
        "Dorrego 150, Monte Grande",
        "4290-7788",
        true,
        List.of(Especialidad.CIRUGIA_ESTETICA, Especialidad.CIRUGIA_TRAUMATOLOGICA)
      )
    );

    lista.add(
      new Veterinaria(
        46,
        "Veterinaria San Vicente",
        "Sarmiento 1000, San Vicente",
        "02225-421111",
        false,
        List.of(Especialidad.CIRUGIA, Especialidad.TRAUMATOLOGIA)
      )
    );

    lista.add(
      new Veterinaria(
        47,
        "Centro Integral Canino",
        "Ruta 205 Km 35, Carlos Spegazzini",
        "4295-8833",
        true,
        List.of(Especialidad.TRAUMATOLOGIA, Especialidad.CIRUGIA)
      )
    );

    lista.add(
      new Veterinaria(
        48,
        "Hospital Animal La Plata",
        "Calle 7 1500, La Plata",
        "0221-4223344",
        true,
        List.of(
          Especialidad.CIRUGIA_NEUROSICA,
          Especialidad.CIRUGIA_ORTOPEDICA,
          Especialidad.CIRUGIA_CARDIACA
        )
      )
    );

    lista.add(
      new Veterinaria(
        49,
        "Veterinaria City Bell",
        "Camino Centenario 2000, City Bell",
        "0221-4801122",
        false,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_PLASTICA)
      )
    );

    lista.add(
      new Veterinaria(
        50,
        "Vet Los Hornos",
        "Av. 66 3000, Los Hornos",
        "0221-4509988",
        true,
        List.of(Especialidad.CIRUGIA, Especialidad.CIRUGIA_ORTOPEDICA)
      )
    );

    return lista;
  }
}
