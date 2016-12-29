package example_code;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.List;

public class Example {

    public static void main(String[] args) {
        try {
            Context ctxt =  new InitialContext();
            try {
                Object payment = ctxt.lookup("shopping/payment");
                System.out.println("Payment method is: "+payment);
                List list = (List) ctxt.lookup("shopping/item");
                System.out.println("Items are: "+list);
            } catch(ClassCastException cce) {
                System.err.println("List not created, instead made: "+ctxt.lookup("shopping/item"));
            }
        } catch(NamingException ne) {
            System.err.println("NamingException: "+ne.getMessage());
        }
    }

}
