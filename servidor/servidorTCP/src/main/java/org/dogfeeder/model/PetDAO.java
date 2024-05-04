package org.dogfeeder.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PetDAO {
    private Conexion con;

    /**
     * Constructor de clase
     */
    public PetDAO(Conexion con){
        this.con = con;
    }

    public Pet getFirstPet(){
        String query = "SELECT * FROM pet LIMIT 1";
        Pet pet = null;
        try {
            PreparedStatement ps = con.getConexion().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                pet = new Pet();
                pet.setID(rs.getInt(1));
                System.out.println("ID: " + rs.getInt(1));
                pet.setName(rs.getString(2));
                System.out.println("name: " + rs.getString(2));
                pet.setBreed(rs.getString(3));
                System.out.println("Breed:  " + rs.getString(3));
                pet.setBirthday(rs.getString(4));
                System.out.println("Birthday:  " + rs.getString(4));
                pet.setWeight(rs.getDouble(5));
                System.out.println("Weight:  " + rs.getDouble(5));
                pet.setType(rs.getString(6));
                System.out.println("type:  " + rs.getString(6));
                pet.setVaccineNotify(rs.getBoolean(7));
                System.out.println("vac notify:  " + rs.getBoolean(7));
                pet.setVaccineDay(rs.getInt(8));
                System.out.println("vac day:  " + rs.getInt(8));
                pet.setGetVaccineMonth(rs.getInt(9));
                System.out.println("vac MONTH:  " + rs.getInt(9));

            }
            
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error al obtener la mascota " + e.getMessage());
        }

        return pet;
    }


    public int postPet(Pet pet){
        //TODO -> Set in TRANSACTION
        var targetPet = this.getFirstPet();
        String query = "INSERT INTO pet (name, breed, birthday, weight, type, vac_notify, vac_day, vac_month) " +
                       "VALUES(?,?,?,?,?,?,?,?)";

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
            System.out.println("Error al actualizar los datos de la mascota " + e.getMessage());
        }

        return retorno;
    }
}
