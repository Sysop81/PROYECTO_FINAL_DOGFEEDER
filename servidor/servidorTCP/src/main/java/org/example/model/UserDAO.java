package org.example.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Conexion con;

    /**
     * Constructor de clase
     */
    public UserDAO(Conexion con){
        this.con = con;
    }

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
            System.out.println("Error al consultar la existencia de la base de datos " + e.getMessage());
        }

        return retorno;
    }

    public User getUserbyEmail(String email){
        String query = "SELECT * FROM user WHERE email = ?";
        User user = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                user = new User();
                System.out.println("ID: " + rs.getInt(1));
                user.setEmail(rs.getString(2));
                System.out.println("email: " + rs.getString(2));
                user.setPassword(rs.getString(3));
                System.out.println("Pass:  " + rs.getString(3));
            }


            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al consultar la existencia de la base de datos " + e.getMessage());
        }

        return user;
    }

    /**
     * Metodo para cerrar la conexion con la BBDD
     * @return true o false.
     */
    /*public void close(){
        try {
            this.con.cerrarConexion();
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión UsuarioDAO " + e.getMessage());
        }
    }*/

}
