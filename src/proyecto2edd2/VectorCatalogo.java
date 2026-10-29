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
 * Lista simplemente enlazada para almacenar y recorrer el catálogo de libros sin usar arreglos.
 */
public class VectorCatalogo {
    private NodoCatalogo cabeza;
    private NodoCatalogo cola;
    private int cantidad;

    public VectorCatalogo() {
        this.cabeza = null;
        this.cola = null;
        this.cantidad = 0;
    }

    public NodoCatalogo getCabeza() { return cabeza; }
    public void setCabeza(NodoCatalogo cabeza) { this.cabeza = cabeza; }

    public NodoCatalogo getCola() { return cola; }
    public void setCola(NodoCatalogo cola) { this.cola = cola; }

    /**
     * Inserta un nuevo libro al final de la lista enlazada en tiempo O(1).
     */
    public void agregar(RegistroLibro libro) {
        NodoCatalogo nuevoNodo = new NodoCatalogo(libro);
        if (cabeza == null) {
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            cola.setSiguiente(nuevoNodo);
            cola = nuevoNodo;
        }
        cantidad++;
    }

    public int obtenerTamano() {
        return cantidad;
    }

    /**
     * Recorre los nodos enlazados hasta la posición solicitada (0..cantidad-1).
     */
    public RegistroLibro obtenerPorIndice(int indice) {
        if (indice < 0 || indice >= cantidad) {
            return null;
        }
        NodoCatalogo actual = cabeza;
        int contador = 0;
        while (actual != null && contador < indice) {
            actual = actual.getSiguiente();
            contador++;
        }
        return (actual != null) ? actual.getLibro() : null;
    }
}
