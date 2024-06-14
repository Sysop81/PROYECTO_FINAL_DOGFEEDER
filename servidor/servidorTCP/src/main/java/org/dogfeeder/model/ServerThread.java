package org.dogfeeder.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pi4j.io.gpio.RaspiPin;
import org.dogfeeder.Utils.Tools;
import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.crypto.Hasher;
import org.dogfeeder.database.*;
import org.dogfeeder.mail.Email;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;

/**
 * Clase ServerThread
 * Clase encargada de modelar el flujo de trabajo de cada instancia (socket) del servicio.
 */
public class ServerThread extends Thread{

    private User loggedUser;
    private UserDAO userDAO;
    private SupplyFoodAuditDAO sfaDAO;
    private PetDAO petDAO;
    private ServoMotor servomotor;
    private HX711 hx711module;
    private UltraSonic ultraSonic;
    private Relay relay;
    private Socket client;
    private int sStatus;
    private String responseCode;
    private SettingsDAO settingsDAO;
    private String msg;
    private static int index = 0;
    private static Logger4j logger = new Logger4j(ServerThread.class);
    private static Gson gson = new Gson();
    private static Semaphore smfPet = new Semaphore(1);
    private static Semaphore smfLedSettings = new Semaphore(1);
    private static Semaphore smfHopperSettings = new Semaphore(1);
    private static Semaphore smfFeederSettings = new Semaphore(1);
    private static Semaphore smfMaxFoodSettings = new Semaphore(1);

