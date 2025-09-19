package com.robin.pos.model;

import java.util.List;

public class Arpfoe {
    private String noCia;
    private String noOrden;
    private String grupo;
    private String noCliente;
    private String division;
    private String noVendedor;
    private String codTped;
    private String codFpago;
    private String fRecepcion;
    private String fechaRegistro;
    private String fAprobacion;
    private String fechaEntrega;
    private String fechaEntregaReal;
    private String fechaVence;
    private String tipoPrecio;
    private String moneda;
    private String subTotal;
    private String tImpuesto;
    private String tPrecio;
    private String impuesto;
    private String estado;
    private String bodega;
    private String cuser;
    private String igv;
    private String indGuiado;
    private String direccionComercial;
    private String motivoTraslado;
    private String nombreCliente;
    private String ruc;
    private String tDescuento;
    private String tipoDocRef;
    private String codClasPed;
    private String tipoFpago;
    private String tDsctoGlobal;
    private String tValorVenta;
    private String codTienda;
    private String nombTienda;
    private String direcTienda;
    private String almaOrigen;
    private String almaDestino;
    private String tipoArti;
    private String tipoDocCli;
    private String numDocCli;
    private String codDirEntrega;
    private String codDirSalida;
    private String noClienteSalida;
    private String estadoAsignacion;
    private String listaPrecAnt;
    private String usuarioAprod;
    private String indVtaAnticipada;
    private String totalBruto;
    private String codTPed1;
    private String codTpedb;
    private String codTpedn;
    private String tipo;
    private String indPvent;
    private String centro;
    private String indFactura1;
    private String indBoleta1;
    private String codCaja;
    private String cajera;
    private String convenio;
    private String centroCosto;
    private String indNotaCred;
    private String indExportacion;
    private String consumo;
    private String indFerias;
    private String indProvincia;
    private String redondeo;
    private String indCodBarra;
    private String indFactTexto;
    private String indGuiaTexto;
    private String facturaTexto;
    private String impuestoFlete;
    private String onLine;
    private String contNeto;
    private String indProforma1;
    private String aCta;
    private String entrega;
    private String horaEntrega;
    private String indPideLote;
    private String motConting;
    private String operExoneradas;
    private String operGratuitas;
    private String operGravadas;
    private String tipoOperacion;
    private String guiaTemp;
    private List<Arpfoe> arpfolList;

    public Arpfoe(){}

