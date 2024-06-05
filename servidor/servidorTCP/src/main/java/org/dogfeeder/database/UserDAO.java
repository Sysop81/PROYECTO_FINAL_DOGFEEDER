package org.dogfeeder.database;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Clase UserDAO
 * Clase de acceso a datos para manejar las E/S de información desde la DB
 */
public class UserDAO {
    private Conexion con;
    private static Logger4j logger = new Logger4j(UserDAO.class);

    /**
     * Constructor de clase
     */
    public UserDAO(Conexion con){
        this.con = con;
    }

    /**
     * Método postUser
     * Método encargado de dar persistencia a un nuevo ususario en la DB
     * @param user Instancia de usuario que se pretende almacenar en la DB
     * @return int Resultado de la operación realizada
     */
    public int postUser(User user){
        String query = "INSERT INTO user (email,password) VALUES(?,?)";
        int retorno = 0;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);

            ps.setString(1,user.getEmail());
            ps.setString(2, user.getPassword());
            retorno = ps.executeUpdate();

            ps.close();

        } catch (SQLException e) {
            var msg = "Error al insertar el usuario con email " + user.getEmail() + "." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return retorno;
    }

    /**
     * Método getUserbyEmail
     * Método encargado de recuperar al usuario mediante su dirección de correo electrónico
     * @param email Email del usuario
     * @return User Instancia de usuario al que pertenece el correo
     */
    public User getUserbyEmail(String email){
        String query = "SELECT * FROM user WHERE email = ?";
        User user = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                user = new User();
                user.setID(rs.getInt(1));
                user.setEmail(rs.getString(2));
                user.setPassword(rs.getString(3));
                user.setValidMail(rs.getBoolean(4));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener el usuario mediante su dirección de email " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);

        }

        return user;
    }

    /**
     * Método setNewPassword
     * Método encargado de establecer una nueva contraseña al usuario.
     * @param email Correo del usuario del cual se pretende cambiar su contraseña
     * @param password Cadena hasheada que representa la nueva contraseña de usuario
     * @return int Resultado de la operación
     */
    public int setNewPassword(String email, String password){
        String query = "UPDATE user SET password = ? WHERE email = ?";
        int retorno = 0;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);

            ps.setString(1,password);
            ps.setString(2, email);
            retorno = ps.executeUpdate();

            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar el password de usuario " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return retorno;
    }


    /**
     * Método getAllValidMail
     * Método encargado de obtener los correos electrónicos de aquellos usuarios marcados como mail válido
     * Método encargado de llevar al cliente los 100 últimos registros de auditoría
     * @return ArrayList<String> Listado con los correos de los usuarios a los que se notificará
     */
    public ArrayList<String> getAllValidMail(){
        String query = "SELECT email " +
                       "FROM user " +
                       "WHERE valid_mail = TRUE " +
                       "ORDER BY ID";
        ArrayList<String> listOfMails = new ArrayList<>();

        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                listOfMails.add(rs.getString(1));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener los correo electrónicos de los usuarios " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return listOfMails;
    }
}
