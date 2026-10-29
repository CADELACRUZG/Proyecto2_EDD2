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
 * Representa un nodo dentro del Montículo Mínimo dinámico (con punteros al padre e hijos).
 */
public class NodoMonticulo {
    private NodoHuffman dato;
    private NodoMonticulo padre;
    private NodoMonticulo izquierdo;
    private NodoMonticulo derecho;

    public NodoMonticulo(NodoHuffman dato) {
        this.dato = dato;
        this.padre = null;
        this.izquierdo = null;
        this.derecho = null;
    }

    public NodoHuffman getDato() { return dato; }
    public void setDato(NodoHuffman dato) { this.dato = dato; }

    public NodoMonticulo getPadre() { return padre; }
    public void setPadre(NodoMonticulo padre) { this.padre = padre; }

    public NodoMonticulo getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoMonticulo izquierdo) { this.izquierdo = izquierdo; }

    public NodoMonticulo getDerecho() { return derecho; }
    public void setDerecho(NodoMonticulo derecho) { this.derecho = derecho; }
}
