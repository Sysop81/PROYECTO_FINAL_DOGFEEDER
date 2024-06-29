package org.dogfeeder;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.database.Conexion;
import org.dogfeeder.database.SettingsDAO;
import org.dogfeeder.database.SupplyFoodAuditDAO;
import org.dogfeeder.database.UserDAO;
import org.dogfeeder.model.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Proyecto DOGFEEDER [DAM]
 * Servidor TCP.
 * @author José Ramón López Guillén
 */
public class Main {
    private static Conexion con;
    private static int port;
    private static GpioPinDigitalInput button_food;
    private static GpioPinDigitalInput button_food_tray;
    private static boolean isTrayOpen = false;
    private static User defaultUser;
    private static SupplyFoodAuditDAO sfaDAO;
    private static SettingsDAO settingsDAO;
    private static Logger4j logger;
    private static Relay relay;
    private static HX711 hx711module;
    private static UltraSonic ultraSonic;
    private static NotificationService notificationService;
    private static volatile boolean serviceRunning;

    /**
     * Método MAIN
     * @param args
     */
    public static void main(String[] args) {
        // Step 1. Inicializamos los datos
        initialize();
        // Step 2. Arrancamos el sercivio
        startDogFeederServer();
    }

    /**
     * Método initialize
     * Método encargado de realizar la carga de datos necesarios para arrancar el servicio DOG-FEEDER
     */
    private static void initialize(){

        // Step 1. Obtenemos una instancia para manejar GPIO
        var gpio = GpioFactory.getInstance();
        // Step 2. Instanciamos el módulo de Relé, ultrasonidos y HX711. Encendido y manejo del microcontrolador Rpi Pico,
        //         medición para la tolva y peso de alimento
        relay = new Relay(RaspiPin.GPIO_05,gpio);

        hx711module = new HX711(gpio.provisionDigitalInputPin(RaspiPin.GPIO_21, "HX_DAT", PinPullResistance.OFF),
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "HX_CLK", PinState.LOW),128);

        ultraSonic = new UltraSonic(gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(RaspiPin.GPIO_28.getAddress()), "TRIGGER", PinState.LOW),
                gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(RaspiPin.GPIO_29.getAddress()), "ECHO"));

        ultraSonic.getFoodLevel();

        // Step 3. Instanciamos: Logger, conexion y Objetos DAO para el acceso a la DB. Así como el usuario por defecto
        //         para registrar las acciones que se realizan desde los botones del comedero.
        serviceRunning = true;
        logger = new Logger4j(Main.class);
        port = Integer.parseInt(System.getenv("TCP_PORT"));
        con = new Conexion(System.getenv("DB_USER"),System.getenv("DB_PASSWORD"));
        var userDAO = new UserDAO(con);
        sfaDAO = new SupplyFoodAuditDAO(con);
        settingsDAO = new SettingsDAO(con);
        defaultUser = userDAO.getUserbyEmail(System.getenv("DEFAULT_USER"));

        // Step 4. Se instancia el botón del comedero para el suministro de alimento. Y de sefine un listener para escuchar
        //         la pulsación del mismo
        button_food = Button.getButton(RaspiPin.GPIO_00);
        button_food.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {

                // Step 1. Se muestra información por el CLI del servidor DOGFEEDER y se define un código de estado por defecto.
                var msg = defaultUser.getEmail() + " inicia acción para suministro de alimento [Botón físico]";
                CLInterface.showAlertInfo(msg);
                logger.setInfo(msg);

                // Step 2. Creamos una instancia del servoMotor principal para el suministro de alimento
                var servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress());
                if (!servomotor.isBussy()){

                    // Step 3. Suministramos alimento a la mascota
                    var responseCode = ServoMotor.supplyFood(servomotor,
                                                             hx711module,
                                                             ultraSonic,
                                                             settingsDAO,
                                                             sfaDAO,
                                                             defaultUser,
                                              true);

                    // Se maneja la información en el Log y CLI
                    msg = "Finaliza el proceso de suministro de alimento con código de estado " + responseCode;
                    CLInterface.showAlertAction(msg);
                    logger.setInfo(msg);

                }else{
                    msg = defaultUser.getEmail() + " no suministra alimento [Servomotor en uso][Botón físico]";
                    CLInterface.showAlertWarning(msg);
                    logger.setWarning(msg);
                }
            }
        });

        // Step 5. Se instancia el botón del comedero para la apertura del tapón de la tolva y se define un listener para
        //         el manejo de la pulsación sobre el mismo.
        button_food_tray = Button.getButton(RaspiPin.GPIO_02);
        button_food_tray.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                if(isTrayOpen){
                    // CLI & Log
                    var msg = defaultUser.getEmail() + " cierra la tapadera de la tolva [Botón físico]";
                    CLInterface.showAlertInfo(msg);
                    logger.setInfo(msg);

                    ServoMotor.startGatePythonScript(2.5);
                    isTrayOpen = false;
                }else{
                    // CLI & Log
                    var msg = defaultUser.getEmail() + " abre la tapadera de la tolva [Botón físico]";
                    CLInterface.showAlertInfo(msg);
                    logger.setInfo(msg);

                    ServoMotor.startGatePythonScript(12.5);
                    isTrayOpen = true;
                }
            }
        });

        // Step 6. Se manejan los parametros de confifuración para la iluminación LED
        var settings = settingsDAO.getSavedSetting();
        if(settings.isLedOn()) relay.relayOn();

        // Step 7. Se registra un Hook para finalizar el servicio al registrar Ctrl + C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            // Step 1. Se detiene el servicio de notificaciones
            notificationService.finish();

            // Step 1.1 Esperamos a que finalice el hilo del notificationService
            while(notificationService.isRunning()){}

            // Step 2. Se detiene el servicio DOG-FEEDER
            var msg = "Servicio DOGFEEDER detenido.";
            CLInterface.showAlertDanger(msg);
            logger.setWarning(msg);
            serviceRunning = false;

            // Step 3. Se cierra la sesion con la DB
            if(con.close()){
                msg = "Se cierra correctamente la sesión con la base de datos";
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
            }

        }));

        // Step 8. Lanzamos es servicio de notificaciones
        notificationService = new NotificationService(con,hx711module,ultraSonic);
        notificationService.start();
    }

    /**
     * Método startDogFeederServer
     * Método encargado de instanciar el Socket del servicio TCP y mantenerse a la escucha de nuevos clientes
     */
    private static void startDogFeederServer(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            // CLI & Log
            CLInterface.showAppTitle("Servicio TCP DOGFEEDER - Puerto: " + port);
            CLInterface.showAlertWarning("Presiona Ctrl + C para finalizar");
            logger.setInfo("Servicio iniciado");

            while (serviceRunning) {
                Socket sClient = serverSocket.accept();
                CLInterface.showAlertInfo("Cliente conectado");
                new ServerThread(sClient, con, hx711module, ultraSonic, relay).start();
                Thread.sleep(1);
            }

        }catch (InterruptedException ie){
            // Se lanza el Hook de parada del servicio
        }catch (Exception e) {
            var msg = "Se ha producido un error en servicio \n" + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }finally {
            if(con.close()){
                var msg = "Se cierra correctamente la sesión con la base de datos";
                CLInterface.showAlertAction(msg);
                logger.setInfo(msg);
            }
        }
    }

    /**
     * Método getTrayStatus
     * Método encargado de determinar si la tapadera de la tolva se encuentra abierta o cerrada.
     * @return boolean con el resultado de la operación
     */
    public static boolean getTrayStatus(){
        return isTrayOpen;
    }
}