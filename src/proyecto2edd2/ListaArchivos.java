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
 * Lista simplemente enlazada que reemplaza a List<String> en Conexion.java.
 */
public class ListaArchivos {
    private NodoNombreArchivo cabeza;
    private NodoNombreArchivo cola;
    private int tamano;

    public ListaArchivos() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    public void agregar(String nombreArchivo) {
        NodoNombreArchivo nuevo = new NodoNombreArchivo(nombreArchivo);
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.setSiguiente(nuevo);
            cola = nuevo;
        }
        tamano++;
    }
    
    public int obtenerTamano() { return tamano; }

    public String obtener(int indice) {
        if (indice < 0 || indice >= tamano) return null;
        NodoNombreArchivo actual = cabeza;
        int i = 0;
        while (actual != null && i < indice) {
            actual = actual.getSiguiente();
            i++;
        }
        return (actual != null) ? actual.getNombre() : null;
    }
}
