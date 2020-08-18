package uk.ac.man.cs.util;

import java.util.Set;
import java.util.*;

public class StringMangler {

    //private String string;
    //private Set<String> strings;

    public StringMangler(){
        ; 
    }

    //public StringMangler(String s){
    //    this.string = s; 
    //}

    //public StringMangler(Set<String> ss){
    //    this.strings = ss; 
    //}

    //ERROR handling missing ...
    public String flattenLineBreaks(String s){
        return s.replace("\n"," ").replace("\r"," "); 
    }

    public Set<String> flattenLineBreaks(Set<String> strings){
        Set<String> result = new HashSet<>();
        for (String s : strings) {
            result.add(this.flattenLineBreaks(s)); 
        }
        return result; 
    } 
}
