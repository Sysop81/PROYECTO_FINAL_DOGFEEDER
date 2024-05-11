package org.dogfeeder.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost/" + System.getenv("DB_NAME");
    private String user;
    private String password;
    //private Connection con;

    public Conexion(String user, String password){
        this.user = user;
        this.password = password;
    }

    /**
     * Método getConexion
     * Se encarga de retornar la propiedad con que representa la conexion activa con la base de datos.
     * @return
     */
    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL,user,password);
    }

    /**
     * Método establecerConexion
     * Se encarga de establecer una conexion con la base de datos e instanciar la propiedad con
     * @throws SQLException
     */
    /*public void establecerConexion() throws SQLException {
        this.con = DriverManager.getConnection(URL,user,password);
    }*/

    /**
     * Método cerrarConexion
     * Se encarga de finalizar la conexion activa con la base de datos
     * @throws SQLException
     */
    /*public void cerrarConexion() throws SQLException {
        this.con.close();
    }*/
}
