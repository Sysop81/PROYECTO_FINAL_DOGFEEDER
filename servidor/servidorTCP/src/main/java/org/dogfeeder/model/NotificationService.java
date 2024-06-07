package org.dogfeeder.model;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.database.*;
import org.dogfeeder.mail.Email;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase NotificationService
 * Clase que representa el servicio de notificaciones para la vacunacion de la mascota, estado crítico de la tolva de
 * alimento y finalmente para comedero vacío o con poco alimento.
 */
public class NotificationService extends Thread{

    private NotificationDAO notificationDAO;
    private SettingsDAO settingsDAO;
    private PetDAO petDAO;
    private UserDAO userDAO;
    private Settings settings;
    private Pet pet;
    private HX711 hx711;
    private UltraSonic ultraSonic;
    private boolean isRunning;
    private static final int VACCINE_NOTIFY_INIT_HOUR = 10;  // PRODUCTION -> 8:00:0
    private static final int VACCINE_NOTIFY_FINISH_HOUR= 22; // PRODUCTION -> 10:00:0
    private static final Long WAIT_INIT = 10000L;
    private static final long WAIT_TIME  = 10000; // 10s DEBUG TIME // PRODUCTION TIME 1h -> 3600000
    private static final double FOOD_WEIGHT = 20.0;
    private static Logger4j logger = new Logger4j(NotificationService.class);

    public NotificationService(Conexion con, HX711 hx711, UltraSonic ultraSonic){
        super("Servicio notificaciones");
        this.isRunning = true;
        this.petDAO = new PetDAO(con);
        this.settingsDAO = new SettingsDAO(con);
        this.notificationDAO = new NotificationDAO(con);
        this.userDAO = new UserDAO(con);
        this.hx711 = hx711;
        this.ultraSonic = ultraSonic;
    }
    @Override
    public void run() {
        // Step 1.  Arranca el sistema de notificaciones
        waiting(WAIT_INIT);
        var msg = "[" + getName() + "] Servicio ACTIVO";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Mientras el servicio esté corriendo
        while(isRunning){
            // Racargar instancias
            pet = petDAO.getFirstPet();
            settings = settingsDAO.getSavedSetting();
            var dateTime = LocalDateTime.now();

            // Informa por CLI barrido de notificaciones del sistema
            CLInterface.showAlertAction("[" + getName() + "] Inicia ciclo de comprobaciones.");

            // Comprobar vacunación
            checkVaccineDay(dateTime);

            // Evaluamos si la tolva se encuentra con nivel crítico
            checkHopperLow(dateTime);

            // Evaluamos comedero cuenta con poco alimento o está vacío
            checkEmptyFeeder(dateTime);

            // Informa por CLI fin barrido notificaciones
            CLInterface.showAlertAction("[" + getName() + "] Finaliza ciclo de comprobaciones.");

            // Step X. Se realiza el tiempo de espera antes de nuevo ciclo de comprobaciones del sistema.
            waiting(null);
        }

        msg = "[" + getName() + "] Servicio DETENIDO";
        CLInterface.showAlertWarning(msg);
        logger.setWarning(msg);
    }


