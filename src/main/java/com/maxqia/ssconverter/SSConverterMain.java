package com.maxqia.ssconverter;

import java.io.IOException;

public class SSConverterMain {

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("ATConverter")) {
                ATConverter.main(shiftArray(args));
            } else if (args[0].equalsIgnoreCase("MappingPrinter")) {
                MappingPrinter.main(shiftArray(args));
            }

        } else {
            System.out.println("Do you need help? Look at the source code m8!");
        }
    }

    public static String[] shiftArray(String[] array) {
        String[] rtr = new String[array.length-1];
        System.arraycopy(array, 1, rtr, 0, array.length-1);
        return rtr;
    }
}
