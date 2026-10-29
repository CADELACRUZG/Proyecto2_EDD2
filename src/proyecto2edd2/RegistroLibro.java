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
 * Representa los datos de un libro disponible en el catálogo del servidor.
 */
public class RegistroLibro {
    private String codigoIsbn;
    private String tituloObra;

    public RegistroLibro(String codigoIsbn, String tituloObra) {
        this.codigoIsbn = codigoIsbn;
        this.tituloObra = tituloObra;
    }

    public String getCodigoIsbn() { return codigoIsbn; }
    public void setCodigoIsbn(String codigoIsbn) { this.codigoIsbn = codigoIsbn; }

    public String getTituloObra() { return tituloObra; }
    public void setTituloObra(String tituloObra) { this.tituloObra = tituloObra; }
}
