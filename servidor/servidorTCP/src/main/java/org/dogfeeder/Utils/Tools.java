package org.dogfeeder.Utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.dogfeeder.Main;
import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Log;
import org.dogfeeder.model.Logger4j;
import org.dogfeeder.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Clase Tools.
 * Herramientas de del sistema
 */
public class Tools {

    private static Logger4j logger = new Logger4j(Tools.class);

    /**
     * Método getRandonCode
     * Este método se encarga de generar un código aleatorio necesario para cuando un usuario realiza operaciones de
     * registro y cambio de contraseña en DOGFEEDER
     * @return
     */
    public static int getRandonCode(){
        StringBuilder num = new StringBuilder();
        int index = 0;
        final int END = 8;
        Random random = new Random();
        while(index < END){
            num.append(String.valueOf(random.nextInt(10)));
            index++;
        }

        return Integer.parseInt(String.valueOf(num));
    }


    /**
     * Método roundToDecimals
     * Este método se encarga de redondear decimales en base al parámetro numberOfDecimals
     * @param number Número a readondear dedcimales
     * @param numberOfDecimals Número de decimales con los que se desea contar
     * @return Número con los decimales redondeados.
     */
    public static double roundToDecimals(double number, int numberOfDecimals) {
        double factor = Math.pow(10, numberOfDecimals);
        return Math.round(number * factor) / factor;
    }


    /**
     * Método getLogItems
     * Este método se encarga de obtener un listado de objetos tipo Log procedentes de la lectaura del fichero de Log
     * generado por el Logger
     * @param fichero Fichero de log con la información
     * @param separador Delimitador de cada propiedad del objeto Log
     * @return ArrayList<Log> Listado de objetos tipo Log para completar un reporte
     */
    public static ArrayList<Log> getLogItems(String fichero, String separador){
        ArrayList<Log> listOfLogs = new ArrayList<>();
        File f;
        BufferedReader bfr = null;
        String msg;
        try{

            f = new File(fichero);
            bfr = new BufferedReader(new FileReader(f));
            String line = "";


            while ((line = bfr.readLine()) != null) {

                String[] aLine = line.split(separador);
                Log log = new Log();

                log.setDate(aLine[0]);

                var timeSplit = aLine[1].split(",");
                log.setTime(timeSplit[0]);
                log.setCode(Integer.parseInt(timeSplit[1]));

                log.setStatus(aLine[2]);
                log.setThread(aLine[3] + " " + aLine[4]);

                msg = "";
                for(int i = 5; i < aLine.length;i++){
                    msg += aLine[i] + " ";
                }

                log.setMsg(msg);

                listOfLogs.add(log);
            }

            logger.setInfo("Reporte generado y almacenado con éxito");

        }catch(FileNotFoundException fnfe){
            msg = "Fichero no encontrado." + fnfe.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }catch(IOException ioe){
            msg = "Error de E/S." + ioe.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }catch(Exception e){
            msg = "Error." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }finally{
            if(bfr != null){
                try{
                    bfr.close();
                }catch(IOException ioe){
                    msg = "Error de E/S." + ioe.getMessage();
                    CLInterface.showAlertDanger(msg);
                    logger.setError(msg);
                }

            }
        }

        return listOfLogs;
    }

    /**
     * Método generateReport
     * Este método se encarga de obtener un reporte con el log del sistema
     * @param logList Listado de items Log para incluir en el reporte
     * @param user Usuario que solicita el reporte
     */
    public static Boolean generateReport(ArrayList<Log> logList, User user){
        final var REPORT_FILE_PATH = System.getenv("REPORTS_FILE_PATH");
        final var IMG_FILE = System.getenv("IMG_LOGO_FILE_PATH");
        boolean response = true;
        try {
            // Step 2. Obtenemos un flujo de bytes del fichero que contiene el informe.
            InputStream fichero = Main.class.getResourceAsStream("/report.jrxml");

            // Step 3. Compilamos el informe
            JasperReport jasperReport = JasperCompileManager.compileReport(fichero);

            // Step 4. Generamos nuestro origen de datos con la finalidad de mapear correctamente el array de notas en el
            //         informe. Recibe como parametro el listado de modulos matriculados del usuario evaludado
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(logList);

            // Step 5. Generamos el listado de parametros que reibe el informe para trabajar.
            HashMap<String,Object> parametros = new HashMap<>();
            parametros.put("logo",IMG_FILE);
            parametros.put("name", user.getEmail());

            // Step 6. Se añaden los datos al informe y se genera el pfd
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parametros,beanCollectionDataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, REPORT_FILE_PATH);
            response = true;
        } catch (Exception e) {
            var msg = "Se ha producido un error al generar el reporte. " + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }
        return response;
    }

}
