/**
 * @version $Id: ConnectionParameter.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2011/12/21 15:11:22
 * @author shingo-takahashi
 * 
 * Copyright 2011-2014 TIDAコンソーシアム All Rights Reserved.
 */
package com.tida_okinawa.corona.ui.data;

/**
 * @author shingo-takahashi
 */
public class ConnectionParameter implements Cloneable {
    public String name;
    public String url;
    public String user;
    public String passwd;


    public ConnectionParameter(String name, String url, String user, String passwd) {
        this.name = name;
        this.url = url;
        this.user = user;
        this.passwd = passwd;
    }


    @Override
    public Object clone() {
        return new ConnectionParameter(name, url, user, passwd);
    }


    public void copy(Object obj) {
        ConnectionParameter cp = (ConnectionParameter) obj;
        this.name = cp.name;
        this.url = cp.url;
        this.user = cp.user;
        this.passwd = cp.passwd;
    }
}
