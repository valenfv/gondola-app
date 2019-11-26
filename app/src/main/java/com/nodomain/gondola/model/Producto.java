package com.nodomain.gondola.model;

import java.io.Serializable;

public class Producto implements Serializable {

    private int id = 0;
    private String barCode;
    private String product;
    private String briefing;
    private String precio;
    private String lista;
    private String imagen;

    public String getImagen() {
        return imagen;
    }
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    public String getBarCode() {
        return barCode;
    }
    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public String getBriefing() {
        return briefing;
    }
    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }
    public String getPrecio() {
        return precio;
    }
    public void setPrecio(String precio) {
        this.precio = precio;
    }
    public String getLista() {
        return lista;
    }
    public void setLista(String lista) {
        this.lista = lista;
    }
}
