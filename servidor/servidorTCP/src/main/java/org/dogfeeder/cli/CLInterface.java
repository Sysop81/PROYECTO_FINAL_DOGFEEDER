package org.dogfeeder.cli;


import static org.dogfeeder.cli.Colors.*;

/**
 * Clase CLInterface
 * Se encarga de adaptar las salidas por consola a las distintas acciones del servidor TCP. Interfaz gráfica modo comando
 * para DOG-FEEDER
 */
public class CLInterface {
    private static final String WHITE_SPACE_CHARACTER = " ";
    private static final String RETURN_CHARACTER = "\n";
    private static final String SECTION_FRAME = "============================================================";

    /**
     * Método showAppTitle
     * Se encarga de mostrar el título de la aplicación
     * @param title Mensaje que se muestra en el título de la aplicación
     */
    public static void showAppTitle(String title){
        System.out.println(ANSI_YELLOW + SECTION_FRAME + RETURN_CHARACTER +
                           ANSI_YELLOW + SECTION_FRAME.charAt(0) + WHITE_SPACE_CHARACTER + ANSI_RESET +
                           tabulateTitle(title,ANSI_YELLOW) + RETURN_CHARACTER +
                           SECTION_FRAME + ANSI_RESET);
    }

    /**
     * Método showAlertInfo
     * Mensaje de salida para la información del sistema
     * @param msg Mensaje a mostrar
     */
    public static void showAlertInfo(String msg){
        System.out.println(ANSI_CYAN + "[INFO] " + msg + ANSI_RESET);
    }

    /**
     * Método showAlertAction
     * Mensaje de salida para las acciones que lleva a cabo el sistema
     * @param msg Mensaje a mostrar
     */
    public static void showAlertAction(String msg){
        System.out.println(ANSI_GREEN + "[ACTION] " + ANSI_WHITE + msg + ANSI_RESET);
    }

    /**
     * Método showAlertWarning
     * Mensaje de salida para las acciones que requiere una alerta o atención
     * @param msg Mensaje a mostrar
     */
    public static void showAlertWarning(String msg){
        System.out.println( ANSI_YELLOW + "[WARNING] " + msg + ANSI_RESET);
    }

    /**
     * Método showAlertDanger
     * Mensajes de salida para identificar acciones de error.
     * @param msg Mensaje a mostrar
     */
    public static void showAlertDanger(String msg){
        System.out.println( ANSI_RED + "[DANGER] " + msg + ANSI_RESET);
    }


    /**
     * MétodoshowAlertDebug
     * Mensajes de salida para identificar acciones de DEBUG.
     * @param msg Mensaje a mostrar
     */
    public static void showAlertDebug(String msg){
        System.out.println(ANSI_RED + "[DEBUG] " + ANSI_WHITE + msg + ANSI_RESET);
    }

    
    /**
     * Método tabulateTitle
     * Este método se encarga de tabular el titulo añadiendo espacios para cuadrarlo y que se vea de forma correcta.
     * @param title
     * @param color
     * @return String
     */
    private static String tabulateTitle(String title, String color){
        StringBuilder _return = new StringBuilder(title);
        int total = SECTION_FRAME.length() - _return.length() - 2;

        for(int i = 0; i < total; i++){
            _return.append(i < total - 1 ? WHITE_SPACE_CHARACTER : color + SECTION_FRAME.charAt(0));
        }
        return _return.toString();
    }
}
