package org.dogfeeder.database;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.Pet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase PetDAO
 * Clase de acceso a datos para manejar las E/S de información desde la DB
 */
public class PetDAO {
    private Conexion con;
    private static Logger4j logger = new Logger4j(PetDAO.class);

    /**
     * Constructor de clase
     */
    public PetDAO(Conexion con){
        this.con = con;
    }

    /**
     * Método getFirstPet
     * Método encargado de obtener de la DB la primera mascota seleccionada, puesto que esta versión de DOG-FEEDER no
     * soporta multimascota ;)
     * @return Pet Mascota asociada a DOGFEEDER
     */
    public Pet getFirstPet(){
        String query = "SELECT * FROM pet LIMIT 1";
        Pet pet = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                pet = new Pet();
                pet.setID(rs.getInt(1));
                pet.setName(rs.getString(2));
                pet.setBreed(rs.getString(3));
                pet.setBirthday(rs.getString(4));
                pet.setWeight(rs.getDouble(5));
                pet.setType(rs.getString(6));
                pet.setVaccineNotify(rs.getBoolean(7));
                pet.setVaccineDay(rs.getInt(8));
                pet.setGetVaccineMonth(rs.getInt(9));
            }
            
            rs.close();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al obtener la mascota " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return pet;
    }


    /**
     * Método postPet
     * Eate método, se encarga de la inserción / actualización de la mascota asociada a DOG-FEEDER
     * @param pet Instancia de mascota con los valores a insertar o actualizar
     * @return int Resultado de la operación
     */
    public int postPet(Pet pet){
        // INSERT
        var targetPet = this.getFirstPet();
        String query = "INSERT INTO pet (name, breed, birthday, weight, type, vac_notify, vac_day, vac_month) " +
                       "VALUES(?,?,?,?,?,?,?,?)";

        // Si la mascota recuperada de la DB existe, se cambia la query por la de actualización
        if(targetPet != null) {
            // UPDATE
            query = "UPDATE pet " +
                    "SET name = ?, breed = ?, birthday = ?, weight = ?, type = ?, vac_notify = ?, vac_day = ?, vac_month = ? " +
                    "WHERE id = ?";
        }

        int retorno = 0;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);

            ps.setString(1,pet.getName());
            ps.setString(2, pet.getBreed());
            ps.setDate(3, pet.getBirthdayToDate());
            ps.setDouble(4,pet.getWeight());
            ps.setString(5,pet.getType().toString());
            ps.setBoolean(6,pet.isVaccineNotify());
            ps.setInt(7,pet.getVaccineDay());
            ps.setInt(8,pet.getGetVaccineMonth());

            if(targetPet != null)  ps.setInt(9, targetPet.getID());

            retorno = ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            var msg = "Error al actualizar los datos de la mascota " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return retorno;
    }
}
