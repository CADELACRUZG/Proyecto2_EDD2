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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    
    private static final String ARCHIVO_USUARIOS = "SHA256.txt";
    private static final String ARCHIVO_CATALOGO = "Catalogo.txt";

    private static String ipServidor = "127.0.0.1";
    private static String direccionDescargas = "descargas_cliente";

    private static conexion conexion = null;
    private static TablaUsuarios usuariosRegistrados = new TablaUsuarios(53);
    private static VectorCatalogo catalogoLibros = new VectorCatalogo();
    private static Scanner teclado = new Scanner(System.in);
    
    public static void main(String[] args) {
        cargarArchivoConfiguracion("config.txt");
        solicitarConfiguracionInicial();

        try {
            conexion = new conexion(ipServidor, direccionDescargas);
            System.out.println("\nConexión exitosa con el servidor (" + ipServidor + ":5000)");
            System.out.println("Carpeta de descargas local: " + conexion.getCarpetaDescargas());

            if (!consumirCatalogosServidor()) {
                System.out.println("No fue posible cargar los catálogos iniciales desde el servidor.");
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
    
   private static void cargarArchivoConfiguracion(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        String p = null, g = null, a = null, b = null;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("IP_SERVIDOR=")) ipServidor = linea.substring(12).trim();
                else if (linea.startsWith("CARPETA_LOCAL=")) direccionDescargas = linea.substring(14).trim();
                else if (linea.startsWith("DH_P=")) p = linea.substring(5).trim();
                else if (linea.startsWith("DH_G=")) g = linea.substring(5).trim();
                else if (linea.startsWith("DH_A=")) a = linea.substring(5).trim();
                else if (linea.startsWith("DH_B=")) b = linea.substring(5).trim();
            }
            MotorSeguridad.configurarDiffieHellman(p, g, a, b);
        } catch (IOException ex) {
            System.out.println("Usando configuración inicial por defecto.");
        }
    }

    private static void guardarArchivoConfiguracion(String ruta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write("IP_SERVIDOR=" + ipServidor + "\n");
            bw.write("CARPETA_LOCAL=" + direccionDescargas + "\n");
            bw.write("DH_P=" + MotorSeguridad.getPrimoP() + "\n");
            bw.write("DH_G=" + MotorSeguridad.getGeneradorG() + "\n");
            bw.write("DH_A=" + MotorSeguridad.getSecretoServidorA() + "\n");
            bw.write("DH_B=" + MotorSeguridad.getSecretoClienteB() + "\n");
        } catch (IOException ex) {
            System.out.println("No se pudo guardar config.txt: " + ex.getMessage());
        }
    }

    private static void solicitarConfiguracionInicial() {
        System.out.println("==========================================");
        System.out.println("         CLIENTE DE LIBROS DIGITALES      ");
        System.out.println("==========================================");
        System.out.print("Ingrese la dirección IP del servidor [Enter para " + ipServidor + "]: ");
        String ipInput = teclado.nextLine().trim();
        if (!ipInput.isEmpty()) ipServidor = ipInput;

        System.out.print("Ingrese la dirección de la carpeta donde se almacenarán los archivos [Enter para " + direccionDescargas + "]: ");
        String dirInput = teclado.nextLine().trim();
        if (!dirInput.isEmpty()) direccionDescargas = dirInput;

        System.out.print("¿Desea modificar los valores predeterminados de Diffie-Hellman? (S/N): ");
        String modDh = teclado.nextLine().trim().toUpperCase();
        if (modDh.equals("S")) {
            System.out.print("Primo P [actual " + MotorSeguridad.getPrimoP() + "]: ");
            String p = teclado.nextLine().trim();
            System.out.print("Generador G [actual " + MotorSeguridad.getGeneradorG() + "]: ");
            String g = teclado.nextLine().trim();
            System.out.print("Secreto Servidor A [actual " + MotorSeguridad.getSecretoServidorA() + "]: ");
            String a = teclado.nextLine().trim();
            System.out.print("Secreto Cliente B [actual " + MotorSeguridad.getSecretoClienteB() + "]: ");
            String b = teclado.nextLine().trim();
            MotorSeguridad.configurarDiffieHellman(p, g, a, b);
        }

        guardarArchivoConfiguracion("config.txt");
    }

    /**
     * Descarga directamente el archivo configurado en ARCHIVO_USUARIOS y ARCHIVO_CATALOGO.
     */
    private static boolean consumirCatalogosServidor() {
        try {
            Path rutaUsuariosLocal = conexion.descargarArchivo("Usuarios", ARCHIVO_USUARIOS);
            if (rutaUsuariosLocal == null) return false;

            Path rutaCatalogoLocal = conexion.descargarArchivo("Catalogo", ARCHIVO_CATALOGO);
            if (rutaCatalogoLocal == null) return false;

            usuariosRegistrados = new TablaUsuarios(53);
            try (BufferedReader lectorUsr = new BufferedReader(new FileReader(rutaUsuariosLocal.toFile(), StandardCharsets.UTF_8))) {
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
            try (BufferedReader lectorCat = new BufferedReader(new FileReader(rutaCatalogoLocal.toFile(), StandardCharsets.UTF_8))) {
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
            System.out.println("   INICIO DE SESIÓN (" + ARCHIVO_USUARIOS + ")");
            System.out.println("==========================================");
            System.out.print("Usuario (o escriba 'salir' para terminar): ");
            String nombre = teclado.nextLine().trim();

            if (nombre.equalsIgnoreCase("salir")) {
                break;
            }

            System.out.print("Contraseña: ");
            String clavePlana = teclado.nextLine().trim();

            CuentaUsuario cuenta = usuariosRegistrados.buscar(nombre);

            if (cuenta != null && MotorSeguridad.verificarContrasena(clavePlana, cuenta.getResumenClave())) {
                System.out.println("\nBienvenido, " + cuenta.getNombreUsuario()
                        + " [Categoría: " + (cuenta.isEsPremium() ? "PREMIUM" : "ESTÁNDAR") + "]");
                ejecutandoSistema = mostrarMenuPrincipal(cuenta);
            } else {
                System.out.println("\n[!] Usuario o contraseña incorrectos.");
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
                System.out.println("2. Lectura Local [No disponible - Cuenta Estándar]");
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
                        System.out.println("[!] Opción exclusiva para usuarios Premium.");
                    }
                    break;
                case "3":
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
            System.out.print("Seleccione una opción: ");
            String sub = teclado.nextLine().trim();
            if (sub.equals("2")) {
                descargarLibroPremium(libroElegido);
            } else {
                leerLibroEnLinea(libroElegido);
            }
        } else {
            leerLibroEnLinea(libroElegido);
        }
    }

    private static void leerLibroEnLinea(RegistroLibro libro) {
        try {
            Path copiaLocalCifrada = conexion.descargarArchivo("Libros", libro.getCodigoIsbn() + ".bin");
            if (copiaLocalCifrada == null) return;

            byte[] bytesCifrados = Files.readAllBytes(copiaLocalCifrada);
            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            String textoPlano = MotorSeguridad.descifrarBytes3DES(bytesCifrados, claveCompartida);

            System.out.println("\n================ LECTURA EN LÍNEA: " + libro.getTituloObra() + " ================");
            System.out.println(textoPlano);
            System.out.println("==========================================================================");
        } catch (IOException ex) {
            System.out.println("Error al transferir o leer el libro: " + ex.getMessage());
        }
    }

    private static void descargarLibroPremium(RegistroLibro libro) {
        try {
            Path copiaLocalCifrada = conexion.descargarArchivo("Libros", libro.getCodigoIsbn() + ".bin");
            if (copiaLocalCifrada == null) return;

            byte[] bytesCifrados = Files.readAllBytes(copiaLocalCifrada);
            BigInteger claveCompartida = MotorSeguridad.acordarSecretoDiffieHellman();
            String textoPlano = MotorSeguridad.descifrarBytes3DES(bytesCifrados, claveCompartida);

            String contenidoComprimido = AlgoritmoHuffman.comprimirTexto(textoPlano);
            Path archivoComprimido = conexion.getCarpetaComprimidos()
                    .resolve(libro.getCodigoIsbn() + "_huffman.txt");

            Files.writeString(archivoComprimido, contenidoComprimido, StandardCharsets.UTF_8);

            System.out.println("\n[+] Libro descargado y comprimido con Huffman.");
            System.out.println(" -> Evidencia cifrada (.bin): " + copiaLocalCifrada);
            System.out.println(" -> Copia local comprimida:   " + archivoComprimido);
        } catch (IOException ex) {
            System.out.println("Error al descargar el libro: " + ex.getMessage());
        }
    }

    private static void realizarLecturaLocal() {
        File carpeta = conexion.getCarpetaComprimidos().toFile();
        File[] archivos = carpeta.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("\nNo hay libros descargados localmente.");
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
            String datosComprimidos = Files.readString(archivos[idx - 1].toPath(), StandardCharsets.UTF_8);
            String textoDescomprimido = AlgoritmoHuffman.descomprimirTexto(datosComprimidos);

            System.out.println("\n================ LECTURA LOCAL (DESCOMPRIMIDO) ================");
            System.out.println(textoDescomprimido);
            System.out.println("===============================================================");
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo comprimido: " + ex.getMessage());
        }
    }
}
