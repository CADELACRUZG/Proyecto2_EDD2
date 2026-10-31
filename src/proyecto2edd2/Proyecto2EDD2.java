/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyecto2edd2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Scanner;

/**
 *
 * @author chris
 */

/**
 * Clase principal que gestiona la conexión al servidor, transferencia de archivos y menús.
 */ 
public class Proyecto2EDD2 {
    
    private static String ipServidor = "127.0.0.1";
    private static String carpetaServidor = "C:\\ServidorLibros";
    private static String direccionDescargas = "local_cifrados";

    private static conexion conexion = null;
    private static TablaUsuarios usuariosRegistrados = new TablaUsuarios(53);
    private static VectorCatalogo catalogoLibros = new VectorCatalogo();
    private static Scanner teclado = new Scanner(System.in);
    
    public static void main(String[] args) {
        // 1. Cargar valores predeterminados desde config.txt (requerido por el enunciado)
        cargarArchivoConfiguracion("config.txt");

        // 2. Permitir ingreso por consola al estilo Laboratorio15 + valores criptográficos
        configurarParametrosInteractivos();

        try {
            // false = Carpeta Compartida (SMB/local) | true = Socket puerto 5000
            conexion = new conexion(ipServidor, carpetaServidor, direccionDescargas, false);
            System.out.println("\nConexión exitosa con el servidor.");
            System.out.println("Carpeta local de descargas (evidencia cifrada): " + conexion.getCarpetaDescargas());

            if (!consumirCatalogosServidor()) {
                System.out.println("No fue posible cargar usuarios.txt o catalogo.txt desde el servidor.");
                return;
            }

            ejecutarCicloAutenticacion();

        } catch (IOException e) {
            System.out.println("No fue posible realizar la conexión: " + e.getMessage());
        } finally {
            if (conexion != null) {
                conexion.cerrarConexion();
            }
            System.out.println("\nConexión finalizada.");
        }
    }
    
    /**
     * Lee el archivo config.txt si existe al iniciar el programa.
     */
    private static void cargarArchivoConfiguracion(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) return;

