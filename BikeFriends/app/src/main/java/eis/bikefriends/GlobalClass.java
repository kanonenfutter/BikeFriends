package eis.bikefriends;


public class GlobalClass {

    private static GlobalClass instance;

    public static GlobalClass getInstance() {
        if (instance == null)
            instance = new GlobalClass();
        return instance;
    }

    private GlobalClass(){
    }
    private static String ipAddresse = "http://10.3.200.22:3000";


    public String getIpAddresse(){
        return ipAddresse;
    }
}
