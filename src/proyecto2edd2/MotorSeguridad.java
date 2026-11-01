/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 *
 * @author chris
 */

/**
 * Gestiona el cálculo de Hash (SHA-256 / MD5), el intercambio Diffie-Hellman
 * y el cifrado/descifrado 3-DES basado en S-DES con 3 llaves de 10 bits.
 */
public class MotorSeguridad {
    // Aquí se cambia entre "SHA-256" o "MD5" según el archivo de usuarios utilizado
    private static final String ALGORITMO_HASH = "SHA-256";

    // Parámetros predeterminados de Diffie-Hellman para Llave 1 (K1 = 57 -> 0000111001)
    private static BigInteger p1 = new BigInteger("97");
    private static BigInteger g1 = new BigInteger("5");
    private static BigInteger a1 = new BigInteger("13");
    private static BigInteger b1 = new BigInteger("19");

    // Parámetros predeterminados de Diffie-Hellman para Llave 2 (K2 = 74 -> 0001001010)
    private static BigInteger p2 = new BigInteger("89");
    private static BigInteger g2 = new BigInteger("41");
    private static BigInteger a2 = new BigInteger("17");
    private static BigInteger b2 = new BigInteger("23");

    // Parámetros predeterminados de Diffie-Hellman para Llave 3 (K3 = 32 -> 0000100000)
    private static BigInteger p3 = new BigInteger("83");
    private static BigInteger g3 = new BigInteger("67");
    private static BigInteger a3 = new BigInteger("11");
    private static BigInteger b3 = new BigInteger("29");

    public static void configurarLlaveDH(int numeroLlave, String p, String g, String a, String b) {
        BigInteger valP = (p != null && !p.isEmpty()) ? new BigInteger(p.trim()) : null;
        BigInteger valG = (g != null && !g.isEmpty()) ? new BigInteger(g.trim()) : null;
        BigInteger valA = (a != null && !a.isEmpty()) ? new BigInteger(a.trim()) : null;
        BigInteger valB = (b != null && !b.isEmpty()) ? new BigInteger(b.trim()) : null;

        if (numeroLlave == 1) {
            if (valP != null) p1 = valP;
            if (valG != null) g1 = valG;
            if (valA != null) a1 = valA;
            if (valB != null) b1 = valB;
        } else if (numeroLlave == 2) {
            if (valP != null) p2 = valP;
            if (valG != null) g2 = valG;
            if (valA != null) a2 = valA;
            if (valB != null) b2 = valB;
        } else if (numeroLlave == 3) {
            if (valP != null) p3 = valP;
            if (valG != null) g3 = valG;
            if (valA != null) a3 = valA;
            if (valB != null) b3 = valB;
        }
    }

    /**
     * Calcula una clave compartida mediante Diffie-Hellman:
     * A = g^a mod p, B = g^b mod p -> K = A^b mod p = B^a mod p
     */
    private static int calcularSecretoDH(BigInteger p, BigInteger g, BigInteger a, BigInteger b) {
        BigInteger publicaA = g.modPow(a, p);
        BigInteger publicaB = g.modPow(b, p);

        BigInteger secretoCliente = publicaA.modPow(b, p);
        BigInteger secretoServidor = publicaB.modPow(a, p);

        if (!secretoCliente.equals(secretoServidor)) {
            throw new IllegalStateException("Fallo en el intercambio Diffie-Hellman.");
        }
        return secretoCliente.intValue() & 0x3FF;
    }

    public static int obtenerLlave1() { return calcularSecretoDH(p1, g1, a1, b1); }
    public static int obtenerLlave2() { return calcularSecretoDH(p2, g2, a2, b2); }
    public static int obtenerLlave3() { return calcularSecretoDH(p3, g3, a3, b3); }

    public static String formatearBinario10Bits(int valor) {
        String bin = Integer.toBinaryString(valor & 0x3FF);
        while (bin.length() < 10) {
            bin = "0" + bin;
        }
        return bin;
    }

    /**
     * Genera el resumen hash de la contraseña utilizando ALGORITMO_HASH.
     */
    public static String generarHashClave(String textoPlano) {
        try {
            MessageDigest digestor = MessageDigest.getInstance(ALGORITMO_HASH);
            byte[] bytesResumen = digestor.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder cadenaHex = new StringBuilder();
            for (int i = 0; i < bytesResumen.length; i++) {
                String hex = Integer.toHexString(0xff & bytesResumen[i]);
                if (hex.length() == 1) {
                    cadenaHex.append('0');
                }
                cadenaHex.append(hex);
            }
            return cadenaHex.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error al calcular el hash con " + ALGORITMO_HASH, ex);
        }
    }

