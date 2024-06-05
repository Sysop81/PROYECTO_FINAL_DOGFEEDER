package org.dogfeeder.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase Logger4j
 * Esta clase se encarga del manejo del Logger para las distintas clases
 */
public class Logger4j {
    private Logger log;

    /**
     * Constructor
     * @param targetClass
     */
    public Logger4j(Class targetClass){
        this.log = LogManager.getLogger(targetClass);
    }

    /**
     * Setter para establecer un mensaje de log a nivel DEBUG
     * @param msg Cadena de caracteres que representa la información a incluir en el LOG
     */
    public void setDebug(String msg){
        this.log.debug(msg);
    }

    /**
     * Setter para establecer un mensaje de log a nivel INFO
     * @param msg Cadena de caracteres que representa la información a incluir en el LOG
     */
    public void setInfo(String msg){
        this.log.info(msg);
    }

    /**
     * Setter para establecer un mensaje de log a nivel WARNING
     * @param msg Cadena de caracteres que representa la información a incluir en el LOG
     */
    public void setWarning(String msg){
        this.log.warn(msg);
    }

    /**
     * Setter para establecer un mensaje de log a nivel ERROR
     * @param msg Cadena de caracteres que representa la información a incluir en el LOG
     */
    public void setError(String msg){
        this.log.error(msg);
    }
}
