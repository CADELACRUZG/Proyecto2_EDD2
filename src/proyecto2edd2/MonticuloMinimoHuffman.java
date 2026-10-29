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
 * Cola de prioridad implementada como un Montículo Mínimo (Min-Heap)
 * utilizando exclusivamente nodos enlazados (sin arreglos primitivos).
 */
public class MonticuloMinimoHuffman {
    private NodoMonticulo raiz;
    private int tamano;

    public MonticuloMinimoHuffman() {
        this.raiz = null;
        this.tamano = 0;
    }

    public NodoMonticulo getRaiz() { return raiz; }
    public void setRaiz(NodoMonticulo raiz) { this.raiz = raiz; }

    public int getTamano() { return tamano; }
    public void setTamano(int tamano) { this.tamano = tamano; }

    /**
     * Localiza un nodo según su número de posición (1..tamano) en el árbol binario
     * completo recorriendo los bits de la posición desde la raíz.
     */
    private NodoMonticulo obtenerNodoEnPosicion(int posicion) {
        if (posicion < 1 || posicion > tamano) {
            return null;
        }
        NodoMonticulo actual = raiz;
        int mascara = Integer.highestOneBit(posicion) >> 1;

        while (mascara > 0 && actual != null) {
            if ((posicion & mascara) == 0) {
                actual = actual.getIzquierdo();
            } else {
                actual = actual.getDerecho();
            }
            mascara >>= 1;
        }
        return actual;
    }

    /**
     * Inserta un nuevo nodo de Huffman en la siguiente posición disponible del árbol
     * y restaura la propiedad de montículo mínimo flotando hacia arriba.
     */
    public void insertar(NodoHuffman elemento) {
        NodoMonticulo nuevo = new NodoMonticulo(elemento);
        tamano++;

        if (tamano == 1) {
            raiz = nuevo;
            return;
        }

        // El padre de la posición 'tamano' se ubica en 'tamano / 2'
        NodoMonticulo padre = obtenerNodoEnPosicion(tamano / 2);
        nuevo.setPadre(padre);

        if (tamano % 2 == 0) {
            padre.setIzquierdo(nuevo);
        } else {
            padre.setDerecho(nuevo);
        }

        flotar(nuevo);
    }

    /**
     * Extrae y retorna el nodo de Huffman con la menor frecuencia ubicado en la raíz.
     */
    public NodoHuffman extraerMinimo() {
        if (tamano == 0 || raiz == null) {
            return null;
        }

        NodoHuffman minimo = raiz.getDato();

        if (tamano == 1) {
            raiz = null;
            tamano = 0;
            return minimo;
        }

        NodoMonticulo ultimoNodo = obtenerNodoEnPosicion(tamano);
        raiz.setDato(ultimoNodo.getDato());

        // Desconectar el último nodo de su padre
        NodoMonticulo padreUltimo = ultimoNodo.getPadre();
        if (padreUltimo.getIzquierdo() == ultimoNodo) {
            padreUltimo.setIzquierdo(null);
        } else {
            padreUltimo.setDerecho(null);
        }
        ultimoNodo.setPadre(null);

        tamano--;
        hundir(raiz);

        return minimo;
    }

    /**
     * Sube el nodo por sus punteros 'padre' mientras su frecuencia sea menor.
     */
    private void flotar(NodoMonticulo actual) {
        while (actual.getPadre() != null
                && actual.getDato().getFrecuencia() < actual.getPadre().getDato().getFrecuencia()) {
            intercambiarDatos(actual, actual.getPadre());
            actual = actual.getPadre();
        }
    }

    /**
     * Baja el nodo por sus punteros 'izquierdo' y 'derecho' hacia el hijo de menor frecuencia.
     */
    private void hundir(NodoMonticulo actual) {
        while (actual != null) {
            NodoMonticulo menor = actual;
            NodoMonticulo izq = actual.getIzquierdo();
            NodoMonticulo der = actual.getDerecho();

            if (izq != null && izq.getDato().getFrecuencia() < menor.getDato().getFrecuencia()) {
                menor = izq;
            }
            if (der != null && der.getDato().getFrecuencia() < menor.getDato().getFrecuencia()) {
                menor = der;
            }

            if (menor != actual) {
                intercambiarDatos(actual, menor);
                actual = menor;
            } else {
                break;
            }
        }
    }

    private void intercambiarDatos(NodoMonticulo nodoA, NodoMonticulo nodoB) {
        NodoHuffman temporal = nodoA.getDato();
        nodoA.setDato(nodoB.getDato());
        nodoB.setDato(temporal);
    }
}
