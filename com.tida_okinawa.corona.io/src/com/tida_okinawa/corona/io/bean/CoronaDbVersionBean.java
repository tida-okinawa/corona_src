package com.tida_okinawa.corona.io.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Coronaデータベースバージョン管理
 * 
 * @author kaori-jiroku
 * 
 */
@Entity
@Table(name = "corona_dbversion")
public class CoronaDbVersionBean implements Serializable {


    /**
	 * 
	 */
    private static final long serialVersionUID = -7014091851697483148L;

    private String version; // バージョン


    /**
     * @return バージョン
     */
    @Id
    @Column(name = "version", nullable = false, columnDefinition = "varchar(10)")
    public String getVersion() {
        return version;
    }


    /**
     * @param version
     *            バージョン
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
