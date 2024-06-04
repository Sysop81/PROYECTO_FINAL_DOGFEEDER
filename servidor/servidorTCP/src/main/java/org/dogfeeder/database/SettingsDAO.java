package org.dogfeeder.database;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase SettingsDAO
 * Clase de acceso a datos para las instancias de Settings
 */
public class SettingsDAO {
    private Conexion con;

    private static Logger4j logger = new Logger4j(SettingsDAO.class);;

    /**
     * Constructor de clase
     */
    public SettingsDAO(Conexion con){
        this.con = con;
    }

    /**
     * Método getSavedFoodRation
     * Método encargado de obtener la cantidad de alimento maximo a suministrar a la mascota asociada a DOG-FEEDER
     * @return int Cantidad preestablecida de alimento para la mascota
     */
    public int getSavedFoodRation(){
        String query = "SELECT food_ration FROM settings LIMIT 1";
        int response = 150;  // DEFAULT value
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                response = rs.getInt(1);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener la cantidad de alimento a suministrar al cuenco. " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return response;
    }

    /**
     * Método getSavedSetting
     * Método encargado de obtener los parámetros de configuración previamente guardados
     * @return Settings Instancia de la configuración preestablecida
     */
    public Settings getSavedSetting(){
        String query = "SELECT * FROM settings LIMIT 1";
        Settings settings = new Settings();
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                settings.setID(rs.getInt(1));
                settings.setFoodRation(rs.getInt(2));
                settings.setLedOn(rs.getBoolean(3));
                settings.setNotifyHopperLow(rs.getBoolean(4));
                settings.setNotifyFeederWithOutFood(rs.getBoolean(5));
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener los parámetros de configuración guardados. " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return settings;
    }

    /**
     * Método setMaxFoodRation
     * Método encargado de establecer el maximo de alimento a suministrar
     * @param maxFoodRation Cantidad maxima de alimento a sumisnitrar a la mascota asociada a DOG-FEEDER
     * @return int Resultado de la operación de actualización
     */
    public int setMaxFoodRation(int maxFoodRation){
        var res = 0;
        String query = "UPDATE settings SET food_ration = ? WHERE (SELECT id FROM settings LIMIT 1)";
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setInt(1,maxFoodRation);

            res = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar el peso máximo por ración de alimento. " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return res;
    }

    /**
     * Método setLedStatus
     * Método encargado de activar / desactivar el estado de los Leds
     * @param ledState Estado de activación para los Leds
     * @return int Resultado de la operación de actualización
     */
    public int setLedStatus(boolean ledState){
        var res = 0;
        String query = "UPDATE settings SET led_on = ? WHERE (SELECT id FROM settings LIMIT 1)";
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setBoolean(1,ledState);

            res = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar el estado del led " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return res;
    }


    /**
     * Método setHopperLowNotify
     * Método encargado de activar / desactivar las notificaciones de estado para el nivel bajo de alimento en la tolva
     * @param isHopperLow Estado de activación para las notificaciones de estado crítico del nivel de alimento de la tolva
     * @return int Resultado de la operación de actualización
     */
    public int setHopperLowNotify(boolean isHopperLow){
        var res = 0;
        String query = "UPDATE settings SET notify_hopper_low = ? WHERE (SELECT id FROM settings LIMIT 1)";
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setBoolean(1,isHopperLow);

            res = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar la notificación tolva vacía " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return res;
    }


    /**
     * Método setFeederEmptyNotify
     * Método encargado de activar / desactivar las notificaciones que avisan si el comedero cuenta o no con alimento
     * @param isEmptyFeeder Estado de activación para las notificaciones comedero sin alimento
     * @return int Resultado de la operación de actualización
     */
    public int setFeederEmptyNotify(boolean isEmptyFeeder){
        var res = 0;
        String query = "UPDATE settings SET notify_feeder_without_food = ? WHERE (SELECT id FROM settings LIMIT 1)";
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setBoolean(1,isEmptyFeeder);

            res = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar la notificación comedero vacío " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return res;
    }
}
