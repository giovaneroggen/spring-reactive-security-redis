package br.com.security;

public interface SecurityClient extends SecurityContract{

    static String url(){
        return "http://SECURITY-SERVICE";
    }
}
