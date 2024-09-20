package com.emulador_caligaert.model.macro_processor;

import java.io.*;
import java.util.*;

public class MacroProcessor {

    // Estrutura para armazenar as macros definidas
    private static Map<String, List<String>> macroDefinitions = new HashMap<>();

    public MacroProcessor(){

    }

    public String processMacros(String inputFileName) throws IOException {
        // Abre os arquivos de entrada e saída
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter("MASMAPRG.ASM"));

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

            // A linha seguinte contém o nome da macro
            if (insideMacro && macroName.isEmpty()) {
                macroName = line;  // Define o nome da macro
                System.out.println("Definindo macro: " + macroName); // Debug para verificar se a macro está sendo lida
                continue;  // Passa para a próxima linha
            }

            // Verifica se a linha contém o fim da definição de macro
            if (line.equals("MEND")) {
                insideMacro = false;
                // Armazena a macro definida no HashMap
                macroDefinitions.put(macroName, new ArrayList<>(macroBody));
                System.out.println("Macro armazenada: " + macroName + " com corpo: " + macroBody); // Debug
                macroBody.clear();
                macroName = "";
                continue;  // Pula a linha "MEND"
            }

            // Se estamos dentro de uma macro, armazena o corpo da macro
            if (insideMacro) {
                macroBody.add(line);  // Armazena as linhas do corpo da macro
                System.out.println("Adicionando linha ao corpo da macro: " + line); // Debug
                continue;
            }

            // Se estamos dentro de uma macro, armazena seu corpo


            // Se a linha contém uma chamada de macro, expandimos ela
            System.out.println(macroDefinitions);
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

        return new File("MASMAPRG.ASM").getAbsolutePath();
    }

    private static void expandMacro(String line, BufferedWriter writer) throws IOException {
        String macroName = line.split(" ")[0];
        List<String> macroBody = macroDefinitions.get(macroName);

        // Escreve o corpo da macro no arquivo de saída
        for (String macroLine : macroBody) {
            System.out.println("Escrevendo linha da macro " + macroName + ": " + macroLine);
            writer.write(macroLine);
            writer.newLine();
        }
    }
}
