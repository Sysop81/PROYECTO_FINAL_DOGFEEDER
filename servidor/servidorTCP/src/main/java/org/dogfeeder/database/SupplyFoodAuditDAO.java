package org.dogfeeder.database;

import org.dogfeeder.database.Conexion;
import org.dogfeeder.model.StatisticFood;
import org.dogfeeder.model.SupplyFoodAudit;
import org.dogfeeder.model.User;

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

    public ArrayList<StatisticFood> getStatisticByYear(int year){
        String query = "SELECT \n" +
                        "    COALESCE(audit.year_number, YEAR(CURRENT_DATE())) AS year_number,\n" +
                        "    months.month_number,\n" +
                        "    months.month_name,\n" +
                        "    COALESCE(audit.count_takes, 0) AS count_takes,\n" +
                        "    COALESCE(audit.total_weight, 0) AS total_weight\n" +
                        "FROM (\n" +
                        "    SELECT \n" +
                        "        1 AS month_number, 'January' AS month_name\n" +
                        "    UNION SELECT \n" +
                        "        2, 'February'\n" +
                        "    UNION SELECT \n" +
                        "        3, 'March'\n" +
                        "    UNION SELECT \n" +
                        "        4, 'April'\n" +
                        "    UNION SELECT \n" +
                        "        5, 'May'\n" +
                        "    UNION SELECT \n" +
                        "        6, 'June'\n" +
                        "    UNION SELECT \n" +
                        "        7, 'July'\n" +
                        "    UNION SELECT \n" +
                        "        8, 'August'\n" +
                        "    UNION SELECT \n" +
                        "        9, 'September'\n" +
                        "    UNION SELECT \n" +
                        "        10, 'October'\n" +
                        "    UNION SELECT \n" +
                        "        11, 'November'\n" +
                        "    UNION SELECT \n" +
                        "        12, 'December'\n" +
                        ") AS months\n" +
                        "LEFT JOIN \n" +
                        "    audit_supply_food_summary AS audit ON months.month_number = audit.month_number\n" +
                        "    AND audit.year_number = YEAR(CURRENT_DATE())\n" +
                        "ORDER BY \n" +
                        "    months.month_number;";

        ArrayList<StatisticFood> listStatistic = new ArrayList<>();

        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                listStatistic.add(new StatisticFood(rs.getInt(1),
                                                    rs.getInt(2),
                                                    rs.getString(3),
                                                    rs.getInt(4),
                                                    rs.getDouble(5)));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al obtener loas registros para el año " + year + ". " + e.getMessage());
        }

        return listStatistic;
    }
}