    /**
     * Constructor
     * @param client Cliente que se conecta al servicio
     * @param con  Conexión con la DB
     * @param hx711module Instancia para la gestión para pesajes
     * @param ultraSonic Instancia para la gestión para mediciónes de tolva
     * @param relay Instancia para la gestión del relé que activa el microcontrolador
     */
    public ServerThread(Socket client, Conexion con, HX711 hx711module, UltraSonic ultraSonic,Relay relay){
        super("Cliente-" + (index++));
        this.client = client;
        this.sStatus = ServerStateCodes.LOGIN.getStatusCode();
        this.servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress());
        this.userDAO = new UserDAO(con);
        this.sfaDAO = new SupplyFoodAuditDAO(con);
        this.petDAO = new PetDAO(con);
        this.settingsDAO = new SettingsDAO(con);
        this.responseCode =  ResponseCodes.ERROR.getCode();
        this.hx711module = hx711module;
        this.ultraSonic = ultraSonic;
        this.relay = relay;
        this.msg = "";
    }


    /**
     * Método run
     * Se encarga del manejo de la maquina de estados al la que accede cada cliente del servicio
     */
    @Override
    public void run() {

        try {
            // Maquina Estados. Rotativo hasta que el estado sea -1
            while(sStatus != -1){
                // Se escucha el endpoint de la petición cliente y se recupera el estado de la instancia
                sStatus = setServerStatus(readMsgFromClient());
                // Se opera en función del estado de cada cliente
                switch(sStatus){
                    case -1:
                        // Exit
                        exit();
                        break;
                    case 0:
                        // Login
                        login();
                        break;
                    case 1:
                        // Register user
                        register();
                        break;
                    case 2:
                        // Recovery pass
                        recoveryPass();
                        break;
                    case 3:
                        // Supply food
                        supplyFood();
                        break;
                    case 4:
                        // GET food audits
                        getSupplyFoodAudits();
                        break;
                    case 5:
                        // Get last food audit
                        getLastSupplyFoodAudit();
                        break;
                    case 6:
                        // Get audit data for statistic graph
                        getDataForSupplyFoodStatistic();
                        break;
                    case 7:
                        // Get Pet data
                        getPetData();
                        break;
                    case 8:
                        // POST && PUT Pet data
                        updatePetData();
                        break;
                    case 9:
                        // GET Dogfeeder settings
                        getDogFeederSettings();
                        break;
                    case 10:
                        // SET led ON / OFF
                        managePowerOfLed();
                        break;
                    case 11:
                        // SET hooper notify
                        manageHopperLowNotify();
                        break;
                    case 12:
                        // Set empty food
                        manageEmptyFeederNotify();
                        break;
                    case 13:
                        // Set food ration
                        manageMaxFoodRation();
                        break;
                    case 14:
                        // Get DOGFEEDER report
                        getDogFeederReport();
                        break;
                }
            }

        }catch (Exception e) {
            msg =  (loggedUser != null ? loggedUser.getEmail() : getName()) + " cierra el programa cliente";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }finally {
            try {
                this.sStatus = ServerStateCodes.LOGIN.getStatusCode();
                this.client.close();
            } catch (IOException e) {
                msg = "Error al cerrar la conexión cliente. " + e.getMessage();
                CLInterface.showAlertDanger(msg);
                logger.setError(msg);
            }
        }
    }


    /**
     * Método getActualUser
     * Este método se encarga de manejar la información procedente de la request del cliente que desea loguearse en el
     * sistema y devolver un nuevo objeto usuario.
     * @param request Objeto con los datos llegados desde el cliente
     * @return User Usuario que pretende loguearse en la app
     */
    private User getActualUser(String request){
        var user = gson.fromJson(request,User.class);
        user.setPassword(Hasher.encode(user.getPassword()));
        return user;
    }


    /***
     * Método setServerStatus
     * Este método se encarga de evaluar el endpoint solicitado por el cliente para de esta manera seleccionar el estado
     * de la maquina de estados
     * @param request Cadena con en endponit
     * @return int Código de estado para la maquina de estados
     */
    private int setServerStatus(String request) {
        try {
            // Step 1. Se obtiene el valor del enumerado y se devuelve el código de estado asociado para
            //         ir a un ENDPOINT de la máquina de estados.
            return ServerStateCodes.valueOf(request.toUpperCase()).getStatusCode();
        } catch (IllegalArgumentException e) {
            return ServerStateCodes.IDLE_STATE.getStatusCode();
        }
    }


    /**
     * Método exit
     * Este método se encarga de realizar algunas acciones necesarias cuando el cliente cierra sesión en el servicio
     * DOGFEEDER.
     * @throws IOException
     */
    private void exit() throws IOException {
        // Step 1. Se maneja la info del CLI del servidor TCP y se añade información al LOG.
        msg = loggedUser != null ? loggedUser.getEmail() : getName() + " finaliza la sesión en servidor TCP - DOGFEEDER";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Cambiamos el código de respuesta y enviamos la información al cliente.
        responseCode = ResponseCodes.OK.getCode();
        sendMsgToClient(responseCode);
    }


    /**
     * Método login
     * Este método se encarga de verificar el LOGIN de un usuario en el servicio DOGFEEDER.
     * @throws IOException
     */
    private void login() throws IOException {
        // Step 0. Se muestra información en el CLI del servidor y se añade la información al log
        msg = getName() + " Inicia acción de LOGIN";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 1. Se obtienen los datos del cliente y se instancia el objeto de tipo usuario. Y se consulta si dicho
        //         usuario existe en la DB.
        var request = readMsgFromClient();
        this.loggedUser = getActualUser(request);
        var targetUser = userDAO.getUserbyEmail(this.loggedUser.getEmail());
        var response =  ResponseCodes.ERROR.getCode();

        // Step 2. Se evalua si el usuario obtenido desde el DB es igual al usuario que se pretende loguear. Si escorrecto,
        //         se modifica el estado en la maquina de estados y se cambia el código de respuesta
        if (targetUser != null && this.loggedUser.getPassword().equals(targetUser.getPassword())){
            setName(this.loggedUser.getEmail());
            this.loggedUser.setID(targetUser.getID());
            sStatus = ServerStateCodes.IDLE_STATE.getStatusCode(); // Logueado con estado ocioso en la maquina de estados
            response =  ResponseCodes.OK.getCode();
        }

        // Step 3. Se maneja la información que se muestra en el servidor TCP y  se almacena en el LOG.
        msg = getName() + " LOGIN ";
        if(sStatus == ServerStateCodes.IDLE_STATE.getStatusCode()){
            CLInterface.showAlertAction( msg + "correcto");
            logger.setInfo(this.loggedUser.getEmail() + " LOGIN correcto");
        }else{
            CLInterface.showAlertDanger(msg + "incorrecto");
            logger.setWarning(this.loggedUser.getEmail() + " LOGIN incorrecto");
        }

        // Step 4. Se envía la respuesta al cliente.
        sendMsgToClient(response);
    }


    /**
     * Método register
     * Este método se encarga de registrar un usuario en el servicio DOGFEEDER
     * @throws IOException
     */
    private void register() throws IOException {
        // Step 0. Se muestra información en el CLI del servidor y se añade la información al log
        msg = getName() + " Inicia acción de registro de nuevo usuario";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 1. Se obtiene el correo electrónico objetivo y se comprueba que no exista en la DB
        var targetEmail = readMsgFromClient();
        if(userDAO.getUserbyEmail(targetEmail.toLowerCase()) != null) {
            msg = "El usuario " + targetEmail.toLowerCase() + " ya existe en la base de datos";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
            sendMsgToClient(ResponseCodes.EMPTY_DATA.getCode());
            sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
            return;
        }

        // Step 2. Se maneja la validación del correo del usuario
        manageValidation(targetEmail,false);

        // Step 3. Realizamoss el proceso de guardado del usuario en la DB
        var password = readMsgFromClient();
        // Realizamos la peticion a la clase de acceso a datos que maneja las operaciones DB para los usuarios.
        //         Obtenemos el código de respuesta para enviar al cliente.
        responseCode = String.valueOf(userDAO.postUser(new User(targetEmail,Hasher.encode(password))));
        if (responseCode.equals(ResponseCodes.OK.getCode())){
            CLInterface.showAlertAction(targetEmail + " registrado correctamente");
            logger.setInfo(targetEmail + " se registra correctamente en el servicio DOGFEEDER");
        }else{
            CLInterface.showAlertWarning(targetEmail + " NO se registra en el servicio DOGFEEDER");
            logger.setWarning(targetEmail + " NO se registra en el servicio DOGFEEDER");
        }

        // Step 4. Se envía la respuesta al cliente y se establece el estado como no logueado.
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.LOGIN.getStatusCode();
    }


    /**
     * Método recoveryPass
     * Método encargado cambiar el password para un usuario
     * @throws IOException
     */
    private void recoveryPass() throws IOException {
        msg = getName() + " Inicia acción para cambio de password";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 0. validar el correo electrónico
        var targetEmail = readMsgFromClient();

        if(userDAO.getUserbyEmail(targetEmail.toLowerCase()) != null) {

            // Step 2. Se maneja la validación del correo del usuario
            manageValidation(targetEmail,true);

            // Step 3. Cambio de password
            var newPassword = readMsgFromClient();
            responseCode = String.valueOf(userDAO.setNewPassword(targetEmail, Hasher.encode(newPassword)));
            sendMsgToClient(responseCode);

        }else{
            // No existe el correo en la DB
            sendMsgToClient(ResponseCodes.ERROR.getCode());
        }
    }


    /**
     * Método manageValidation
     * Método encargado de manejar la validación del correo electrónico del usuario
     * @param targetEmail Email de usuario que pretende recuperar o registrarse en la aplicación
     * @param isRecoveryPass Boolean que determina si es una operación de recupueración de registro
     * @throws IOException
     */
    private void manageValidation(String targetEmail,boolean isRecoveryPass) throws IOException {
        // Step 1. Se obtiene el código aleatorio para realizar el registro
        var validCode = Tools.getRandonCode();

        // Step 2. Se envía el correo de comprobación con el código a insertar en el cliente
        Email email = new Email();
        boolean isSend = email.sendMailTo(targetEmail,
                "DOG-FEEDER" +
                        (isRecoveryPass ?  " Solicitud de cambio de contraseña" : " Verificación de cuenta de correo"),
                "Código de verificación: " + validCode,null);

        // Se informa mediante CLI y Log
        msg = "Se envía email al usuario " + targetEmail.toLowerCase() + " con el código de verificación";
        CLInterface.showAlertWarning(msg);
        logger.setInfo(msg);

        // Se notifica al cliente el envío del código
        responseCode = isSend ? ResponseCodes.OK.getCode() : ResponseCodes.ERROR.getCode();
        sendMsgToClient(responseCode);

        // Step 3. El usuario envía el código recibido por correo
        boolean isValidCode = false;
        while(!isValidCode){
            // Se recibe y evalua el código enviado por el cliente
            var clientCode = readMsgFromClient();
            isValidCode = String.valueOf(validCode).equals(clientCode);
            msg = "Se recibe el código de verificación del " + getName() + ". ESTADO CÓDIGO : " + (isValidCode ? "VÁLIDO" : "INVALIDO");

            // CLI y Log en función de si el código es válido o no
            if(isValidCode){
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
            }else{
                CLInterface.showAlertWarning(msg);
                logger.setError(msg);
            }
            // Step 4. Se envía el código de respuesta al cliente en base a la validación
            responseCode = String.valueOf(validCode).equals(clientCode) ? ResponseCodes.OK.getCode() : ResponseCodes.ERROR.getCode();
            sendMsgToClient(responseCode);
        }
    }


    /***
     * Método updatePetData
     * Este método se encarga de realizar las operaciones de inserción / actualización de la mascota asociada a DOGFEEDER
     * @throws IOException
     */
    private void updatePetData() throws IOException {
        // Step 0. Se informa mediante CLI de la operación a realizar
        msg = loggedUser.getEmail() + " inicia acción para actualizar los datos de la mascota";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 1. Comprobamos si hay algún usuario estableciendo datos para la mascota
        if(smfPet.tryAcquire()) {

            // Se procede a la lectura de datos procedentes desde el cliente y se instancia un nuevo objeto Pet
            var strPet = readMsgFromClient();
            var pet = gson.fromJson(strPet, Pet.class);

            // Se obtiene el código de respuesta procedente de la operación realizada en la DB.
            responseCode = String.valueOf(petDAO.postPet(pet));

            // Manejo de alertas informativas CLI y log del sistema
            if (responseCode.equals(ResponseCodes.OK.getCode())) {
                logger.setInfo(loggedUser.getEmail() + " actualiza los datos de la mascota en la base de datos correctamente");
                CLInterface.showAlertAction(loggedUser.getEmail() + " operación realizada correctamente");
            } else {
                logger.setWarning(loggedUser.getEmail() + " no puede actualizar los datos de la mascota. Código respuesta -> " + responseCode);
                CLInterface.showAlertWarning(loggedUser.getEmail() + " operación NO realizada correctamente");
            }

            // Liberamos los permisos
            smfPet.release();
        }else{
            responseCode = ResponseCodes.ERROR_USER_WORKING.getCode();
            msg = loggedUser.getEmail() + " no puede establecer los datos para la mascota, hay otro usuario trabajando en ello";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 2. Se da respuesta al cliente y se establece el estado ocioso
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método getPetData
     * Método encargado de recuperar los datos de la mascota asociada a DOGFEEDER
     * @throws IOException
     */
    private void getPetData() throws IOException {
        // Step 1. CLI & Log
        msg = loggedUser.getEmail() + " inicia acción para recuperar la mascota asociada a DOG-FEEDER";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Se recuperan los datos de la mascota o no existe mascota asociada. Se maneja la información del CLI y
        //         Log
        var pet = petDAO.getFirstPet();
        var res = ResponseCodes.EMPTY_DATA.getCode();
        if(pet != null){
            res =  gson.toJson(pet, Pet.class);
            msg = "Recupera los datos de la mascota asociada a DOG-FEEDER";
            CLInterface.showAlertAction(msg);
            logger.setWarning(msg);
        }else{
            msg = "No existe una mascota asociada a DOG-FEEDER";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 3. Se envía la respuesta al cliente
        sendMsgToClient(res);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método supplyFood
     * Se encarga de suministrar alimento al recipiente de peso que esta controlado por la galga que mide el pesaje de alimento.
     * Códigos de estado.
     *     código 0 -> Error de funcionamiento al suministrar alimento.
     *     código 1 -> Respuesta correcta para el suministro de alimento.
     *     código 3 -> Error debido a que ya se está proporcionando alimento. El servoMotor está en uso
     *     código 4 -> Error tolva vacía (tambien puede estar atascada).
     *     código 5 -> Error tolva vacía
     * @throws IOException
     */
    private void supplyFood() throws IOException {
        // Step 1. Se muestra información por el CLI del servidor DOGFEEDER y se define un código de estado por defecto.
        msg = loggedUser.getEmail() + " inicia acción para suministro de alimento";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);
        responseCode = ResponseCodes.ERROR_SERVO_IN_USE.getCode();

        // Step 2. Se evalua si el servoMotor para el suministro de alimento está en uso o no.
        if (!servomotor.isBussy()) {

            // Se suministra alimento a la mascota
            responseCode = ServoMotor.supplyFood(servomotor,   // Instancia del servoMotor principal
                                                 hx711module,  // Instancia del módulo de pesaje
                                                 ultraSonic,   // Instancia del módulo de medición tolva
                                                 settingsDAO,  // DAO preferencias del sistema
                                                 sfaDAO,       // DAO Auditoría de suministro de alimento
                                                 loggedUser,   // Usuario que realiza la operación
                                                 false);       // No es el comodero quien realiza la acción

            // Se maneja la información en el Log y CLI
            msg = "Finaliza el proceso de suministro de alimento con código de estado " + responseCode;
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);

        }else{
            // Se manejan los mensajes de warning en el CLI y Log
            CLInterface.showAlertWarning(loggedUser.getEmail() + " el servomotor para el suministro de alimentos está en uso");
            logger.setWarning(loggedUser.getEmail() + " no suministra alimento [Servomotor en uso]");
        }

        // Step 3. Se envía el código de respuesta al cliente
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método getSupplyFoodAudits
     * Este método se encarga de obtener los registros de auditoría para el suministro de alimento
     * @throws IOException
     */
    private void getSupplyFoodAudits() throws IOException {
        // Step 0. Salida de información por el CLI del servidor
        msg = loggedUser.getEmail() + " inicia acción para obtener los registros de auditoría";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 1. Se accede a la DB para obtener los registros
        var listOfAuditSupplyFood = sfaDAO.getAll();

        // Step 2. Se evalua si existes registros de auditoría que enviar al cliente.
        if(listOfAuditSupplyFood.isEmpty()){
            // No existen registros
            msg = "No existen registros de auditoría que mostrar";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
            sendMsgToClient(ResponseCodes.EMPTY_DATA.getCode());
        }else{
            // Existen registros
            // Generación Array JSON de salida
            JsonArray jsonArray = new JsonArray();
            for(SupplyFoodAudit item : listOfAuditSupplyFood){
                var aStrDateTime = item.getTimeStamp().split(" ");
                JsonObject jsonOb = new JsonObject();
                jsonOb.addProperty("ID",item.getID());
                jsonOb.addProperty("user",item.getUser().getEmail());
                jsonOb.addProperty("date",aStrDateTime[0]);
                jsonOb.addProperty("time",aStrDateTime[1]);
                jsonOb.addProperty("weight",item.getWeight());
                jsonArray.add(jsonOb);
            }

            String res = jsonArray.toString();

            // Step 3. Se realiza el envío de datos al cliente
            sendMsgToClient(res);

            // Step 4. Se registra la acción en el CLI y Logger
            msg = loggedUser.getEmail() + " procesa y envía " + listOfAuditSupplyFood.size() + " registros";
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);
        }

        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /***
     * Método getLastSupplyFoodAudit
     * Este método se encarga de obtener el último registro de comida dispensado a la mascota
     * @throws IOException
     */
    private void getLastSupplyFoodAudit() throws IOException {
        // Step 0. Se muestra la información por el CLI del servidor
        msg = loggedUser.getEmail() + " inicia acción para recuperar al último registro de alimento suministrado";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 1. Se obtiene el útimo registro de comida suministrada y se envía al cliente. En su defecto, se informa
        //         de la situación manejando un código de error generíco
        var lastSfa = sfaDAO.getLastSupplyFoodRegister();
        var res = ResponseCodes.ERROR.getCode(); //Default response
        msg = loggedUser.getEmail() + " procesa y envía el útimo registro de alimento suministrado";

        if(lastSfa != null){
            var aStrDateTime = lastSfa.getTimeStamp().split(" ");
            JsonObject jsonOb = new JsonObject();
            jsonOb.addProperty("ID",lastSfa.getID());
            jsonOb.addProperty("user",lastSfa.getUser().getEmail());
            jsonOb.addProperty("date",aStrDateTime[0]);
            jsonOb.addProperty("time",aStrDateTime[1]);
            jsonOb.addProperty("weight",lastSfa.getWeight());
            jsonOb.addProperty("hopperStatus",ultraSonic.getFoodLevel());

            res = jsonOb.toString();

            CLInterface.showAlertInfo(msg);
            logger.setInfo(msg);
        }else{
            msg = loggedUser.getEmail() + " No obtiene ningún registro de la DB. Código de error -> " + res;
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 2. Se envía la respuesta al cliente y se establece el estado ocioso
        sendMsgToClient(res);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método getDataForSupplyFoodStatistic
     * Método encargado de obtener los registros de la vista establecida en la base de datos para recopilar información
     * para que el cliente genere una gráfica de barras con;
     *  1º Tomas realizadas por mensualidad
     *  2ª Gramos de alimento suministrado por mensualidad
     * @throws IOException
     */
    private void getDataForSupplyFoodStatistic() throws IOException {
        // Step 1. Se maneja CLI y Log
        msg = loggedUser.getEmail() +
                " inicia acción para acceder al historial de estadítica para generar la gráfica de alimimento suministrado";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Se recuperan los registros de la vista
        var listOfStatistic = sfaDAO.getStatisticByYear(LocalDate.now().getYear());

        if(listOfStatistic.isEmpty()){
            msg = "No existen registros para mostrar el historial de estadística";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
            sendMsgToClient(ResponseCodes.EMPTY_DATA.getCode());
        }else {
            // Existen datos. Se crea la cadena de respuesta

            // Generación Array JSON de salida
            JsonArray jsonArray = new JsonArray();
            for(StatisticFood item : listOfStatistic){
                JsonObject jsonOb = new JsonObject();
                jsonOb.addProperty("monthNumber",item.getMonth_number());
                jsonOb.addProperty("CountTakes",item.getCount_takes());
                jsonOb.addProperty("totalWeight",item.getTotal_weight());
                jsonArray.add(jsonOb);
            }

            String res = jsonArray.toString();

            // Se realiza el envío de datos al cliente
            sendMsgToClient(res);

            // CLI & Log
            msg = loggedUser.getEmail()+" recupera datos para recrear la gráfica de estadística.";
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);
        }
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método getDogFeederSettings
     * Metodo encargado de obtener los parametros de configuració previamente guardados
     * @throws IOException
     */
    private void getDogFeederSettings() throws IOException{
        // Step 1. CLI & Log
        msg = loggedUser.getEmail() + " inicia acción para obtener los parámetros de configuración de DOGFEEDER";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Se obtienen los parámetros de configuración
        var settings = settingsDAO.getSavedSetting();
        var res = ResponseCodes.EMPTY_DATA.getCode();

        // Step 3. Se genera la informacion CLI y Log. Se establece el JSONstring en la variable de resupuesta, en caso de que
        // los parametros de configuración no sean nulos.
        if (settings != null) {
            res = gson.toJson(settings, Settings.class);
            msg = "Recupera los parámetros de configuración asociados a DOG-FEEDER";
            CLInterface.showAlertAction(msg);
            logger.setWarning(msg);
        } else {
            msg = "No se pueden recuperar los parametros de configuración asociados a DOG-FEEDER";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 4. Se realiza el envío de datos al cliente
        sendMsgToClient(res);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método managePowerOfLed
     * Método encargado de realizar la operación de encendido y pagado de la tira de leds que ilumina el habitáculo
     * donde se encuentra la electrónica de DOGFEEDER. Para ello activa / desactiva el relé que alimenta la raspberry
     * pi PICO.
     * @throws IOException
     */
    private void managePowerOfLed() throws IOException {
        // Step 1. Se muestra por el CLI del servidor la operación que pretende realizar el usuario
        msg = loggedUser.getEmail() + " inicia acción para cambiar estado de los LEDs";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Comprobamos que no existe ningún usuario aplicando alguna configuración
        if(smfLedSettings.tryAcquire()) {
            // Se realiza la lectura de los datos asociados a la petición
            var isOn = Boolean.valueOf(readMsgFromClient());

            // Se realiza la operación ternaria sobre el relé encargado de encencer / apagar el microcontrolador
            if (isOn) {
                msg = "LEDs encendidos";
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
                relay.relayOn(); // ON
            } else {
                msg = "LEDs apagados";
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
                relay.relayOff(); // OFF
            }

            // Se establece la configuración en la base de datos y se liberan los recursos
            responseCode = String.valueOf(settingsDAO.setLedStatus(isOn));
            smfLedSettings.release();
        }else{
            // Respuesta en caso de que otro usuario este cambiando la configuración
            responseCode = ResponseCodes.ERROR_USER_WORKING.getCode();
            msg = loggedUser.getEmail() + " no puede establecer la configuración para la iluminación, Hay otro usuario trabajandoen ello";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 3. Se devuelve la respuesta al cliente
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método manageHopperLowNotify
     * Método encargado de activar / desactivar las notificaciones para el estado crítico de la tolva
     * @throws IOException
     */
    private void manageHopperLowNotify() throws IOException {
        // Step 1. Se muestra por el CLI del servidor la operación que pretende realizar el usuario
        msg = loggedUser.getEmail() + " inicia acción para cambiar la notificación de estado de la tolva";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Comprobamos que no exista otro usuario aplicando la configuración
        if(smfHopperSettings.tryAcquire()) {
            // Se realiza la lectura de los datos asociados a la petición
            var isNotifyOn = Boolean.valueOf(readMsgFromClient());

            msg = (isNotifyOn ? "Activa" : "Desactiva") + " la notificación de aviso para tolva vacía";
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);

            // Se establece la configuración en la base de datos y liberamos recursos
            responseCode = String.valueOf(settingsDAO.setHopperLowNotify(isNotifyOn));
            smfHopperSettings.release();
        }else{
            // Respuesta en caso de que otro usuario este cambiando la configuración
            responseCode = ResponseCodes.ERROR_USER_WORKING.getCode();
            msg = loggedUser.getEmail() + " no puede establecer la configuración para la notificación de la tolva, Hay otro usuario trabajandoen ello";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 3. Se devuelve la respuesta al cliente
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método manageEmptyFeederNotify
     * Método encargado de activar / desactivar las notificaciones para aviso recipiente de alimento vacío
     * @throws IOException
     */
    private void manageEmptyFeederNotify() throws IOException {
        // Step 1. Se muestra por el CLI del servidor la operación que pretende realizar el usuario
        msg = loggedUser.getEmail() + " inicia acción para cambiar la notificación de estado comedero vacío";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Comprobamos que no exista otro usuario aplicando la configuración
        if(smfFeederSettings.tryAcquire()) {
            // Se realiza la lectura de los datos asociados a la petición
            var isNotifyOn = Boolean.valueOf(readMsgFromClient());

            msg = (isNotifyOn ? "Activa" : "Desactiva") + " la notificación de aviso para tolva vacía";
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);

            // Se establece la configuración en la base de datos
            responseCode = String.valueOf(settingsDAO.setFeederEmptyNotify(isNotifyOn));
            smfFeederSettings.release();
        }else{
            // Respuesta en caso de que otro usuario este cambiando la configuración
            responseCode = ResponseCodes.ERROR_USER_WORKING.getCode();
            msg = loggedUser.getEmail() + " no puede establecer la configuración para la notificación de cuenco sin alimento, Hay otro usuario trabajandoen ello";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 3. Se devuelve la respuesta al cliente
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método manageMaxFoodRation
     * Método encargado de establecer el parametro de configuración asociado al peso maximo por ración de alimento.
     * @throws IOException
     */
    private void manageMaxFoodRation() throws IOException {
        // Step 1. Se muestra por el CLI del servidor la operación que pretende realizar el usuario
        msg = loggedUser.getEmail() + " inicia acción para actualizar el máximo de alimento a suministrar";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Comprobamos que no exista otro usuario aplicando la configuración
        if(smfMaxFoodSettings.tryAcquire()) {
            // Se realiza la lectura de los datos asociados a la petición
            var newWeight = Integer.parseInt(readMsgFromClient());

            // Se establece la configuración en la base de datos
            responseCode = String.valueOf(settingsDAO.setMaxFoodRation(newWeight));

            // CLI y Log
            msg = (responseCode.equals(ResponseCodes.OK.getCode()) ? "" : "No") +
                    " establece el máximo de alimento a suministrar en " + newWeight;
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);

            // Liberamos recursos
            smfMaxFoodSettings.release();
        }else{
            // Respuesta en caso de que otro usuario este cambiando la configuración
            responseCode = ResponseCodes.ERROR_USER_WORKING.getCode();
            msg = loggedUser.getEmail() + " no puede establecer la configuración para el máximo de alimento de corte a suministrar, Hay otro usuario trabajandoen ello";
            CLInterface.showAlertWarning(msg);
            logger.setWarning(msg);
        }

        // Step 3. Se devuelve la respuesta al cliente
        sendMsgToClient(responseCode);
        sStatus = ServerStateCodes.IDLE_STATE.getStatusCode();
    }


    /**
     * Método getDogFeederReport
     * Se encarga del envío del reporte de logs del sistema al cliente que lo solicita.
     * @throws IOException
     */
    private void getDogFeederReport() throws IOException {
        // Step 0. Declaración de variables
        final String FILE_PATH = System.getenv("LOG_FILE_PATH");
        final String REPORTS_FILE_PATH = System.getenv("REPORTS_FILE_PATH");
        final String DELIMITER =  " ";
        responseCode = ResponseCodes.ERROR.getCode();

        // Step 1. Se muestra por el CLI del servidor la operación que pretende realizar el usuario
        msg = loggedUser.getEmail() + " solicita reporte de logs del sistema";
        CLInterface.showAlertInfo(msg);
        logger.setInfo(msg);

        // Step 2. Se genera el listado de LOG procedente de la lectura del fichero
        var logList = Tools.getLogItems(FILE_PATH,DELIMITER);

        // Step 3. Se genera el informe con el listado extraido del fichero de Logs
        if(Tools.generateReport(logList,loggedUser)){
            // CLI y Log
            msg = "Se ha completado el proceso de generación del reporte.";
            CLInterface.showAlertAction(msg);
            logger.setInfo(msg);

            // Se realiza el envío del informe al usuario logeado
            Email email = new Email();
            var response = email.sendMailTo(loggedUser.getEmail(),
                    "DOG-FEEDER solicitud de informe del sistema",
                    "Hola " + loggedUser.getEmail() + ",\n se le envía el reporte solicitado con fecha :" +
                            LocalDateTime.now(),REPORTS_FILE_PATH);

            // CLI y Log. También se modifica el código de respuesta.
            if(response){
                responseCode = ResponseCodes.OK.getCode();
                msg = "Se completa el envío del reporte a " + loggedUser.getEmail();
                CLInterface.showAlertWarning(msg);
                logger.setInfo(msg);
            }else{
                msg = "No se ha completado el proceso de envío de informe del sistema";
                CLInterface.showAlertDanger(msg);
                logger.setError(msg);
            }
        }
        // Step 4. Se realiza el envío del código de respuesta
        sendMsgToClient(responseCode);
    }


    /**
     * Método sendMsgToClient
     * Método encargado de realizar envíos de respuesta con códigos de estado o datos asociados al cliente.
     * @param msg Cadena de caracteres a enviar al cliente
     * @throws IOException
     */
    private void sendMsgToClient(String msg) throws IOException {
        // Step 1. Se instancia el objeto OutputStream y DataOutputStream para el envío del flujo de bytes al cliente
        OutputStream stream = client.getOutputStream();
        DataOutputStream streamOut = new DataOutputStream(stream);
        // Step 2. Se envía el mensaje al cliente.
        streamOut.writeUTF(msg);
    }


    /**
     * Método readMsgFromClient
     * Método encargado de realizar lecturas de las peticiones enviadas por el cliente al servidor.
     * @return String Cadena con el mensaje enviado por el cliente
     * @throws IOException
     */
    private String readMsgFromClient() throws IOException {
        // Step 1. Instancia un objeto de tipo DataInputStream para manejar el el flujo de bytes de entrada
        InputStream in = client.getInputStream();
        DataInputStream streamIn = new DataInputStream(in);

        // Step 2. Obtenemos la cadena de caracteres resultante
        String res = streamIn.readUTF();

        // Step 3. Retornamos la cadena
        return res;
    }
}
