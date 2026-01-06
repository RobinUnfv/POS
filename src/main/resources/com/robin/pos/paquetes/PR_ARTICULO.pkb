CREATE OR REPLACE PACKAGE BODY INVE.PR_ARTICULO IS

  /*---------------------------------------------------------------------------------------
   Nombre      : GUARDAR
   Proposito   : PROCEDIMIENTO QUE NOS PERMITE ACTUALIZAR O REGISTRAR ARTICULOS
   Parametro  :

   Log de Cambios:
     Fecha        Autor                     Descripción
     05/01/2026   Robinzon Santana          Creador
  -----------------------------------------------------------------------------------------*/
    PROCEDURE GUARDAR(p_cNoCia IN INVE.ARINDA1.NO_CIA%TYPE,
                               p_cTipoArti IN INVE.ARINDA1.TIPO_ARTI%TYPE,
                               p_cNoArti IN INVE.ARINDA1.NO_ARTI%TYPE,
                               p_cDescripcion IN INVE.ARINDA1.DESCRIPCION%TYPE,
                               p_cMedida IN INVE.ARINDA1.MEDIDA%TYPE,
                               p_cMoneda IN INVE.ARINDA1.MONEDA%TYPE,
                               p_cVigente IN INVE.ARINDA1.VIGENTE%TYPE,
                               p_nCostoUni IN INVE.ARINDA1.COSTO_UNI%TYPE,
                               p_nStkMinimo IN INVE.ARINDA1.STK_MINIMO%TYPE,
                               p_nStkMaximo IN INVE.ARINDA1.STK_MAXIMO%TYPE,
                               p_cIndCodBarra IN INVE.ARINDA1.IND_COD_BARRA%TYPE
                              ) IS
    
    BEGIN
       UPDATE INVE.ARINDA1
       SET TIPO_ARTI = p_cTipoArti,
           DESCRIPCION = p_cDescripcion,
           DESCRIP = p_cDescripcion,
           MEDIDA = p_cMedida,
           MONEDA = p_cMoneda,
           VIGENTE = p_cVigente,
           COSTO_UNI = p_nCostoUni,
           STK_MINIMO = p_nStkMinimo,
           STK_MAXIMO = p_nStkMaximo,
           IND_COD_BARRA = p_cIndCodBarra,
           USU_MODI = USER,
           FEC_MODI = SYSDATE
       WHERE NO_CIA = p_cNoCia
       AND NO_ARTI = p_cNoArti;
       
       IF SQL%ROWCOUNT = 0 THEN
          insert into INVE.ARINDA1 (NO_CIA, TIPO_ARTI, CLASE, CATEGORIA, FAMILIA, NO_ARTI, ARTI_PROVE,
           DESCRIP, DESCRIP_PROVE, DESCRIPCION, ARTI_NACIONAL, IND_LOTE, IND_ART_SURTIDO, IND_NRO_SERIE, MEDIDA,
           MEDIDA_PATRON, CANTIDAD_PATRON, PESO, PORC_DESC_VENTA, TIEMPO_REP, GRACIA_PVENCER, PUNTO_REORDEN, CRIT_STOCK, PART_EN_PROD,
           ABC_VOL_PRECIO, ABC_PART_EN_PROD, IMP_VEN, MONEDA, TRANSITO, CODIGO, CENTRO_GASTO, ISC, PORC_ISC, PERIOCIDAD,
           PORC_COMISION, UNIDAD, MARCA, GENERO, MODELO, VIGENTE, ORDEN_IMPRESION, IND_DESCUENTOS,
           IND_DESC_TC, IND_DESC_CP, IND_DESC_PROMO, COSTO_UNI, COND_ARTI, CONS_ARTI, FACTOR, IND_COSTEO,
           CRITERIO_PRECIO, CRITERIO_CALIDAD, NO_ARTI_OLD, T_DESPACHO, TARA, IND_SEC_REF, NO_SOLICITUD,
            LUGAR_RECEP, IND_PRESU, MIN_RECEP, DES, REFERENCIA, PORC_DSCTO, RUBRO_CONTADO, RUBRO_CREDITO,
            RUBRO_COMPRA, ORIGEN, COD_CLASIF, COD_TIP_ART, CONT_CAJ_MAST, PESO_CAJ_UMED, PESO_CAJ_MAST,
            STK_MINIMO, STK_MAXIMO, PARTIDA_ARANC, PORC_ARANC, FECHA_CREACION, COSTO_REPO, FECHA_REPO, FECHA_COSTO_UNI, COSTO_CONT, FECHA_COST_CONT,
             VOL_UNITARIO, LARG_VOLUMEN, ANCH_VOLUMEN, ALTU_VOLUMEN, PESO_BRU_CAJ_MAST, LARG_VOLUMEN_CJ_MAST, ANCH_VOLUMEN_CJ_MAST, ALTU_VOLUMEN_CJ_MAST,
              VOL_UNITARIO_CJ_MAST, TIPO_ARTI_CONT, REFE_BRUNO_01, REFE_BRUNO_02, IND_PIDE_DETALLE, IND_PIDE_LOTE, LOTE_DIVISIBLE, COLECCION, FEC_EST, 
              TITULO, IND_BAJA, FEC_BAJA, OBSEQUIO, CONCEPTO_CTA, FEC_CREA, USU_CREA, 
             USUARIO, COD_PERFEDIT, NO_ARTI_ANTERIOR, IND_COD_BARRA, ANO_COD_BARRA, GRADO_COD_BARRA, 
             CURSO_COD_BARRA, REL_COD_BARRA, NO_ARTI_ORIGINAL, IND_DESCUENTO, CODI_CAMP, IND_ART_COMPON,
              MARCA_STOCK, TIPO_AFECTACION, IMAGEN_ARTI, RUTA_ARCHIVO_ARTI)
          values (p_cNoCia, p_cTipoArti, 'ZZZ', 'ZZZ', 'ZZZ', 'BOL001', null,
           p_cDescripcion, null, p_cDescripcion, 'S', 'N', 'N', 'N', p_cMedida, null, 1, 0, 0, 0, null, 0, 'M',
            null, null, 'A', 'S', p_cMoneda, null, null, null, 'N', 0,
             null, null, null, 'ZZZ', null, null, p_cVigente, null, null,
              'N', 'N', 'N', p_nCostoUni, null, null, '*', 'A', 
              'S', 'N', null, 'N', null, 'N', null, 
              null, null, 0, null, null, null, null, null,
               null, '03', 'ZZZ', '01', null, 0, 0, p_nStkMinimo, p_nStkMaximo, null, null, sysdate, null, null, null, null, null, 0,
               0, 0, 0, null, null, null, null, null, null, null, null, 
               'N', 'N', 'N', '004', SYSDATE, '9999', 'N', null, 'N', 'VME', 
               SYSDATE, USER,  USER, null, null, p_cIndCodBarra, null, null, null, null, null,
                'N', null, 'N', null, '10', null, null);
       END IF;
       
       COMMIT;
      
    EXCEPTION
       WHEN OTHERS THEN
          ROLLBACK;
    END GUARDAR;

END PR_ARTICULO;
/