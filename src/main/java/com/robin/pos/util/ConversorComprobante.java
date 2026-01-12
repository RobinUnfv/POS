package com.robin.pos.util;

import com.robin.pos.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase utilitaria para convertir entre entidades de base de datos
 * y modelos de la aplicación.
 *
 * Conversiones disponibles:
 * - Arfafe → ResultadoEmision
 * - Arfafe → DatosCliente
 * - Arfafe → DatosVenta
 * - Arfafl → DetalleVenta
 *
 * @author Robin POS
 * @version 1.0
 */
public class ConversorComprobante {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Convierte un objeto Arfafe a ResultadoEmision
     *
     * @param arfafe Cabecera del comprobante de la base de datos
     * @return ResultadoEmision con los datos mapeados
     */
    public static ResultadoEmision convertirAResultadoEmision(Arfafe arfafe) {
        if (arfafe == null) {
            return null;
        }

        ResultadoEmision resultado = new ResultadoEmision();

        // Datos básicos
        resultado.setExito(true);
        resultado.setNoCia(arfafe.getNoCia());
        resultado.setNoCliente(arfafe.getNoCliente());
        resultado.setNoOrden(arfafe.getNoOrden());
        resultado.setNoGuia(arfafe.getGuiaTemp());
        resultado.setNoFactu(arfafe.getNoFactu());

        // Fecha
        if (arfafe.getFecha() != null) {
            LocalDate fecha = convertirDateALocalDate(arfafe.getFecha());
            resultado.setFecha(fecha.format(DATE_FORMATTER));
        }

        // Extraer serie y correlativo del número de factura
        // Formato esperado: "B0010000026" → Serie: "B001", Correlativo: "0000026"
        String noFactu = arfafe.getNoFactu();
        if (noFactu != null && noFactu.length() >= 4) {
            resultado.setSerie(noFactu.substring(0, 4));
            resultado.setCorrelativo(noFactu.substring(4));
        }

        // Estado como resultado
        String estado = arfafe.getEstado();
        if ("D".equals(estado)) {
            resultado.setResultadoOracle("OK");
            resultado.setMensaje("Comprobante despachado correctamente");
        } else if ("A".equals(estado)) {
            resultado.setResultadoOracle("ANULADO");
            resultado.setMensaje("Comprobante anulado");
        } else {
            resultado.setResultadoOracle("PENDIENTE");
            resultado.setMensaje("Comprobante pendiente de proceso");
        }

        return resultado;
    }

    /**
     * Convierte un objeto Arfafe a DatosCliente
     *
     * @param arfafe Cabecera del comprobante de la base de datos
     * @return DatosCliente con los datos mapeados
     */
    public static DatosCliente convertirADatosCliente(Arfafe arfafe) {
        if (arfafe == null) {
            return null;
        }

        DatosCliente cliente = new DatosCliente();

        cliente.setNombre(arfafe.getNbrCliente());
        cliente.setNumeroDocumento(arfafe.getNumDocCli());
        cliente.setTipoDocumento(convertirTipoDocumento(arfafe.getTipoDocCli()));
        cliente.setDireccion(arfafe.getDireccion());

        // La dirección no está en Arfafe, se puede obtener de otra fuente o dejar vacío
        //cliente.setDireccion("");

        return cliente;
    }

    /**
     * Convierte un objeto Arfafe a DatosVenta
     *
     * @param arfafe Cabecera del comprobante de la base de datos
     * @return DatosVenta con los datos mapeados
     */
    public static DatosVenta convertirADatosVenta(Arfafe arfafe) {
        if (arfafe == null) {
            return null;
        }

        DatosVenta venta = new DatosVenta();

        // Fecha de emisión
        if (arfafe.getFecha() != null) {
            venta.setFechaEmision(convertirDateALocalDate(arfafe.getFecha()));
        } else {
            venta.setFechaEmision(LocalDate.now());
        }

        // Moneda
        String moneda = arfafe.getMoneda();
        if (moneda != null) {
            venta.setMoneda(convertirMoneda(moneda));
        }

        // Condición de pago (por defecto CONTADO si no hay información adicional)
        venta.setCondicionPago("VENTA CONTADO");

        // Porcentaje IGV - Calcular desde los montos si es posible
        BigDecimal subTotal = arfafe.getSubTotal();
        BigDecimal impuesto = arfafe.getImpuesto();
        if (subTotal != null && impuesto != null && subTotal.compareTo(BigDecimal.ZERO) > 0) {
            double porcentaje = impuesto.doubleValue() / subTotal.doubleValue() * 100;
            venta.setPorcentajeIgv((int) Math.round(porcentaje));
        } else {
            venta.setPorcentajeIgv(18); // Por defecto 18%
        }

        // Vendedor (no disponible en Arfafe, se puede setear después)
        venta.setVendedor("");

        // Orden de compra
        venta.setOrdenCompra(arfafe.getNoOrden());

        return venta;
    }

