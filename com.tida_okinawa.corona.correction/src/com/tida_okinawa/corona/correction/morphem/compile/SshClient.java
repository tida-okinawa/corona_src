/**
 * @version $Id: SshClient.java 1839 2014-04-16 02:33:51Z yukihiro-kinjyo $
 * 
 * 2013/01/21 20:00:08
 * @author yoshikazu-imai
 * 
 * Copyright 2011-2014 TIDAコンソーシアム  All Rights Reserved.
 */
package com.tida_okinawa.corona.correction.morphem.compile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * サーバーに rsh でコマンドを実行
 * 
 * @author imai
 * 
 */
public class SshClient {
    final String hostname;
    final String username;
    final String password;

    final Connection conn;


    /**
     * コンストラクタ
     * 
     * @param hostname
     * @param username
     * @param password
     */
    public SshClient(String hostname, String username, String password) throws IOException {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.conn = new Connection(hostname);
        conn.connect();

        if (!conn.authenticateWithPassword(username, password)) {
            throw new IOException("接続失敗:" + hostname);
        }
    }


    /**
     * 複数のコマンドを実行
     * 
     * @param commands
     * @return
     * @throws IOException
     */
    public String[] execCmds(String[] commands) throws IOException {
        Session sess = conn.openSession();
        String[] result = new String[commands.length];

        for (int i = 0; i < commands.length; i++) {
            result[i] = execCmd_(sess, commands[i]);
        }

        sess.close();
        return result;
    }


    /**
     * コマンド実行
     * 
     * @param command
     * @return
     * @throws IOException
     */
    public String execCmd(String command) throws IOException {
        Session sess = conn.openSession();
        String r = execCmd_(sess, command);
        sess.close();

        return r;
    }


    private static String execCmd_(Session sess, String command) throws IOException {
        sess.execCommand(command);
        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

        StringBuilder sb = new StringBuilder();

        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            sb.append(line + "\n");
        }

        return sb.toString();
    }


    /**
     * ファイルアップロード
     * 
     * @param localFile
     * @param remoteDir
     * @throws IOException
     */
    public void upload(String localFile, String remoteDir) throws IOException {
        SCPClient scp = new SCPClient(conn);
        scp.put(localFile, remoteDir);
    }


    /**
     * 複数ファイルのアップロード
     * 
     * @param localFiles
     * @param remoteDir
     * @throws IOException
     */
    public void upload(String localFiles[], String remoteDir) throws IOException {
        SCPClient scp = new SCPClient(conn);
        for (String localFile : localFiles) {
            scp.put(localFile, remoteDir);
        }
    }


    @Override
    protected void finalize() {
        conn.close();
    }


    static public void main(String[] args) throws IOException {
        SshClient client = new SshClient("192.168.20.23", "imai", "imai79");
        client.upload("c:\\temp\\j.txt", "/home/imai");
        String r = client.execCmd("ls -l j.txt");
        System.out.println(r);
    }
}
