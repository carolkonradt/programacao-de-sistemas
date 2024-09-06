package com.emulador_caligaert.model.virtual_machine;

public class ErrorMessage {
    private int errorCode;
    private String errorMessage;


    public String getErrorMessage(int errorCode) {
        return getErrorMessageForCode(errorCode);
    }

    private String getErrorMessageForCode(int errorCode) {
        switch(errorCode){
            case 1: return "Caracter inválido: Unidade sintática não reconhecida (caracter inválido em algum elemento da linha).";
            case 2: return "Linha muito longa: Não deve haver mais de 80 caracteres numa linha.";
            case 3: return "Dígito inválido: Presença de um caracter não reconhecido como dígito para a base que está a ser usada.";
            case 4: return "Espaço ou final de linha esperado: Delimitador de final de linha depois do último operando ou instrução não reconhecido como válido.";
            case 5: return "Valor fora dos limites: Constante muito longa para o tamanho de palavra do computador.";
            case 6: return "Erro de sintaxe: Falta ou excesso de operandos em instruções, ou labels mal formados.";
            case 7: return "Símbolo redefinido: Referência simbólica com definições múltiplas.";
            case 8: return "Símbolo não definido: Referência simbólica não definida.";
            case 9: return "Instrução inválida: Mnemônico não corresponde a nenhuma instrução do computador.";
            case 10: return "Falta diretiva END: Indicação da ausência de pseudo-instrução END.";
            default: return "Erro não identificado.";
        }
    }
}