    /**
     * Convierte un objeto Arfafl a DetalleVenta
     *
     * @param arfafl Línea de detalle de la base de datos
     * @param numeroItem Número de ítem/línea para asignar
     * @return DetalleVenta con los datos mapeados
     */
    public static DetalleVenta convertirADetalleVenta(Arfafl arfafl, int numeroItem) {
        if (arfafl == null) {
            return null;
        }

        DetalleVenta detalle = new DetalleVenta();

        // Número de ítem
        detalle.setItem(numeroItem);

        // Crear Arinda1 con los datos del artículo
        Arinda1 articulo = new Arinda1();
        articulo.setCodigo(arfafl.getNoArti());
        articulo.setDescripcion(arfafl.getDescripcion());
        articulo.setMedida(arfafl.getMedida());
        detalle.setArinda1(articulo);

        // Cantidad
        BigDecimal cantidad = arfafl.getCantidadFact();
        if (cantidad != null) {
            detalle.setCantidad(cantidad.doubleValue());
        } else {
            detalle.setCantidad(0.0);
        }

        // Precio unitario (con IGV)
        BigDecimal precioIgv = arfafl.getPrecIgv();
        if (precioIgv != null) {
            detalle.setPrecio(precioIgv.doubleValue());
        } else {
            BigDecimal precioUnit = arfafl.getPrecioUnit();
            if (precioUnit != null) {
                detalle.setPrecio(precioUnit.doubleValue());
            } else {
                detalle.setPrecio(0.0);
            }
        }

        // IGV de la línea
        BigDecimal igv = arfafl.getImpIgv();
        if (igv != null) {
            detalle.setIgv(igv.doubleValue());
        } else {
            detalle.setIgv(0.0);
        }

        // El total se calcula automáticamente en DetalleVenta mediante binding
        // pero si necesitamos setearlo manualmente:
        // detalle.setTotal(arfafl.getTotalLin().doubleValue());

        return detalle;
    }

    /**
     * Convierte una lista de Arfafl a lista de DetalleVenta
     *
     * @param listaArfafl Lista de líneas de detalle de la base de datos
     * @return Lista de DetalleVenta
     */
    public static List<DetalleVenta> convertirListaADetalleVenta(List<Arfafl> listaArfafl) {
        List<DetalleVenta> listaDetalle = new ArrayList<>();

        if (listaArfafl == null || listaArfafl.isEmpty()) {
            return listaDetalle;
        }

        int item = 1;
        for (Arfafl arfafl : listaArfafl) {
            DetalleVenta detalle = convertirADetalleVenta(arfafl, item++);
            if (detalle != null) {
                listaDetalle.add(detalle);
            }
        }

        return listaDetalle;
    }

    /**
     * Convierte todos los datos de un comprobante de una sola vez
     *
     * @param arfafe Cabecera del comprobante
     * @param listaArfafl Lista de líneas de detalle
     * @return DatosComprobanteCompleto con todos los objetos convertidos
     */
    public static DatosComprobanteCompleto convertirComprobanteCompleto(Arfafe arfafe, List<Arfafl> listaArfafl) {
        DatosComprobanteCompleto datos = new DatosComprobanteCompleto();

        datos.setResultadoEmision(convertirAResultadoEmision(arfafe));
        datos.setDatosCliente(convertirADatosCliente(arfafe));
        datos.setDatosVenta(convertirADatosVenta(arfafe));
        datos.setDetalles(convertirListaADetalleVenta(listaArfafl));

        return datos;
    }


    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte java.util.Date a LocalDate
     */
    private static LocalDate convertirDateALocalDate(Date date) {
        if (date == null) {
            return null;
        }

        // Si es java.sql.Date, usar toLocalDate() directamente
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate(); // ✅ Correcto
        }

        // Si es java.util.Date, convertir usando Calendar
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        return LocalDate.of(
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
    }

    /**
     * Convierte código de tipo de documento a descripción
     *
     * @param codigo Código del tipo de documento (1, 6, 7, etc.)
     * @return Descripción del tipo de documento
     */
    private static String convertirTipoDocumento(String codigo) {
        if (codigo == null) {
            return "OTROS";
        }
        return switch (codigo) {
            case "1" -> "DNI";
            case "6" -> "RUC";
            case "7" -> "CE";
            case "4" -> "CARNET EXT.";
            case "0" -> "OTROS";
            case "A" -> "CÉDULA";
            default -> codigo;
        };
    }

    /**
     * Convierte código de moneda a descripción
     *
     * @param codigo Código de moneda (S, D, etc.)
     * @return Descripción de la moneda
     */
    private static String convertirMoneda(String codigo) {
        if (codigo == null) {
            return "SOL";
        }
        return switch (codigo.toUpperCase()) {
            case "S", "PEN" -> "SOL";
            case "D", "USD" -> "DOLAR";
            case "E", "EUR" -> "EURO";
            default -> codigo;
        };
    }

}