    public Arpfoe(String noCia, String noOrden, String grupo, String noCliente, String division, String noVendedor, String codTped, String codFpago, String fRecepcion, String fechaRegistro, String fAprobacion, String fechaEntrega, String fechaEntregaReal, String fechaVence, String tipoPrecio, String moneda, String subTotal, String tImpuesto, String tPrecio, String impuesto, String estado, String bodega, String cuser, String igv, String indGuiado, String direccionComercial, String motivoTraslado, String nombreCliente, String ruc, String tDescuento, String tipoDocRef, String codClasPed, String tipoFpago, String tDsctoGlobal, String tValorVenta, String codTienda, String nombTienda, String direcTienda, String almaOrigen, String almaDestino, String tipoArti, String tipoDocCli, String numDocCli, String codDirEntrega, String codDirSalida, String noClienteSalida, String estadoAsignacion, String listaPrecAnt, String usuarioAprod, String indVtaAnticipada, String totalBruto, String codTPed1, String codTpedb, String codTpedn, String tipo, String indPvent, String centro, String indFactura1, String indBoleta1, String codCaja, String cajera, String convenio, String centroCosto, String indNotaCred, String indExportacion, String consumo, String indFerias, String indProvincia, String redondeo, String indCodBarra, String indFactTexto, String indGuiaTexto, String facturaTexto, String impuestoFlete, String onLine, String contNeto, String indProforma1, String aCta, String entrega, String horaEntrega, String indPideLote, String motConting, String operExoneradas, String operGratuitas, String operGravadas, String tipoOperacion, String guiaTemp, List<Arpfoe> arpfolList) {
        this.noCia = noCia;
        this.noOrden = noOrden;
        this.grupo = grupo;
        this.noCliente = noCliente;
        this.division = division;
        this.noVendedor = noVendedor;
        this.codTped = codTped;
        this.codFpago = codFpago;
        this.fRecepcion = fRecepcion;
        this.fechaRegistro = fechaRegistro;
        this.fAprobacion = fAprobacion;
        this.fechaEntrega = fechaEntrega;
        this.fechaEntregaReal = fechaEntregaReal;
        this.fechaVence = fechaVence;
        this.tipoPrecio = tipoPrecio;
        this.moneda = moneda;
        this.subTotal = subTotal;
        this.tImpuesto = tImpuesto;
        this.tPrecio = tPrecio;
        this.impuesto = impuesto;
        this.estado = estado;
        this.bodega = bodega;
        this.cuser = cuser;
        this.igv = igv;
        this.indGuiado = indGuiado;
        this.direccionComercial = direccionComercial;
        this.motivoTraslado = motivoTraslado;
        this.nombreCliente = nombreCliente;
        this.ruc = ruc;
        this.tDescuento = tDescuento;
        this.tipoDocRef = tipoDocRef;
        this.codClasPed = codClasPed;
        this.tipoFpago = tipoFpago;
        this.tDsctoGlobal = tDsctoGlobal;
        this.tValorVenta = tValorVenta;
        this.codTienda = codTienda;
        this.nombTienda = nombTienda;
        this.direcTienda = direcTienda;
        this.almaOrigen = almaOrigen;
        this.almaDestino = almaDestino;
        this.tipoArti = tipoArti;
        this.tipoDocCli = tipoDocCli;
        this.numDocCli = numDocCli;
        this.codDirEntrega = codDirEntrega;
        this.codDirSalida = codDirSalida;
        this.noClienteSalida = noClienteSalida;
        this.estadoAsignacion = estadoAsignacion;
        this.listaPrecAnt = listaPrecAnt;
        this.usuarioAprod = usuarioAprod;
        this.indVtaAnticipada = indVtaAnticipada;
        this.totalBruto = totalBruto;
        this.codTPed1 = codTPed1;
        this.codTpedb = codTpedb;
        this.codTpedn = codTpedn;
        this.tipo = tipo;
        this.indPvent = indPvent;
        this.centro = centro;
        this.indFactura1 = indFactura1;
        this.indBoleta1 = indBoleta1;
        this.codCaja = codCaja;
        this.cajera = cajera;
        this.convenio = convenio;
        this.centroCosto = centroCosto;
        this.indNotaCred = indNotaCred;
        this.indExportacion = indExportacion;
        this.consumo = consumo;
        this.indFerias = indFerias;
        this.indProvincia = indProvincia;
        this.redondeo = redondeo;
        this.indCodBarra = indCodBarra;
        this.indFactTexto = indFactTexto;
        this.indGuiaTexto = indGuiaTexto;
        this.facturaTexto = facturaTexto;
        this.impuestoFlete = impuestoFlete;
        this.onLine = onLine;
        this.contNeto = contNeto;
        this.indProforma1 = indProforma1;
        this.aCta = aCta;
        this.entrega = entrega;
        this.horaEntrega = horaEntrega;
        this.indPideLote = indPideLote;
        this.motConting = motConting;
        this.operExoneradas = operExoneradas;
        this.operGratuitas = operGratuitas;
        this.operGravadas = operGravadas;
        this.tipoOperacion = tipoOperacion;
        this.guiaTemp = guiaTemp;
        this.arpfolList = arpfolList;
    }

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getNoOrden() {
        return noOrden;
    }

