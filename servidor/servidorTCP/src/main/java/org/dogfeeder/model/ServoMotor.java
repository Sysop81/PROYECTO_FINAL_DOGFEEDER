package org.dogfeeder.model;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;
import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.database.SettingsDAO;
import org.dogfeeder.database.SupplyFoodAuditDAO;

/**
 * Clase ServoMotor
 * Se encarga de manejar servoMotores de rotación continua y 180 grados.
 */
public class ServoMotor {

    private int pin;
    private int time;
    private final int DEFAULT_TIME = 5000;
    private final int INITIAL_POS = 0;
    private final int FINAL_POS = 100;
    private static boolean isInUse = false;
    private static Logger4j logger = new Logger4j(ServoMotor.class);


    /**
     * Constructor ServoMotor
     * @param pin
     */
    public ServoMotor(int pin){
        this.pin = pin;
        this.time =DEFAULT_TIME;
        Gpio.wiringPiSetup();
    }

    /**
     * Getter para la propiedad PIN GPIO utilizado por la instancia del servoMotor
     * @return int GPIO utilizado por el ServoMotor
     */
    public int getPin() {
        return pin;
    }

    /**
     * Setter para la propiedad PIN GPIO
     * @param pin
     */
    public void setPin(int pin) {
        this.pin = pin;
    }

    /**
     * Getter para la propiedad time
     * @return int Tiempo en milisegundos utilizado por el servoMotor para realizar las esperas
     */
    public int getTime() {
        return time;
    }

    /**
     * Setter para la propiedad time
     * @param time
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * Método rotate360
     * Este método se encarga de hacer girar el motor en fracciones marcadas por la propiedad tiempo.
     * @return boolean Resultado de la operación de rotación
     */
    public boolean rotate360(){
        // Si esta en uso, se realiza un retorno anticipado con valor a falso
        if(isInUse) return false;
        boolean isOk = false;
        try {
            // Se marca la bandera de servoMotor uso y se crea el canal PWM para comunicarnos con el servoMotor
            isInUse = true;
            SoftPwm.softPwmCreate(this.pin,INITIAL_POS,FINAL_POS);

            // Se marca un pulso constante para hacerlo rotar y se utiliza sleep para dejarlo actuar durante el tiempo
            // marcado por la propiedad pin
            SoftPwm.softPwmWrite(pin, 7);
            Thread.sleep(time);
            // Se cambia la respuesta del método para indicar que el funcionamiento ha sido correcto
            isOk = true;
        }catch (InterruptedException e){
            var msg = "Ocurrió un error durante el suministro de comida.\n" + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }finally {
            isInUse = false;
        }

        return isOk;
    }

