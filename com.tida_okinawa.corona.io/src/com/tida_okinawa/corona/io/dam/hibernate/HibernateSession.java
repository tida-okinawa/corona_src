package com.tida_okinawa.corona.io.dam.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Hibernateのセッションを開始するためのインスタンスを作成する。
 * ConfigurationやsessionFactoryは複数作成する意味が無いのでSingletonにて処理。
 * 
 * @author S.Minakata
 * 
 */
public class HibernateSession {

    // TODO 20131203 HibernateSessionの運用は、IoServiceの運用に切り替える予定。（最終的にはHibernateSessionはカットする予定）

    private static volatile HibernateSession instance;


    private HibernateSession() {
        serviceRegistry = new ServiceRegistryBuilder().applySettings(new Configuration().configure().getProperties()).buildServiceRegistry();
        sessionFactory = new Configuration().configure().buildSessionFactory(serviceRegistry);
    }

    private ServiceRegistry serviceRegistry;
    private SessionFactory sessionFactory;


    /**
     * 
     * @return
     */
    public static HibernateSession getInstance() {
        if (instance == null) {
            instance = new HibernateSession();
        }
        return instance;
    }


    /**
     * セッションオブジェクトを取得する。
     * トランザクション処理が必要な場合は{@link Session#beginTransaction()}を利用する。
     * 
     * @return
     */
    public Session getSession() {
        return sessionFactory.openSession();

    }

}
