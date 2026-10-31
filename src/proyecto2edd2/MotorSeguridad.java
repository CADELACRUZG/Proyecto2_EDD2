/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2edd2;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
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
    // Valores de Diffie-Hellman (configurables por el usuario o por archivo config.txt)
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
     * Aplica SHA-256 a la contraseña ingresada durante el inicio de sesión.
     */
    public static String generarHashClave(String textoPlano) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
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
            throw new RuntimeException("Error al procesar hash de autenticación", ex);
        }
    }

    /**
     * Ejecuta el intercambio de claves Diffie-Hellman con los valores configurados.
     */
    public static BigInteger acordarSecretoDiffieHellman() {
        BigInteger publicaServidor = generadorG.modPow(secretoServidorA, primoP);
        BigInteger publicaCliente = generadorG.modPow(secretoClienteB, primoP);

        BigInteger secretoCliente = publicaServidor.modPow(secretoClienteB, primoP);
        BigInteger secretoServidor = publicaCliente.modPow(secretoServidorA, primoP);

        if (!secretoCliente.equals(secretoServidor)) {
            throw new IllegalStateException("Fallo en el acuerdo de clave Diffie-Hellman");
        }
        return secretoCliente;
    }

    private static SecretKeySpec construirLlave3DES(BigInteger secretoCompartido) throws Exception {
        byte[] semilla = secretoCompartido.toString().getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digerido = sha.digest(semilla);
        byte[] bytesLlave24 = new byte[24];
        for (int i = 0; i < 24; i++) {
            bytesLlave24[i] = digerido[i];
        }
        return new SecretKeySpec(bytesLlave24, "DESede");
    }

    /**
     * Descifra el contenido del libro utilizando 3DES y la clave secreta compartida.
     */
    public static String descifrarContenido3DES(String contenidoCifradoBase64, BigInteger secretoDH) {
        try {
            SecretKeySpec llaveSimetrica = construirLlave3DES(secretoDH);
            Cipher descifrador = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            descifrador.init(Cipher.DECRYPT_MODE, llaveSimetrica);
            byte[] bytesDecodificados = Base64.getDecoder().decode(contenidoCifradoBase64.trim());
            byte[] bytesDescifrados = descifrador.doFinal(bytesDecodificados);
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "[Error al descifrar con 3DES: Verifique los parámetros Diffie-Hellman o el archivo] " + ex.getMessage();
        }
    }
}
