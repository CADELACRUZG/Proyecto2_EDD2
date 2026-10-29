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
 * Tabla de dispersión (Hash Table) implementada como una multilista de nodos dinámicos.
 */
public class TablaUsuarios {
    private NodoCubeta primeraCubeta;
    private int capacidad;

    public TablaUsuarios() {
        this.primeraCubeta = null;
        this.capacidad = 53;
    }

    public TablaUsuarios(int capacidad) {
        this.primeraCubeta = null;
        this.capacidad = capacidad > 0 ? capacidad : 53;
    }

    public NodoCubeta getPrimeraCubeta() { return primeraCubeta; }
    public void setPrimeraCubeta(NodoCubeta primeraCubeta) { this.primeraCubeta = primeraCubeta; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    /**
     * Calcula el índice de dispersión modular a partir de los caracteres del nombre de usuario.
     */
    private int calcularIndice(String clave) {
        int acumulador = 0;
        for (int i = 0; i < clave.length(); i++) {
            acumulador = (31 * acumulador + clave.charAt(i)) % capacidad;
        }
        return Math.abs(acumulador);
    }

    /**
     * Busca una cubeta por su índice o la crea dinámicamente si aún no existe.
     */
    private NodoCubeta obtenerOCrearCubeta(int indiceBuscado) {
        if (primeraCubeta == null) {
            primeraCubeta = new NodoCubeta(indiceBuscado);
            return primeraCubeta;
        }

        NodoCubeta actual = primeraCubeta;
        NodoCubeta anterior = null;

        while (actual != null) {
            if (actual.getIndiceCubeta() == indiceBuscado) {
                return actual;
            }
            anterior = actual;
            actual = actual.getSiguienteCubeta();
        }

        NodoCubeta nuevaCubeta = new NodoCubeta(indiceBuscado);
        anterior.setSiguienteCubeta(nuevaCubeta);
        return nuevaCubeta;
    }

    /**
     * Inserta un usuario en la cubeta correspondiente manejando colisiones por encadenamiento.
     */
    public void insertar(String nombre, String hash, boolean premium) {
        int pos = calcularIndice(nombre);
        NodoCubeta cubeta = obtenerOCrearCubeta(pos);

        // Si el usuario ya existe en la cubeta, actualiza sus datos
        CuentaUsuario cursor = cubeta.getPrimerUsuario();
        while (cursor != null) {
            if (cursor.getNombreUsuario().equals(nombre)) {
                cursor.setResumenClave(hash);
                cursor.setEsPremium(premium);
                return;
            }
            cursor = cursor.getSiguiente();
        }

        // Inserción al inicio de la lista enlazada de la cubeta
        CuentaUsuario nuevoUsuario = new CuentaUsuario(nombre, hash, premium);
        nuevoUsuario.setSiguiente(cubeta.getPrimerUsuario());
        cubeta.setPrimerUsuario(nuevoUsuario);
    }

    /**
     * Localiza la cubeta correspondiente al hash del nombre y busca el nodo del usuario.
     */
    public CuentaUsuario buscar(String nombre) {
        int pos = calcularIndice(nombre);
        NodoCubeta cubetaActual = primeraCubeta;

        while (cubetaActual != null) {
            if (cubetaActual.getIndiceCubeta() == pos) {
                CuentaUsuario usuarioActual = cubetaActual.getPrimerUsuario();
                while (usuarioActual != null) {
                    if (usuarioActual.getNombreUsuario().equals(nombre)) {
                        return usuarioActual;
                    }
                    usuarioActual = usuarioActual.getSiguiente();
                }
                return null;
            }
            cubetaActual = cubetaActual.getSiguienteCubeta();
        }
        return null;
    }
}
