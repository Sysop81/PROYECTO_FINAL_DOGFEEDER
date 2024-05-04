package org.example.model;

import com.pi4j.io.gpio.RaspiPin;
import org.example.Utils.Tools;
import org.example.crypto.Hasher;
import org.example.mail.Email;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{

    private User loggedUser;
    private UserDAO userDAO;
    private SupplyFoodAuditDAO sfaDAO;
    private PetDAO petDAO;
    private ServoMotor servomotor;
    private Socket client;
    private static int index = 0;
    private int sStatus;
    private String responseCode;

    public ServerThread(Socket client,Conexion con){
        super("Cliente-" + (index++));
        this.client = client;
        this.sStatus = 0;
        this.servomotor = new ServoMotor(RaspiPin.GPIO_01.getAddress(),400);
        this.userDAO = new UserDAO(con);
        this.sfaDAO = new SupplyFoodAuditDAO(con);
        this.petDAO = new PetDAO(con);
        this.responseCode = "0";
    }


    @Override
    public void run() {

        try {

            // Step 1. Maquina Estados. Rotativo hasta que el estado sea -1
            while(sStatus != -1){

                sStatus = setServerStatus(readMsgFromClient());

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
                        // Get Pet data
                        getPetData();
                        break;
                    case 7:
                        // POST && PUT Pet data
                        updatePetData();
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
            case "CLOSE":
                return -1;
            case "LOGIN":
                return 0;
            case "REGISTER":
                return 1;
            case "RECOVERYPASS":
                return 2;
            case "FOOD":
                return 3;
            case "AUDITS":
                return 4;
            case "LASTAUDIT":
                return 5;
            case "PETDATA":
                return 6;
            case "POSTPET":
                return 7;

        }

        return 10;
    }

    private void login() throws IOException {

        var request = readMsgFromClient();

        this.loggedUser = getActualUser(request);
        var targetUser = userDAO.getUserbyEmail(this.loggedUser.getEmail());
        var response = "0"; // Se devuelve por defecto falso o no logueado

        if (targetUser != null && this.loggedUser.getPassword().equals(targetUser.getPassword())){
            setName(this.loggedUser.getEmail());
            this.loggedUser.setID(targetUser.getID());
            sStatus = 10; // Logueado con estado ocioso en la maquina de estados
            response = "1"; // Se cambia la respuesta a devolver al cliente
        }

        System.out.println( sStatus == 10 ?  getName() + " logueado correctamente." : "LOGIN incorrecto");
        sendMsgToClient(response); // Se envia estado correcto al cliente

    }

    private void exit() throws IOException {
        System.out.println(getName() + " finaliza la sesión en servidor TCP- DOGFEEDER");
        responseCode = "1";
        sendMsgToClient(responseCode);
    }

    private void register() throws IOException {
        var newUser = getActualUser(readMsgFromClient());
        responseCode = String.valueOf(userDAO.postUser(newUser));
        var msg =  newUser.getEmail() + (responseCode.equals("1") ? " registrado correctamente" : " no se ha registrado en la BD");
        System.out.println(msg);
        sendMsgToClient(responseCode);
        sStatus = 0; // Usuario Registrado pero No logueado
    }

    private void updatePetData() throws IOException {
        System.out.println("Actualizando los datos de la mascota");
        var strPet = readMsgFromClient();
        System.out.println(strPet);
        var pet = new Pet(strPet.split("_"));
        System.out.println("llega para ser guardado: " + pet);
        responseCode = String.valueOf(petDAO.postPet(pet));
        sendMsgToClient(responseCode);
        sStatus = 10;
    }

    private void recoveryPass() throws IOException {
        // Step 1. Enviar código de verificación
        var targetEmail = readMsgFromClient();
        var validCode = Tools.getRandonCode();
        Email email = new Email();
        boolean isSend = email.sendMailTo(targetEmail,
                                "DOG-FEEDER Solicitud de cambio de contraseña",
                                  "Código de verificación: " + validCode);
        System.out.println("Código enviado por mail: " + validCode);
        responseCode = isSend ? "1" : "0";
        sendMsgToClient(responseCode);

        // Step 2. Recepción del código enviado vái mail al cliente que solicita el cambio
        var clientCode = readMsgFromClient();
        System.out.println("Cliente envia: " + clientCode);
        responseCode = String.valueOf(validCode).equals(clientCode) ? "1" : "0";
        sendMsgToClient(responseCode);

        // Step 3. Cambio de password
        var newPassword = readMsgFromClient();
        responseCode = String.valueOf(userDAO.setNewPassword(targetEmail,Hasher.encode(newPassword)));
        sendMsgToClient(responseCode);

        System.out.println("Cambio de password realizado con éxito");
    }

    private void supplyFood() throws IOException {
        responseCode = "3";

        if (!servomotor.isBussy()) {
            System.out.println("Suministrando comida al comedero");
            var res = this.servomotor.supplyFood(); //this.servomotor.supplyFlashesFood(); //
            if(res) sfaDAO.postSupplyFood(new SupplyFoodAudit(loggedUser,250)); // TODO --> Erase hardcored value
            responseCode = res ? "1" : "0";
        }else{

            System.err.println("Espere!!! El servomotor para el suministro de alimentos está en uso");
        }
        sendMsgToClient(responseCode);
        sStatus = 10;
    }

    private void getSupplyFoodAudits() throws IOException {
        System.out.println("Preparando para obtener el listado de auditoría");
        var listOfAuditSupplyFood = sfaDAO.getAll();

        // TODO -> Refact
        String res = "";
        for ( SupplyFoodAudit item : listOfAuditSupplyFood) {
            var aStrDateTime = item.getTimeStamp().split(" ");
            res += item.getID() + "_" + item.getUser().getEmail() + "_" + aStrDateTime[0] + "_" + aStrDateTime[1] + "&";
        }

        sendMsgToClient(res);
        sStatus = 10;
    }

    private void getLastSupplyFoodAudit() throws IOException {
        System.out.println("Recuperando el útimo registro de suministro de alimento dispensado");
        var lastSfa = sfaDAO.getLastSupplyFoodRegister();
        var aStrDateTime = lastSfa.getTimeStamp().split(" ");
        var res = lastSfa.getID() + "_" + lastSfa.getUser().getEmail() + "_" + aStrDateTime[0] + "_" + aStrDateTime[1];
        sendMsgToClient(res);
    }

    private void getPetData() throws IOException {
        System.out.println("Preparando para obtener los datos de la mascota");
        var pet = petDAO.getFirstPet();

        sendMsgToClient(pet != null ? pet.toString() : "0");
        sStatus = 10;
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
