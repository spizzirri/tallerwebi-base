package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;

import java.util.List;
import java.util.Map;

public interface ServicioArmaTuPc {
    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDto(String tipoComponente, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDto(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException;

    ArmadoPcDto quitarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException;

    Boolean sePuedeAgregarMasUnidades(String tipoComponente, ArmadoPcDto armadoPcDto);

    Boolean armadoCompleto(ArmadoPcDto armadoPcDto);

    Componente obtenerComponentePorId(Long idComponente);

    ComponenteDto obtenerComponenteDtoPorId(Long idComponente);

    List<ProductoCarritoDto> pasajeAProductoDtoParaAgregarAlCarrito(ArmadoPcDto armadoPcDto);

    Map<String, Double> obtenerMapaDeRequisitosMinimosSeleccionados(List<String> seleccionados);

    Map<String, Double> obtenerMapaDeRequisitosRecomendadosSeleccionados(List<String> seleccionados);

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosMinimos(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosMinimos(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosRecomendados(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosRecomendados(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;
}
