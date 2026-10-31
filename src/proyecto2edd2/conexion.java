/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author chris
 */
/**
 * Gestiona la conexión con el servidor (vía Carpeta Compartida o Socket TCP)
 * y la transferencia de archivos mediante flujos de datos.
 */

public class conexion {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private boolean modoSocket;

    private String ipServidor;
    private String recursoCompartido;
    private Path carpetaDescargas;
    private Path carpetaComprimidos;

    /**
     * Constructor que inicializa las carpetas locales y conecta con el servidor.
     */
    public conexion(String ip, String recursoCompartido, String direccionDescargas, boolean usarSocket)
            throws IOException {

        this.ipServidor = ip;
        this.recursoCompartido = recursoCompartido;
        this.modoSocket = usarSocket;

        this.carpetaDescargas = Path.of(direccionDescargas)
                .toAbsolutePath()
                .normalize();
        this.carpetaComprimidos = Path.of(direccionDescargas, "comprimidos_huffman")
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(this.carpetaDescargas);
        Files.createDirectories(this.carpetaComprimidos);

        if (modoSocket) {
            socket = new Socket(ip, 5000);
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());
        } else {
            Path rutaRed = obtenerRutaServidor("");
            if (!Files.exists(rutaRed)) {
                throw new IOException("No se encontró la carpeta compartida en: " + rutaRed);
            }
        }
    }

    public Path getCarpetaDescargas() { return carpetaDescargas; }
    public Path getCarpetaComprimidos() { return carpetaComprimidos; }

    /**
     * Construye la ruta hacia el servidor (local o de red \\IP\Carpeta).
     */
    public Path obtenerRutaServidor(String subRuta) {
        String base;
        if (ipServidor.equalsIgnoreCase("localhost") || ipServidor.equals("127.0.0.1")) {
            base = recursoCompartido;
        } else if (recursoCompartido.startsWith("\\\\") || recursoCompartido.contains(":\\")) {
            base = recursoCompartido;
        } else {
            base = "\\\\" + ipServidor + "\\" + recursoCompartido;
        }
        return subRuta.isEmpty() ? Path.of(base) : Path.of(base, subRuta);
    }

    /**
     * Lista los archivos de una subcarpeta sin usar java.util.List ni ArrayList.
     */
    public ListaArchivos listarArchivos(String subCarpeta) throws IOException {
        ListaArchivos lista = new ListaArchivos();

        if (modoSocket) {
            salida.writeUTF("LISTAR");
            salida.writeUTF(subCarpeta);
            salida.flush();

            int cantidadArchivos = entrada.readInt();
            for (int i = 0; i < cantidadArchivos; i++) {
                lista.agregar(entrada.readUTF());
            }
        } else {
            File directorio = obtenerRutaServidor(subCarpeta).toFile();
            File[] archivos = directorio.listFiles();
            if (archivos != null) {
                for (int i = 0; i < archivos.length; i++) {
                    if (archivos[i].isFile()) {
                        lista.agregar(archivos[i].getName());
                    }
                }
            }
        }
        return lista;
    }

    /**
     * Transfiere un archivo desde el servidor hacia la carpeta local del cliente
     * utilizando un buffer de 8192 bytes para garantizar que viaje por la aplicación.
     */
    public Path descargarArchivo(String subCarpeta, String nombreArchivo) throws IOException {
        Path destino = carpetaDescargas.resolve(nombreArchivo).normalize();

        if (!destino.startsWith(carpetaDescargas)) {
            throw new IOException("Nombre de archivo no válido.");
        }

        if (modoSocket) {
            salida.writeUTF("DESCARGAR");
            salida.writeUTF(subCarpeta);
            salida.writeUTF(nombreArchivo);
            salida.flush();

            boolean existe = entrada.readBoolean();
            if (!existe) {
                throw new IOException("El archivo no existe en el servidor: " + nombreArchivo);
            }

            long tamano = entrada.readLong();
            try (OutputStream archivoSalida = Files.newOutputStream(destino)) {
                byte[] buffer = new byte[8192];
                long bytesRestantes = tamano;
                while (bytesRestantes > 0) {
                    int cantidad = (int) Math.min(buffer.length, bytesRestantes);
                    entrada.readFully(buffer, 0, cantidad);
                    archivoSalida.write(buffer, 0, cantidad);
                    bytesRestantes -= cantidad;
                }
            }
        } else {
            Path origen = subCarpeta.isEmpty()
                    ? obtenerRutaServidor(nombreArchivo)
                    : obtenerRutaServidor(subCarpeta).resolve(nombreArchivo);

            if (!Files.exists(origen)) {
                throw new IOException("El archivo no está disponible en el servidor: " + origen);
            }

            long tamano = Files.size(origen);
            try (InputStream flujoEntrada = Files.newInputStream(origen);
                 OutputStream archivoSalida = Files.newOutputStream(destino)) {
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = flujoEntrada.read(buffer)) != -1) {
                    archivoSalida.write(buffer, 0, leidos);
                }
            }
            System.out.println("\nTransferencia completada hacia carpeta local.");
            System.out.println("Archivo cifrado (evidencia): " + nombreArchivo + " (" + tamano + " bytes)");
            System.out.println("Ubicación local: " + destino);
        }

        return destino;
    }

    public void cerrarConexion() {
        try {
            if (modoSocket) {
                if (salida != null) {
                    salida.writeUTF("SALIR");
                    salida.flush();
                }
                if (entrada != null) entrada.close();
                if (salida != null) salida.close();
                if (socket != null) socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
