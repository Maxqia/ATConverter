package com.maxqia.atconverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;

public class Converter {

    static boolean remap;
    static JarMapping mapping;
    static JarRemapper remapper;

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            FileReader fileReader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            if (args.length >= 2) {
                remap = true; // load mapping
                mapping = new JarMapping();
                mapping.loadMappings(args[1],
                        false, false, null, null);
                remapper = new JarRemapper(mapping);
            }
            doLoop(bufferedReader);
        } else {
            doLoop(new BufferedReader(new InputStreamReader(System.in)));
        }
        //throw new NotLinkException("No file defined");
    }

    public static void doLoop(BufferedReader bufferedReader) throws IOException {
        System.out.println("# Generated using Maxqia's ATConverter");
        String line;
        while((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            if (line.isEmpty()) continue;

            ArrayList<String> toLine = new ArrayList<String>();
            String[] lineSplit = line.split("\\s+");
            toLine.add(lineSplit[0].replace("-final", "-f"));

            String split = lineSplit[1];
            String name = split;
            String desc = "";
            int index = name.indexOf('(');
            if (index != -1) {
                // found a desc!
                name = split.substring(0, index);
                desc = split.substring(index, split.length());
            }

            List<String> nameSplit = new ArrayList<String>(Arrays.asList(lineSplit[1].split("\\/")));
            String nameOf = nameSplit.remove(nameSplit.size()-1);
            String classOf = String.join(".", nameSplit);

            if (remap) {
                String intClass = String.join("/", nameSplit); // join class name again, but with internal name
                toLine.add(remapper.map(intClass)); // add class line

                //int index = nameOf.indexOf('('); // find desc
                if (index == -1) {
                    // didn't find desc, must be a field!
                    toLine.add(remapper.mapFieldName(intClass, nameOf, null));
                } else {
                    String methodName = nameOf.substring(0, index);
                    String methodDesc = nameOf.substring(index, nameOf.length());
                    String rmmethNm = remapper.mapMethodName(intClass, methodName, methodDesc);
                    String rmmethDc =remapper.mapMethodDesc(methodDesc);
                    toLine.add(rmmethNm+rmmethDc);
                }
                toLine.add("#" + "nameOf"); // add comment of original name
            } else {
                toLine.add(classOf);
                toLine.add(nameOf+desc);
            }
            System.out.println(String.join(" ", toLine));
        }

    }

}
