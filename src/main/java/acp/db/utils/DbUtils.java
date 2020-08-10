package acp.db.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.hibernate.SessionFactory;

import acp.db.DbConnect;
import acp.utils.*;

public class DbUtils {

  private static SessionFactory sessionFactory = DbConnect.getSessionFactory();;

  public static String getValueV(String strQuery) {
    String val = "";
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createSQLQuery(strQuery);
      // ----------------------------------
      val = (String) query.uniqueResult();
      // ----------------------------------
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e);
      val = "";
    }
    return val;
  }

  public static Long getValueL(String strQuery) {
    Long val = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createSQLQuery(strQuery);
      // ----------------------------------
      val = (Long) query.uniqueResult();
      // ----------------------------------
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e);
      val = null;
    }
    return val;
  }

  public static Timestamp getValueT(String strQuery) {
    Timestamp val = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createSQLQuery(strQuery);
      // ----------------------------------
      val = (Timestamp) query.uniqueResult();
      // ----------------------------------
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e);
      val = null;
    }
    return val;
  }

  public static String getUser() {
    String usr = getValueV("select user from dual");
    return usr;
  }

  public static Timestamp getSysdate() {
    Timestamp tst = getValueT("select sysdate from dual");
    return tst;
  }

  public static List<String[]> getListString(String strQuery) {
    ArrayList<String[]> cache = new ArrayList<>();
    List<?> objList =  null;
    int cntCols = 2; 
    // --------------------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createSQLQuery(strQuery);
      objList = query.list();
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e);
    }
    // -------------------------------------------
    for (int i=0; i < objList.size(); i++) {
      Object[] objArr =  (Object[]) objList.get(i);
      String[] record = new String[cntCols];
      for (int j = 0; j < cntCols; j++) {
        record[j] = objArr[j].toString();
      }
      cache.add(record);
    }
    // -------------------------------------------
    return cache;
  }

}
