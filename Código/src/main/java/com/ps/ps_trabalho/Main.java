/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.ps.ps_trabalho;

import com.ps.modules.Machine;
import java.util.HashMap;

/**
 *
 * @author rboeira
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        
        Machine pc = new Machine(1);
        // 34 = 00100010; 130 = 10000010; 98 = 01100010; 66 = 01000010; 2 = 00000010
        pc.decode("34 03");
    }
}
