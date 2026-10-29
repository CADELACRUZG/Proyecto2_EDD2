/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

/**
 *
 * @author chris
 */
public class CuentaUsuario {
    private String nombreUsuario;
    private String resumenClave;
    private boolean esPremium;
    private CuentaUsuario siguiente;

    public CuentaUsuario(String nombreUsuario, String resumenClave, boolean esPremium) {
        this.nombreUsuario = nombreUsuario;
        this.resumenClave = resumenClave;
        this.esPremium = esPremium;
        this.siguiente = null;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getResumenClave() { return resumenClave; }
    public void setResumenClave(String resumenClave) { this.resumenClave = resumenClave; }

    public boolean isEsPremium() { return esPremium; }
    public void setEsPremium(boolean esPremium) { this.esPremium = esPremium; }

    public CuentaUsuario getSiguiente() { return siguiente; }
    public void setSiguiente(CuentaUsuario siguiente) { this.siguiente = siguiente; }
}
