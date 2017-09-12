package com.qq.vip.singleangel.wifi_direct_demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by singl on 2017/8/23.
 */

public class ClientList implements Serializable {
    public List<String> clientIps;

    public ClientList(){
        clientIps = new ArrayList<String>();
    }
    public ClientList(List<String> list){
        this.clientIps = list;
    }

    public void addClient(String value){
        int size = clientIps.size();
        if (size > 0 ){
            if (!isIn(value))
                clientIps.add(value);
        }else {
            clientIps.add(value);
        }
    }

    public int size(){
        return clientIps.size();
    }

    public boolean isIn(String value){
        for (String string : clientIps){
            if (string.equals(value)){
                return true;
            }
        }
        return false;
    }

    public List<String> getRestClient(String string){
        List<String> restClient = new ArrayList<String>();
        for (String value : clientIps){
            if (!value.equals(string)){
                restClient.add(value);
            }
        }
        return restClient;
    }

    public List<String> getClientIps(){
        return clientIps;
    }

    public String toString(){
        String string = "size is zero";
        if (clientIps.size() == 0){
            return string;
        }else {
            for (String s : clientIps){
                string = string + "  " + s;
            }
            return string;
        }
    }
}
