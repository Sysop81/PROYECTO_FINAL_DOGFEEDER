package org.dogfeeder.database;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.Notification;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Clase NotificationDAO
 * Clase de acceso a datos para dar persistenca al servicio de notificaciones de DOG-FEEDER
 */
public class NotificationDAO {
    private Conexion con;
    private static Logger4j logger = new Logger4j(NotificationDAO.class);

    /**
     * Constructor de clase
     */
    public NotificationDAO(Conexion con){
        this.con = con;
    }

    /**
     * Método getLastNotifyByType
     * Método encargado de recuperar el último registro de notificación en función de su tipo
     * @param type Tipo de notificación a filtrar
     * @return Notification Instancia de la notificación deseada
     */
    public Notification getLastNotifyByType(Notification.NotificationType type){
        String query = "SELECT * " +
                       "FROM notification " +
                       "WHERE type=? " +
                       "ORDER BY timestamp DESC " +
                       "LIMIT 1";

        Notification notification = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ps.setString(1,type.toString());
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                notification = new Notification(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3)
                );

            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener última notificación de tipo " + type + "." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return notification;
    }


    /**
     * Método postNotification
     * Método encargado de insertar o actualizar la notificación que llega como parametro. La dinámica es, se intenta
     * recuperar la notificación en base a su tipo, si esta no existe, se inserta. En caso contrario se actualiza.
     * De esta forma, siempre mantenemos únicamnte 3 tipos de notificación insertados en la BD
     * @param notification Notificación a insertar o actualizar
     * @return int con el resultado de la operacion
     */
    public int postNotification(Notification notification){
        // INSERT
        var targetNotify = getLastNotifyByType(notification.getType());

        String query = "INSERT INTO notification (type) " +
                       "VALUES(?)";

        // Si la notificación recuperada de la DB existe, se cambia la query por la de actualización
        if(targetNotify != null) {
            // UPDATE
            query = "UPDATE notification " +
                    "SET type = ?, timestamp = ? " +
                    "WHERE id = ?";
        }

        int retorno = 0;

        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);

            ps.setString(1,notification.getType().toString());

            if(targetNotify != null){
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, targetNotify.getID());
            }

            retorno = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar los datos de la notificación de tipo " + notification.getType() +"." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }
        return retorno;
    }
}
