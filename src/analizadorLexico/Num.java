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
public class Num extends Token {

    public final int valor;

    public Num(int v) {
        super(Etiqueta.NUM);
        valor = v;
    }

    public String toString() {
        return "" + valor;
    }
}
