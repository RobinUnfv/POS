package com.robin.pos.dao;

import com.robin.pos.model.DetalleVenta;
import com.robin.pos.model.ParametrosComprobante;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

public class ComprobantePagoDao {

    private static final Logger LOGGER = Logger.getLogger(ComprobantePagoDao.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formato de números con coma decimal (estilo español/peruano)
    private static final DecimalFormat DECIMAL_FORMAT;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PE"));
        symbols.setDecimalSeparator(',');
        DECIMAL_FORMAT = new DecimalFormat("#0.00", symbols);
    }

    /**
     * Construye el XML de entrada para el procedimiento EMISION_COMPRO_PAGO
     */
    private String construirXmlEntrada(ParametrosComprobante params) {
        StringBuilder xml = new StringBuilder();
        String fechaActual = params.getFecha().format(DATE_FORMATTER);

        xml.append("<emisionComprobantePago>\n");
        xml.append("    <arpfoe>\n");

        // Datos de cabecera del pedido/venta
        xml.append("        <noCia>").append(params.getNoCia()).append("</noCia>\n");
        xml.append("        <noOrden></noOrden>\n");
        xml.append("        <grupo>").append(params.getGrupo()).append("</grupo>\n");
        xml.append("        <noCliente>").append(params.getNoCliente()).append("</noCliente>\n");
        xml.append("        <division>").append(params.getDivision()).append("</division>\n");
        xml.append("        <noVendedor>").append(params.getNoVendedor()).append("</noVendedor>\n");
        xml.append("        <codTped>").append(params.getCodTped()).append("</codTped>\n");
        xml.append("        <codFpago>").append(params.getCodFpago()).append("</codFpago>\n");
        xml.append("        <fRecepcion>").append(fechaActual).append("</fRecepcion>\n");
        xml.append("        <fechaRegistro>").append(fechaActual).append("</fechaRegistro>\n");
        xml.append("        <fAprobacion>").append(fechaActual).append("</fAprobacion>\n");
        xml.append("        <fechaEntrega>").append(fechaActual).append("</fechaEntrega>\n");
        xml.append("        <fechaEntregaReal>").append(fechaActual).append("</fechaEntregaReal>\n");
        xml.append("        <fechaVence>").append(fechaActual).append("</fechaVence>\n");
        xml.append("        <tipoPrecio>").append(params.getTipoPrecio()).append("</tipoPrecio>\n");
        xml.append("        <moneda>").append(params.getMoneda()).append("</moneda>\n");

        // Totales
        xml.append("        <subTotal>").append(formatearNumero(params.getSubTotal())).append("</subTotal>\n");
        xml.append("        <tImpuesto>").append(formatearNumero(params.getTotalIgv())).append("</tImpuesto>\n");
        xml.append("        <tPrecio>").append(formatearNumero(params.getTotalPrecio())).append("</tPrecio>\n");
        xml.append("        <impuesto>").append(params.getPorcentajeIgv()).append("</impuesto>\n");
        xml.append("        <estado>C</estado>\n");
        xml.append("        <bodega>").append(params.getBodega()).append("</bodega>\n");
        xml.append("        <cuser>").append(params.getUsuario()).append("</cuser>\n");
        xml.append("        <igv>").append(params.getPorcentajeIgv()).append("</igv>\n");
        xml.append("        <indGuiado>").append(params.getIndGuiado()).append("</indGuiado>\n");
        xml.append("        <direccionComercial>").append(escapeXml(params.getDireccionComercial())).append("</direccionComercial>\n");
        xml.append("        <motivoTraslado>").append(params.getMotivoTraslado()).append("</motivoTraslado>\n");
        xml.append("        <nombreCliente>").append(escapeXml(params.getNombreCliente())).append("</nombreCliente>\n");
        xml.append("        <ruc>").append(params.getRuc()).append("</ruc>\n");
        xml.append("        <tDescuento>").append(formatearNumero(params.getTotalDescuento())).append("</tDescuento>\n");
        xml.append("        <tipoDocRef>OC</tipoDocRef>\n");
        xml.append("        <codClasPed>V</codClasPed>\n");
        xml.append("        <tipoFpago>").append(params.getTipoFpago()).append("</tipoFpago>\n");
        xml.append("        <tDsctoGlobal>0</tDsctoGlobal>\n");
        xml.append("        <tValorVenta>").append(formatearNumero(params.getSubTotal())).append("</tValorVenta>\n");
        xml.append("        <codTienda>").append(params.getCodTienda()).append("</codTienda>\n");
        xml.append("        <nombTienda>").append(params.getNombTienda()).append("</nombTienda>\n");
        xml.append("        <direcTienda>").append(escapeXml(params.getDirecTienda())).append("</direcTienda>\n");
        xml.append("        <almaOrigen>").append(params.getAlmaOrigen()).append("</almaOrigen>\n");
        xml.append("        <almaDestino>").append(params.getAlmaDestino()).append("</almaDestino>\n");
        xml.append("        <tipoArti>1</tipoArti>\n");
        xml.append("        <tipoDocCli>").append(params.getTipoDocCli()).append("</tipoDocCli>\n");
        xml.append("        <numDocCli>").append(params.getNoCliente()).append("</numDocCli>\n");
        xml.append("        <codDirEntrega>").append(params.getCodDirEntrega()).append("</codDirEntrega>\n");
        xml.append("        <codDirSalida>").append(params.getCodDirSalida()).append("</codDirSalida>\n");
        xml.append("        <noClienteSalida>").append(params.getNoCliente()).append("</noClienteSalida>\n");
        xml.append("        <estadoAsignacion>C</estadoAsignacion>\n");
        xml.append("        <listaPrecAnt>N</listaPrecAnt>\n");
        xml.append("        <usuarioAprod>").append(params.getUsuario()).append("</usuarioAprod>\n");
        xml.append("        <indVtaAnticipada>N</indVtaAnticipada>\n");
        xml.append("        <totalBruto>").append(formatearNumero(params.getSubTotal())).append("</totalBruto>\n");
        xml.append("        <codTPed1>").append(params.getCodTped1()).append("</codTPed1>\n");
        xml.append("        <codTpedb>").append(params.getCodTpedb()).append("</codTpedb>\n");
        xml.append("        <codTpedn>").append(params.getCodTpedn()).append("</codTpedn>\n");
        xml.append("        <tipo>N</tipo>\n");
        xml.append("        <indPvent>S</indPvent>\n");
        xml.append("        <centro>").append(params.getCentro()).append("</centro>\n");

        // Indicadores de tipo de documento
        if ("F".equals(params.getTipoDocumento())) {
            xml.append("        <indFactura1>S</indFactura1>\n");
            xml.append("        <indBoleta1>N</indBoleta1>\n");
        } else {
            xml.append("        <indFactura1>N</indFactura1>\n");
            xml.append("        <indBoleta1>S</indBoleta1>\n");
        }

        xml.append("        <codCaja>").append(params.getCodCaja()).append("</codCaja>\n");
        xml.append("        <cajera>").append(params.getCajera()).append("</cajera>\n");
        xml.append("        <convenio>N</convenio>\n");
        xml.append("        <centroCosto>").append(params.getCentroCosto()).append("</centroCosto>\n");
        xml.append("        <indNotaCred>N</indNotaCred>\n");
        xml.append("        <indExportacion>N</indExportacion>\n");
        xml.append("        <consumo>N</consumo>\n");
        xml.append("        <indFerias>N</indFerias>\n");
        xml.append("        <indProvincia>N</indProvincia>\n");
        xml.append("        <redondeo>0</redondeo>\n");
        xml.append("        <indCodBarra>N</indCodBarra>\n");
        xml.append("        <indFactTexto>N</indFactTexto>\n");
        xml.append("        <indGuiaTexto>N</indGuiaTexto>\n");
        xml.append("        <facturaTexto>N</facturaTexto>\n");
        xml.append("        <impuestoFlete>0</impuestoFlete>\n");
        xml.append("        <onLine>N</onLine>\n");
        xml.append("        <contNeto>N</contNeto>\n");
        xml.append("        <indProforma1>N</indProforma1>\n");
        xml.append("        <aCta>0</aCta>\n");
        xml.append("        <entrega>").append(fechaActual).append("</entrega>\n");
        xml.append("        <horaEntrega></horaEntrega>\n");
        xml.append("        <indPideLote>N</indPideLote>\n");
        xml.append("        <motConting>").append(params.getMotConting()).append("</motConting>\n");
        xml.append("        <operExoneradas>0</operExoneradas>\n");
        xml.append("        <operGratuitas>0</operGratuitas>\n");
        xml.append("        <operGravadas>").append(formatearNumero(params.getSubTotal())).append("</operGravadas>\n");
        xml.append("        <tipoOperacion>0101</tipoOperacion>\n");
        xml.append("        <guiaTemp>").append(params.getGuiaRemision() != null ? params.getGuiaRemision() : "").append("</guiaTemp>\n");

        // Lista de detalles (productos)
        xml.append("        <arpfolList>\n");

        int noLinea = 1;
        for (DetalleVenta detalle : params.getDetalles()) {
            xml.append(construirXmlDetalle(params, detalle, noLinea++));
        }

        xml.append("        </arpfolList>\n");
        xml.append("    </arpfoe>\n");

        // Datos adicionales del comprobante (arfafe)
        xml.append("    <arfafe>\n");
        xml.append("      <tipoDoc>").append(params.getTipoDocumento()).append("</tipoDoc>\n");
        xml.append("      <tipoCliente>V</tipoCliente>\n");
        xml.append("      <tipoCambio>").append(formatearNumero(params.getTipoCambio())).append("</tipoCambio>\n");
        xml.append("      <indDoc>N</indDoc>\n");
        xml.append("      <mDsctoGlobal>0</mDsctoGlobal>\n");
        xml.append("    </arfafe>\n");

        xml.append("</emisionComprobantePago>");

        return xml.toString();
    }

