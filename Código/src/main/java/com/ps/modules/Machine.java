/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ps.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author rboeira
 */
public class Machine {
    private HashMap<Integer, LinkedList<String>> instructions;
    private Memory mem;
    private HashMap<String, Register> registers;
    private Stack stack;
    
    public Machine(int memSize){
        this.instructions = new HashMap<>();
        this.mem = new Memory(memSize);
        this.registers = new HashMap<>();
        this.stack = new Stack();

        LinkedList<String> instructionInfo = new LinkedList<>();
        
        instructionInfo.add("1");       // Numero de operandos
        instructionInfo.add("0/1/2");   // Endereçamento: 0->D; 1->In; 2->Im
        
        instructions.put(2, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(0, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(5, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(1, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(4, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(15, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("2");
        instructionInfo.add("0/1;0/1/2");
        
        instructions.put(13, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(10, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(3, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(14, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(12, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("0");
        instructionInfo.add("");
        
        instructions.put(16, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("0");
        instructionInfo.add("");
        
        instructions.put(11, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1");
        
        instructions.put(7, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(6, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        
        instructions.put(8, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();
        
        registers.put("ACC", new Register(16));
        registers.put("MOP", new Register(8));
        registers.put("RI", new Register(16));
        registers.put("RE", new Register(16));
        registers.put("PC", new Register(16));
        registers.put("SP", new Register(16));
    }
    
    public String selectAccessMode(int numOfOperands, int accessConfig){
        if (accessConfig > 5)
            return "";
        
        if (numOfOperands == 1){
            if (accessConfig == 0)
                return "0";
            if (accessConfig == 1)
                return "1";
            if (accessConfig == 4)
                return "2";
        }
        
        if (numOfOperands == 2){
            if (accessConfig == 0)
                return "0/0";
            if (accessConfig == 1)
                return "1/0";
            if (accessConfig == 2)
                return "0/1";
            if (accessConfig == 3)
                return "1/1";
            if (accessConfig == 4)
                return "0/2";
            if (accessConfig == 5)
                return "1/2";
        }
        
        return "";
    }
    
    public boolean decode(String instruction){
        LinkedList<String> instructionInfo; 
        
        ArrayList<Integer> operands = new ArrayList<>();
        String[] instructionParts = instruction.split(" ");
        
        String allowedAccessModes;
        
        int operation = Integer.parseInt(instructionParts[0]) & 31;
        System.out.println("op " + operation);
        int bitFive = (Integer.parseInt(instructionParts[0]) & 32) >> 5;
        System.out.println("5 " + bitFive);
        int bitSix = (Integer.parseInt(instructionParts[0]) & 64) >> 6;
        System.out.println("6 " + bitSix);
        int bitSeven = (Integer.parseInt(instructionParts[0]) & 128) >> 7;
        System.out.println("7 " + bitSeven);
        
        int accessConfig = (Integer.parseInt(instructionParts[0]) & 224) >> 5;  //Valor dos ultimos 3 bits
        System.out.println("ac "+accessConfig);
        int numOfOperands = instructionParts.length - 1;
        System.out.println("opr "+numOfOperands);
        
        int requiredOperands;
        
        try{
            instructionInfo = instructions.get(operation);
        } catch(Exception e) {
            System.out.println("Instrução não encontrada!");
            return false;
        }
        
        requiredOperands = Integer.parseInt(instructionInfo.get(0)); 
        System.out.println("req "+requiredOperands);
        
        allowedAccessModes = instructionInfo.get(1);    
        System.out.println("all "+ allowedAccessModes);
        
        if (numOfOperands != requiredOperands){
            System.out.printf("São necessários %d operandos, mas foram lidos %d!\n", requiredOperands, numOfOperands);
            return false;
        }
        
        if (((bitFive + bitSix + bitSeven) != requiredOperands) && (accessConfig != 0)){
            System.out.println("Modo de endereçamento incorreto");
            return false;
        }
        
        String[] instructionAccessMode = selectAccessMode(numOfOperands, accessConfig).split("/");
        for (int i=0; i<numOfOperands; i++){
            String mode = instructionAccessMode[i];
            System.out.println("md "+ mode);
            
            if (!(allowedAccessModes.contains(mode)) || mode.equals("")){
                System.out.println("A instrução não permite esse modo de endereçamento!");
                return false;
            }
        }
        
        if (requiredOperands == 0)
            return run(operation, 0, null, null);
        
        for (int i=0; i<requiredOperands; i++)
            operands.add(Integer.parseInt(instructionParts[i+1]));
        
        return run(operation, numOfOperands, operands, instructionAccessMode);
    }
    
    public boolean run(int operation, int numOfOperands, ArrayList<Integer> operands, String[] instructionAccessMode){
        for (int i=0; i<numOfOperands; i++){
            int operand = operands.get(i);
            int mode = Integer.parseInt(instructionAccessMode[i]);
            
            operands.set(i, searchOperand(mode, operand));
            System.out.println("opr: "+operands.get(i));
        }
        
        switch(operation){
            case 0:
                return br(operands.get(0));
            case 1:
                return brpos(operands.get(0));
            case 2:
                return add(operands.get(0));
            case 3: 
                return load(operands.get(0));
            case 4: 
                return brzero(operands.get(0));
            case 5:
                return brneg(operands.get(0));
            case 6:
                return sub(operands.get(0));
            case 7:
                return store(operands.get(0));
            case 8: 
                return write();
            case 10: 
                return divide(operands.get(0));
            case 11:
                return stop();
            case 12:
                return read();
            case 13:
                return copy(operands.get(0), operands.get(1));
            case 14: 
                return mult(operands.get(0));
            case 15: 
                return call(operands.get(0));
            case 16:
                return ret();
        }
        return true;
    }
    
    // Implementação dos modos de endereçamento
    private int directAddress(int operand) {
        return mem.read(operand); // O operando é o próprio endereço na memória
    }

    private int indirectAddress(int operand) {
        // O operando é um endereço que aponta para outro endereço na memória
        return mem.read(mem.read(operand));
    }
    
    private int searchOperand(int mode, int operand) {
        switch (mode) {
            case 0: // Direto
                return directAddress(operand);
            case 1: // Indireto
                return indirectAddress(operand);
            case 2: // Imediato
                return operand;
            default:
                throw new IllegalArgumentException("Modo de endereçamento inválido");
        }
    }

    private boolean add(int operand){
        return true;
    }
    
    private boolean sub(int operand){
        return true;
    }
    
    private boolean mult(int operand){
        return true;
    }
    
    private boolean divide(int operand){
        return true;
    }
    
    private boolean br(int operand){
        return true;
    }
    
    private boolean brneg(int operand){
        return true;
    }
    
    private boolean brpos(int operand){
        return true;
    }

    private boolean brzero(int operand){
        return true;
    }
    
    private boolean load(int memPosition){
        return true;
    }
    
    private boolean store(int memPosition){
        return true;
    }
    
    private boolean copy(int operandA, int operandB){
        return true;
    }
    
    private boolean read(){
        return true;
    }
    
    private boolean write(){
        return true;
    }
    
    private boolean call(int operand){
        return true;
    }
    
    private boolean ret(){
        return true;
    }
    
    private boolean stop(){
        return true;
    }
}

