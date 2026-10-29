/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */
public class TablaUsuarios {
    private CuentaUsuario[] cubetas;
    private int capacidad;

    public TablaUsuarios(int capacidad) {
        this.capacidad = capacidad;
        this.cubetas = new CuentaUsuario[capacidad];
    }

    private int calcularIndice(String clave) {
        int acumulador = 0;
        for (int i = 0; i < clave.length(); i++) {
            acumulador = (31 * acumulador + clave.charAt(i)) % capacidad;
        }
        return Math.abs(acumulador);
    }

    public void insertar(String nombre, String hash, boolean premium) {
        int pos = calcularIndice(nombre);
        CuentaUsuario nuevo = new CuentaUsuario(nombre, hash, premium);
        nuevo.setSiguiente(cubetas[pos]);
        cubetas[pos] = nuevo;
    }

    public CuentaUsuario buscar(String nombre) {
        int pos = calcularIndice(nombre);
        CuentaUsuario actual = cubetas[pos];
        while (actual != null) {
            if (actual.getNombreUsuario().equals(nombre)) {
                return actual;
            }
            actual = actual.getSiguiente();
        }
        return null;
    }
}
