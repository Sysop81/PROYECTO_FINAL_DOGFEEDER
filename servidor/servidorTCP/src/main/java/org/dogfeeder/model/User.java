package org.dogfeeder.model;

/**
 * Clase User
 * Modela instancias de tipo usuario del servicio DOG-FEEDER
 */
public class User {
    private int ID;
    private String email;
    private String password;
    private boolean isValidMail;

    /**
     * Constructor
     */
    public User(){}

    /**
     * Constructor con parametros
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     */
    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor con parametros [Sobrecarga]
     * @param email Correo electronico del usuario
     * @param password Contraseña del usuario
     * @param isValidMail Booleano que determina si el correo es valido para notificaciones
     */
    public User(String email, String password,boolean isValidMail){
        this.email = email;
        this.password = password;
        this.isValidMail = isValidMail;
    }

    /**
     * Getter para la porpiedad ID
     * @return int Representa el identificacor del usuario
     */
    public int getID() {
        return ID;
    }

    /**
     * Setter para la propiedad ID
     * @param ID int Valor que determina el identificador de usuario
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Getter pora la propiedad email
     * @return String Correo electrónico del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter para la propiedad email
     * @param email Correo electrónico del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter para la propiedad password
     * @return String Contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter para la propiedad password
     * @param password Contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter para la propiedad validEmail
     * @return boolean Determina si es un correo válido para notificaciones o no
     */
    public boolean isValidMail() {
        return isValidMail;
    }

    /**
     * Setter para la propiedad validMail
     * @param validMail Determina si el correo es valido para las notificaciones
     */
    public void setValidMail(boolean validMail) {
        isValidMail = validMail;
    }

    @Override
    public String toString() {
        return "User{" +
                "ID=" + ID +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isValidMail=" + isValidMail +
                '}';
    }
}
