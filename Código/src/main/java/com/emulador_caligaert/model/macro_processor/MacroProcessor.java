package com.emulador_caligaert.model.macro_processor;

import java.io.*;
import java.util.*;

public class MacroProcessor {

    // Estrutura para armazenar as macros definidas
    private static Map<String, List<String>> macroDefinitions = new HashMap<>();

    public static void main(String[] args) {
        String inputFileName = "codigo_com_macros.asm";  // Nome do arquivo de entrada
        String outputFileName = "MASMAPRG.ASM";  // Nome do arquivo de saída

        try {
            processMacros(inputFileName, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processMacros(String inputFileName, String outputFileName) throws IOException {
        // Abre os arquivos de entrada e saída
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

        String line;
        boolean insideMacro = false;
        List<String> macroBody = new ArrayList<>();
        String macroName = "";

        // Processa linha por linha
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Verifica se a linha contém uma definição de macro
            if (line.startsWith("MACRO")) {
                insideMacro = true;
                continue; // pula a linha "MACRO"
            }

            // Verifica se a linha contém o fim da definição de macro
            if (line.equals("MEND")) {
                insideMacro = false;
                macroDefinitions.put(macroName, new ArrayList<>(macroBody));
                macroBody.clear();
                macroName = "";
                continue; // pula a linha "MEND"
            }

            // Se estamos dentro de uma macro, armazena seu corpo
            if (insideMacro) {
                if (macroName.isEmpty()) {
                    // A primeira linha após "MACRO" é o nome da macro
                    macroName = line.split(" ")[0];
                } else {
                    macroBody.add(line);
                }
                continue; // pula as linhas dentro da definição da macro
            }

            // Se a linha contém uma chamada de macro, expandimos ela
            if (macroDefinitions.containsKey(line.split(" ")[0])) {
                expandMacro(line, writer);
            } else {
                writer.write(line);
                writer.newLine();
            }
        }

        // Fecha os leitores e escritores
        reader.close();
        writer.close();
    }

    private static void expandMacro(String line, BufferedWriter writer) throws IOException {
        String macroName = line.split(" ")[0];
        List<String> macroBody = macroDefinitions.get(macroName);

        // Escreve o corpo da macro no arquivo de saída
        for (String macroLine : macroBody) {
            writer.write(macroLine);
            writer.newLine();
        }
    }
}
