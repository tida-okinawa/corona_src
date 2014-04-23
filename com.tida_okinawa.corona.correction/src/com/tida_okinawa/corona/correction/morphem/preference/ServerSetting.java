package com.tida_okinawa.corona.correction.morphem.preference;

import java.util.Set;
import java.util.TreeSet;

/**
 * Juman/KNP実行に関する設定
 * <p>
 * Juman/KNPのサーバー/クライアントモードに対応するために
 * <p>
 * プリファレンスから、プログラムのパスとサーバー設定（アドレス、ポート番号、プロセス数）を参照する
 * 
 * @author imai
 * 
 */
public class ServerSetting {

    ServerParam[] serverParams;


    /**
     * サーバーモード
     * 
     * @param programPath
     * @param opt
     * @param serverConf
     */
    public ServerSetting(String serverConf) {
        String[] lines = serverConf.split("\n");
        serverParams = new ServerParam[lines.length];
        for (int i = 0; i < lines.length; i++) {
            serverParams[i] = new ServerParam(lines[i]);
        }
    }


    /**
     * サーバー名の一覧
     * <p>
     * 同じホスト名の重複は省いている
     * 
     * @return
     */
    public String[] getHostNames() {
        Set<String> hosts = new TreeSet<String>();
        for (ServerParam param : serverParams) {
            hosts.add(param.host);
        }
        return hosts.toArray(new String[hosts.size()]);
    }


    /**
     * サーバーの設定の数
     * 
     * @return
     */
    public int getNumOfServers() {
        return serverParams.length;
    }


    /**
     * サーバーごとの実行の引数
     * 
     * @param サーバー番号
     * @return
     */
    public String[] getServerOpts(int serverId) {
        int n = serverId % serverParams.length;
        return serverParams[n].createOpts();
    }


    static class ServerParam {
        final String host;
        final int port;


        /**
         * 
         * <p>
         * host
         * <p>
         * host:port
         * 
         * @param conf
         */
        ServerParam(String conf) {
            try {
                int pos = conf.indexOf(":");
                if (pos == -1) {
                    this.port = -1;
                    this.host = conf.trim();
                } else {
                    this.host = conf.substring(0, pos).trim();
                    String sport = conf.substring(pos + 1).trim();
                    this.port = sport.isEmpty() ? -1 : Integer.parseInt(sport);
                }
            } catch (Exception e) {
                throw new RuntimeException("記述エラー: " + conf, e);
            }
        }


        /**
         * juman, knp のサーバー指定のオプション
         * <p>
         * -C サーバーホスト名
         * <p>
         * -C サーバーホスト名: ポート番号
         * 
         * @return
         */
        String[] createOpts() {
            if (port > 0) {
                return new String[] { "-C", host + ":" + port };
            } else {
                return new String[] { "-C", host };
            }
        }
    }

}
