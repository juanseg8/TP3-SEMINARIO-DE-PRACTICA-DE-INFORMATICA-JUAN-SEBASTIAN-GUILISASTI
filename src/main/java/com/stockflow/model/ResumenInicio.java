package com.stockflow.model;

public class ResumenInicio {
    private final int deficit;
    private final int sobrestock;
    private final int recomendacionesPendientes;
    private final int transferenciasEnTransito;

    public ResumenInicio(int deficit, int sobrestock, int recomendacionesPendientes,
                         int transferenciasEnTransito) {
        this.deficit = deficit;
        this.sobrestock = sobrestock;
        this.recomendacionesPendientes = recomendacionesPendientes;
        this.transferenciasEnTransito = transferenciasEnTransito;
    }

    public int getDeficit() { return deficit; }
    public int getSobrestock() { return sobrestock; }
    public int getRecomendacionesPendientes() { return recomendacionesPendientes; }
    public int getTransferenciasEnTransito() { return transferenciasEnTransito; }
}
