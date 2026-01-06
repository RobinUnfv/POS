CREATE OR REPLACE PACKAGE INVE.PR_ARTICULO IS

 -- PROCEDIMIENTO QUE NOS PERMITE ACTUALIZAR O REGISTRAR ARTICULOS
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
                              );

END PR_ARTICULO;
/