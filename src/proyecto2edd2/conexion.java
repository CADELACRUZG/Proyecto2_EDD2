/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author chris
 */

/**
 * Gestiona la conexión cliente-servidor mediante Sockets TCP en el puerto 5000
 * y la transferencia de archivos por flujos de datos sin utilizar colecciones de Java.
 */

public class conexion {
    private static final int PUERTO_SERVIDOR = 5000;

    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private Path carpetaDescargas;
    private Path carpetaComprimidos;

    /**
     * Constructor que crea los directorios locales del cliente e inicia la conexión
     * con el servidor en el puerto 5000.
     *
     * @param ip Dirección IPv4 del servidor.
     * @param direccionDescargas Ruta local donde se guardarán las copias recibidas.
     * @throws IOException Si ocurre un error de red o al crear las carpetas locales.
     */
    public conexion(String ip, String direccionDescargas) throws IOException {
        this.carpetaDescargas = Path.of(direccionDescargas)
                .toAbsolutePath()
                .normalize();

        this.carpetaComprimidos = Path.of(direccionDescargas, "comprimidos_huffman")
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(this.carpetaDescargas);
        Files.createDirectories(this.carpetaComprimidos);

        this.socket = new Socket(ip, PUERTO_SERVIDOR);
        this.entrada = new DataInputStream(this.socket.getInputStream());
        this.salida = new DataOutputStream(this.socket.getOutputStream());
    }

    // Métodos GET y SET para cumplir con el encapsulamiento
    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }

    public DataInputStream getEntrada() { return entrada; }
    public void setEntrada(DataInputStream entrada) { this.entrada = entrada; }

    public DataOutputStream getSalida() { return salida; }
    public void setSalida(DataOutputStream salida) { this.salida = salida; }

    public Path getCarpetaDescargas() { return carpetaDescargas; }
    public void setCarpetaDescargas(Path carpetaDescargas) { this.carpetaDescargas = carpetaDescargas; }

    public Path getCarpetaComprimidos() { return carpetaComprimidos; }
    public void setCarpetaComprimidos(Path carpetaComprimidos) { this.carpetaComprimidos = carpetaComprimidos; }

    /**
     * Solicita al servidor el listado de archivos de una carpeta ("Usuarios", "Catalogo" o "Libros")
     * y los almacena en una lista simplemente enlazada propia (ListaArchivos).
     *
     * @param carpeta Nombre de la carpeta en el servidor.
     * @return ListaArchivos con los nombres de los archivos disponibles.
     * @throws IOException Si falla la comunicación por el socket.
     */
    public ListaArchivos listarArchivos(String carpeta) throws IOException {
        ListaArchivos archivos = new ListaArchivos();

        salida.writeUTF("LISTAR");
        salida.writeUTF(carpeta);
        salida.flush();

        int cantidadArchivos = entrada.readInt();

        for (int i = 0; i < cantidadArchivos; i++) {
            String nombreArchivo = entrada.readUTF();
            archivos.agregar(nombreArchivo);
        }

        return archivos;
    }

    /**
     * Descarga un archivo desde una carpeta del servidor ("Usuarios", "Catalogo" o "Libros")
     * hacia la carpeta local del cliente mediante un flujo de bytes y retorna su ruta local.
     *
     * @param carpeta Nombre de la carpeta en el servidor.
     * @param nombreArchivo Nombre del archivo a transferir (ej. "SHA256.txt", "ISBN001.bin").
     * @return Path del archivo guardado localmente, o null si no existe o es inválido.
     * @throws IOException Si ocurre un error durante la lectura/escritura del flujo.
     */
    public Path descargarArchivo(String carpeta, String nombreArchivo) throws IOException {
        salida.writeUTF("DESCARGAR");
        salida.writeUTF(carpeta);
        salida.writeUTF(nombreArchivo);
        salida.flush();

        boolean existe = entrada.readBoolean();

        if (!existe) {
            System.out.println("El archivo '" + nombreArchivo + "' no existe o no está disponible en '" + carpeta + "'.");
            return null;
        }

        long tamano = entrada.readLong();

        Path destino = carpetaDescargas
                .resolve(nombreArchivo)
                .normalize();

        if (!destino.startsWith(carpetaDescargas)) {
            System.out.println("Nombre de archivo no válido.");
            return null;
        }

        try (OutputStream archivo = Files.newOutputStream(destino)) {
            byte[] buffer = new byte[8192];
            long bytesRestantes = tamano;

            while (bytesRestantes > 0) {
                int cantidad = (int) Math.min(buffer.length, bytesRestantes);
                entrada.readFully(buffer, 0, cantidad);
                archivo.write(buffer, 0, cantidad);
                bytesRestantes = bytesRestantes - cantidad;
            }
        }

        System.out.println("\nDescarga completada.");
        System.out.println("Archivo: " + nombreArchivo);
        System.out.println("Tamaño: " + tamano + " bytes");
        System.out.println("Ubicación local: " + destino);

        return destino;
    }

    /**
     * Notifica al servidor la finalización de la sesión con el comando "SALIR"
     * y cierra los flujos de datos y el socket.
     */
    public void cerrarConexion() {
        try {
            if (salida != null) {
                salida.writeUTF("SALIR");
                salida.flush();
            }
            if (entrada != null) {
                entrada.close();
            }
            if (salida != null) {
                salida.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
