package org.example;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.example.model.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {

    private static Conexion con;
    private static final int PORT = 2000;
    private static GpioPinDigitalInput button_food;
    private static GpioPinDigitalInput button_food_tray;
    private static boolean isTrayOpen = false;
    private static User defaultUser;
    private static SupplyFoodAuditDAO sfaDAO;


    public static void main(String[] args) {
        initialize();
        startDogFeederServer();
    }

    private static void initialize(){

        // TODO: DELETE WHEN REPAIR A INITIAL MOVE PROBLEM ON POWER ON
        new ServoMotor(RaspiPin.GPIO_01.getAddress()).resetServoPosition();


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
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Servicio TCP DOGFEEDER - Puerto: " + PORT);
            while(true){
                Socket sClient = serverSocket.accept();
                System.out.println("Cliente conectado");
                new ServerThread(sClient,con).start();
            }

        } catch (Exception e) {
            System.err.println("Se ha producido una excepción \n" + e.getMessage());
        }
    }
}