    /**
     * Construye el XML para un detalle de venta (arpfol)
     */
    private String construirXmlDetalle(ParametrosComprobante params, DetalleVenta detalle, int noLinea) {
        StringBuilder xml = new StringBuilder();
        String fechaActual = params.getFecha().format(DATE_FORMATTER);

        // Calcular valores
        double precioConIgv = detalle.getPrecio();
        double precioSinIgv = precioConIgv / (1 + params.getPorcentajeIgv() / 100.0);
        double cantidad = detalle.getCantidad();
        double totalLinea = precioSinIgv * cantidad;
        double igvLinea = totalLinea * (params.getPorcentajeIgv() / 100.0);
        double totalConIgv = totalLinea + igvLinea;

        xml.append("            <arpfol>\n");
        xml.append("                <noCia>").append(params.getNoCia()).append("</noCia>\n");
        xml.append("                <noOrden></noOrden>\n");
        xml.append("                <grupo>").append(params.getGrupo()).append("</grupo>\n");
        xml.append("                <noCliente>").append(params.getNoCliente()).append("</noCliente>\n");
        xml.append("                <noArti>").append(detalle.getArinda1().getCodigo()).append("</noArti>\n");
        xml.append("                <tipoArti>C</tipoArti>\n");
        xml.append("                <artiNuevo>N</artiNuevo>\n");
        xml.append("                <bodega>").append(params.getBodega()).append("</bodega>\n");
        xml.append("                <cantComp>").append((int) cantidad).append("</cantComp>\n");
        xml.append("                <cantSolicitada>").append((int) cantidad).append("</cantSolicitada>\n");
        xml.append("                <cantEntreg>").append((int) cantidad).append("</cantEntreg>\n");
        xml.append("                <cantAsignada>").append((int) cantidad).append("</cantAsignada>\n");
        xml.append("                <cantReasignada>0</cantReasignada>\n");
        xml.append("                <fechaRegistro>").append(fechaActual).append("</fechaRegistro>\n");
        xml.append("                <precio>").append(formatearNumeroLargo(precioSinIgv)).append("</precio>\n");
        xml.append("                <totLinea>").append(formatearNumeroLargo(totalLinea)).append("</totLinea>\n");
        xml.append("                <estado>P</estado>\n");
        xml.append("                <dsctoCliente>0</dsctoCliente>\n");
        xml.append("                <dPromo>0</dPromo>\n");
        xml.append("                <igv>").append(params.getPorcentajeIgv()).append("</igv>\n");
        xml.append("                <noLinea>").append(noLinea).append("</noLinea>\n");
        xml.append("                <pDscto3>0</pDscto3>\n");
        xml.append("                <mDscto2>0</mDscto2>\n");
        xml.append("                <mDscto3>0</mDscto3>\n");
        xml.append("                <impIgv>").append(formatearNumeroLargo(igvLinea)).append("</impIgv>\n");
        xml.append("                <precioSigv>0</precioSigv>\n");
        xml.append("                <totalLin>").append(formatearNumero(totalConIgv)).append("</totalLin>\n");
        xml.append("                <descripcion>").append(escapeXml(detalle.getArinda1().getDescripcion())).append("</descripcion>\n");
        xml.append("                <parte>1</parte>\n");
        xml.append("                <tipoBs>L</tipoBs>\n");
        xml.append("                <indPideLote>N</indPideLote>\n");
        xml.append("                <operExoneradas>0</operExoneradas>\n");
        xml.append("                <operGratuitas>0</operGratuitas>\n");
        xml.append("                <operGravadas>").append(formatearNumero(totalLinea)).append("</operGravadas>\n");
        xml.append("                <operInafectas>0</operInafectas>\n");
        xml.append("                <tipoAfectacion>10</tipoAfectacion>\n");
        xml.append("                <precIgv>").append(formatearNumero(precioConIgv)).append("</precIgv>\n");
        xml.append("                <medida>NIU</medida>\n");
        xml.append("            </arpfol>\n");

        return xml.toString();
    }

    /**
     * Extrae un valor de una etiqueta XML simple
     */
    private String extraerValorXml(String xml, String etiqueta) {
        String inicio = "<" + etiqueta + ">";
        String fin = "</" + etiqueta + ">";
        int posInicio = xml.indexOf(inicio);
        int posFin = xml.indexOf(fin);

        if (posInicio >= 0 && posFin > posInicio) {
            return xml.substring(posInicio + inicio.length(), posFin).trim();
        }
        return null;
    }

    /**
     * Formatea un número con coma decimal (2 decimales)
     */
    private String formatearNumero(double valor) {
        return DECIMAL_FORMAT.format(valor);
    }

    /**
     * Formatea un número con más decimales para precios unitarios
     */
    private String formatearNumeroLargo(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PE"));
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#0.0000000", symbols);
        return df.format(valor);
    }

    /**
     * Escapa caracteres especiales para XML
     */
    private String escapeXml(String texto) {
        if (texto == null) return "";
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

}
