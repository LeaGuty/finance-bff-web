package cl.duoc.finance_bff_web.service;

import cl.duoc.finance_bff_web.model.ResumenWebDTO;

public interface FinanceWebService {
    ResumenWebDTO obtenerResumenCuenta(Long id);
}