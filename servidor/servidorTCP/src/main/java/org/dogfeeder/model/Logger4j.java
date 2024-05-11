package org.dogfeeder.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Logger4j {
    private Logger log;

    public Logger4j(Class targetClass){
        this.log = LogManager.getLogger(targetClass);
    }

    public void setDebug(String msg){
        this.log.debug(msg);
    }

    public void setInfo(String msg){
        this.log.info(msg);
    }

    public void setWarning(String msg){
        this.log.warn(msg);
    }

    public void setError(String msg){
        this.log.error(msg);
    }
}
