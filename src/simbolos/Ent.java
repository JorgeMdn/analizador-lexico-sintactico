/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simbolos;

import java.util.*;
import analizadorLexico.*;
import inter.*;
import simbolos.*;

/**
 *
 * @author jarvis
 */
public class Ent {

    private Hashtable tabla;
    protected Ent ant;

    public Ent(Ent n) {
        tabla = new Hashtable();
        ant = n;
    }

    public void put(Token w, Id i) {
        tabla.put(w, i);
    }

    public Id get(Token w) {
        for (Ent e = this; e != null; e = e.ant) {
            Id encontro = (Id) (e.tabla.get(w));
            if (encontro != null) {
                return encontro;
            }
        }
        return null;
    }
}
