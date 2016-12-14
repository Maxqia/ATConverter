package com.maxqia.ssconverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.NotLinkException;
import java.util.Map.Entry;

import org.objectweb.asm.commons.Remapper;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.NodeType;
import net.md_5.specialsource.Ownable;
import net.md_5.specialsource.writer.MappingWriter;
import net.md_5.specialsource.writer.Searge;

/**
 * Takes a CSRG file and converts it into a SRG file
 * @author Maxqia
 */
public class MappingPrinter extends Remapper {

    static JarMapping mapping;
    static JarRemapper remapper;
    static MappingWriter writer;

    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            mapping = new JarMapping();
            mapping.loadMappings(args[0],
                    false, false, null, null);
            //mapping.packages.put(".", "net/minecraft/server/");
            mapping.packages.put(".", "net/minecraft/server/v1_11_R1/");
            mapping.packages.put("net/minecraft/server/", "net/minecraft/server/v1_11_R1/");
            remapper = new JarRemapper(mapping);
            writer = new Searge(args[0], "itself, using Maxqia's MappingPrinter");
            new MappingPrinter().jarMappingToWriter(mapping, writer);
            writer.write(new PrintWriter(System.out));
        } else {
            throw new NotLinkException("No file defined");
        }
        //throw new NotLinkException("No file defined");
    }

    public void jarMappingToWriter(JarMapping mapping, MappingWriter writer) {
        for (Entry<String, String> entry : mapping.classes.entrySet()) {
            writer.addClassMap(entry.getKey(), remapper.map(remapper.map(entry.getKey()))); // csrg is weird ...
        }

        for (Entry<String, String> entry : mapping.fields.entrySet()) {
            String fieldSplit = entry.getKey();
            int fieldLoc = fieldSplit.lastIndexOf('/');
            String field = fieldSplit.substring(fieldLoc+1, fieldSplit.length()); // add one to avoid dash
            String owner = fieldSplit.substring(0, fieldLoc);

            Ownable original = new Ownable(NodeType.FIELD, this.map(owner), field, null, 0);
            Ownable modified = new Ownable(NodeType.FIELD, remapper.map(owner), entry.getValue(), null, 0);
            writer.addFieldMap(original, modified);
        }

        for (Entry<String, String> entry : mapping.methods.entrySet()) {
            String[] descriptorSplit = entry.getKey().split("\\s+");
            String descriptor = descriptorSplit[1];

            String methodSplit = descriptorSplit[0];
            int methodLoc = methodSplit.lastIndexOf('/');
            String method = methodSplit.substring(methodLoc+1, methodSplit.length()); // add one to avoid dash
            String owner = methodSplit.substring(0, methodLoc);

            Ownable original = new Ownable(NodeType.METHOD, this.map(owner), method, this.mapMethodDesc(descriptor), 0);
            Ownable modified = new Ownable(NodeType.METHOD, remapper.map(owner), entry.getValue(), remapper.mapMethodDesc(descriptor), 0);
            writer.addMethodMap(original, modified);
        }
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
