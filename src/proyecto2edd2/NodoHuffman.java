/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */

//Nodo del árbol para la codificación de Huffman
public class NodoHuffman {
    private char simbolo;
    private int frecuencia;
    private NodoHuffman izquierdo;
    private NodoHuffman derecho;

    public NodoHuffman(char simbolo, int frecuencia) {
        this.simbolo = simbolo;
        this.frecuencia = frecuencia;
        this.izquierdo = null;
        this.derecho = null;
    }

    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }

    public char getSimbolo() { return simbolo; }
    public void setSimbolo(char simbolo) { this.simbolo = simbolo; }

    public int getFrecuencia() { return frecuencia; }
    public void setFrecuencia(int frecuencia) { this.frecuencia = frecuencia; }

    public NodoHuffman getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoHuffman izquierdo) { this.izquierdo = izquierdo; }

    public NodoHuffman getDerecho() { return derecho; }
    public void setDerecho(NodoHuffman derecho) { this.derecho = derecho; }
}
