/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */
public class ListaDiccionario {
    private NodoDiccionario cabeza;

    public ListaDiccionario() {
        this.cabeza = null;
    }

    public NodoDiccionario getCabeza() { return cabeza; }
    public void setCabeza(NodoDiccionario cabeza) { this.cabeza = cabeza; }

    /**
     * Incrementa la frecuencia del carácter si ya existe o crea un nuevo nodo al final.
     */
    public void registrarCaracter(char c) {
        if (cabeza == null) {
            cabeza = new NodoDiccionario(c);
            return;
        }
        NodoDiccionario actual = cabeza;
        NodoDiccionario anterior = null;
        while (actual != null) {
            if (actual.getSimbolo() == c) {
                actual.setFrecuencia(actual.getFrecuencia() + 1);
                return;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        anterior.setSiguiente(new NodoDiccionario(c));
    }

    public void asignarCodigo(char c, String codigo) {
        NodoDiccionario actual = cabeza;
        while (actual != null) {
            if (actual.getSimbolo() == c) {
                actual.setCodigoBinario(codigo);
                return;
            }
            actual = actual.getSiguiente();
        }
    }

    public String obtenerCodigo(char c) {
        NodoDiccionario actual = cabeza;
        while (actual != null) {
            if (actual.getSimbolo() == c) {
                return actual.getCodigoBinario();
            }
            actual = actual.getSiguiente();
        }
        return "";
    }
}
