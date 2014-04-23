/**
 * @version $Id: ServerController.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/21 20:00:08
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import java.io.IOException;

import com.tida_okinawa.corona.correction.morphem.preference.MorphemePreference;


/**
 * サーバーでJuman, Knp を実行、停止
 * 
 * @author imai
 * 
 */
abstract public class ServerController {

    final SshClient[] clients;


    public ServerController() {
        String username = MorphemePreference.getServerUsername();
        String passwd = MorphemePreference.getServerPasswd();
        String[] hostnames = getHostNames();
        clients = new SshClient[hostnames.length];
        for (int i = 0; i < hostnames.length; i++) {
            try {
                clients[i] = new SshClient(hostnames[i], username, passwd);
            } catch (IOException e) {
                // TODO: メッセージ表示
                System.err.println(e);
            }
        }
    }


    abstract protected String[] getHostNames();


    abstract protected String getStartCmd();


    abstract protected String getStopCmd();


    abstract protected String getRestartCmd();


    public void start() {
        for (SshClient client : clients) {
            try {
                client.execCmd(getStartCmd());
            } catch (IOException e) {
                // TODO: メッセージ表示
                System.err.println(e);
            }
        }
    }


    public void stop() {
        for (SshClient client : clients) {
            try {
                client.execCmd(getStopCmd());
            } catch (IOException e) {
                // TODO: メッセージ表示
                System.err.println(e);
            }
        }
    }


    public void restart() {
        for (SshClient client : clients) {
            try {
                client.execCmd(getRestartCmd());
            } catch (IOException e) {
                // TODO: メッセージ表示
                System.err.println(e);
            }
        }
    }


    /**
     * ファイルをサーバーへコピーする
     * 
     * @param localFiles
     * @param remoteDir
     */
    public void upload(String[] localFiles, String remoteDir) {
        for (SshClient client : clients) {
            try {
                client.upload(localFiles, remoteDir);
            } catch (IOException e) {
                // TODO: メッセージ表示
                System.err.println(e);
            }
        }

    }
}
