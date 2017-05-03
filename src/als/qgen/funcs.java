/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package als.qgen;

import java.util.prefs.Preferences;


public class funcs {
    String PREF_NAME;
    Preferences prefs;
    
    public funcs(){
    PREF_NAME = "qgen_sets";   
    prefs = Preferences.userNodeForPackage(als.qgen.qGenGUI.class);
    }
    
    public void setSetting(String set, String value){
        prefs.put(set, value);
    }
    public String getSetting(String set, String defaultval){
        return prefs.get(set, defaultval);
    }
    
}
