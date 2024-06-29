package org.dogfeeder.model;

/**
 * Enum ServerStateCodes
 * Códigos de estado para el manejo del servicio
 */
public enum ServerStateCodes {
    CLOSE(-1),
    LOGIN(0),
    REGISTER(1),
    RECOVERYPASS(2),
    FOOD(3),
    AUDITS(4),
    LASTAUDIT(5),
    GRAPHAUDITS(6),
    PETDATA(7),
    POSTPET(8),
    SETTINGSDATA(9),
    LED(10),
    HOPPERLOW(11),
    FEEDEREMPTY(12),
    MAXFOODRATION(13),
    REPORT(14),
    RESET(15),
    IDLE_STATE(20);

    private final int STATUS_CODE;

    ServerStateCodes(int code){
        this.STATUS_CODE = code;
    }

    public int getStatusCode(){
        return STATUS_CODE;
    }

}
