/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */

/**
 * Nodo simple para almacenar nombres de archivos sin usar ArrayList.
 */
public class NodoNombreArchivo {
    private String nombre;
    private NodoNombreArchivo siguiente;

    public NodoNombreArchivo(String nombre) {
        this.nombre = nombre;
        this.siguiente = null;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public NodoNombreArchivo getSiguiente() { return siguiente; }
    public void setSiguiente(NodoNombreArchivo siguiente) { this.siguiente = siguiente; }
}
