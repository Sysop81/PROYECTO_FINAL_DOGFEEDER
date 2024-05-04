package org.dogfeeder.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SupplyFoodAuditDAO {
    private Conexion con;

    /**
     * Constructor de clase
     */
    public SupplyFoodAuditDAO(Conexion con){
        this.con = con;
    }

    public int postSupplyFood(SupplyFoodAudit sfa){
        String query = "INSERT INTO audit_supply_food (id_user,weight) VALUES(?,?)";
        int retorno = 0;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);

            ps.setInt(1,sfa.getUser().getID());
            ps.setDouble(2, sfa.getWeight());
            retorno = ps.executeUpdate();

            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al insertar registro de auditoría para el suministro de comida " + e.getMessage());
        }

        return retorno;
    }


    public ArrayList<SupplyFoodAudit> getAll(){
        String query = "SELECT a.*, u.email " +
                       "FROM audit_supply_food a, user u  " +
                       "WHERE a.id_user = u.id " +
                       "ORDER BY a.timestamp DESC";
        ArrayList<SupplyFoodAudit> listFoodAudits = new ArrayList<>();
        User user = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                user = new User();
                user.setID(rs.getInt(2));
                user.setEmail(rs.getString(5));

                listFoodAudits.add(new SupplyFoodAudit(rs.getInt(1),
                        user, rs.getString(3),rs.getDouble(4)));
            }


            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al obtener todos los registros de auditoría " + e.getMessage());
        }

        return listFoodAudits;
    }


    public SupplyFoodAudit getLastSupplyFoodRegister(){
        String query = "SELECT a.*, u.email " +
                "FROM audit_supply_food a, user u  " +
                "WHERE a.id_user = u.id " +
                "ORDER BY a.timestamp DESC " +
                "LIMIT 1";
        SupplyFoodAudit supplyFoodAudit = null;
        User user = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                user = new User();
                user.setID(rs.getInt(2));
                user.setEmail(rs.getString(5));

                supplyFoodAudit = new SupplyFoodAudit(rs.getInt(1),
                                                      user,
                                                      rs.getString(3),
                                                      rs.getDouble(4));
            }


            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al obtener el útimo registro de auditoría " + e.getMessage());
        }

        return supplyFoodAudit;
    }
}
