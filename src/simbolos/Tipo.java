/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simbolos;

import analizadorLexico.*;

/**
 *
 * @author jarvis
 */
public class Tipo extends Palabra {

    public int anchura = 0; // anchura se usa para asingación de almacenamiento

    public Tipo(String s, int etiqueta, int w) {
        super(s, etiqueta);
        anchura = w;
    }
    public static final Tipo 
            Int = new Tipo("int", Etiqueta.BASIC, 4),
            Float = new Tipo("float", Etiqueta.BASIC, 8),
            Char = new Tipo("char", Etiqueta.BASIC, 1),
            Bool = new Tipo("bool", Etiqueta.BASIC, 1);

    // se utiliza para las conversiones de tipos (char, int y float)
    public static boolean numerico(Tipo p) {
        if (p == Tipo.Char || p == Tipo.Int || p == Tipo.Float) {
            return true;
        } else {
            return false;
        }
    }

    public static Tipo max(Tipo p1, Tipo p2) {
        if (!numerico(p1) || !numerico(p2)) {
            return null;
        } else if (p1 == Tipo.Float || p2 == Tipo.Float) {
            return Tipo.Float;
        } else if (p1 == Tipo.Int || p2 == Tipo.Int) {
            return Tipo.Int;
        } else {
            return Tipo.Char;
        }
    }
}