    /**
     * Método supplyFood
     * Este método se encarga de suministrar alimento al comedero
     * @param servoMotor
     * @param hx711module
     * @param ultraSonic
     * @param settingsDAO
     * @param sfaDAO
     * @param user
     * @param isFromDogFeeder
     * @return String Código de respuesta a enviar al cliente
     */
    public static String supplyFood(ServoMotor servoMotor,
                                    HX711 hx711module,
                                    UltraSonic ultraSonic,
                                    SettingsDAO settingsDAO,
                                    SupplyFoodAuditDAO sfaDAO,
                                    User user,
                                    Boolean isFromDogFeeder) {
        // Step 0. Se procede a realizar una lectura inicial del peso del cuenco de alimento y se obtiene el peso
        //         neto inicial. (Peso neto -> Descontado el peso del cuenco de comida)
        hx711module.read();
        var suppliedFoodSum = hx711module.getNetWeight();

        // Step 1. Definición de variables locales
        var responseCode = ResponseCodes.ERROR.getCode();
        double weightDif;
        int counter = 0;
        boolean res = false;
        final int MAX_FOOD = settingsDAO.getSavedFoodRation();

        // Step 2. Se inicia el bucle hasta que se llegue al peso indicado (o Mientras el peso neto sea inferior a ...)
        while (hx711module.getNetWeight() < MAX_FOOD) {
            // Se obtiene el peso neto (Si en valor del cuenco)
            var weightSupplyed = hx711module.getNetWeight();
            CLInterface.showAlertDebug("Iniciando ciclo número " + counter + " de suministro de alimento.");
            // Se realiza el giro de servoMotor principal para el suministro de alimento
            res = servoMotor.rotate360();
            // Se realiza una nueva lectura del peso total registrado (Cuenco + alimento suministrado)
            hx711module.read();

            // Se evalua si la respuesta de funcionamiento del servoMotor es correcta, en caso contrario se rompe el ciclo
            // En este caso un false representa un error de iterrupción del servoMotor (Falla de corriente o rotura)
            if (!res) break;

            // Evaluamos la direncia entre actual y antes de suministrar
            weightDif = hx711module.getNetWeight() - weightSupplyed;

            // Evaluamos si la diferencia se encuentra entre 0 y 15 gramos durante mas de un ciclo de giro. Si esto ocurre
            // es posible que el servoMotor se encuentre atascado o no quede alimento entre la tolva y tornillo para ser
            // extraido. Poer ello es necesario finalizar la rotación del servoMotor.
            if (weightDif >= 0 & weightDif <= 15 & counter > 0) {
                CLInterface.showAlertDebug("Tolva en estado crítico -> " + (ultraSonic.isLowFoodLevel() ? "Sí":"No"));

                // Establecemos el código de respuesta en función de si la medición de la tolva se encuentra en nivel bajo,
                // si esta se encuentra en un nivel optimo, es 99% seguro que el servoMotor se encuentra atascado
                responseCode = ultraSonic.isLowFoodLevel() ? ResponseCodes.ERROR_STUCK_HOPPER.getCode() :
                        ResponseCodes.ERROR_EMPTY_HOPPER.getCode();
                CLInterface.showAlertDebug("Código respuesta -> " + responseCode);

                break;
            } else {
                // Actualizamos el sumatorio de peso
                suppliedFoodSum += weightDif;
            }
            counter++;
        }

        // Detenemos el servoMotor. Liberación de recursos GPIO
        CLInterface.showAlertDebug("Finalizando Servomotor 360. " + "suministrado : " + hx711module.getNetWeight() + " == " + suppliedFoodSum);
        servoMotor.stopRotate();

        // Se avalua que los códigos de respuesta no sean por atasco o tolva nivel crítico
        if(!responseCode.equals(ResponseCodes.ERROR_STUCK_HOPPER.getCode() ) &&          // codigo 4
                !responseCode.equals(ResponseCodes.ERROR_EMPTY_HOPPER.getCode() )) {     // codigo 5
            if (res) {
                // Se inserta un nuevo registro de auditoría
                sfaDAO.postSupplyFood(new SupplyFoodAudit(user, suppliedFoodSum));
                // Se muesta msg por el CLI del servidor y se actualiza el logger
                var msg = user.getEmail() + " suministra alimento al comedero" + (isFromDogFeeder ? "[Botón físico]" : "");
                CLInterface.showAlertInfo(msg);
                logger.setInfo(msg);
                // Se actualiza el código de respuesta
                responseCode = ResponseCodes.OK.getCode();
            }else{
                // Se actualiza el logger y se muestra información por el CLI del servidor
                var msg = user.getEmail() + " NO suministra alimento al comedero" + (isFromDogFeeder ? "[Botón físico]" : "") + "[ERROR]";
                CLInterface.showAlertDanger(msg);
                logger.setInfo(msg);
            }
        }
        // Se retorna el código de estado.
        return responseCode;
    }

    /**
     * Método stopRotate
     * Este método se encarga de finalizar la rotación del servoMotor
     */
    public void stopRotate(){
        SoftPwm.softPwmWrite(pin, 0);
    }


    /**
     * Método isBussy
     * Getter para la propiedad isInUse.
     * @return boolean Determina el estado del servoMotor
     */
    public boolean isBussy(){
        return isInUse;
    }



    /**
     * Método startGatePythonScript
     * Este método se encarga de utilizar un script de python para apertura y cierre de la compuerta de la tolva
     * @param duttyValue Valor del ciclo de trabajo para el servoMotor
     */
    public static void startGatePythonScript(double duttyValue){
        final String COMMAND = "python3 " + System.getenv("GATE_SCRIPT_NAME") + " " + duttyValue;
        try{
            Runtime.getRuntime().exec(COMMAND);
        } catch (Exception ioe) {
            var msg = "Error al lanzar el script python para la apertura de la puerta de alimento. " + ioe.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }
    }
}
