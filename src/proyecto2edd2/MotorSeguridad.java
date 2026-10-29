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
    // Valores predeterminados para Diffie-Hellman (ajustables según los datos del servidor)
    private static final BigInteger PRIMO_P = new BigInteger("23");
    private static final BigInteger GENERADOR_G = new BigInteger("5");
    private static final BigInteger SECRETO_SERVIDOR_A = new BigInteger("6");
    private static final BigInteger SECRETO_CLIENTE_B = new BigInteger("15");

    /**
     * Aplica SHA-256 a la contraseña ingresada y devuelve su representación hexadecimal.
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
     * Ejecuta el intercambio de claves Diffie-Hellman y retorna el secreto compartido.
     */
    public static BigInteger acordarSecretoDiffieHellman() {
        // Llave pública del servidor: A = g^a mod p
        BigInteger publicaServidor = GENERADOR_G.modPow(SECRETO_SERVIDOR_A, PRIMO_P);
        // Llave pública del cliente: B = g^b mod p
        BigInteger publicaCliente = GENERADOR_G.modPow(SECRETO_CLIENTE_B, PRIMO_P);

        // El cliente calcula la llave compartida: S = A^b mod p
        BigInteger secretoCliente = publicaServidor.modPow(SECRETO_CLIENTE_B, PRIMO_P);
        // El servidor calcula la llave compartida: S = B^a mod p
        BigInteger secretoServidor = publicaCliente.modPow(SECRETO_SERVIDOR_A, PRIMO_P);

        if (!secretoCliente.equals(secretoServidor)) {
            throw new IllegalStateException("Fallo en la sincronización Diffie-Hellman");
        }
        return secretoCliente;
    }
    /**
     * Ajusta la clave obtenida de Diffie-Hellman a un arreglo exacto de 24 bytes para 3DES.
     */
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
     * Descifra un texto en Base64 utilizando el algoritmo 3DES (DESede).
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
            return "[Error al descifrar con 3DES: Verifique formato Base64 o parámetros de clave] Detalle: " + ex.getMessage();
        }
    }

    /**
     * Método auxiliar para cifrar libros de prueba cuando prepares tu carpeta del servidor.
     */
    public static String cifrarContenido3DES(String textoOriginal, BigInteger secretoDH) {
        try {
            SecretKeySpec llaveSimetrica = construirLlave3DES(secretoDH);
            Cipher cifrador = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cifrador.init(Cipher.ENCRYPT_MODE, llaveSimetrica);
            byte[] cifrado = cifrador.doFinal(textoOriginal.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception ex) {
            throw new RuntimeException("Error al cifrar texto de prueba", ex);
        }
    }
}
