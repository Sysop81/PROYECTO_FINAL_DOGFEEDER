package org.dogfeeder.cli;


import static org.dogfeeder.cli.Colors.*;

public class CLInterface {
    private static final String WHITE_SPACE_CHARACTER = " ";
    private static final String RETURN_CHARACTER = "\n";
    private static final String SECTION_FRAME = "============================================================";

    public static void showAppTitle(String title){
        System.out.println(ANSI_YELLOW + SECTION_FRAME + RETURN_CHARACTER +
                           ANSI_YELLOW + SECTION_FRAME.charAt(0) + WHITE_SPACE_CHARACTER + ANSI_RESET +
                           tabulateTitle(title,ANSI_YELLOW) + RETURN_CHARACTER +
                           SECTION_FRAME + ANSI_RESET);
    }


    public static void showAlertInfo(String msg){
        System.out.println(ANSI_CYAN + "[INFO] " + msg + ANSI_RESET);
    }

    public static void showAlertWarning(String msg){
        System.out.println( ANSI_YELLOW + "[WARNING] " + msg + ANSI_RESET);
    }

    public static void showAlertError(String msg){
        System.out.println( ANSI_RED + "[ERROR] " + msg + ANSI_RESET);
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
