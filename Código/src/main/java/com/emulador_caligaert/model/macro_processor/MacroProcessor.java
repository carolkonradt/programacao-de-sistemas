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
        boolean flagNewMacro = false;

        // Pilha para controlar a definição de macros aninhadas
        Stack<String> macroDefinitionStack = new Stack<>();

        // Processa linha por linha
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // Verifica se a linha contém uma definição de macro
            if (line.startsWith("MACRO")) {
                insideMacro = true;
                flagNewMacro = true;
                //continue; // pula a linha "MACRO"
            } else {
                    
                // Verifica se a linha contém o fim da definição de macro
                if (line.equals("MEND")) {
                    macroDefinitionStack.pop();
                    if(macroDefinitionStack.empty()){
                        insideMacro = false;
                    }
                } else {
                        
                    // Se estamos dentro de uma macro, armazena seu corpo
                    if (insideMacro) {
                        if (flagNewMacro) {
                            // A primeira linha após "MACRO" é o nome da macro
                            macroName = line.split(" ")[0];
                            macroDefinitions.put(macroName, new ArrayList<>());
                            macroDefinitionStack.push(macroName);
                            flagNewMacro = false;
                        } else {
                            for(String key: macroDefinitionStack){
                                macroBody = macroDefinitions.get(key);
                                macroBody.add(line);
                            }
                        }
                        
                    } else {
                            // Se a linha contém uma chamada de macro, expandimos ela
                        if (macroDefinitions.containsKey(line.split(" ")[0])) {
                            String macroNameExp = line.split(" ")[0];
                            List<String> macroBodyExp  = macroDefinitions.get(macroNameExp);

                            // Escreve o corpo da macro no arquivo de saída
                            for (String macroLine : macroBodyExp) {
                                System.out.println("Escrevendo linha da macro " + macroName + ": " + macroLine);
                                writer.write(macroLine);
                                writer.newLine();
                            }
                        } else {
                            writer.write(line);
                            writer.newLine();
                        }
                    }                        
                }
            }
            continue;
        }

        System.out.println(macroDefinitions);

        // Fecha os leitores e escritores
        reader.close();
        writer.close();

        return new File("MASMAPRG.ASM").getAbsolutePath();
    }
}


