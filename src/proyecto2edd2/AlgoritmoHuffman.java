/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */

//gestiona la compresión y descompresión de textos utilizando el algoritmo Huffman
public class AlgoritmoHuffman {
    /**
     * Comprime el texto plano y devuelve un formato serializado (Cabecera de frecuencias + Bits).
     */
    public static String comprimirTexto(String textoOriginal) {
        if (textoOriginal == null || textoOriginal.isEmpty()) {
            return "";
        }

        ListaDiccionario diccionario = new ListaDiccionario();
        for (int i = 0; i < textoOriginal.length(); i++) {
            diccionario.registrarCaracter(textoOriginal.charAt(i));
        }

        MonticuloMinimoHuffman monticulo = new MonticuloMinimoHuffman();
        StringBuilder cabecera = new StringBuilder();

        NodoDiccionario cursor = diccionario.getCabeza();
        while (cursor != null) {
            monticulo.insertar(new NodoHuffman(cursor.getSimbolo(), cursor.getFrecuencia()));
            if (cabecera.length() > 0) {
                cabecera.append(";");
            }
            cabecera.append((int) cursor.getSimbolo()).append(":").append(cursor.getFrecuencia());
            cursor = cursor.getSiguiente();
        }

        NodoHuffman raizArbol = construirArbol(monticulo);

        if (raizArbol.esHoja()) {
            diccionario.asignarCodigo(raizArbol.getSimbolo(), "0");
        } else {
            generarCodigosRecursivo(raizArbol, "", diccionario);
        }

        StringBuilder cuerpoComprimido = new StringBuilder();
        for (int i = 0; i < textoOriginal.length(); i++) {
            cuerpoComprimido.append(diccionario.obtenerCodigo(textoOriginal.charAt(i)));
        }

        return cabecera.toString() + "\n" + cuerpoComprimido.toString();
    }

    /**
     * Reconstruye el árbol de Huffman leyendo la cabecera y descomprime la secuencia de bits.
     */
    public static String descomprimirTexto(String contenidoArchivoComprimido) {
        if (contenidoArchivoComprimido == null || !contenidoArchivoComprimido.contains("\n")) {
            return "[Archivo comprimido inválido o vacío]";
        }

        int salto = contenidoArchivoComprimido.indexOf('\n');
        String lineaCabecera = contenidoArchivoComprimido.substring(0, salto).trim();
        String secuenciaBits = contenidoArchivoComprimido.substring(salto + 1).trim();

        MonticuloMinimoHuffman monticulo = new MonticuloMinimoHuffman();
        cargarCabeceraEnMonticulo(lineaCabecera, monticulo);

        NodoHuffman raiz = construirArbol(monticulo);
        if (raiz == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        if (raiz.esHoja()) {
            for (int i = 0; i < raiz.getFrecuencia(); i++) {
                resultado.append(raiz.getSimbolo());
            }
            return resultado.toString();
        }

        NodoHuffman actual = raiz;
        for (int i = 0; i < secuenciaBits.length(); i++) {
            char bit = secuenciaBits.charAt(i);
            if (bit == '0') {
                actual = actual.getIzquierdo();
            } else {
                actual = actual.getDerecho();
            }

            if (actual.esHoja()) {
                resultado.append(actual.getSimbolo());
                actual = raiz;
            }
        }
        return resultado.toString();
    }

    /**
     * Recorre la cadena de la cabecera ("codigoAscii:frecuencia;...") e inserta cada par
     * en el montículo de nodos sin crear arreglos intermedios.
     */
    private static void cargarCabeceraEnMonticulo(String lineaCabecera, MonticuloMinimoHuffman monticulo) {
        int inicio = 0;
        int longitud = lineaCabecera.length();

        while (inicio < longitud) {
            int finPar = lineaCabecera.indexOf(';', inicio);
            if (finPar == -1) {
                finPar = longitud;
            }

            String tokenPar = lineaCabecera.substring(inicio, finPar);
            int posDosPuntos = tokenPar.indexOf(':');
            if (posDosPuntos != -1) {
                char simbolo = (char) Integer.parseInt(tokenPar.substring(0, posDosPuntos));
                int frec = Integer.parseInt(tokenPar.substring(posDosPuntos + 1));
                monticulo.insertar(new NodoHuffman(simbolo, frec));
            }

            inicio = finPar + 1;
        }
    }

    /**
     * Combina los dos nodos de menor frecuencia hasta que queda un único árbol de Huffman.
     */
    private static NodoHuffman construirArbol(MonticuloMinimoHuffman monticulo) {
        while (monticulo.getTamano() > 1) {
            NodoHuffman menor1 = monticulo.extraerMinimo();
            NodoHuffman menor2 = monticulo.extraerMinimo();

            NodoHuffman padre = new NodoHuffman('\0', menor1.getFrecuencia() + menor2.getFrecuencia());
            padre.setIzquierdo(menor1);
            padre.setDerecho(menor2);
            monticulo.insertar(padre);
        }
        return monticulo.extraerMinimo();
    }

    private static void generarCodigosRecursivo(NodoHuffman nodo, String camino, ListaDiccionario diccionario) {
        if (nodo == null) {
            return;
        }
        if (nodo.esHoja()) {
            diccionario.asignarCodigo(nodo.getSimbolo(), camino);
            return;
        }
        generarCodigosRecursivo(nodo.getIzquierdo(), camino + "0", diccionario);
        generarCodigosRecursivo(nodo.getDerecho(), camino + "1", diccionario);
    }
}
