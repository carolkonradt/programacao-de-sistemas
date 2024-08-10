/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ps.modules;

/**
 *
 * @author rboeira
 */
public class Memory {
    private int[] memory; // Array para armazenar as palavras de memória (16 bits cada)
    private int size; // Tamanho da memória em palavras

    // Construtor para inicializar a memória com um tamanho especificado
    public Memory(int sizeKB) {
        // Convertendo o tamanho de KB para palavras (1 KB = 1024 palavras de 16 bits)
        this.size = sizeKB * 1024;
        this.memory = new int[size];
        
        for (int i=0; i<size; i++)
            memory[i] = 0;
    }

    // Método para escrever uma palavra na memória em um endereço específico
    public void write(int address, int data) {
        if (address >= 0 && address < size)
            memory[address] = data & 0xFFFF; // Garantindo que o valor seja de 16 bits
        else
            System.out.println("Erro: Endereço fora dos limites da memória.");
    }

    // Método para ler uma palavra da memória em um endereço específico
    public int read(int address) {
        if (address >= 0 && address < size) {
            return memory[address];
        } else {
            System.out.println("Erro: Endereço fora dos limites da memória.");
            return -1; // Retorna um valor inválido se o endereço for fora dos limites
        }
    }
}

