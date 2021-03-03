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
public class Real extends Token {

    public final float valor;

    public Real(float v) {
        super(Etiqueta.REAL);
        valor = v;
    }

    public String toString() {
        return "" + valor;
    }
}
