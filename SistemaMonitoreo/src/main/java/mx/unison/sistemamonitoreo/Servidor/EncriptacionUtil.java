/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.sistemamonitoreo.Servidor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


public class EncriptacionUtil {
    
    private static final String CLAVE_SECRETA = "UnisonMonitor123";
    private static final String ALGORITMO = "AES";
    
    public static String encriptar(String mensaje) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(), ALGORITMO);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] mensajeEncriptado = cipher.doFinal(mensaje.getBytes());
            return Base64.getEncoder().encodeToString(mensajeEncriptado);
            
        } catch (Exception e) {
            System.err.println("Error al encriptar: " + e.getMessage());
            return null;
        }
    }
    
    public static String desencriptar(String mensajeEncriptado) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(), ALGORITMO);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] mensajeDesencriptado = cipher.doFinal(Base64.getDecoder().decode(mensajeEncriptado));
            return new String(mensajeDesencriptado);
            
        } catch (Exception e) {
            System.err.println("Error al desencriptar: " + e.getMessage());
            return null;
        }
    }
}
