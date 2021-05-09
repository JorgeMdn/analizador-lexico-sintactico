/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analizadorLexico;

/**
 *
 * @author jarvis
 */
public class Token {

    public final int etiqueta;

    public Token(int t) {
        etiqueta = t;
    }

    public String toString() {
        return "" + (char) etiqueta;
    }
}