        String p = null, g = null, a = null, b = null;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("IP_SERVIDOR=")) ipServidor = linea.substring(12).trim();
                else if (linea.startsWith("CARPETA_SERVIDOR=")) carpetaServidor = linea.substring(17).trim();
                else if (linea.startsWith("CARPETA_LOCAL=")) direccionDescargas = linea.substring(14).trim();
                else if (linea.startsWith("DH_P=")) p = linea.substring(5).trim();
                else if (linea.startsWith("DH_G=")) g = linea.substring(5).trim();
                else if (linea.startsWith("DH_A=")) a = linea.substring(5).trim();
                else if (linea.startsWith("DH_B=")) b = linea.substring(5).trim();
            }
            MotorSeguridad.configurarDiffieHellman(p, g, a, b);
        } catch (IOException ex) {
            System.out.println("Aviso: No se pudo leer config.txt, se usarán valores por defecto.");
        }
    }

    /**
     * Guarda la configuración actual en config.txt para dejar evidencia del archivo de configuración.
     */
    private static void guardarArchivoConfiguracion(String rutaArchivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write("IP_SERVIDOR=" + ipServidor + "\n");
            bw.write("CARPETA_SERVIDOR=" + carpetaServidor + "\n");
            bw.write("CARPETA_LOCAL=" + direccionDescargas + "\n");
            bw.write("DH_P=" + MotorSeguridad.getPrimoP().toString() + "\n");
            bw.write("DH_G=" + MotorSeguridad.getGeneradorG().toString() + "\n");
            bw.write("DH_A=" + MotorSeguridad.getSecretoServidorA().toString() + "\n");
            bw.write("DH_B=" + MotorSeguridad.getSecretoClienteB().toString() + "\n");
        } catch (IOException ex) {
            System.out.println("Error al guardar config.txt: " + ex.getMessage());
        }
    }

    /**
     * Pide al usuario la IP, carpetas y parámetros criptográficos (como en Laboratorio15).
     */
    private static void configurarParametrosInteractivos() {
        System.out.println("==========================================");
        System.out.println("   CONFIGURACIÓN DE CONEXIÓN Y SEGURIDAD  ");
        System.out.println("==========================================");
        System.out.println("Configuración actual en config.txt:");
        System.out.println(" - IP Servidor: " + ipServidor);
        System.out.println(" - Carpeta Compartida Servidor: " + carpetaServidor);
        System.out.println(" - Carpeta Local Descargas: " + direccionDescargas);
        System.out.println(" - Diffie-Hellman (P=" + MotorSeguridad.getPrimoP()
                + ", G=" + MotorSeguridad.getGeneradorG()
                + ", A=" + MotorSeguridad.getSecretoServidorA()
                + ", B=" + MotorSeguridad.getSecretoClienteB() + ")");

        System.out.print("\n¿Desea ingresar manualmente la IP y los valores criptográficos? (S/N): ");
        String resp = teclado.nextLine().trim().toUpperCase();

        if (resp.equals("S")) {
            System.out.print("Ingrese la dirección IP del servidor (ej. 192.168.1.45 o localhost): ");
            String nuevaIp = teclado.nextLine().trim();
            if (!nuevaIp.isEmpty()) ipServidor = nuevaIp;

            System.out.print("Ingrese el nombre o ruta de la carpeta compartida (ej. ServidorLibros): ");
            String nuevaCarpetaSrv = teclado.nextLine().trim();
            if (!nuevaCarpetaSrv.isEmpty()) carpetaServidor = nuevaCarpetaSrv;

            System.out.print("Ingrese la dirección de la carpeta local donde se almacenarán los archivos: ");
            String nuevaDescarga = teclado.nextLine().trim();
            if (!nuevaDescarga.isEmpty()) direccionDescargas = nuevaDescarga;

            System.out.println("\n--- Parámetros Criptográficos Diffie-Hellman ---");
            System.out.print("Ingrese el número primo (P) [Enter para mantener " + MotorSeguridad.getPrimoP() + "]: ");
            String p = teclado.nextLine().trim();

            System.out.print("Ingrese la raíz primitiva / generador (G) [Enter para mantener " + MotorSeguridad.getGeneradorG() + "]: ");
            String g = teclado.nextLine().trim();

            System.out.print("Ingrese el secreto del servidor (A) [Enter para mantener " + MotorSeguridad.getSecretoServidorA() + "]: ");
            String a = teclado.nextLine().trim();

            System.out.print("Ingrese el secreto del cliente (B) [Enter para mantener " + MotorSeguridad.getSecretoClienteB() + "]: ");
            String b = teclado.nextLine().trim();

            MotorSeguridad.configurarDiffieHellman(p, g, a, b);
        }

        guardarArchivoConfiguracion("config.txt");
    }

    /**
     * Descarga y consume los catálogos de usuarios y libros desde el servidor.
     */
    private static boolean consumirCatalogosServidor() {
        try {
            Path archivoUsuarios = conexion.descargarArchivo("", "usuarios.txt");
            Path archivoCatalogo = conexion.descargarArchivo("", "catalogo.txt");

            usuariosRegistrados = new TablaUsuarios(53);
            try (BufferedReader lectorUsr = new BufferedReader(new FileReader(archivoUsuarios.toFile()))) {
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
            }

            catalogoLibros = new VectorCatalogo();
            try (BufferedReader lectorCat = new BufferedReader(new FileReader(archivoCatalogo.toFile()))) {
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
            }
            return true;
        } catch (IOException ex) {
            System.out.println("Error al consumir catálogos del servidor: " + ex.getMessage());
            return false;
        }
    }

    private static void ejecutarCicloAutenticacion() {
        boolean ejecutandoSistema = true;
        while (ejecutandoSistema) {
            System.out.println("\n==========================================");
            System.out.println("   PLATAFORMA DE LIBROS DIGITALES - LOGIN ");
            System.out.println("==========================================");
            System.out.print("Usuario (o escriba 'salir' para terminar): ");
            String nombre = teclado.nextLine().trim();

            if (nombre.equalsIgnoreCase("salir")) {
                break;
            }

            System.out.print("Contraseña: ");
            String clavePlana = teclado.nextLine().trim();
            String hashCalculado = MotorSeguridad.generarHashClave(clavePlana);

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
    }

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

            String opcion = teclado.nextLine().trim();
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
            seleccion = Integer.parseInt(teclado.nextLine().trim());
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
            String subOpcion = teclado.nextLine().trim();
            if (subOpcion.equals("2")) {
                descargarLibroPremium(libroElegido);
            } else {
                leerLibroEnLinea(libroElegido);
            }
        } else {
            leerLibroEnLinea(libroElegido);
        }
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

    private static void leerLibroEnLinea(RegistroLibro libro) {
        try {
            Path copiaLocalCifrada = conexion.descargarArchivo("libros", libro.getCodigoIsbn() + ".txt");
            String textoCifrado = leerTextoCompleto(copiaLocalCifrada.toFile());

            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            System.out.println("[Diffie-Hellman] Clave secreta compartida obtenida: " + claveCompartida);

            String textoPlano = MotorSeguridad.descifrarContenido3DES(textoCifrado, claveCompartida);

            System.out.println("\n================ LECTURA EN LÍNEA: " + libro.getTituloObra() + " ================");
            System.out.println(textoPlano);
            System.out.println("==========================================================================");
        } catch (IOException ex) {
            System.out.println("Error durante la transferencia o lectura del libro: " + ex.getMessage());
        }
    }

    private static void descargarLibroPremium(RegistroLibro libro) {
        try {
            Path copiaLocalCifrada = conexion.descargarArchivo("libros", libro.getCodigoIsbn() + ".txt");
            String textoCifrado = leerTextoCompleto(copiaLocalCifrada.toFile());

            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            System.out.println("[Diffie-Hellman] Clave secreta compartida obtenida: " + claveCompartida);

            String textoPlano = MotorSeguridad.descifrarContenido3DES(textoCifrado, claveCompartida);
            String contenidoComprimido = AlgoritmoHuffman.comprimirTexto(textoPlano);

            Path archivoComprimido = conexion.getCarpetaComprimidos()
                    .resolve(libro.getCodigoIsbn() + "_huffman.txt");

            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoComprimido.toFile()))) {
                escritor.write(contenidoComprimido);
            }

            System.out.println("\n[+] Descarga y compresión Huffman completadas.");
            System.out.println(" -> Copia cifrada (evidencia): " + copiaLocalCifrada);
            System.out.println(" -> Copia comprimida (Huffman): " + archivoComprimido);
        } catch (IOException ex) {
            System.out.println("Error al descargar el libro: " + ex.getMessage());
        }
    }

    private static void realizarLecturaLocal() {
        File carpeta = conexion.getCarpetaComprimidos().toFile();
        File[] archivos = carpeta.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("\nNo tienes libros comprimidos descargados localmente.");
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
            idx = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            return;
        }

        if (idx <= 0 || idx > archivos.length) return;

        try {
            String datosComprimidos = leerTextoCompleto(archivos[idx - 1]);
            String textoDescomprimido = AlgoritmoHuffman.descomprimirTexto(datosComprimidos);

            System.out.println("\n================ LECTURA LOCAL (DESCOMPRIMIDO) ================");
            System.out.println(textoDescomprimido);
            System.out.println("===============================================================");
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo local: " + ex.getMessage());
        }
    }
}
