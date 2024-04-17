package org.example;

import org.example.crypto.Hasher;
import org.example.model.Conexion;
import org.example.model.ServerThread;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        Conexion con = new Conexion("dogfeeder","dogfeeder"); // TODO SET IN ENV or PROPERTIES FILE
        try {
            var c = con.getConexion();
            System.out.println(c.getMetaData());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        final int PORT = 2000;

        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Servicio TCP DOGFEEDER - Puerto: " + PORT);
            while(true){
                Socket sClient = serverSocket.accept();
                System.out.println("Cliente DOGFEEER conectado");
                new ServerThread(sClient,con).start();
            }

        } catch (Exception e) {
            System.err.println("Se ha producido una excepción \n" + e.getMessage());
        }
    }
}