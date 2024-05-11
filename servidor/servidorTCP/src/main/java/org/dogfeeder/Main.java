package org.dogfeeder;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.database.Conexion;
import org.dogfeeder.database.SupplyFoodAuditDAO;
import org.dogfeeder.database.UserDAO;
import org.dogfeeder.model.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {

    private static Conexion con;
    private static int port;
    private static GpioPinDigitalInput button_food;
    private static GpioPinDigitalInput button_food_tray;
    private static boolean isTrayOpen = false;
    private static User defaultUser;
    private static SupplyFoodAuditDAO sfaDAO;

    private static Logger4j logger;


    public static void main(String[] args) {
        initialize();
        startDogFeederServer();
    }

    private static void initialize(){

        // TODO: DELETE WHEN REPAIR A INITIAL MOVE PROBLEM ON POWER ON
        new ServoMotor(RaspiPin.GPIO_01.getAddress()).resetServoPosition();
        logger = new Logger4j(Main.class);
        port = Integer.parseInt(System.getenv("TCP_PORT"));
        con = new Conexion(System.getenv("DB_USER"),System.getenv("DB_PASSWORD"));
        var userDAO = new UserDAO(con);
        sfaDAO = new SupplyFoodAuditDAO(con);
        defaultUser = userDAO.getUserbyEmail(System.getenv("DEFAULT_USER"));

        button_food = Button.getButton(RaspiPin.GPIO_00);
        button_food.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                System.out.println("Suministrando comida al comedero");
                var servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress(),2000);
                if (!servomotor.isBussy()){
                    var msg = "Comida no suministrada";
                    if(servomotor.supplyFood()){
                        msg = "Comida suministrada";
                        sfaDAO.postSupplyFood(new SupplyFoodAudit(defaultUser,250)); // TODO -> remove hardcored value
                    }
                    System.out.println(msg);
                }else{
                    System.err.println("Espere!!! El servomotor para el suministro de alimentos está en uso");
                }
            }
        });

        button_food_tray = Button.getButton(RaspiPin.GPIO_02);
        button_food_tray.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                var servoMotor = new ServoMotor(RaspiPin.GPIO_26.getAddress());
                if(isTrayOpen){
                    System.out.println("Cerrando la bandeja de suministro de pienso");
                    // Si esta abierta
                    servoMotor.close();
                    isTrayOpen = false;
                }else{
                    System.out.println("Abriendo la bandeja de suministro de pienso");
                    // Si esta cerrada
                    servoMotor.open();
                    isTrayOpen = true;
                }
            }
        });
    }

    private static void startDogFeederServer(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            CLInterface.showAppTitle("Servicio TCP DOGFEEDER - Puerto: " + port);
            logger.setInfo("Servicio iniciado");
            while(true){
                Socket sClient = serverSocket.accept();
                CLInterface.showAlertInfo("Cliente conectado");
                new ServerThread(sClient,con).start();
            }

        } catch (Exception e) {
            CLInterface.showAlertError("Se ha producido un error en servicio \n" + e.getMessage());
        }
    }
}