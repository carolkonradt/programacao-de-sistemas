/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ps.modules;

import static java.lang.Math.pow;

/**
 *
 * @author rboeira
 */
class Register {
    private int data;
    private int size;

    // Construtor para inicializar o registrador com o tamanho em bits
    public Register(int sizeBits) {
        this.size = sizeBits;
        this.data = 0; // Inicialmente, o registrador contém 0
    }

    // Método para definir o valor do registrador
    public void setData(int data) {
        this.data = data & ((int)pow(2, size));
    }

    // Método para obter o valor do registrador
    public int getData() {
        return this.data;
    }
}