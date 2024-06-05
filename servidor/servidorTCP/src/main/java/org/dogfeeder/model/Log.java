package org.dogfeeder.model;

/**
 * Clase Log
 * Clase encargada de modelar los registros de log capturados en cada sesión del servicio
 */
public class Log {
    private String date;
    private String time;
    private int code;
    private String status;
    private String thread;
    private String msg;

    /**
     * Getter para la propiedad Date
     * @return String Cadena que representa la fecha
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter para la propiedad date
     * @param date Cadena que representa la fecha
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter para propiedad time
     * @return String Cadena que representa la hora
     */
    public String getTime() {
        return time;
    }

    /**
     * Setter para la propiedad time
     * @param time Cadena que representa la hora
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Getter para la propiedad code
     * @return int Entero que representa el código
     */
    public int getCode() {
        return code;
    }

    /**
     * Setter para la propiedad code
     * @param code entero que representa el código
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Getter para la propiedad status
     * @return String Código de estado del log
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter para la propiedad status
     * @param status Código de estado del log
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter para la propiedad thread
     * @return String Cadena que represnta el hilo que lanza el log
     */
    public String getThread() {
        return thread;
    }

    /**
     * Stter para la propiedad Thread
     * @param thread Hilo que lanza la acción del log
     */
    public void setThread(String thread) {
        this.thread = thread;
    }

    /**
     * Getter para la propiedad msg
     * @return String Cadena con el msg capturado en el log
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Setter para la propiedad msg
     * @param msg Cadena que representa el mensaje del log
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Log{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", code=" + code +
                ", status='" + status + '\'' +
                ", thread='" + thread + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
