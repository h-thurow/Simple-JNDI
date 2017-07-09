package org.osjava.sj;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Holger Thurow (thurow.h@gmail.com) on 22/12/2016.
 */
public class BeanWithSetterStringsOnly {
    private String firstName;
    private String surname;
    private String city;
    private List<String> languages = new ArrayList<String>();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

// Not working
//    public List<String> getLanguages() {
//        return languages;
//    }
//
//    public void setLanguages(List<String> languages) {
//        this.languages = languages;
//    }

    public void addLanguage(int i, String language) {
        getLanguages().add(i, language);
    }

    public List<String> getLanguages() {
        return languages;
    }
}
