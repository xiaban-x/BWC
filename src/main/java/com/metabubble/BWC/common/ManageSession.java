package com.metabubble.BWC.common;

import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ManageSession {

    Map<String, HttpSession> manageSession=new HashMap<>();

     //setter
     public Map<String, HttpSession> getManageSession() {
            return manageSession;
     }
    //getter
    public void setManageSession(Map<String, HttpSession> manageSession) {
              this.manageSession = manageSession;
     }
}
