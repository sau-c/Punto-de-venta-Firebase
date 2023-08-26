package com.example.puntodeventa.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Accesorio {
    String id;
    String producto;
    String marca;
    double precio;
    int stock;
    String tiempo;

    public Accesorio() {
    }
    public Accesorio(String id, String producto, String marca, double precio, int stock) {
        this.id = id;
        this.producto = producto;
        this.marca = marca;
        this.precio = precio;
        this.stock = stock;
    }

    public Accesorio(String id, String producto, String marca, double precio, int stock, String tiempo) {
        this.id = id;
        this.producto = producto;
        this.marca = marca;
        this.precio = precio;
        this.stock = stock;
        this.tiempo = tiempo;
    }

    public String getId() {
        return id;
    }
    public String getProducto() {
        return producto;
    }
    public String getMarca() {
        return marca;
    }
    public double getPrecio() {
        return precio;
    }
    public String getTiempo() { return tiempo; }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setTiempo(String tiempo) { this.tiempo = tiempo; }
}
