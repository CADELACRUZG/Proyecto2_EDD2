/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author chris
 */

/*
 * Gestiona el cálculo de hashes, intercambio Diffie-Hellman y cifrado 3DES
 */
public class MotorSeguridad {
    private static final String ALGORITMO_HASH = "SHA-256";

    private static BigInteger primoP = new BigInteger("23");
    private static BigInteger generadorG = new BigInteger("5");
    private static BigInteger secretoServidorA = new BigInteger("6");
    private static BigInteger secretoClienteB = new BigInteger("15");

    public static void configurarDiffieHellman(String p, String g, String a, String b) {
        if (p != null && !p.isEmpty()) primoP = new BigInteger(p.trim());
        if (g != null && !g.isEmpty()) generadorG = new BigInteger(g.trim());
        if (a != null && !a.isEmpty()) secretoServidorA = new BigInteger(a.trim());
        if (b != null && !b.isEmpty()) secretoClienteB = new BigInteger(b.trim());
    }

    public static BigInteger getPrimoP() { return primoP; }
    public static BigInteger getGeneradorG() { return generadorG; }
    public static BigInteger getSecretoServidorA() { return secretoServidorA; }
    public static BigInteger getSecretoClienteB() { return secretoClienteB; }

    /**
     * Aplica la función hash configurada en ALGORITMO_HASH a la contraseña introducida.
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

    /**
     * Compara el hash de la contraseña ingresada con el hash almacenado en el archivo de usuarios.
     */
    public static boolean verificarContrasena(String contrasenaIngresada, String hashAlmacenado) {
        if (hashAlmacenado == null) return false;
        String hashCalculado = generarHashClave(contrasenaIngresada);
        return hashCalculado.equalsIgnoreCase(hashAlmacenado.trim());
    }

    /**
     * Realiza el intercambio de claves Diffie-Hellman y retorna la clave secreta compartida.
     */
    public static BigInteger acordarSecretoDiffieHellman() {
        BigInteger publicaServidor = generadorG.modPow(secretoServidorA, primoP);
        BigInteger publicaCliente = generadorG.modPow(secretoClienteB, primoP);

        BigInteger secretoCliente = publicaServidor.modPow(secretoClienteB, primoP);
        BigInteger secretoServidor = publicaCliente.modPow(secretoServidorA, primoP);

        if (!secretoCliente.equals(secretoServidor)) {
            throw new IllegalStateException("Error en sincronización Diffie-Hellman.");
        }
        return secretoCliente;
    }

    /**
     * Descifra los bytes del libro utilizando 3DES con la clave obtenida de Diffie-Hellman.
     */
    public static String descifrarBytes3DES(byte[] bytesCifrados, BigInteger secretoDH) {
        try {
            byte[] semilla = secretoDH.toString().getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digerido = sha.digest(semilla);
            byte[] bytesLlave24 = new byte[24];
            for (int i = 0; i < 24; i++) {
                bytesLlave24[i] = digerido[i];
            }
            SecretKeySpec llaveSimetrica = new SecretKeySpec(bytesLlave24, "DESede");
            Cipher descifrador = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            descifrador.init(Cipher.DECRYPT_MODE, llaveSimetrica);
            byte[] bytesDescifrados = descifrador.doFinal(bytesCifrados);
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "[Error al descifrar con 3DES: " + ex.getMessage() + "]";
        }
    }
}
