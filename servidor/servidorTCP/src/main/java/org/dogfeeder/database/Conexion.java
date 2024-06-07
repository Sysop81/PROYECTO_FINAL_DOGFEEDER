package org.dogfeeder.database;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/***
 * Clase Conexion
 * Representa la conexión con la DB MYSQL
 */
public class Conexion {

    private static final String URL = "jdbc:mysql://localhost/" + System.getenv("DB_NAME");
    private String user;
    private String password;
    private Connection connection;

    private static Logger4j logger = new Logger4j(Conexion.class);

    /**
     * Constructor
     * @param user Usuario de la DB
     * @param password Contraseña de usuario
     */
    public Conexion(String user, String password){
        this.user = user;
        this.password = password;
        logger.setInfo(user + " inicia la instancia de conexión con la DB");
    }

    /**
     * Método getConexion
     * Se encarga de retornar la propiedad con que representa la conexion activa con la base de datos.
     * @return Connection Objeto de tipo connection
     */
    public Connection getConexion() throws SQLException {
        if(connection == null){
            connection = DriverManager.getConnection(URL,user,password);
        }
        return connection;
    }


    /**
     * Método close
     * @return boolean Determina si la sesión con la DB se ha cerrado de forma correcta
     */
    public boolean close(){
        boolean res = false;
        if(connection != null){
            try {
                connection.close();
                res = true;
            } catch (SQLException e) {
                var msg = "Error al realizar el cierre de la sesión con la DB." + e.getMessage();
                CLInterface.showAlertDanger(msg);
                logger.setError(msg);
            }
        }
        return res;
    }

}
