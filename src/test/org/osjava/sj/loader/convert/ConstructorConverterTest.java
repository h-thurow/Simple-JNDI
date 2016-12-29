package org.osjava.sj.loader.convert;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Properties;

public class ConstructorConverterTest {
    @Test
    public void convertSingleValueAttribute() throws Exception {
        final ConstructorConverter converter = new ConstructorConverter();
        Properties props = new Properties();
        props.setProperty("", "1");
        String type = "java.lang.Integer";
        converter.convert(props, type);
    }
/*
properties = {Properties@863}  size = 2
 0 = {Hashtable$Entry@881} "type" -> "java.lang.Integer"
  key = "type"
  value = "java.lang.Integer"
 1 = {Hashtable$Entry@882} "" -> " size = 3"
  key = ""
  value = {LinkedList@886}  size = 3
   0 = "24"
   1 = "25"
   2 = "99"
 */
    @Test(expected = java.lang.RuntimeException.class)
    public void convertMultiValueAttribute() throws Exception {
        final ConstructorConverter converter = new ConstructorConverter();
        Properties props = new Properties();
        final LinkedList<Integer> integers = new LinkedList<Integer>();
        integers.add(24);
        integers.add(25);
        integers.add(99);
        props.put("", integers);
        String type = "java.lang.Integer";
        converter.convert(props, type);
    }

}