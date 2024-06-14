package org.dogfeeder.model;

/**
 * Enum ResponseCodes
 * Códigos de estado para el Servidor
 */
public enum ResponseCodes {
    ERROR(0),        // Códido de respuesta de error genérico
    OK(1),           // Código de respuesta correcta genérico
    EMPTY_DATA(2),
    ERROR_SERVO_IN_USE(3),
    ERROR_STUCK_HOPPER(4),
    ERROR_EMPTY_HOPPER(5),
    ERROR_USER_WORKING(6);

    private final int CODE;

    ResponseCodes(int code){
        this.CODE = code;
    }

    public String getCode(){
        return String.valueOf(CODE);
    }
}
