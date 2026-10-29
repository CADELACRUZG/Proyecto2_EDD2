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
 * Representa una cubeta (bucket) dentro de la tabla hash dinámica sin usar arreglos.
 * Cada cubeta se enlaza con la siguiente cubeta activa y contiene su propia lista de usuarios.
 */
public class NodoCubeta {
    private int indiceCubeta;
    private CuentaUsuario primerUsuario;
    private NodoCubeta siguienteCubeta;

    public NodoCubeta(int indiceCubeta) {
        this.indiceCubeta = indiceCubeta;
        this.primerUsuario = null;
        this.siguienteCubeta = null;
    }

    public int getIndiceCubeta() { return indiceCubeta; }
    public void setIndiceCubeta(int indiceCubeta) { this.indiceCubeta = indiceCubeta; }

    public CuentaUsuario getPrimerUsuario() { return primerUsuario; }
    public void setPrimerUsuario(CuentaUsuario primerUsuario) { this.primerUsuario = primerUsuario; }

    public NodoCubeta getSiguienteCubeta() { return siguienteCubeta; }
    public void setSiguienteCubeta(NodoCubeta siguienteCubeta) { this.siguienteCubeta = siguienteCubeta; }
}
