package com.stockflow.service;

import com.stockflow.exception.AutenticacionException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class PasswordHasher {
    private PasswordHasher() { }

    public static boolean verificar(char[] password, String codificado) {
        if (password == null || codificado == null) return false;
        String[] partes = codificado.split("\\$");
        if (partes.length != 3) return false;
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, esperado.length * 8);
            byte[] obtenido = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(esperado, obtenido);
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AutenticacionException("No se pudo validar la contraseña configurada.");
        }
    }
}
