package org.example;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.example.model.Button;
import org.example.model.Conexion;
import org.example.model.ServerThread;
import org.example.model.ServoMotor;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Main {

    private static Conexion con;
    private static final int PORT = 2000;
    private static GpioPinDigitalInput button_food;

    public static void main(String[] args) {
        initialize();
        startDogFeederServer();
    }

    private static void initialize(){

        // TODO: DELETE WHEN REPAIR A INITIAL MOVE PROBLEM ON POWER ON
        new ServoMotor(RaspiPin.GPIO_01.getAddress()).resetServoPosition();

        con = new Conexion(System.getenv("DB_USER"),System.getenv("DB_PASSWORD"));
        try {
            var c = con.getConexion();
            System.out.println(c.getMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        button_food = Button.getButton(RaspiPin.GPIO_00);
        button_food.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                System.out.println("Suministrando comida al comedero");
                var servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress(),2000);
                if (!servomotor.isBussy()){
                    System.out.println(servomotor.supplyFood() ? "Comida suministrada" : "Comida no suministrada");
                }else{
                    System.err.println("Espere!!! El servomotor para el suministro de alimentos está en uso");
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