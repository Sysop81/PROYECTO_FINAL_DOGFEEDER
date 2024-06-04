package org.dogfeeder.database;


import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.StatisticFood;
import org.dogfeeder.model.SupplyFoodAudit;
import org.dogfeeder.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Clase SupplyFoodAuditDAO
 * Clase de acceso a datos para manejar las E/S de información desde la DB para los registros de auditoría de suministro
 * de alimento
 */
public class SupplyFoodAuditDAO {
    private Conexion con;
    private static Logger4j logger = new Logger4j(SupplyFoodAuditDAO.class);

    /**
     * Constructor de clase
     */
    public SupplyFoodAuditDAO(Conexion con){
        this.con = con;
    }

    /**
     * Método postSupplyFood
     * Método encargado de realizar una inserción de un nuevo registro de audoría de alimento en la DB
     * @param sfa Instancia que representa el registro de auditoría
     * @return int Valor que representa el resultado de la operación
     */
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
            var msg = "Error al insertar registro de auditoría para el suministro de alimento." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return retorno;
    }

    /**
     * Método getAll [Limit 100]
     * Método encargado de llevar al cliente los 100 últimos registros de auditoría
     * @return ArrayList<SupplyFoodAudit> Listado con los registros de auditoría
     */
    public ArrayList<SupplyFoodAudit> getAll(){
        String query = "SELECT a.*, u.email " +
                       "FROM audit_supply_food a, user u  " +
                       "WHERE a.id_user = u.id " +
                       "ORDER BY a.timestamp DESC LIMIT 100";
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
            var msg = "Error al obtener los 100 últimos registros de auditoría " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return listFoodAudits;
    }


    /**
     * Método getLastSupplyFoodRegister
     * Método encargdo de obtener el último registro de auditoría
     * @return SupplyFoodAudit Instancia que representa el último registro de auditoría
     */
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
            var msg = "Error al obtener el útimo registro de auditoría " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return supplyFoodAudit;
    }

    /**
     * Método getStatisticByYear
     * Método encargado de obtener la información necesaria para montar una gráfica de estadística en la que se representan
     *  1º -> Número de tomas por mensualidad [Año en curso]
     *  2º -> Cantidad de alimento [gramos] por mesualidad [Año en curso]
     * @param year Año en curso
     * @return ArrayList<StatisticFood> Listado con instancias StatisticFood para montar una gráfica de barras
     */
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
            var msg = "Error al obtener loas registros para el año " + year + ". " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return listStatistic;
    }
}