    public void setNoOrden(String noOrden) {
        this.noOrden = noOrden;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getNoCliente() {
        return noCliente;
    }

    public void setNoCliente(String noCliente) {
        this.noCliente = noCliente;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getNoVendedor() {
        return noVendedor;
    }

    public void setNoVendedor(String noVendedor) {
        this.noVendedor = noVendedor;
    }

    public String getCodTped() {
        return codTped;
    }

    public void setCodTped(String codTped) {
        this.codTped = codTped;
    }

    public String getCodFpago() {
        return codFpago;
    }

    public void setCodFpago(String codFpago) {
        this.codFpago = codFpago;
    }

    public String getfRecepcion() {
        return fRecepcion;
    }

    public void setfRecepcion(String fRecepcion) {
        this.fRecepcion = fRecepcion;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getfAprobacion() {
        return fAprobacion;
    }

    public void setfAprobacion(String fAprobacion) {
        this.fAprobacion = fAprobacion;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getFechaEntregaReal() {
        return fechaEntregaReal;
    }

    public void setFechaEntregaReal(String fechaEntregaReal) {
        this.fechaEntregaReal = fechaEntregaReal;
    }

    public String getFechaVence() {
        return fechaVence;
    }

    public void setFechaVence(String fechaVence) {
        this.fechaVence = fechaVence;
    }

    public String getTipoPrecio() {
        return tipoPrecio;
    }

    public void setTipoPrecio(String tipoPrecio) {
        this.tipoPrecio = tipoPrecio;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String gettImpuesto() {
        return tImpuesto;
    }

    public void settImpuesto(String tImpuesto) {
        this.tImpuesto = tImpuesto;
    }

    public String gettPrecio() {
        return tPrecio;
    }

    public void settPrecio(String tPrecio) {
        this.tPrecio = tPrecio;
    }

    public String getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(String impuesto) {
        this.impuesto = impuesto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getBodega() {
        return bodega;
    }

    public void setBodega(String bodega) {
        this.bodega = bodega;
    }

    public String getCuser() {
        return cuser;
    }

    public void setCuser(String cuser) {
        this.cuser = cuser;
    }

    public String getIgv() {
        return igv;
    }

    public void setIgv(String igv) {
        this.igv = igv;
    }

    public String getIndGuiado() {
        return indGuiado;
    }

    public void setIndGuiado(String indGuiado) {
        this.indGuiado = indGuiado;
    }

    public String getDireccionComercial() {
        return direccionComercial;
    }

    public void setDireccionComercial(String direccionComercial) {
        this.direccionComercial = direccionComercial;
    }

    public String getMotivoTraslado() {
        return motivoTraslado;
    }

    public void setMotivoTraslado(String motivoTraslado) {
        this.motivoTraslado = motivoTraslado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String gettDescuento() {
        return tDescuento;
    }

    public void settDescuento(String tDescuento) {
        this.tDescuento = tDescuento;
    }

    public String getTipoDocRef() {
        return tipoDocRef;
    }

    public void setTipoDocRef(String tipoDocRef) {
        this.tipoDocRef = tipoDocRef;
    }

    public String getCodClasPed() {
        return codClasPed;
    }

    public void setCodClasPed(String codClasPed) {
        this.codClasPed = codClasPed;
    }

    public String getTipoFpago() {
        return tipoFpago;
    }

    public void setTipoFpago(String tipoFpago) {
        this.tipoFpago = tipoFpago;
    }

    public String gettDsctoGlobal() {
        return tDsctoGlobal;
    }

    public void settDsctoGlobal(String tDsctoGlobal) {
        this.tDsctoGlobal = tDsctoGlobal;
    }

    public String gettValorVenta() {
        return tValorVenta;
    }

    public void settValorVenta(String tValorVenta) {
        this.tValorVenta = tValorVenta;
    }

    public String getCodTienda() {
        return codTienda;
    }

    public void setCodTienda(String codTienda) {
        this.codTienda = codTienda;
    }

    public String getNombTienda() {
        return nombTienda;
    }

    public void setNombTienda(String nombTienda) {
        this.nombTienda = nombTienda;
    }

    public String getDirecTienda() {
        return direcTienda;
    }

    public void setDirecTienda(String direcTienda) {
        this.direcTienda = direcTienda;
    }

    public String getAlmaOrigen() {
        return almaOrigen;
    }

    public void setAlmaOrigen(String almaOrigen) {
        this.almaOrigen = almaOrigen;
    }

    public String getAlmaDestino() {
        return almaDestino;
    }

    public void setAlmaDestino(String almaDestino) {
        this.almaDestino = almaDestino;
    }

    public String getTipoArti() {
        return tipoArti;
    }

    public void setTipoArti(String tipoArti) {
        this.tipoArti = tipoArti;
    }

    public String getTipoDocCli() {
        return tipoDocCli;
    }

    public void setTipoDocCli(String tipoDocCli) {
        this.tipoDocCli = tipoDocCli;
    }

    public String getNumDocCli() {
        return numDocCli;
    }

    public void setNumDocCli(String numDocCli) {
        this.numDocCli = numDocCli;
    }

    public String getCodDirEntrega() {
        return codDirEntrega;
    }

    public void setCodDirEntrega(String codDirEntrega) {
        this.codDirEntrega = codDirEntrega;
    }

    public String getCodDirSalida() {
        return codDirSalida;
    }

    public void setCodDirSalida(String codDirSalida) {
        this.codDirSalida = codDirSalida;
    }

    public String getNoClienteSalida() {
        return noClienteSalida;
    }

    public void setNoClienteSalida(String noClienteSalida) {
        this.noClienteSalida = noClienteSalida;
    }

    public String getEstadoAsignacion() {
        return estadoAsignacion;
    }

    public void setEstadoAsignacion(String estadoAsignacion) {
        this.estadoAsignacion = estadoAsignacion;
    }

    public String getListaPrecAnt() {
        return listaPrecAnt;
    }

    public void setListaPrecAnt(String listaPrecAnt) {
        this.listaPrecAnt = listaPrecAnt;
    }

    public String getUsuarioAprod() {
        return usuarioAprod;
    }

    public void setUsuarioAprod(String usuarioAprod) {
        this.usuarioAprod = usuarioAprod;
    }

    public String getIndVtaAnticipada() {
        return indVtaAnticipada;
    }

    public void setIndVtaAnticipada(String indVtaAnticipada) {
        this.indVtaAnticipada = indVtaAnticipada;
    }

    public String getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(String totalBruto) {
        this.totalBruto = totalBruto;
    }

    public String getCodTPed1() {
        return codTPed1;
    }

    public void setCodTPed1(String codTPed1) {
        this.codTPed1 = codTPed1;
    }

    public String getCodTpedb() {
        return codTpedb;
    }

    public void setCodTpedb(String codTpedb) {
        this.codTpedb = codTpedb;
    }

    public String getCodTpedn() {
        return codTpedn;
    }

    public void setCodTpedn(String codTpedn) {
        this.codTpedn = codTpedn;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIndPvent() {
        return indPvent;
    }

    public void setIndPvent(String indPvent) {
        this.indPvent = indPvent;
    }

    public String getCentro() {
        return centro;
    }

    public void setCentro(String centro) {
        this.centro = centro;
    }

    public String getIndFactura1() {
        return indFactura1;
    }

    public void setIndFactura1(String indFactura1) {
        this.indFactura1 = indFactura1;
    }

    public String getIndBoleta1() {
        return indBoleta1;
    }

    public void setIndBoleta1(String indBoleta1) {
        this.indBoleta1 = indBoleta1;
    }

    public String getCodCaja() {
        return codCaja;
    }

    public void setCodCaja(String codCaja) {
        this.codCaja = codCaja;
    }

    public String getCajera() {
        return cajera;
    }

    public void setCajera(String cajera) {
        this.cajera = cajera;
    }

    public String getConvenio() {
        return convenio;
    }

    public void setConvenio(String convenio) {
        this.convenio = convenio;
    }

    public String getCentroCosto() {
        return centroCosto;
    }

    public void setCentroCosto(String centroCosto) {
        this.centroCosto = centroCosto;
    }

    public String getIndNotaCred() {
        return indNotaCred;
    }

    public void setIndNotaCred(String indNotaCred) {
        this.indNotaCred = indNotaCred;
    }

    public String getIndExportacion() {
        return indExportacion;
    }

    public void setIndExportacion(String indExportacion) {
        this.indExportacion = indExportacion;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public String getIndFerias() {
        return indFerias;
    }

    public void setIndFerias(String indFerias) {
        this.indFerias = indFerias;
    }

    public String getIndProvincia() {
        return indProvincia;
    }

    public void setIndProvincia(String indProvincia) {
        this.indProvincia = indProvincia;
    }

    public String getRedondeo() {
        return redondeo;
    }

    public void setRedondeo(String redondeo) {
        this.redondeo = redondeo;
    }

    public String getIndCodBarra() {
        return indCodBarra;
    }

    public void setIndCodBarra(String indCodBarra) {
        this.indCodBarra = indCodBarra;
    }

    public String getIndFactTexto() {
        return indFactTexto;
    }

    public void setIndFactTexto(String indFactTexto) {
        this.indFactTexto = indFactTexto;
    }

    public String getIndGuiaTexto() {
        return indGuiaTexto;
    }

    public void setIndGuiaTexto(String indGuiaTexto) {
        this.indGuiaTexto = indGuiaTexto;
    }

    public String getFacturaTexto() {
        return facturaTexto;
    }

    public void setFacturaTexto(String facturaTexto) {
        this.facturaTexto = facturaTexto;
    }

    public String getImpuestoFlete() {
        return impuestoFlete;
    }

    public void setImpuestoFlete(String impuestoFlete) {
        this.impuestoFlete = impuestoFlete;
    }

    public String getOnLine() {
        return onLine;
    }

    public void setOnLine(String onLine) {
        this.onLine = onLine;
    }

    public String getContNeto() {
        return contNeto;
    }

    public void setContNeto(String contNeto) {
        this.contNeto = contNeto;
    }

    public String getIndProforma1() {
        return indProforma1;
    }

    public void setIndProforma1(String indProforma1) {
        this.indProforma1 = indProforma1;
    }

    public String getaCta() {
        return aCta;
    }

    public void setaCta(String aCta) {
        this.aCta = aCta;
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(String horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public String getIndPideLote() {
        return indPideLote;
    }

    public void setIndPideLote(String indPideLote) {
        this.indPideLote = indPideLote;
    }

    public String getMotConting() {
        return motConting;
    }

    public void setMotConting(String motConting) {
        this.motConting = motConting;
    }

    public String getOperExoneradas() {
        return operExoneradas;
    }

    public void setOperExoneradas(String operExoneradas) {
        this.operExoneradas = operExoneradas;
    }

    public String getOperGratuitas() {
        return operGratuitas;
    }

    public void setOperGratuitas(String operGratuitas) {
        this.operGratuitas = operGratuitas;
    }

    public String getOperGravadas() {
        return operGravadas;
    }

    public void setOperGravadas(String operGravadas) {
        this.operGravadas = operGravadas;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public String getGuiaTemp() {
        return guiaTemp;
    }

    public void setGuiaTemp(String guiaTemp) {
        this.guiaTemp = guiaTemp;
    }

    public List<Arpfoe> getArpfolList() {
        return arpfolList;
    }

    public void setArpfolList(List<Arpfoe> arpfolList) {
        this.arpfolList = arpfolList;
    }
}
