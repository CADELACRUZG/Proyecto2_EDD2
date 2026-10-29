/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */

//esta clase se implementa como arreglo dinámico propio para gestionar el catálogo de libros sin arraylist
public class VectorCatalogo {
    private RegistroLibro[] elementos;
    private int cantidad;

    public VectorCatalogo() {
        this.elementos = new RegistroLibro[10];
        this.cantidad = 0;
    }

    public void agregar(RegistroLibro libro) {
        if (cantidad == elementos.length) {
            RegistroLibro[] nuevoArreglo = new RegistroLibro[elementos.length * 2];
            for (int i = 0; i < cantidad; i++) {
                nuevoArreglo[i] = elementos[i];
            }
            elementos = nuevoArreglo;
        }
        elementos[cantidad++] = libro;
    }

    public int obtenerTamano() { return cantidad; }

    public RegistroLibro obtenerPorIndice(int indice) {
        if (indice >= 0 && indice < cantidad) {
            return elementos[indice];
        }
        return null;
    }
}
