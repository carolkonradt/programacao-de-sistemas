package com.emulador_caligaert.model.virtual_machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Scanner;
import java.util.Stack;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

/**Classe dedicada a implementação da máquina virtual
 * @author rboeira
 */
public class Machine {
    private HashMap<Integer, LinkedList<String>> instructions;
    private Memory mem;
    private HashMap<String, Register> registers;
    private Stack stack;
    private int stackSize = 10;
    private int programStart;
    private int programEnd;
    private boolean stopReached = false;
    private TextArea outputArea;
    private HashMap<Integer, String> errorMessages;
    private ErrorMessage errorMessage;

    /**
     * Método construtor da máquina virtual.
     * Adiciona a um HashMap todas as instruções da máquina.
     * Estas instruções serão decodificadas de acordo com o código inserido na máquina.
     * @param memSize - um int que determina o tamanho da memória
     */
    public Machine(int memSize, int sizeBits, TextArea outputArea){
        this.instructions = new HashMap<>();
        this.mem = new Memory(memSize, sizeBits);
        this.registers = new HashMap<>();
        this.stack = new Stack();
        this.errorMessage = new ErrorMessage();
        this.outputArea = outputArea;



        LinkedList<String> instructionInfo = new LinkedList<>();

        instructionInfo.add("1");       // Numero de operandos
        instructionInfo.add("0/1/2");   // Endereçamento: 0->D; 1->In; 2->Im
        instructionInfo.add("2");       // Tamanho da instrução

        instructions.put(2, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(0, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(5, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(1, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(4, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(15, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("2");
        instructionInfo.add("0/1;0/1/2");
        instructionInfo.add("3");

        instructions.put(13, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(10, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(3, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(14, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(12, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("0");
        instructionInfo.add("");
        instructionInfo.add("1");

        instructions.put(16, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("0");
        instructionInfo.add("");
        instructionInfo.add("1");

        instructions.put(11, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(7, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(6, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(8, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        registers.put("ACC", new Register(sizeBits));
        registers.put("MOP", new Register(8));
        registers.put("RI", new Register(sizeBits));
        registers.put("RE", new Register(sizeBits));
        registers.put("PC", new Register(sizeBits));
        registers.put("SP", new Register(sizeBits));
    }

    /**Método que seleciona o modo de acesso à memória.
     * @param numOfOperands - número de operandos;
     * @param accessConfig - modo como estão arranjados os bits que definem o tipo de endereçamento;
     * @return modo de acesso à memória pela instrução: 0->D; 1->In; 2->Im.
     */
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

    public boolean runProgram(){
        mem.write(2, Integer.toString(stackSize));
        if (getMOP() == 1){
            registers.get("PC").setData(Integer.toString(3+stackSize));
            mem.write(2, Integer.toString(stackSize));
            while (!stopReached) {
                if (!runInstruction())
                    return false;
            }
            return true;
        }

        if (getMOP() == 2 && !stopReached) {
            if (Integer.parseInt(registers.get("PC").getData()) == 0)
                registers.get("PC").setData(Integer.toString(3 + stackSize));
            return runInstruction();
        }

        return false;
    }

    private boolean runInstruction(){
        int currentPC = Integer.parseInt(registers.get("PC").getData());

        registers.get("RI").setData(mem.read(currentPC));
        registers.get("RE").setData(mem.read(currentPC+1));
        int operation = Integer.parseInt(mem.read(currentPC)) & 31;
        LinkedList<String> instructionInfo;

        try{
            instructionInfo = instructions.get(operation);
        } catch(Exception e) {
            outputArea.appendText(errorMessage.getErrorMessage(9));
            return false;
        }

        int instructionSize = Integer.parseInt(instructionInfo.get(2));

        String instruction = mem.read(currentPC);
        for (int i=1; i<instructionSize; i++){
            String operand = mem.read(currentPC + i);
            instruction = instruction + " " + operand;
        }

        return decode(instruction);
    }

    public boolean loadProgram(String filePath){
        int i = 0;
        programStart = 3+stackSize;

        try {
            File program = new File(filePath);
            Scanner fileReader = new Scanner(program);

            while (fileReader.hasNextLine()) {
                String instruction = fileReader.nextLine();

                for (String code: instruction.split(" ")){
                    mem.write(programStart+i, code);
                    i++;
                }
            }
            programEnd = programStart+i;
            fileReader.close();
        } catch (FileNotFoundException e) {
            outputArea.appendText(errorMessage.getErrorMessage(11));
            return false;
        }
        return true;
    }

    /**Método que decodifica a instrução, descobre quantos operandos tem, e utiliza
     * outros métodos como o selectAccessMode para ajudar na tarefa
     * @param instruction - a instrução a ser decodificada
     * @return uma chamada ao método run com parâmetros operation, numOfOperands,
     *          operands, instructionAccessMode.
     */
    public boolean decode(String instruction){
        LinkedList<String> instructionInfo;

        ArrayList<String> operands = new ArrayList<>();
        String[] instructionParts = instruction.split(" ");

        String allowedAccessModes;

        int operation = Integer.parseInt(instructionParts[0]) & 31;
        int bitFive = (Integer.parseInt(instructionParts[0]) & 32) >> 5;
        int bitSix = (Integer.parseInt(instructionParts[0]) & 64) >> 6;
        int bitSeven = (Integer.parseInt(instructionParts[0]) & 128) >> 7;

        int accessConfig = (Integer.parseInt(instructionParts[0]) & 224) >> 5;  //Valor dos ultimos 3 bits
        int numOfOperands = instructionParts.length - 1;

        int requiredOperands;

        instructionInfo = instructions.get(operation);

        requiredOperands = Integer.parseInt(instructionInfo.get(0));

        allowedAccessModes = instructionInfo.get(1);
        if (numOfOperands != requiredOperands){
            outputArea.appendText(errorMessage.getErrorMessage(6));
            return false;
        }

        if (((bitFive + bitSix + bitSeven) != requiredOperands) && (accessConfig != 0)){
            outputArea.appendText(errorMessage.getErrorMessage(12));
            return false;
        }

        String[] instructionAccessMode = selectAccessMode(numOfOperands, accessConfig).split("/");
        for (int i=0; i<numOfOperands; i++){
            String mode = instructionAccessMode[i];

            if (!(allowedAccessModes.contains(mode)) || mode.equals("")){
                outputArea.appendText(errorMessage.getErrorMessage(12));
                return false;
            }
        }
        int newPC = Integer.parseInt(registers.get("PC").getData()) + Integer.parseInt(instructionInfo.get(2));

        if (requiredOperands == 0)
            return execute(operation, 0, null, null, newPC);

        for (int i=0; i<requiredOperands; i++)
            operands.add(instructionParts[i+1]);

        return execute(operation, numOfOperands, operands, instructionAccessMode, newPC);
    }

    /**
     *
     * @param operation - define qual operação será executada
     * @param numOfOperands - número de operandos da operação
     * @param operands - quais os operandos da operação
     * @param instructionAccessMode - modo de acesso à memória pela operação
     * @return boolean
     */

    public boolean execute(int operation, int numOfOperands, ArrayList<String> operands, String[] instructionAccessMode, int newPC){
        ArrayList<String> data;

        switch(operation){
            case 0:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return br(data.get(0), newPC);
            case 1:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return brpos(data.get(0), newPC);
            case 2:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return add(data.get(0), newPC);
            case 3:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return load(data.get(0), newPC);
            case 4:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return brzero(data.get(0), newPC);
            case 5:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return brneg(data.get(0), newPC);
            case 6:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return sub(data.get(0), newPC);
            case 7:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return store(data.get(0), newPC);
            case 8:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return write(data.get(0), newPC);
            case 10:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return divide(data.get(0), newPC);
            case 11:
                return stop(newPC);
            case 12:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return read(data.get(0), newPC);
            case 13:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return copy(data.get(0), getOperands(operands, instructionAccessMode, numOfOperands, 0).get(1), newPC);
            case 14:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 0);
                return mult(data.get(0), newPC);
            case 15:
                data = getOperands(operands, instructionAccessMode, numOfOperands, 1);
                return call(data.get(0), newPC);
            case 16:
                return ret();
            default:
                break;
        }
        return true;
    }


    /**
     * Implementação do modo de endereçamento direto
     * @param operand - operando
     * @return um inteiro que representa a palavra que está dentro do endereço
     */
    private String directAddress(String operand) {
        int op = 0;
        try{
            op = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                op = firstLetter;
            }
        }
        return mem.read(op + programStart); 
    }

    /**
     * Implementação do modo de endereçamento indireto
     * @param operand - operando
     * @return um inteiro que representa a palavra que está dentro do endereço apontado pelo endereço
     *         do operando.
     */
    private String indirectAddress(String operand) {
        int op = 0;
        try{
            op = Integer.parseInt(directAddress(operand));
        } catch (Exception e){
            int firstLetter = getFirstLetter(directAddress(operand));
            if (firstLetter != -1){
                op = firstLetter;
            }
        }
        return mem.read(op + programStart);
    }

    private ArrayList<String> getOperands(ArrayList<String> operands, String[] instructionAccessMode, int numOfOperands, int opType){
        ArrayList<String> data = new ArrayList<>();
        HashMap<Integer, Integer> modeRelation = new HashMap<>();
        modeRelation.put(0, 2);
        modeRelation.put(1, 0);
        modeRelation.put(2, 2);

        for (int i=0; i<numOfOperands; i++){
            String operand = operands.get(i);
            int mode = Integer.parseInt(instructionAccessMode[i]);

            if (opType == 0){
                data.add(searchOperand(mode, operand));
            } else {
                if (mode != 2){
                    int op = 0;
                    try{
                        op = Integer.parseInt(searchOperand(modeRelation.get(mode), operand));
                    } catch (Exception e){
                        int firstLetter = getFirstLetter(searchOperand(modeRelation.get(mode), operand));
                        if (firstLetter != -1){
                            op = firstLetter;
                        }
                    }
                    data.add(Integer.toString(op+programStart));
                    continue;
                }
                data.add(searchOperand(modeRelation.get(mode), operand));
            }
        }
        return data;
    }

    /**
     *
     * @param mode - modo de acesso à memória
     * @param operand - operando a ser utilizado na operação
     * @return um int que representa a palavra buscada.
     */
    private String searchOperand(int mode, String operand) {
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

    private int getFirstLetter(String operand){
        for (int i=0; i<operand.length(); i++){
            char letter = operand.charAt(i);

            if (!Character.isDigit(letter))
                return (int) letter;
        }
        return -1;
    } 

    private boolean add(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;
        boolean isLetter = false;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
                isLetter = true;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
                isLetter = true;
            }
        }
        
        int newACC = opA + opB;
            
        if (isLetter)
            registers.get("ACC").setData("" + (char) newACC);
        else
            registers.get("ACC").setData(Integer.toString(newACC));
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean sub(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;
        boolean isLetter = false;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
                isLetter = true;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
                isLetter = true;
            }
        }
        int newACC = opB - opA;
            
        if (isLetter)
            registers.get("ACC").setData("" + (char) newACC);
        else
            registers.get("ACC").setData(Integer.toString(newACC));
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean mult(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;
        boolean isLetter = false;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
                isLetter = true;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
                isLetter = true;
            }
        }
        int newACC = opB * opA;
            
        if (isLetter)
            registers.get("ACC").setData("" + (char) newACC);
        else
            registers.get("ACC").setData(Integer.toString(newACC));
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean divide(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;
        boolean isLetter = false;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
                isLetter = true;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
                isLetter = true;
            }
        }
        int newACC = opB / opA;
            
        if (isLetter)
            registers.get("ACC").setData("" + (char) newACC);
        else
            registers.get("ACC").setData(Integer.toString(newACC));
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean br(String operand, int newPC){
        int op = 0;

        try{
            op = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                op = firstLetter;
            }
        }
        registers.get("PC").setData(Integer.toString(op));
        return true;
    }

    private boolean brneg(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
            }
        }
        if (opB < 0){
            registers.get("PC").setData(Integer.toString(opA));
            return true;
        }
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean brpos(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
            }
        }
        if (opB > 0){
            registers.get("PC").setData(Integer.toString(opA));
            return true;
        }
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean brzero(String operand, int newPC){
        String currentACC = registers.get("ACC").getData();
        int opA = 0;
        int opB = 0;

        try{
            opA = Integer.parseInt(operand);
        } catch (Exception e){
            int firstLetter = getFirstLetter(operand);
            if (firstLetter != -1){
                opA = firstLetter;
            }
        }

        try{
            opB = Integer.parseInt(currentACC);
        } catch (Exception e){
            int firstLetter = getFirstLetter(currentACC);
            if (firstLetter != -1){
                opB = firstLetter;
            }
        }
        if (opB == 0){
            registers.get("PC").setData(Integer.toString(opA));
            return true;
        }
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean load(String operand, int newPC){
        registers.get("ACC").setData(operand);
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean store(String memPosition, int newPC){
        int op = 0;

        try{
            op = Integer.parseInt(memPosition);
        } catch (Exception e){
            int firstLetter = getFirstLetter(memPosition);
            if (firstLetter != -1){
                op = firstLetter;
            }
        }
        if (!mem.write(op, registers.get("ACC").getData())){
            outputArea.appendText("Erro: endereço fora do alcance da memória: " + memPosition);
            return false;
        }
        
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean copy(String memPosition, String data, int newPC){
        int op = 0;

        try{
            op = Integer.parseInt(memPosition);
        } catch (Exception e){
            int firstLetter = getFirstLetter(memPosition);
            if (firstLetter != -1){
                op = firstLetter;
            }
        }

        if (!mem.write(op, data)){
            outputArea.appendText("Erro: endereço fora do alcance da memória: " + memPosition);
            return false;
        }
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean read(String memPosition, int newPC){
        TextInputDialog td = new TextInputDialog("Insira um valor");

        td.setHeaderText("Ler Entrada");
        Optional<String> input = td.showAndWait();

        if (input.isEmpty()){   
            outputArea.appendText("Erro: nenhum valor informado para a leitura.");
            return false;
        }

        int op = 0;

        try{
            op = Integer.parseInt(memPosition);
        } catch (Exception e){
            int firstLetter = getFirstLetter(memPosition);
            if (firstLetter != -1){
                op = firstLetter;
            }
        }

        String data = input.get();
        if (data.isEmpty())
            data = "0";

        try{
            int intData = Integer.parseInt(data);
        } catch (Exception e){
            int firstLetter = getFirstLetter(data);
            data = ""+(char) firstLetter; 
        }
        if (!mem.write(op, data)){
            outputArea.appendText("Erro: endereço fora do alcance da memória: " + memPosition);
            return false;
        }
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean write(String data, int newPC){
        outputArea.appendText(data+'\n');
        registers.get("PC").setData(Integer.toString(newPC));
        return true;
    }

    private boolean call(String operand, int newPC){
        if (stack.size() == stackSize){
            registers.get("SP").setData("0");
            outputArea.appendText("Stack Overflow\n");
            return false;
        }

        stack.push(newPC);
        mem.write(2+stack.size(), Integer.toString(newPC));
        registers.get("PC").setData(operand);
        registers.get("SP").setData(Integer.toString(stack.size()));
        return true;
    }

    private boolean ret(){
        int memPosition = 2+stack.size();
        if (memPosition != 2)
            mem.write(memPosition, "0");
        try{
            registers.get("PC").setData(Integer.toString((int) stack.pop()));
        } catch (EmptyStackException es){
            outputArea.appendText("Erro: tentativa de desempilhar stack vazia.");
            return false;
        }
        registers.get("SP").setData(Integer.toString(stack.size()));
        return true;
    }

    private boolean stop(int newPC){
        stopReached = true;
        setMOP(0);
        registers.get("PC").setData(Integer.toString(newPC));
        outputArea.appendText("Programa executado com sucesso!\n");
        return true;
    }

    public void setMOP(int opMode){
        registers.get("MOP").setData(Integer.toString(opMode));
    }

    public int getMOP(){
        return Integer.parseInt(registers.get("MOP").getData());
    }

    public void restartMachine(){
        mem.clear();
        stack.clear();
        stackSize = 10;
        stopReached = false;

        for (String key: registers.keySet())
            registers.get(key).clear();
    }

    public Memory getMemory(){
        return mem;
    }

    public HashMap<String, Register> getRegisters() {
        return registers;
    }

    public Stack getStack(){
        return stack;
    }

    public void setStackSize(int size){
        this.stackSize = size;
    }

    public int getStackSize(){
        return this.stackSize;
    }
}

