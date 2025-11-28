package org.example.models;

public class Producto {
    private int id;
    private String nombre;
    private String tipo;
    private double precio;

    // Constructor, getters y setters
    public Producto(int id, String nombre, String tipo, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.precio = precio;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override
    public String toString() {
        return nombre + " - " + precio + "€ (" + tipo + ")";
    }
}