    /**
     * Método checkVaccineDay
     * Método encargado de comprobar si es necesario realizar la notificación para la vacunación anual de la mascota
     * @param dateTime Tiempo y hora actuales para el ciclo de comprobación
     */
    private void checkVaccineDay(LocalDateTime dateTime){
        // Step 1. Si se cumplen las condiciones para comprobar si se debe notificar. [Notificación anual]
        if(pet.isVaccineNotify() & dateTime.getMonthValue() == (pet.getGetVaccineMonth() + 1) &
                dateTime.getDayOfMonth() == pet.getVaccineDay()      &
                dateTime.getHour() >= VACCINE_NOTIFY_INIT_HOUR       &
                dateTime.getHour() <= VACCINE_NOTIFY_FINISH_HOUR){

            // Se inicia el proceso para checkear si se debe iniciar el proceso de notificación
            if(checkNotificationInDB(Notification.NotificationType.VACCINE)){
                // Se informa por CLI y Log
                var msg = "[" + getName() + "] Procede a realizar notificaciones. Día para vacunación de " + pet.getName();
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
                // Se realiza el envío de notificaciones
                sendNotificationToUsers( "DOG-FEEDER [NOTIFICACIÓN][ALERTA] Vacunación anual de " + pet.getName(),
                        "Hoy es el día de vacunación para el año " + LocalDate.now().getYear() + ".\n" +
                             "Hora de notificación : " + dateTime);
            }
            // SI ES FALSO -> Se ha notificado ya para este año [INFO -> Determinado por la fecha de vacunacion de la mascota. Si
            // la fecha de vacunación es modificada por algún usuario, se dispara un trigger en la DB que elimina la notificación
            // por tanto, si la fecha está por llegar, se volvera a notificar]. No se requieren acciones
        }
        // SI ES FALSO -> Servicio desactivado. No se requieren acciones
    }


    /**
     * Método checkHopperLow
     * Método encargado de determinsar si existe un nivel bajo en la tolva de alimento, de ser así inicia la acción para
     * notificar a los usuarios de DOG-FEEDER. Este tipo de notificacion es diarias [1 al día]
     * @param dateTime Tiempo y hora actuales para el ciclo de comprobación
     */
    private void checkHopperLow(LocalDateTime dateTime){
        // Step 1. se comprueba si está activa la notificación para el comedero vacío
        if(settings.isNotifyHopperLow()){
            // Step 2. Comprobamos  si la tolva presenta un nivel bajo
            if(ultraSonic.isLowFoodLevel()){
                // Step 3. Checkeamos si debemos notificar
                if(checkNotificationInDB(Notification.NotificationType.HOPPER)) {
                    // Informamos por CLI y Log
                    var msg = "[" + getName() + "] Procede a realizar notificaciones. La tolva cuenta con un nivel bajo de alimento";
                    CLInterface.showAlertAction(msg);
                    logger.setInfo(msg);
                    // Se realiza el envío de notificaciones
                    sendNotificationToUsers( "DOG-FEEDER [NOTIFICACIÓN][ALERTA] Tolva con poco alimento o vacío",
                            pet.getName() + " cuenta con poco alimento en la tolva. Realice acción de suministro lo antes posible.\n" +
                                 "Hora de notificación : " + dateTime);
                }

                // SI ES FALSO -> Se ha notificado ya en este día HOOPER OR FEEDER. No se requieren acciones

            }
            // SI ES FALSO -> Hay alimento en la tolva por encima del nivel bajo o crítico. No se requieren acciones
        }
        // SI ES FALSO -> Servicio desactivado. No se requieren acciones
    }


