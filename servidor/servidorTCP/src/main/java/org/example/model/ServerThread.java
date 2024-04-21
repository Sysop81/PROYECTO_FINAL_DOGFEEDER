package org.example.model;

import com.pi4j.io.gpio.RaspiPin;
import org.example.crypto.Hasher;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{

    private User loggedUser;
    private UserDAO userDAO;
    private ServoMotor servomotor;
    private Socket client;
    private static int index = 0;
    private int sStatus;

    public ServerThread(Socket client,Conexion con){
        super("Cliente-" + (index++));
        this.client = client;
        this.sStatus = 0;
        this.servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress(),2000);
        this.userDAO = new UserDAO(con);
    }


    @Override
    public void run() {

        try {
            // Step 1. Comprobación password
            while (sStatus == 0) {

                var request = readMsgFromClient();

                this.loggedUser = getActualUser(request);
                var targetUser = userDAO.getUserbyEmail(this.loggedUser.getEmail());
                var response = "0"; // Se devuelve por defecto falso o no logueado

                if (targetUser != null && this.loggedUser.getPassword().equals(targetUser.getPassword())){
                    setName(this.loggedUser.getEmail());
                    sStatus = 10; // Logueado con estado ocioso en la maquina de estados
                    response = "1"; // Se cambia la respuesta a devolver al cliente
                }

                System.out.println( sStatus == 10 ?  getName() + " logueado correctamente." : "LOGIN incorrecto");
                sendMsgToClient(response); // Se envia estado correcto al cliente

            }


            // Step 2. Maquina Estados. Rotativo hasta que el estado sea -1
            while(sStatus != -1){

                sStatus = setServerStatus(readMsgFromClient());

                switch(sStatus){
                    case -1:
                        System.out.println(getName() + " finaliza la sesión en servidor TCP- DOGFEEDR");
                        sendMsgToClient("1");
                        break;
                    case 1:
                        // Suministrar comida
                        var responseCode = "3";

                        if (!servomotor.isBussy()) {
                            System.out.println("Suministrando comida al comedero");
                            var res = this.servomotor.supplyFood();
                            responseCode = res ? "1" : "0";
                        }else{

                            System.err.println("Espere!!! El servomotor para el suministro de alimentos está en uso");
                        }
                        sendMsgToClient(responseCode);
                        sStatus = 10;
                        break;
                    case 2:
                        // Registrar usuario
                        break;
                    case 3:
                        // recuperar password
                        break;
                }
            }

        }catch (Exception e) {
            System.out.println("Cliente " + getName() + " cierra el programa cliente");
        }finally {
            try {
                this.sStatus = 0;
                this.client.close();
                //this.userDAO.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión cliente. " + e.getMessage());
            }
        }
    }

    private User getActualUser(String request){
        var userProperties = request.split("_");
        return new User(userProperties[0], Hasher.encode(userProperties[1]));
    }

    private int setServerStatus(String request){
        switch (request.toUpperCase()){
            case "FOOD":
                return 1;
            case "REGISTER":
                return 2;
            case "CLOSE":
                return -1;
        }

        return 10;
    }

    private void sendMsgToClient(String msg) throws IOException {
        // Step 1. Se instancia el objeto OutputStream y DataOutputStream para el envío del flujo de bytes al cliente
        OutputStream stream = client.getOutputStream();
        DataOutputStream streamOut = new DataOutputStream(stream);
        // Step 2. Se envía el mensaje al cliente.
        streamOut.writeUTF(msg);
    }

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