    public static boolean verificarContrasena(String contrasenaIngresada, String hashAlmacenado) {
        if (hashAlmacenado == null) return false;
        String hashCalculado = generarHashClave(contrasenaIngresada);
        return hashCalculado.equalsIgnoreCase(hashAlmacenado.trim());
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE S-DES Y 3-DES (Intercalando Llave1, Llave2 y Llave3)
    // =========================================================================

    private static int bitEn(int valor, int posDesdeIzq, int totalBits) {
        return (valor >> (totalBits - posDesdeIzq)) & 1;
    }

    private static int permutarP10(int clave10) {
        return (bitEn(clave10, 3, 10) << 9)
             | (bitEn(clave10, 5, 10) << 8)
             | (bitEn(clave10, 2, 10) << 7)
             | (bitEn(clave10, 7, 10) << 6)
             | (bitEn(clave10, 4, 10) << 5)
             | (bitEn(clave10, 10, 10) << 4)
             | (bitEn(clave10, 1, 10) << 3)
             | (bitEn(clave10, 9, 10) << 2)
             | (bitEn(clave10, 8, 10) << 1)
             |  bitEn(clave10, 6, 10);
    }

    private static int permutarP8(int clave10) {
        return (bitEn(clave10, 6, 10) << 7)
             | (bitEn(clave10, 3, 10) << 6)
             | (bitEn(clave10, 7, 10) << 5)
             | (bitEn(clave10, 4, 10) << 4)
             | (bitEn(clave10, 8, 10) << 3)
             | (bitEn(clave10, 5, 10) << 2)
             | (bitEn(clave10, 10, 10) << 1)
             |  bitEn(clave10, 9, 10);
    }

    private static int permutarIP(int bloque8) {
        return (bitEn(bloque8, 2, 8) << 7)
             | (bitEn(bloque8, 6, 8) << 6)
             | (bitEn(bloque8, 3, 8) << 5)
             | (bitEn(bloque8, 1, 8) << 4)
             | (bitEn(bloque8, 4, 8) << 3)
             | (bitEn(bloque8, 8, 8) << 2)
             | (bitEn(bloque8, 5, 8) << 1)
             |  bitEn(bloque8, 7, 8);
    }

    private static int permutarIPInversa(int bloque8) {
        return (bitEn(bloque8, 4, 8) << 7)
             | (bitEn(bloque8, 1, 8) << 6)
             | (bitEn(bloque8, 3, 8) << 5)
             | (bitEn(bloque8, 5, 8) << 4)
             | (bitEn(bloque8, 7, 8) << 3)
             | (bitEn(bloque8, 2, 8) << 2)
             | (bitEn(bloque8, 8, 8) << 1)
             |  bitEn(bloque8, 6, 8);
    }

    private static int expandirEP(int mitad4) {
        return (bitEn(mitad4, 4, 4) << 7)
             | (bitEn(mitad4, 1, 4) << 6)
             | (bitEn(mitad4, 2, 4) << 5)
             | (bitEn(mitad4, 3, 4) << 4)
             | (bitEn(mitad4, 2, 4) << 3)
             | (bitEn(mitad4, 3, 4) << 2)
             | (bitEn(mitad4, 4, 4) << 1)
             |  bitEn(mitad4, 1, 4);
    }

    private static int permutarP4(int bloque4) {
        return (bitEn(bloque4, 2, 4) << 3)
             | (bitEn(bloque4, 4, 4) << 2)
             | (bitEn(bloque4, 3, 4) << 1)
             |  bitEn(bloque4, 1, 4);
    }

    private static int desplazarIzquierda5Bits(int mitad5, int pasos) {
        return ((mitad5 << pasos) | (mitad5 >> (5 - pasos))) & 0x1F;
    }

    private static int generarSubllaveK1(int llave10Bits) {
        int p10 = permutarP10(llave10Bits);
        int izq = desplazarIzquierda5Bits((p10 >> 5) & 0x1F, 1);
        int der = desplazarIzquierda5Bits(p10 & 0x1F, 1);
        return permutarP8((izq << 5) | der);
    }

    private static int generarSubllaveK2(int llave10Bits) {
        int p10 = permutarP10(llave10Bits);
        int izq = desplazarIzquierda5Bits((p10 >> 5) & 0x1F, 3);
        int der = desplazarIzquierda5Bits(p10 & 0x1F, 3);
        return permutarP8((izq << 5) | der);
    }

    private static int evaluarCajaS(int numCaja, int fila, int col) {
        int indice = (fila * 4 + col) * 2;
        long tablaS0 = 0b01_00_11_10__11_10_01_00__00_10_01_11__11_01_11_10L;
        long tablaS1 = 0b00_01_10_11__10_00_01_11__11_00_01_00__10_01_00_11L;
        long tabla = (numCaja == 0) ? tablaS0 : tablaS1;
        return (int) ((tabla >> (30 - indice)) & 0b11);
    }

    private static int funcionFk(int bloque8, int subllaveK) {
        int izq = (bloque8 >> 4) & 0x0F;
        int der = bloque8 & 0x0F;

        int expandido = expandirEP(der) ^ subllaveK;
        int mitadIzq = (expandido >> 4) & 0x0F;
        int mitadDer = expandido & 0x0F;

        int filaS0 = (bitEn(mitadIzq, 1, 4) << 1) | bitEn(mitadIzq, 4, 4);
        int colS0  = (bitEn(mitadIzq, 2, 4) << 1) | bitEn(mitadIzq, 3, 4);

        int filaS1 = (bitEn(mitadDer, 1, 4) << 1) | bitEn(mitadDer, 4, 4);
        int colS1  = (bitEn(mitadDer, 2, 4) << 1) | bitEn(mitadDer, 3, 4);

        int salidaS = (evaluarCajaS(0, filaS0, colS0) << 2) | evaluarCajaS(1, filaS1, colS1);
        int p4 = permutarP4(salidaS);

        return ((izq ^ p4) << 4) | der;
    }

    private static int intercambiarMitades(int bloque8) {
        return ((bloque8 & 0x0F) << 4) | ((bloque8 >> 4) & 0x0F);
    }

    public static int sdesCifrar(int byteEntrada, int llave10Bits) {
        int k1 = generarSubllaveK1(llave10Bits);
        int k2 = generarSubllaveK2(llave10Bits);

        int temp = permutarIP(byteEntrada & 0xFF);
        temp = funcionFk(temp, k1);
        temp = intercambiarMitades(temp);
        temp = funcionFk(temp, k2);
        return permutarIPInversa(temp);
    }

    public static int sdesDescifrar(int byteEntrada, int llave10Bits) {
        int k1 = generarSubllaveK1(llave10Bits);
        int k2 = generarSubllaveK2(llave10Bits);

        int temp = permutarIP(byteEntrada & 0xFF);
        temp = funcionFk(temp, k2);
        temp = intercambiarMitades(temp);
        temp = funcionFk(temp, k1);
        return permutarIPInversa(temp);
    }

    /**
     * Desencripción 3-DES:
     * SDES_DESCIFRAR(Llave3) -> SDES_CIFRAR(Llave2) -> SDES_DESCIFRAR(Llave1)
     */
    public static int tripleSdesDescifrarByte(int byteCifrado, int k1_10b, int k2_10b, int k3_10b) {
        int paso1 = sdesDescifrar(byteCifrado, k3_10b);
        int paso2 = sdesCifrar(paso1, k2_10b);
        return sdesDescifrar(paso2, k1_10b);
    }

    /**
     * Obtiene las 3 llaves de 10 bits mediante Diffie-Hellman y descifra el archivo .bin.
     */
    public static String descifrarBytes3DES(byte[] bytesCifrados) {
        int k1 = obtenerLlave1();
        int k2 = obtenerLlave2();
        int k3 = obtenerLlave3();

        byte[] bytesDescifrados = new byte[bytesCifrados.length];
        for (int i = 0; i < bytesCifrados.length; i++) {
            int byteActual = bytesCifrados[i] & 0xFF;
            bytesDescifrados[i] = (byte) tripleSdesDescifrarByte(byteActual, k1, k2, k3);
        }

        return new String(bytesDescifrados, StandardCharsets.UTF_8);
    }
}
