package org.dogfeeder.crypto;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Clase Hasher
 * Se encarga de la codificación unidireccional
 */
public class Hasher {

    private static Logger4j logger = new Logger4j(Hasher.class);

    /**
     * Método encode. Unidireccional [MessageDigest][SHA-256]
     * Este método se encarga de Hashear de forma unidireccional la cadena que entra como parametro.
     * @param originalPassword
     * @return String Cadena codificada
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
            var msg = "Se ha producido un error al codificar la cadena de entrada." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return null;
    }
}
