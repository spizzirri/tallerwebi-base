package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

// agregar transaccional
// agregar service
@Service
@Transactional
public class ServicioArmaTuPcImpl implements ServicioArmaTuPc {

    private RepositorioComponente repositorioComponente;

    @Autowired
    public ServicioArmaTuPcImpl(RepositorioComponente repositorioComponente) {
        this.repositorioComponente = repositorioComponente;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.repositorioComponente.obtenerComponentePorId(idComponente);
    }

    @Override
    public List<ComponenteDto> obtenerListaDeComponentesDto(String tipoComponente) {

        List<Componente> componentesDeTipo = this.repositorioComponente.obtenerComponentesPorTipo(tipoComponente);

        // traer componentes compatibles

        List<ComponenteDto> listaDeComponentesDto = transformarComponentesADtos(componentesDeTipo);

        return listaDeComponentesDto;
    }

    private ComponenteDto transformarADto(Componente componente) {
        return new ComponenteDto(componente.getId(),componente.getTipo(),componente.getNombre(),componente.getPrecio(),componente.getImagen(),componente.getStock());
    }

    private List<ComponenteDto> transformarComponentesADtos(List<Componente> componentesDeTipo) {

        List<ComponenteDto> listaDeComponentesDto = new ArrayList<ComponenteDto>();

        for(Componente c : componentesDeTipo){
            listaDeComponentesDto.add(transformarADto(c));
        }

        return listaDeComponentesDto;
    }

    @Override
    public ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        Componente componenteSolicitado = obtenerComponentePorId(idComponente);

        seExcedeDeLimite(tipoComponente, cantidad, armadoPcDto);

        // determinar un metodo de componente que diga cuando un componente es compatible con el armado

        switch(tipoComponente){
            case "procesador":
                armadoPcDto.setProcesador(transformarADto(componenteSolicitado));
                break;
            case "motherboard":
                armadoPcDto.setMotherboard(transformarADto(componenteSolicitado));
                break;
            case "cooler":
                armadoPcDto.setCooler(transformarADto(componenteSolicitado));
                break;
            case "memoria":
                for(int i = 0; i<cantidad;i++) armadoPcDto.getRams().add(transformarADto(componenteSolicitado));
                break;
            case "gpu":
                armadoPcDto.setGpu(transformarADto(componenteSolicitado));
                break;
            case "almacenamiento":
                for(int i = 0; i<cantidad;i++) armadoPcDto.getAlmacenamiento().add(transformarADto(componenteSolicitado));
                break;
            case "fuente":
                armadoPcDto.setFuente(transformarADto(componenteSolicitado));
                break;
            case "gabinete":
                armadoPcDto.setGabinete(transformarADto(componenteSolicitado));
                break;
            case "monitor":
                armadoPcDto.setMonitor(transformarADto(componenteSolicitado));
                break;
            case "periferico":
                armadoPcDto.getPerifericos().add(transformarADto(componenteSolicitado));
                break;

        }

        return armadoPcDto;
    }

    private void seExcedeDeLimite(String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        if ((tipoComponente.equalsIgnoreCase("memoria") && armadoPcDto.getRams().size() + cantidad > 4) ||
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

}
