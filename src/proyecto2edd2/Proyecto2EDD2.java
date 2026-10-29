/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyecto2edd2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

/**
 *
 * @author chris
 */

/**
 * Clase principal que gestiona la conexión al servidor, transferencia de archivos y menús.
 */ 
public class Proyecto2EDD2 {
    
    private static String rutaServidor = "";
    private static final String CARPETA_LOCAL_CIFRADOS = "local_cifrados";
    private static final String CARPETA_LOCAL_COMPRIMIDOS = "local_comprimidos";

    private static TablaUsuarios usuariosRegistrados = new TablaUsuarios(53);
    private static VectorCatalogo catalogoLibros = new VectorCatalogo();
    private static Scanner entrada = new Scanner(System.in);
    
    public static void main(String[] args) {
        prepararDirectoriosLocales();
        if (!cargarConfiguracionConexion("config.txt")) {
            System.out.println("No se pudo iniciar la aplicación por falta de archivo config.txt.");
            return;
        }

        if (!consumirCatalogosServidor()) {
            System.out.println("No fue posible leer los archivos del servidor en: " + rutaServidor);
            return;
        }

        ejecutarCicloAutenticacion();
    }
    
    private static void prepararDirectoriosLocales() {
        new File(CARPETA_LOCAL_CIFRADOS).mkdirs();
        new File(CARPETA_LOCAL_COMPRIMIDOS).mkdirs();
    }

