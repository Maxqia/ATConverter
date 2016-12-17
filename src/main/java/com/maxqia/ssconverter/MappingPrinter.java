package com.maxqia.ssconverter;

import java.io.IOException;
import java.nio.file.NotLinkException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;

import org.objectweb.asm.commons.Remapper;

import com.google.common.collect.Sets;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;

/**
 * Takes a CSRG file and converts it into a SRG file
 * @author Maxqia
 */
public class MappingPrinter extends Remapper {

    static JarMapping mapping;
    static JarRemapper remapper;

    static HashSet<String> lines = Sets.newLinkedHashSet();

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            mapping = new JarMapping();
            mapping.loadMappings(args[0],
                    false, false, null, null);
            //mapping.packages.put(".", "net/minecraft/server/");
            mapping.packages.put(".", "net/minecraft/server/v1_11_R1/");
            mapping.packages.put("net/minecraft/server/", "net/minecraft/server/v1_11_R1/");
            remapper = new JarRemapper(mapping);
            new MappingPrinter().jarMappingToSet(mapping);
            printSet();
        } else {
            throw new NotLinkException("No file defined");
        }
        //throw new NotLinkException("No file defined");
    }

    public void jarMappingToSet(JarMapping mapping) {
        for (Entry<String, String> entry : mapping.classes.entrySet()) {
            addClassMap(entry.getKey(), remapper.map(entry.getValue()));
        }

        for (Entry<String, String> entry : mapping.fields.entrySet()) {
            String fieldSplit = entry.getKey();
            int fieldLoc = fieldSplit.lastIndexOf('/');
            String field = fieldSplit.substring(fieldLoc+1, fieldSplit.length()); // add one to avoid dash

            String owner = fieldSplit.substring(0, fieldLoc);
            addClassMap(owner);
            lines.add("FD: " + this.map(owner) + "/" + field + " " + remapper.map(owner) + "/" + entry.getValue());
        }

        for (Entry<String, String> entry : mapping.methods.entrySet()) {
            String[] descriptorSplit = entry.getKey().split("\\s+");
            String descriptor = descriptorSplit[1];

            String methodSplit = descriptorSplit[0];
            int methodLoc = methodSplit.lastIndexOf('/');
            String method = methodSplit.substring(methodLoc+1, methodSplit.length()); // add one to avoid dash

            String owner = methodSplit.substring(0, methodLoc);
            addClassMap(owner);

            lines.add("MD: " + this.map(owner) + "/" + method + " " + this.mapMethodDesc(descriptor) + " "
                    + remapper.map(owner) + "/" + entry.getValue() + " " + remapper.mapMethodDesc(descriptor));
        }
    }

    public void addClassMap(String string) {
        addClassMap(this.map(string), remapper.map(string));
    }

    public static void addClassMap(String from, String to) {
        /*if (!to.contains("v1_11_R1"))
            System.out.println();*/
        lines.add("CL: " + from + " " + to);
    }

    public static void printSet() {
        System.out.println("# Generated using Maxqia's MappingPrinter");
        System.out.println();

        ArrayList<String> finalList = new ArrayList<String>();
        finalList.addAll(lines);
        Collections.sort(finalList);
        for (String string : finalList) {
            System.out.println(string);
        }
    }

    // csrg is weird ...
    public static String doubleMap(String string) {
        return remapper.map(remapper.map(string));
    }

    @Override
    public String map(String owner) {
        for (Entry<String, String> entry : mapping.classes.entrySet()) {
            if (owner.equals(entry.getValue())) {
                String[] split = entry.getKey().split("\\/");
                return split[split.length-1];
            }
        }
        return owner; // fallback if there's none found
    }

}
