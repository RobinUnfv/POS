package com.robin.pos.model;

import java.util.List;
import java.time.LocalDate;

public class ParametrosComprobante {
    private String noCia = "01";
    private String grupo = "00";
    private String division = "003";
    private String bodega = "1A002";
    private String codTienda = "001";
    private String nombTienda = "LEGAL";
    private String direcTienda = "";
    private String almaOrigen = "1A002";
    private String almaDestino = "1XLIE";
    private String centro = "41";
    private String centroCosto = "3201";

    // Datos del cliente
    private String noCliente;
    private String nombreCliente;
    private String ruc;
    private String tipoDocCli = "OTR";
    private String direccionComercial = "";

    // Datos del vendedor/cajero
    private String noVendedor = "48750185";
    private String usuario;
    private String codCaja = "C11";
    private String cajera = "900002";

    // Datos del documento
    private String tipoDocumento; // B=Boleta, F=Factura
    private LocalDate fecha = LocalDate.now();
    private String codTped = "1315";
    private String codTped1 = "1352";
    private String codTpedb = "1353";
    private String codTpedn = "1214";
    private String codFpago = "01";
    private String tipoFpago = "20";
    private String tipoPrecio = "A3";
    private String moneda = "SOL";
    private double tipoCambio = 3.37;

    // Totales
    private double subTotal;
    private double totalIgv;
    private double totalPrecio;
    private double totalDescuento = 0;
    private int porcentajeIgv = 18;

    // Otros parámetros
    private String indGuiado = "S";
    private String motivoTraslado = "1";
    private String codDirEntrega = "001";
    private String codDirSalida = "201";
    private int motConting = 0;
    private String guiaRemision;

    // Detalles de la venta
    private List<DetalleVenta> detalles;

    // Getters y Setters
    public String getNoCia() { return noCia; }
    public void setNoCia(String noCia) { this.noCia = noCia; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getBodega() { return bodega; }
    public void setBodega(String bodega) { this.bodega = bodega; }

    public String getCodTienda() { return codTienda; }
    public void setCodTienda(String codTienda) { this.codTienda = codTienda; }

    public String getNombTienda() { return nombTienda; }
    public void setNombTienda(String nombTienda) { this.nombTienda = nombTienda; }

    public String getDirecTienda() { return direcTienda; }
    public void setDirecTienda(String direcTienda) { this.direcTienda = direcTienda; }

    public String getAlmaOrigen() { return almaOrigen; }
    public void setAlmaOrigen(String almaOrigen) { this.almaOrigen = almaOrigen; }

    public String getAlmaDestino() { return almaDestino; }
    public void setAlmaDestino(String almaDestino) { this.almaDestino = almaDestino; }

    public String getCentro() { return centro; }
    public void setCentro(String centro) { this.centro = centro; }

    public String getCentroCosto() { return centroCosto; }
    public void setCentroCosto(String centroCosto) { this.centroCosto = centroCosto; }

    public String getNoCliente() { return noCliente; }
    public void setNoCliente(String noCliente) { this.noCliente = noCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTipoDocCli() { return tipoDocCli; }
    public void setTipoDocCli(String tipoDocCli) { this.tipoDocCli = tipoDocCli; }

    public String getDireccionComercial() { return direccionComercial; }
    public void setDireccionComercial(String direccionComercial) { this.direccionComercial = direccionComercial; }

    public String getNoVendedor() { return noVendedor; }
    public void setNoVendedor(String noVendedor) { this.noVendedor = noVendedor; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCodCaja() { return codCaja; }
    public void setCodCaja(String codCaja) { this.codCaja = codCaja; }

    public String getCajera() { return cajera; }
    public void setCajera(String cajera) { this.cajera = cajera; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getCodTped() { return codTped; }
    public void setCodTped(String codTped) { this.codTped = codTped; }

    public String getCodTped1() { return codTped1; }
    public void setCodTped1(String codTped1) { this.codTped1 = codTped1; }

    public String getCodTpedb() { return codTpedb; }
    public void setCodTpedb(String codTpedb) { this.codTpedb = codTpedb; }

    public String getCodTpedn() { return codTpedn; }
    public void setCodTpedn(String codTpedn) { this.codTpedn = codTpedn; }

    public String getCodFpago() { return codFpago; }
    public void setCodFpago(String codFpago) { this.codFpago = codFpago; }

    public String getTipoFpago() { return tipoFpago; }
    public void setTipoFpago(String tipoFpago) { this.tipoFpago = tipoFpago; }

    public String getTipoPrecio() { return tipoPrecio; }
    public void setTipoPrecio(String tipoPrecio) { this.tipoPrecio = tipoPrecio; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public double getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(double tipoCambio) { this.tipoCambio = tipoCambio; }

    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }

    public double getTotalIgv() { return totalIgv; }
    public void setTotalIgv(double totalIgv) { this.totalIgv = totalIgv; }

    public double getTotalPrecio() { return totalPrecio; }
    public void setTotalPrecio(double totalPrecio) { this.totalPrecio = totalPrecio; }

    public double getTotalDescuento() { return totalDescuento; }
    public void setTotalDescuento(double totalDescuento) { this.totalDescuento = totalDescuento; }

    public int getPorcentajeIgv() { return porcentajeIgv; }
    public void setPorcentajeIgv(int porcentajeIgv) { this.porcentajeIgv = porcentajeIgv; }

    public String getIndGuiado() { return indGuiado; }
    public void setIndGuiado(String indGuiado) { this.indGuiado = indGuiado; }

    public String getMotivoTraslado() { return motivoTraslado; }
    public void setMotivoTraslado(String motivoTraslado) { this.motivoTraslado = motivoTraslado; }

    public String getCodDirEntrega() { return codDirEntrega; }
    public void setCodDirEntrega(String codDirEntrega) { this.codDirEntrega = codDirEntrega; }

    public String getCodDirSalida() { return codDirSalida; }
    public void setCodDirSalida(String codDirSalida) { this.codDirSalida = codDirSalida; }

    public int getMotConting() { return motConting; }
    public void setMotConting(int motConting) { this.motConting = motConting; }

    public String getGuiaRemision() { return guiaRemision; }
    public void setGuiaRemision(String guiaRemision) { this.guiaRemision = guiaRemision; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
