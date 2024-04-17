package org.example.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Hasher {

    /**
     * Método encode. Unidireccional [MessageDigest][SHA-256]
     * Este método se encarga de Hashear de forma unidireccional la cadena que entra como parametro.
     * @param originalPassword
     * @return String
     * @throws NoSuchAlgorithmException
     */
    public static String encode(String originalPassword){
        try {
            // Step 1. Se obtiene una instancia de MessageDigest con el algoritmo SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Step 2. Generamos un flujo de butes con la contraseña
            md.update(originalPassword.getBytes());

            // Step 3. Obtenemos el Hash resultante
            byte[] hashedBytes = md.digest();

            // Step 4. Se convierte el hash de bytes a una representación Base64 y se retorna
            return Base64.getEncoder().encodeToString(hashedBytes);
        }catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
