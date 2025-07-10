package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import com.tallerwebi.presentacion.RequisitosJuegos;
import com.tallerwebi.presentacion.RequisitosProgramas;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.*;

// agregar transaccional
// agregar service
@Service("servicioArmaTuPc")
@Transactional
public class ServicioArmaTuPcImpl implements ServicioArmaTuPc {

    private RepositorioComponente repositorioComponente;
    private ServicioPrecios servicioPrecios;
    private ServicioCompatibilidades servicioCompatibilidades;
    private final RestTemplate restTemplate;
    private final String URLProgramas = "https://686ef83391e85fac429f6ce1.mockapi.io/programas";
    private final String URLJuegos = "https://686ef83391e85fac429f6ce1.mockapi.io/juegos";

    private final Map<String, String> correspondenciaDeVistaConTablasEnLaBD = new LinkedHashMap<>() {{
        put("procesador", "Procesador");
        put("motherboard", "Motherboard");
        put("cooler", "CoolerCPU");
        put("memoria", "MemoriaRAM");
        put("gpu", "PlacaDeVideo");
        put("almacenamiento", "Almacenamiento");
        put("fuente", "FuenteDeAlimentacion");
        put("gabinete", "Gabinete");
        put("monitor", "Monitor");
        put("periferico", "Periferico");
    }};



    @Autowired
    public ServicioArmaTuPcImpl(RepositorioComponente repositorioComponente, ServicioPrecios servicioPrecios , ServicioCompatibilidades servicioCompatibilidades, RestTemplate restTemplate) {
        this.repositorioComponente = repositorioComponente;
        this.servicioPrecios = servicioPrecios;
        this.servicioCompatibilidades = servicioCompatibilidades;
        this.restTemplate = restTemplate;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.repositorioComponente.obtenerComponentePorId(idComponente);
    }

    @Override
    public ComponenteDto obtenerComponenteDtoPorId(Long idComponente) {
        return new ComponenteDto(this.obtenerComponentePorId(idComponente));
    }

    @Override
    public List<ProductoCarritoDto> pasajeAProductoDtoParaAgregarAlCarrito(ArmadoPcDto armadoPcDto) {

        List<ProductoCarritoDto> productoCarritoDtos = new ArrayList<>();

        Map<Long, Integer> idDeComponentesDelArmadoYCantidades = armadoPcDto.getIdYCantidadComponentes();

        for(Map.Entry<Long, Integer> componente : idDeComponentesDelArmadoYCantidades.entrySet()){

            Componente componenteEntidad = this.repositorioComponente.obtenerComponentePorId(componente.getKey());
            ProductoCarritoDto productoCarritoDto = new ProductoCarritoDto(componenteEntidad , componente.getValue());
            productoCarritoDtos.add(productoCarritoDto);
        }

        return productoCarritoDtos;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesDto(String tipoComponente, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }


        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);

