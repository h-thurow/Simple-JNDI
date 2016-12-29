package example_code;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

public class Example {

    public static void main(String[] args) {
        try {
            Context ctxt =  new InitialContext();
            try {
                DataSource ds = (DataSource) ctxt.lookup("FlamefewDS");
                System.out.println("Found FlamefewDS: "+ds);
            } catch(ClassCastException cce) {
                System.err.println("DataSource not created, instead made: "+ctxt.lookup("java:/FlamefewDS"));
            }
            try {
                DataSource ds = (DataSource) ctxt.lookup("java:/FlamefewDS");
                System.out.println("Found java:/FlamefewDS: "+ds);
            } catch(ClassCastException cce) {
                System.err.println("DataSource not created, instead made: "+ctxt.lookup("java:/FlamefewDS"));
            }
        } catch(NamingException ne) {
            System.err.println("NamingException: "+ne.getMessage());
        }
    }

}
