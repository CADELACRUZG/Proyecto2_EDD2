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
 * Nodo para almacenar la frecuencia y el código binario de cada carácter sin usar arreglos.
 */
public class NodoDiccionario {
    private char simbolo;
    private int frecuencia;
    private String codigoBinario;
    private NodoDiccionario siguiente;

    public NodoDiccionario(char simbolo) {
        this.simbolo = simbolo;
        this.frecuencia = 1;
        this.codigoBinario = "";
        this.siguiente = null;
    }

    public char getSimbolo() { return simbolo; }
    public void setSimbolo(char simbolo) { this.simbolo = simbolo; }

    public int getFrecuencia() { return frecuencia; }
    public void setFrecuencia(int frecuencia) { this.frecuencia = frecuencia; }

    public String getCodigoBinario() { return codigoBinario; }
    public void setCodigoBinario(String codigoBinario) { this.codigoBinario = codigoBinario; }

    public NodoDiccionario getSiguiente() { return siguiente; }
    public void setSiguiente(NodoDiccionario siguiente) { this.siguiente = siguiente; }
}