        return listaDeComponentesDto;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDto(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException {
        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);

        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente, nombreFiltro);

        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);

        return listaDeComponentesDto;
    }

    private List<ComponenteDto> transformarComponentesADtos(List<Componente> componentesDeTipo) {

        List<ComponenteDto> listaDeComponentesDto = new ArrayList<ComponenteDto>();

        for(Componente c : componentesDeTipo) listaDeComponentesDto.add(new ComponenteDto(c));

        return listaDeComponentesDto;
    }

    // LOS SWITCH DE AGREGAR Y QUITAR PODRIAN ESTAR EN UN SOLO METODO QUE DEPENDIENDO EL PARAMETRO DE AGREGAR O QUITAR EJECUTE LO CORRESPONDIENTE (esto para mantener un solo switch y que no sea tan engorroso)

    @Override
    public ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        Componente componenteSolicitado = obtenerComponentePorId(idComponente);

        seExcedeDeLimite(tipoComponente, cantidad, armadoPcDto);

        // determinar un metodo de componente que diga cuando un componente es compatible con el armado

        List<ComponenteDto> perifericosPrecargados = armadoPcDto.getPerifericos();
        ComponenteDto monitorPrecargado = armadoPcDto.getMonitor();

        ComponenteDto componenteSolicitadoDto = new ComponenteDto(componenteSolicitado);
        String precioFormateado = this.servicioPrecios.conversionDolarAPeso(componenteSolicitadoDto.getPrecio());
        componenteSolicitadoDto.setPrecioFormateado(precioFormateado);

        switch(tipoComponente.toLowerCase()){
            case "procesador":

                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setProcesador(componenteSolicitadoDto);
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);

                break;
            case "motherboard":

                ComponenteDto procesadorPrecargado = armadoPcDto.getProcesador();
                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);
                armadoPcDto.setProcesador(procesadorPrecargado);
                armadoPcDto.setMotherboard(componenteSolicitadoDto);

                break;
            case "cooler":
                armadoPcDto.setCooler(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);
                armadoPcDto.setGabinete(null);

                break;
            case "memoria":

                for(int i = 0; i<cantidad;i++) armadoPcDto.getRams().add(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            case "gpu":

                armadoPcDto.setGpu(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            case "almacenamiento":

                for(int i = 0; i<cantidad;i++) armadoPcDto.getAlmacenamiento().add(componenteSolicitadoDto);
                armadoPcDto.setFuente(null);

                break;
            case "fuente":

                armadoPcDto.setFuente(componenteSolicitadoDto);

                break;
            case "gabinete":

                armadoPcDto.setGabinete(componenteSolicitadoDto);

                break;
            case "monitor":
                armadoPcDto.setMonitor(componenteSolicitadoDto);
                break;
            case "periferico":
                armadoPcDto.getPerifericos().add(componenteSolicitadoDto);
                break;
        }

        return armadoPcDto;
    }

    @Override
    public ArmadoPcDto quitarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {

        if (!this.verificarExistenciaDeComponenteEnElArmadoDto(idComponente, armadoPcDto)) throw new QuitarComponenteInvalidoException();

        List<ComponenteDto> perifericosPrecargados = armadoPcDto.getPerifericos();
        ComponenteDto monitorPrecargado = armadoPcDto.getMonitor();

        switch(tipoComponente.toLowerCase()){
            case "procesador":

                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);

                break;

            case "motherboard":

                ComponenteDto procesadorPrecargado = armadoPcDto.getProcesador();
                armadoPcDto = new ArmadoPcDto();
                armadoPcDto.setPerifericos(perifericosPrecargados);
                armadoPcDto.setMonitor(monitorPrecargado);
                armadoPcDto.setProcesador(procesadorPrecargado);

                break;

            case "cooler":

                armadoPcDto.setCooler(null);
                armadoPcDto.setFuente(null);
                armadoPcDto.setGabinete(null);

                break;
            case "memoria":
                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getRams(), idComponente, cantidad);
                armadoPcDto.setFuente(null);
                break;
            case "gpu":
                armadoPcDto.setGpu(null);
                armadoPcDto.setFuente(null);
                break;
            case "almacenamiento":
                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getAlmacenamiento(), idComponente, cantidad);
                armadoPcDto.setFuente(null);
                break;
            case "fuente":
                armadoPcDto.setFuente(null);
                break;
            case "gabinete":
                armadoPcDto.setGabinete(null);
                break;
            case "monitor":
                armadoPcDto.setMonitor(null);
                break;
            case "periferico":
                this.eliminarComponenteDeLaListaDeDtosPorId(armadoPcDto.getPerifericos(), idComponente, cantidad);
                break;

        }

        return armadoPcDto;
    }

    private Boolean verificarExistenciaDeComponenteEnElArmadoDto(Long idComponente, ArmadoPcDto armadoPcDto) {

        List<ComponenteDto> componentesDelArmado = new ArrayList<>();

        componentesDelArmado.add(armadoPcDto.getProcesador());
        componentesDelArmado.add(armadoPcDto.getMotherboard());
        componentesDelArmado.add(armadoPcDto.getCooler());
        componentesDelArmado.add(armadoPcDto.getGpu());
        componentesDelArmado.add(armadoPcDto.getFuente());
        componentesDelArmado.add(armadoPcDto.getGabinete());
        componentesDelArmado.add(armadoPcDto.getMonitor());
        componentesDelArmado.addAll(armadoPcDto.getRams());
        componentesDelArmado.addAll(armadoPcDto.getAlmacenamiento());
        componentesDelArmado.addAll(armadoPcDto.getPerifericos());

        for(ComponenteDto c : componentesDelArmado) if(c != null && c.getId().equals(idComponente)) return true;

        return false;
    }

    private void eliminarComponenteDeLaListaDeDtosPorId(List<ComponenteDto> componentesDto, Long idComponente, Integer cantidad) throws QuitarStockDemasDeComponenteException {

        List<ComponenteDto> componentesAEliminar = new ArrayList<>();

        for(ComponenteDto componenteDto : componentesDto) if(componenteDto.getId().equals(idComponente) && componentesAEliminar.size() < cantidad) componentesAEliminar.add(componenteDto);

        if(componentesAEliminar.size() >= cantidad && !componentesAEliminar.isEmpty()) for(ComponenteDto componenteAEliminar : componentesAEliminar) componentesDto.remove(componenteAEliminar);

        else throw new QuitarStockDemasDeComponenteException();
    }


    private void seExcedeDeLimite(String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        if ((tipoComponente.equalsIgnoreCase("memoria") && armadoPcDto.getRams().size() + cantidad > 4) ||
                // verificar cantidad de slots de memoria de entidad y cantidad de cada tipo de almacenamiento(SATA y M.2)
                (tipoComponente.equalsIgnoreCase("almacenamiento") && armadoPcDto.getAlmacenamiento().size() + cantidad > 6) ||
                (tipoComponente.equalsIgnoreCase("periferico") && armadoPcDto.getPerifericos().size() + cantidad > 10)) {


            throw new LimiteDeComponenteSobrepasadoEnElArmadoException();
        }
    }

    @Override
    public Boolean sePuedeAgregarMasUnidades(String tipoComponente, ArmadoPcDto armadoPcDto) {

        switch (tipoComponente.toLowerCase()) {

            case "memoria":
                return armadoPcDto.getRams().size() < 4;

            case "almacenamiento":
                return armadoPcDto.getAlmacenamiento().size() < 6;

            case "periferico":
                return armadoPcDto.getPerifericos().size() < 10;

            case "procesador":
            case "motherboard":
            case "cooler":
            case "gpu":
            case "fuente":
            case "gabinete":
            case "monitor":
                return false;

            default:
                // Si no conocemos el tipo, por las dudas no permitimos más
                return false;
        }
    }

    @Override
    public Boolean armadoCompleto(ArmadoPcDto armadoPcDto) {
        return armadoPcDto.getMotherboard() != null
                && armadoPcDto.getProcesador() != null
                && armadoPcDto.getCooler() != null
                && armadoPcDto.getGabinete() != null;
    }


    @Override
    public Map<String, Double> obtenerMapaDeRequisitosMinimosSeleccionados(List<String> seleccionados) {
        RequisitosProgramas[] requisitosProgramas = restTemplate.getForObject(URLProgramas, RequisitosProgramas[].class);
        RequisitosJuegos[] requisitosJuegos = restTemplate.getForObject(URLJuegos, RequisitosJuegos[].class);
        List<String> programaSeleccionado = new ArrayList<>();
        List<String> juegoSeleccionado = new ArrayList<>();
        Map<String, Double> requisitosMinimosSeleccionados = new HashMap<>();
        for (String app : seleccionados) {
            for(RequisitosProgramas programa : requisitosProgramas) {
                if (app.equals(programa.getNombre())) {
                    programaSeleccionado.add(app);
                } else {
                    juegoSeleccionado.add(app);
                }
            }
        }
        //Comparacion para los Programas
        for (String programa : programaSeleccionado) {
            for (RequisitosProgramas requisitosPrograma : requisitosProgramas) {
                if (programa.equals(requisitosPrograma.getId())) {
                    //Comparacion Procesador AMD
                    if (requisitosMinimosSeleccionados.containsKey("ProcesadorAMD")) {
                        if (requisitosPrograma.getRequisitosMinimos().get("ProcesadorAMD") > requisitosMinimosSeleccionados.get("ProcesadorAMD")) {
                            requisitosMinimosSeleccionados.put("ProcesadorAMD", requisitosPrograma.getRequisitosMinimos().get("ProcesadorAMD"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("ProcesadorAMD", requisitosPrograma.getRequisitosMinimos().get("ProcesadorAMD"));
                    }
                    //Comparacion Procesador INTEL
                    if (requisitosMinimosSeleccionados.containsKey("ProcesadorINTEL")) {
                        if (requisitosPrograma.getRequisitosMinimos().get("ProcesadorINTEL") > requisitosMinimosSeleccionados.get("ProcesadorINTEL")) {
                            requisitosMinimosSeleccionados.put("ProcesadorINTEL", requisitosPrograma.getRequisitosMinimos().get("ProcesadorINTEL"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("ProcesadorINTEL", requisitosPrograma.getRequisitosMinimos().get("ProcesadorINTEL"));
                    }
                    //Comparacion RAM
                    if (requisitosMinimosSeleccionados.containsKey("RAM")) {
                        if (requisitosPrograma.getRequisitosMinimos().get("RAM") > requisitosMinimosSeleccionados.get("RAM")) {
                            requisitosMinimosSeleccionados.put("RAM", requisitosPrograma.getRequisitosMinimos().get("RAM"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("RAM", requisitosPrograma.getRequisitosMinimos().get("RAM"));
                    }
                    //Comparacion GPU
                    if (requisitosMinimosSeleccionados.containsKey("GPU")) {
                        if (requisitosPrograma.getRequisitosMinimos().get("GPU") > requisitosMinimosSeleccionados.get("GPU")) {
                            requisitosMinimosSeleccionados.put("GPU", requisitosPrograma.getRequisitosMinimos().get("GPU"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("GPU", requisitosPrograma.getRequisitosMinimos().get("GPU"));
                    }
                    //Comparacion Espacio Disco
                    if (requisitosMinimosSeleccionados.containsKey("EspacioDisco")) {
                        if (requisitosPrograma.getRequisitosMinimos().get("EspacioDisco") > requisitosMinimosSeleccionados.get("EspacioDisco")) {
                            requisitosMinimosSeleccionados.put("EspacioDisco", requisitosPrograma.getRequisitosMinimos().get("EspacioDisco"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("EspacioDisco", requisitosPrograma.getRequisitosMinimos().get("EspacioDisco"));
                    }
                }
            }
        }
        //Comparacion para los Juegos
        for (String juego : juegoSeleccionado) {
            for (RequisitosJuegos requisitosJuego : requisitosJuegos) {
                if (juego.equals(requisitosJuego.getId())) {
                    //Comparacion Procesador AMD
                    if (requisitosMinimosSeleccionados.containsKey("ProcesadorAMD")) {
                        if (requisitosJuego.getRequisitosMinimos().get("ProcesadorAMD") > requisitosMinimosSeleccionados.get("ProcesadorAMD")) {
                            requisitosMinimosSeleccionados.put("ProcesadorAMD", requisitosJuego.getRequisitosMinimos().get("ProcesadorAMD"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("ProcesadorAMD", requisitosJuego.getRequisitosMinimos().get("ProcesadorAMD"));
                    }
                    //Comparacion Procesador INTEL
                    if (requisitosMinimosSeleccionados.containsKey("ProcesadorINTEL")) {
                        if (requisitosJuego.getRequisitosMinimos().get("ProcesadorINTEL") > requisitosMinimosSeleccionados.get("ProcesadorINTEL")) {
                            requisitosMinimosSeleccionados.put("ProcesadorINTEL", requisitosJuego.getRequisitosMinimos().get("ProcesadorINTEL"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("ProcesadorINTEL", requisitosJuego.getRequisitosMinimos().get("ProcesadorINTEL"));
                    }
                    //Comparacion RAM
                    if (requisitosMinimosSeleccionados.containsKey("RAM")) {
                        if (requisitosJuego.getRequisitosMinimos().get("RAM") > requisitosMinimosSeleccionados.get("RAM")) {
                            requisitosMinimosSeleccionados.put("RAM", requisitosJuego.getRequisitosMinimos().get("RAM"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("RAM", requisitosJuego.getRequisitosMinimos().get("RAM"));
                    }
                    //Comparacion GPU
                    if (requisitosMinimosSeleccionados.containsKey("GPU")) {
                        if (requisitosJuego.getRequisitosMinimos().get("GPU") > requisitosMinimosSeleccionados.get("GPU")) {
                            requisitosMinimosSeleccionados.put("GPU", requisitosJuego.getRequisitosMinimos().get("GPU"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("GPU", requisitosJuego.getRequisitosMinimos().get("GPU"));
                    }
                    //Comparacion Espacio Disco
                    if (requisitosMinimosSeleccionados.containsKey("EspacioDisco")) {
                        if (requisitosJuego.getRequisitosMinimos().get("EspacioDisco") > requisitosMinimosSeleccionados.get("EspacioDisco")) {
                            requisitosMinimosSeleccionados.put("EspacioDisco", requisitosJuego.getRequisitosMinimos().get("EspacioDisco"));
                        }
                    } else {
                        requisitosMinimosSeleccionados.put("EspacioDisco", requisitosJuego.getRequisitosMinimos().get("EspacioDisco"));
                    }
                }
            }
        }
        return requisitosMinimosSeleccionados;
    }

    @Override
    public Map<String, Double> obtenerMapaDeRequisitosRecomendadosSeleccionados(List<String> seleccionados) {
        RequisitosProgramas[] requisitosProgramas = restTemplate.getForObject(URLProgramas, RequisitosProgramas[].class);
        RequisitosJuegos[] requisitosJuegos = restTemplate.getForObject(URLJuegos, RequisitosJuegos[].class);
        List<String> programaSeleccionado = new ArrayList<>();
        List<String> juegoSeleccionado = new ArrayList<>();
        Map<String, Double> requisitosRecomendadosSeleccionados = new HashMap<>();
        for (String app : seleccionados) {
            Boolean esPrograma = false;
            for(RequisitosProgramas programa : requisitosProgramas) {
                if (app.equals(programa.getNombre())) {
                    programaSeleccionado.add(app);
                    esPrograma = true;
                    break;
                }
            }
            if (!esPrograma) {
                juegoSeleccionado.add(app);
            }
        }
        //Comparacion para los Programas
        for (String programa : programaSeleccionado) {
            for (RequisitosProgramas requisitosPrograma : requisitosProgramas) {
                if (programa.equals(requisitosPrograma.getId())) {
                    //Comparacion Procesador AMD
                    if (requisitosRecomendadosSeleccionados.containsKey("ProcesadorAMD")) {
                        if (requisitosPrograma.getRequisitosRecomendados().get("ProcesadorAMD") > requisitosRecomendadosSeleccionados.get("ProcesadorAMD")) {
                            requisitosRecomendadosSeleccionados.put("ProcesadorAMD", requisitosPrograma.getRequisitosRecomendados().get("ProcesadorAMD"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("ProcesadorAMD", requisitosPrograma.getRequisitosRecomendados().get("ProcesadorAMD"));
                    }
                    //Comparacion Procesador INTEL
                    if (requisitosRecomendadosSeleccionados.containsKey("ProcesadorINTEL")) {
                        if (requisitosPrograma.getRequisitosRecomendados().get("ProcesadorINTEL") > requisitosRecomendadosSeleccionados.get("ProcesadorINTEL")) {
                            requisitosRecomendadosSeleccionados.put("ProcesadorINTEL", requisitosPrograma.getRequisitosRecomendados().get("ProcesadorINTEL"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("ProcesadorINTEL", requisitosPrograma.getRequisitosRecomendados().get("ProcesadorINTEL"));
                    }
                    //Comparacion RAM
                    if (requisitosRecomendadosSeleccionados.containsKey("RAM")) {
                        if (requisitosPrograma.getRequisitosRecomendados().get("RAM") > requisitosRecomendadosSeleccionados.get("RAM")) {
                            requisitosRecomendadosSeleccionados.put("RAM", requisitosPrograma.getRequisitosRecomendados().get("RAM"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("RAM", requisitosPrograma.getRequisitosRecomendados().get("RAM"));
                    }
                    //Comparacion GPU
                    if (requisitosRecomendadosSeleccionados.containsKey("GPU")) {
                        if (requisitosPrograma.getRequisitosRecomendados().get("GPU") > requisitosRecomendadosSeleccionados.get("GPU")) {
                            requisitosRecomendadosSeleccionados.put("GPU", requisitosPrograma.getRequisitosRecomendados().get("GPU"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("GPU", requisitosPrograma.getRequisitosRecomendados().get("GPU"));
                    }
                    //Comparacion Espacio Disco
                    if (requisitosRecomendadosSeleccionados.containsKey("EspacioDisco")) {
                        if (requisitosPrograma.getRequisitosRecomendados().get("EspacioDisco") > requisitosRecomendadosSeleccionados.get("EspacioDisco")) {
                            requisitosRecomendadosSeleccionados.put("EspacioDisco", requisitosPrograma.getRequisitosRecomendados().get("EspacioDisco"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("EspacioDisco", requisitosPrograma.getRequisitosRecomendados().get("EspacioDisco"));
                    }
                }
            }
        }
        //Comparacion para los Juegos
        for (String juego : juegoSeleccionado) {
            for (RequisitosJuegos requisitosJuego : requisitosJuegos) {
                if (juego.equals(requisitosJuego.getId())) {
                    //Comparacion Procesador AMD
                    if (requisitosRecomendadosSeleccionados.containsKey("ProcesadorAMD")) {
                        if (requisitosJuego.getRequisitosRecomendados().get("ProcesadorAMD") > requisitosRecomendadosSeleccionados.get("ProcesadorAMD")) {
                            requisitosRecomendadosSeleccionados.put("ProcesadorAMD", requisitosJuego.getRequisitosRecomendados().get("ProcesadorAMD"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("ProcesadorAMD", requisitosJuego.getRequisitosRecomendados().get("ProcesadorAMD"));
                    }
                    //Comparacion Procesador INTEL
                    if (requisitosRecomendadosSeleccionados.containsKey("ProcesadorINTEL")) {
                        if (requisitosJuego.getRequisitosRecomendados().get("ProcesadorINTEL") > requisitosRecomendadosSeleccionados.get("ProcesadorINTEL")) {
                            requisitosRecomendadosSeleccionados.put("ProcesadorINTEL", requisitosJuego.getRequisitosRecomendados().get("ProcesadorINTEL"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("ProcesadorINTEL", requisitosJuego.getRequisitosRecomendados().get("ProcesadorINTEL"));
                    }
                    //Comparacion RAM
                    if (requisitosRecomendadosSeleccionados.containsKey("RAM")) {
                        if (requisitosJuego.getRequisitosRecomendados().get("RAM") > requisitosRecomendadosSeleccionados.get("RAM")) {
                            requisitosRecomendadosSeleccionados.put("RAM", requisitosJuego.getRequisitosRecomendados().get("RAM"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("RAM", requisitosJuego.getRequisitosRecomendados().get("RAM"));
                    }
                    //Comparacion GPU
                    if (requisitosRecomendadosSeleccionados.containsKey("GPU")) {
                        if (requisitosJuego.getRequisitosRecomendados().get("GPU") > requisitosRecomendadosSeleccionados.get("GPU")) {
                            requisitosRecomendadosSeleccionados.put("GPU", requisitosJuego.getRequisitosRecomendados().get("GPU"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("GPU", requisitosJuego.getRequisitosRecomendados().get("GPU"));
                    }
                    //Comparacion Espacio Disco
                    if (requisitosRecomendadosSeleccionados.containsKey("EspacioDisco")) {
                        if (requisitosJuego.getRequisitosRecomendados().get("EspacioDisco") > requisitosRecomendadosSeleccionados.get("EspacioDisco")) {
                            requisitosRecomendadosSeleccionados.put("EspacioDisco", requisitosJuego.getRequisitosRecomendados().get("EspacioDisco"));
                        }
                    } else {
                        requisitosRecomendadosSeleccionados.put("EspacioDisco", requisitosJuego.getRequisitosRecomendados().get("EspacioDisco"));
                    }
                }
            }
        }
        return requisitosRecomendadosSeleccionados;
    }


    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosMinimos(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        if (!tablaDelTipoDeComponente.equals("Procesador") && !tablaDelTipoDeComponente.equals("MemoriaRAM") && !tablaDelTipoDeComponente.equals("PlacaDeVideo") && !tablaDelTipoDeComponente.equals("Almacenamiento")) {
            List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);
            return listaDeComponentesDto;
        }

        List<Componente> componentesCompatiblesRequisitosMinimos = new ArrayList<>();
        for (Componente componente : componentesCompatibles) {
            if (componente instanceof Procesador) {
                String familia = ((Procesador) componente).getFamilia();
                Double ultimoCaracter = (double) familia.charAt(familia.length() - 1);
                if (ultimoCaracter >= seleccionados.get("ProcesadorAMD") || ultimoCaracter >= seleccionados.get("ProcesadorINTEL")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof MemoriaRAM) {
                String capacidad = ((MemoriaRAM) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("RAM")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof PlacaDeVideo) {
                String vram = ((PlacaDeVideo) componente).getCapacidadRAM();
                String[] partes = vram.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("GPU")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof Almacenamiento) {
                String capacidad = ((Almacenamiento) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("EspacioDisco")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
        }

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatiblesRequisitosMinimos);
        return listaDeComponentesDto;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosMinimos(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente, nombreFiltro);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        if (!tablaDelTipoDeComponente.equals("Procesador") && !tablaDelTipoDeComponente.equals("MemoriaRAM") && !tablaDelTipoDeComponente.equals("PlacaDeVideo") && !tablaDelTipoDeComponente.equals("Almacenamiento")) {
            List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);
            return listaDeComponentesDto;
        }

        List<Componente> componentesCompatiblesRequisitosMinimos = new ArrayList<>();
        for (Componente componente : componentesCompatibles) {
            if (componente instanceof Procesador) {
                String familia = ((Procesador) componente).getFamilia();
                Double ultimoCaracter = (double) familia.charAt(familia.length() - 1);
                if (ultimoCaracter >= seleccionados.get("ProcesadorAMD") || ultimoCaracter >= seleccionados.get("ProcesadorINTEL")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof MemoriaRAM) {
                String capacidad = ((MemoriaRAM) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("RAM")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof PlacaDeVideo) {
                String vram = ((PlacaDeVideo) componente).getCapacidadRAM();
                String[] partes = vram.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("GPU")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
            if (componente instanceof Almacenamiento) {
                String capacidad = ((Almacenamiento) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("EspacioDisco")) {
                    componentesCompatiblesRequisitosMinimos.add(componente);
                }
            }
        }

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatiblesRequisitosMinimos);
        return listaDeComponentesDto;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosRecomendados(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        if (!tablaDelTipoDeComponente.equals("Procesador") && !tablaDelTipoDeComponente.equals("MemoriaRAM") && !tablaDelTipoDeComponente.equals("PlacaDeVideo") && !tablaDelTipoDeComponente.equals("Almacenamiento")) {
            List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);
            return listaDeComponentesDto;
        }

        List<Componente> componentesCompatiblesRequisitosRecomendados = new ArrayList<>();

        for (Componente componente : componentesCompatibles) {
            if (componente instanceof Procesador) {
                String familia = ((Procesador) componente).getFamilia();
                Double amd = seleccionados.get("ProcesadorAMD");
                Double intel = seleccionados.get("ProcesadorINTEL");

                if (familia != null && !familia.isEmpty()) {
                    // Buscar último número completo en el string
                    String[] partes = familia.split(" ");
                    String ultimaParte = partes[partes.length - 1].replaceAll("[^0-9]", "");
                    if (!ultimaParte.isEmpty()) {
                        Double numeroFamilia = Double.parseDouble(ultimaParte);
                        if ((amd != null && numeroFamilia >= amd) || (intel != null && numeroFamilia >= intel)) {
                            componentesCompatiblesRequisitosRecomendados.add(componente);
                        }
                    }
                }
            }

            if (componente instanceof MemoriaRAM) {
                String capacidad = ((MemoriaRAM) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                Double capacidadEnDouble = Double.parseDouble(partes[0]);
                Double requisito = seleccionados.get("RAM");

                if (requisito != null && capacidadEnDouble >= requisito) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }

            if (componente instanceof PlacaDeVideo) {
                String vram = ((PlacaDeVideo) componente).getCapacidadRAM();
                String[] partes = vram.split(" ");
                Double capacidadEnDouble = Double.parseDouble(partes[0]);
                Double requisito = seleccionados.get("GPU");

                if (requisito != null && capacidadEnDouble >= requisito) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }

            if (componente instanceof Almacenamiento) {
                String capacidad = ((Almacenamiento) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                Double capacidadEnDouble = Double.parseDouble(partes[0]);
                Double requisito = seleccionados.get("EspacioDisco");

                if (requisito != null && capacidadEnDouble >= requisito) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }
        }

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatiblesRequisitosRecomendados);
        return listaDeComponentesDto;
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosRecomendados(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException {

        String tablaDelTipoDeComponente = this.correspondenciaDeVistaConTablasEnLaBD.get(tipoComponente);
        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(tablaDelTipoDeComponente, nombreFiltro);
        List<Componente> componentesCompatibles = new ArrayList<>();

        for (Componente componente : componentesDeTipo) {
            Boolean esCompatibleConElArmado = this.servicioCompatibilidades.esCompatibleConElArmado(componente, armadoPcDto.obtenerEntidad());
            if (esCompatibleConElArmado) componentesCompatibles.add(componente);
        }

        if (!tablaDelTipoDeComponente.equals("Procesador") && !tablaDelTipoDeComponente.equals("MemoriaRAM") && !tablaDelTipoDeComponente.equals("PlacaDeVideo") && !tablaDelTipoDeComponente.equals("Almacenamiento")) {
            List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatibles);
            return listaDeComponentesDto;
        }

        List<Componente> componentesCompatiblesRequisitosRecomendados = new ArrayList<>();
        for (Componente componente : componentesCompatibles) {
            if (componente instanceof Procesador) {
                String familia = ((Procesador) componente).getFamilia();
                Double ultimoCaracter = (double) familia.charAt(familia.length() - 1);
                if (ultimoCaracter >= seleccionados.get("ProcesadorAMD") || ultimoCaracter >= seleccionados.get("ProcesadorINTEL")) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }
            if (componente instanceof MemoriaRAM) {
                String capacidad = ((MemoriaRAM) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("RAM")) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }
            if (componente instanceof PlacaDeVideo) {
                String vram = ((PlacaDeVideo) componente).getCapacidadRAM();
                String[] partes = vram.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("GPU")) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }
            if (componente instanceof Almacenamiento) {
                String capacidad = ((Almacenamiento) componente).getCapacidad();
                String[] partes = capacidad.split(" ");
                String numeroEnString = partes[0];
                Double capacidadEnDouble = (double) numeroEnString.charAt(0);
                if (capacidadEnDouble >= seleccionados.get("EspacioDisco")) {
                    componentesCompatiblesRequisitosRecomendados.add(componente);
                }
            }
        }
        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesCompatiblesRequisitosRecomendados);
        return listaDeComponentesDto;
    }



}