    /**
     * Método checkEmptyFeeder
     * Método encargado de comprobar si el comedero de la mascota asociada a DOG-FEEDER cuenta con poco alimento o se encuentra
     * vacío. En tal caso realiza el envío de notificaciones a los usuarios. Este tipo de notificacion es diarias [1 al día]
     * @param dateTime Tiempo y hora actuales para el ciclo de comprobación
     */
    private void checkEmptyFeeder(LocalDateTime dateTime){
        // Step 1. se comprueba si está activa la notificación para el comedero vacío
        if(settings.isNotifyFeederWithOutFood()){
            // Step 2. Realizamos medición de la galga de peso (O mediciones, ya que suelen dar medidas imprecisas,
            // repetimos la medición mientras sea <= ZERO)
            double foodWeight = 0;
            while(foodWeight <= 0){
                hx711.read();                      // Iniciamos lectura del modulo HX711
                foodWeight = hx711.getNetWeight(); // Obtenemos el peso del contenido del cuenco
            }

            // Step 3. Si el pesaje es menor que el peso límite para determinar si esta vacío o poco lleno.
            if(foodWeight < FOOD_WEIGHT){
                // Step 4. Checkeamos si se ha notificado en el día actual -> Así para FEEDER y HOOPER
                if(checkNotificationInDB(Notification.NotificationType.FEEDER)) {
                    // Informamos en CLI y Log
                    var msg = "[" + getName() + "] Procede a realizar notificaciones. El comedero cuenta con menos de " + FOOD_WEIGHT + " gramos";
                    CLInterface.showAlertAction(msg);
                    logger.setInfo(msg);
                    // Iniciamos la acción de envío de notificaciones MAIL
                    sendNotificationToUsers( "DOG-FEEDER [NOTIFICACIÓN][ALERTA] Comedero con poco alimento o vacío",
                            pet.getName() + " cuenta con poco alimento en su comedero. Realice acción de suministro lo antes posible.\n" +
                             "Hora de notificación : " + dateTime);
                }
                // SI ES FALSO -> Se ha notificado ya en este día HOOPER OR FEEDER. No se requieren acciones
            }
            // SI ES FALSO -> Alimento disponible. No se requieren acciones
        }
        // SI ES FALSO -> Servicio desactivado. No se requieren acciones
    }


    /**
     * Método checkNotificationInDB
     * Método encargado de determinar en función del tipo de notificación y de si esta previamente existe en la DB, si
     * se ha producido una acción de inserción o actualización, ya que esto determina que se debe realizar el proceso de
     * notificación a los usuarios validados.
     * @param type Tipo de notificación
     * @return boolean Devuelve verdadero en caso de realizar alguna acción de inserción o actualización
     */
    private boolean checkNotificationInDB(Notification.NotificationType type){
        var notification =  notificationDAO.getLastNotifyByType(type);
        var isNotify = true;

        if(notification == null){
            // No existe en la DB y se procede a insertar
            notificationDAO.postNotification(new Notification(type));
        }else if(type.equals(Notification.NotificationType.VACCINE) && !notification.isNotifyThisYear()){
            // Actualizamos la notificacion Vacunacion ANUAL
            notificationDAO.postNotification(notification);
        }else if(!type.equals(Notification.NotificationType.VACCINE) && !notification.isNotifyToday()){
            // Actualizamos las notificaciones HOPPER y FEEDER DIARIAS
            notificationDAO.postNotification(notification);
        }else{
            isNotify = false;
        }

        return isNotify;
    }


    /**
     * Método sendNotificationToUsers
     * Método encargado de realizar el envío de notificaciones a los usuario validos para recibir emails
     * @param msgSubject Título del correo electrónico
     * @param msg Cuerpo del mensaje
     */
    private void sendNotificationToUsers(String msgSubject, String msg){
        // Se procede a enviar los correos
        Email email = new Email();
        email.sendMailTo(userDAO.getAllValidMail(),msgSubject,msg);

        // Se informa mediante CLI & Log
        var infoMsg = "[" + getName() + "] Finaliza el proceso de notificación";
        CLInterface.showAlertAction(infoMsg);
        logger.setInfo(infoMsg);
    }


    /**
     * Método waiting
     * Método encargado de esperar el tiempo por defecto para los ciclos del metodo rum o bien personalizar la espera
     * enviando el parametro time no nulo
     * @param time Tiempo de espera
     */
    private void waiting(Long time){
        try {
            Thread.sleep( time != null ? time : WAIT_TIME);
        } catch (InterruptedException e) {
            var msg = "Servicio de notificaciones interrumpido. " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }
    }


    /**
     * Método finish
     * Método encargado de interrumpir el servicio de notificaciones
     */
    public void finish(){
        isRunning = false;
    }

    /**
     * Getter para la propiedad isRunning
     * @return boolean Valor de la propiedad isRunning
     */
    public boolean isRunning(){
        return isRunning;
    }
}