    /**
     * Lee el archivo local de configuración para obtener la ruta de la carpeta compartida.
     */
    private static boolean cargarConfiguracionConexion(String rutaConfig) {
        File archivo = new File(rutaConfig);
        if (!archivo.exists()) return false;

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("RUTA_SERVIDOR=")) {
                    rutaServidor = linea.substring("RUTA_SERVIDOR=".length()).trim();
                }
            }
            return !rutaServidor.isEmpty();
        } catch (IOException ex) {
            System.out.println("Error al leer configuración: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Consume los archivos de usuarios y catálogo alojados en el servidor.
     */
    private static boolean consumirCatalogosServidor() {
        File archivoUsuarios = new File(rutaServidor, "usuarios.txt");
        File archivoCatalogo = new File(rutaServidor, "catalogo.txt");

        if (!archivoUsuarios.exists() || !archivoCatalogo.exists()) {
            return false;
        }

        usuariosRegistrados = new TablaUsuarios(53);
        try (BufferedReader lectorUsr = new BufferedReader(new FileReader(archivoUsuarios))) {
            String linea;
            while ((linea = lectorUsr.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                int primerSep = linea.indexOf('|');
                int segundoSep = (primerSep != -1) ? linea.indexOf('|', primerSep + 1) : -1;

                if (primerSep != -1 && segundoSep != -1) {
                    String usr = linea.substring(0, primerSep).trim();
                    String hash = linea.substring(primerSep + 1, segundoSep).trim();
                    boolean prem = Boolean.parseBoolean(linea.substring(segundoSep + 1).trim());
                    usuariosRegistrados.insertar(usr, hash, prem);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error al cargar usuarios del servidor: " + ex.getMessage());
            return false;
        }

        catalogoLibros = new VectorCatalogo();
        try (BufferedReader lectorCat = new BufferedReader(new FileReader(archivoCatalogo))) {
            String linea;
            while ((linea = lectorCat.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                int sep = linea.indexOf('|');
                if (sep != -1) {
                    String isbn = linea.substring(0, sep).trim();
                    String titulo = linea.substring(sep + 1).trim();
                    catalogoLibros.agregar(new RegistroLibro(isbn, titulo));
                }
            }
        } catch (IOException ex) {
            System.out.println("Error al cargar catálogo del servidor: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Pantalla de inicio de sesión.
     */
    private static void ejecutarCicloAutenticacion() {
        boolean ejecutandoSistema = true;
        while (ejecutandoSistema) {
            System.out.println("\n==========================================");
            System.out.println("   PLATAFORMA DE LIBROS DIGITALES - LOGIN ");
            System.out.println("==========================================");
            System.out.print("Usuario (o escriba 'salir' para terminar): ");
            String nombre = entrada.nextLine().trim();

            if (nombre.equalsIgnoreCase("salir")) {
                break;
            }

            System.out.print("Contraseña: ");
            String clavePlana = entrada.nextLine().trim();
            String hashCalculado = MotorSeguridad.generarHashClave(clavePlana);

            // Refrescamos catálogos del servidor para soportar concurrencia en tiempo real
            consumirCatalogosServidor();
            CuentaUsuario cuenta = usuariosRegistrados.buscar(nombre);

            if (cuenta != null && cuenta.getResumenClave().equalsIgnoreCase(hashCalculado)) {
                System.out.println("\nBienvenido, " + cuenta.getNombreUsuario()
                        + " [Categoría: " + (cuenta.isEsPremium() ? "PREMIUM" : "ESTÁNDAR") + "]");
                ejecutandoSistema = mostrarMenuPrincipal(cuenta);
            } else {
                System.out.println("\n[!] Credenciales incorrectas. Intente nuevamente.");
            }
        }
        System.out.println("Aplicación finalizada.");
    }

    /**
     * Menú principal tras iniciar sesión. Retorna false si el usuario elige Salir (Opción 4).
     */
    private static boolean mostrarMenuPrincipal(CuentaUsuario usuarioActual) {
        boolean sesionActiva = true;
        while (sesionActiva) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Explorar catálogo");
            if (usuarioActual.isEsPremium()) {
                System.out.println("2. Lectura Local (Exclusivo Premium)");
            } else {
                System.out.println("2. Lectura Local [Bloqueado - Requiere cuenta Premium]");
            }
            System.out.println("3. Cerrar sesión");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = entrada.nextLine().trim();
            switch (opcion) {
                case "1":
                    explorarCatalogo(usuarioActual);
                    break;
                case "2":
                    if (usuarioActual.isEsPremium()) {
                        realizarLecturaLocal();
                    } else {
                        System.out.println("[!] Esta función está habilitada únicamente para cuentas Premium.");
                    }
                    break;
                case "3":
                    System.out.println("Cerrando sesión de " + usuarioActual.getNombreUsuario() + "...");
                    sesionActiva = false;
                    break;
                case "4":
                    return false;
                default:
                    System.out.println("Opción no válida.");
            }
        }
        return true;
    }

    /**
     * Opción 1: Muestra el catálogo y gestiona la lectura en línea o descarga.
     */
    private static void explorarCatalogo(CuentaUsuario usuario) {
        System.out.println("\n--- CATÁLOGO DE LIBROS DISPONIBLES ---");
        if (catalogoLibros.obtenerTamano() == 0) {
            System.out.println("No hay libros registrados en el servidor.");
            return;
        }

        for (int i = 0; i < catalogoLibros.obtenerTamano(); i++) {
            RegistroLibro lib = catalogoLibros.obtenerPorIndice(i);
            System.out.println((i + 1) + ". [" + lib.getCodigoIsbn() + "] " + lib.getTituloObra());
        }
        System.out.println("0. Retornar a la vista anterior");
        System.out.print("Seleccione el número de libro: ");

        int seleccion;
        try {
            seleccion = Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return;
        }

        if (seleccion == 0) return;
        RegistroLibro libroElegido = catalogoLibros.obtenerPorIndice(seleccion - 1);
        if (libroElegido == null) {
            System.out.println("Selección fuera de rango.");
            return;
        }

        if (usuario.isEsPremium()) {
            System.out.println("\nLibro seleccionado: " + libroElegido.getTituloObra());
            System.out.println("1. Leer en línea");
            System.out.println("2. Descargar libro (Compresión Huffman)");
            System.out.print("Elija una acción: ");
            String subOpcion = entrada.nextLine().trim();
            if (subOpcion.equals("2")) {
                descargarLibroPremium(libroElegido);
            } else {
                leerLibroEnLinea(libroElegido);
            }
        } else {
            // Usuario no premium inicia inmediatamente lectura en línea
            leerLibroEnLinea(libroElegido);
        }
    }

    /**
     * Transfiere el archivo cifrado desde el servidor hacia la carpeta local del cliente
     * a través de flujos de datos de la aplicación (conservando la evidencia local).
     */
    private static File transferirCopiaCifradaAlCliente(String isbn) throws IOException {
        File carpetaLibrosServidor = new File(rutaServidor, "libros");
        File origenServidor = new File(carpetaLibrosServidor, isbn + ".txt");
        if (!origenServidor.exists()) {
            throw new IOException("El archivo del libro no existe en el servidor: " + origenServidor.getPath());
        }

        File destinoLocalCifrado = new File(CARPETA_LOCAL_CIFRADOS, isbn + ".txt");
        try (FileInputStream flujoEntrada = new FileInputStream(origenServidor);
             FileOutputStream flujoSalida = new FileOutputStream(destinoLocalCifrado)) {
            byte[] buffer = new byte[4096];
            int bytesLeidos;
            while ((bytesLeidos = flujoEntrada.read(buffer)) != -1) {
                flujoSalida.write(buffer, 0, bytesLeidos);
            }
        }
        return destinoLocalCifrado;
    }

    private static String leerTextoCompleto(File archivo) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    /**
     * 1.1 Lectura en línea: Traslada copia cifrada, acuerda clave DH, descifra con 3DES y muestra en consola.
     */
    private static void leerLibroEnLinea(RegistroLibro libro) {
        try {
            File copiaLocalCifrada = transferirCopiaCifradaAlCliente(libro.getCodigoIsbn());
            String textoCifrado = leerTextoCompleto(copiaLocalCifrada);

            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            String textoPlano = MotorSeguridad.descifrarContenido3DES(textoCifrado, claveCompartida);

            System.out.println("\n================ LECTURA EN LÍNEA: " + libro.getTituloObra() + " ================");
            System.out.println(textoPlano);
            System.out.println("==========================================================================");
        } catch (IOException ex) {
            System.out.println("Error durante la transferencia o lectura del libro: " + ex.getMessage());
        }
    }

    /**
     * 1.2 Descarga de libros: Traslada copia cifrada, descifra con DH + 3DES, comprime con Huffman y guarda copia local.
     */
    private static void descargarLibroPremium(RegistroLibro libro) {
        try {
            File copiaLocalCifrada = transferirCopiaCifradaAlCliente(libro.getCodigoIsbn());
            String textoCifrado = leerTextoCompleto(copiaLocalCifrada);

            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            String textoPlano = MotorSeguridad.descifrarContenido3DES(textoCifrado, claveCompartida);

            String contenidoComprimido = CompresorHuffman.comprimirTexto(textoPlano);
            File archivoComprimido = new File(CARPETA_LOCAL_COMPRIMIDOS, libro.getCodigoIsbn() + "_huffman.txt");

            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoComprimido))) {
                escritor.write(contenidoComprimido);
            }

            System.out.println("\n[+] Descarga exitosa.");
            System.out.println(" -> Copia cifrada (evidencia): " + copiaLocalCifrada.getAbsolutePath());
            System.out.println(" -> Copia comprimida (Huffman): " + archivoComprimido.getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Error al descargar el libro: " + ex.getMessage());
        }
    }

    /**
     * Opción 2: Lectura Local (descomprime un libro previamente descargado con Huffman).
     */
    private static void realizarLecturaLocal() {
        File carpeta = new File(CARPETA_LOCAL_COMPRIMIDOS);
        File[] archivos = carpeta.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("\nNo tienes libros descargados localmente.");
            return;
        }

        System.out.println("\n--- LIBROS DESCARGADOS LOCALMENTE ---");
        for (int i = 0; i < archivos.length; i++) {
            System.out.println((i + 1) + ". " + archivos[i].getName());
        }
        System.out.println("0. Volver");
        System.out.print("Seleccione un archivo para descomprimir y leer: ");

        int idx;
        try {
            idx = Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException e) {
            return;
        }

        if (idx <= 0 || idx > archivos.length) return;

        try {
            String datosComprimidos = leerTextoCompleto(archivos[idx - 1]);
            String textoDescomprimido = CompresorHuffman.descomprimirTexto(datosComprimidos);

            System.out.println("\n================ LECTURA LOCAL (DESCOMPRIMIDO) ================");
            System.out.println(textoDescomprimido);
            System.out.println("===============================================================");
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo local: " + ex.getMessage());
        }
    }
}
