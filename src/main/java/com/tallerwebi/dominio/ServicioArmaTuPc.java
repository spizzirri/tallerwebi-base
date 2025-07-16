package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;

import java.util.List;
import java.util.Set;
import java.util.Map;

public interface ServicioArmaTuPc {
    Set<ComponenteDto> obtenerListaDeComponentesCompatiblesDto(String tipoComponente, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    Set<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDto(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto) throws ComponenteDeterminateDelArmadoEnNullException;

    ArmadoPcDto agregarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws LimiteDeComponenteSobrepasadoEnElArmadoException, ComponenteSinStockPedidoException;

    ArmadoPcDto quitarComponenteAlArmado(Long idComponente, String tipoComponente, Integer cantidad, ArmadoPcDto armadoPcDto) throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException;

    Boolean sePuedeAgregarMasUnidades(String tipoComponente, ArmadoPcDto armadoPcDto);

    Boolean armadoCompleto(ArmadoPcDto armadoPcDto);

    Componente obtenerComponentePorId(Long idComponente);

    ComponenteDto obtenerComponenteDtoPorId(Long idComponente);

    List<ProductoCarritoArmadoDto> pasajeAProductoArmadoDtoParaAgregarAlCarrito(ArmadoPcDto armadoPcDto);

    Integer obtenerWattsTotalesDeArmado(ArmadoPcDto armadoPcDto);

    void devolverStockDeArmado(ArmadoPcDto armadoPcDto);
    Map<String, Double> obtenerMapaDeRequisitosMinimosSeleccionados(List<String> seleccionados);

    Map<String, Double> obtenerMapaDeRequisitosRecomendadosSeleccionados(List<String> seleccionados);

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosMinimos(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosMinimos(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    Integer obtenerSlotsRamDeMotherboard(ComponenteDto motherboard);

    Integer obtenerSlotsSataDeMotherboard(ComponenteDto motherboard);

    Integer obtenerSlotsM2DeMotherboard(ComponenteDto motherboard);

    List<ProductoCarritoArmadoDto> configurarNumeroDeArmadoYEscencialidadAProductosCarritoArmadoDto(Integer numeroDeUltimoArmadoEnElCarrito, List<ProductoCarritoArmadoDto> productoCarritoArmadoDtos);
    List<ComponenteDto> obtenerListaDeComponentesCompatiblesDtoCustomRequisitosRecomendados(String tipoComponente, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;

    List<ComponenteDto> obtenerListaDeComponentesCompatiblesFiltradosDtoCustomRequisitosRecomendados(String tipoComponente, String nombreFiltro, ArmadoPcDto armadoPcDto, Map<String, Double> seleccionados) throws ComponenteDeterminateDelArmadoEnNullException;
}
