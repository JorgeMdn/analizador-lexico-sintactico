/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter;
import analizadorLexico.*;
import simbolos.*;

/**
 *
 * @author jarvis
 */
public class Id extends Expr {
    public int desplazamiento;
    public Id(Palabra id, Tipo p, int b) {
        super(id,p); 
        desplazamiento = b;
    }
}
