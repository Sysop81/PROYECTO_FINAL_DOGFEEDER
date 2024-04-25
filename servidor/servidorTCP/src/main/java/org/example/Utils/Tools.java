package org.example.Utils;

import java.util.Random;

public class Tools {

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
}
