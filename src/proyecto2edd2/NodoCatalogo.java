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
 * Nodo para enlazar dinámicamente los libros dentro del catálogo.
 */
public class NodoCatalogo {
    private RegistroLibro libro;
    private NodoCatalogo siguiente;

    public NodoCatalogo(RegistroLibro libro) {
        this.libro = libro;
        this.siguiente = null;
    }

    public RegistroLibro getLibro() { return libro; }
    public void setLibro(RegistroLibro libro) { this.libro = libro; }

    public NodoCatalogo getSiguiente() { return siguiente; }
    public void setSiguiente(NodoCatalogo siguiente) { this.siguiente = siguiente; }
}
