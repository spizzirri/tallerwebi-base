package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;

import java.util.Collection;
import java.util.List;

public interface ServicioArmaTuPc {
    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDto(String tipoComponente, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDto(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException;

    ArmadoPcDto quitarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException;

    Boolean sePuedeAgregarMasUnidades(String tipoComponente, ArmadoPcDto armadoPcDto);

    Boolean armadoCompleto(ArmadoPcDto armadoPcDto);

    Componente obtenerComponentePorId(Long idComponente);

    ComponenteDto obtenerComponenteDtoPorId(Long idComponente);

    List<ProductoCarritoArmadoDto> pasajeAProductoArmadoDtoParaAgregarAlCarrito(ArmadoPcDto armadoPcDto);

    Integer obtenerWattsTotalesDeArmado(ArmadoPcDto armadoPcDto);
}
