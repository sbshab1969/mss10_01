package acp.db.service;

import org.hibernate.HibernateException;
//import org.hibernate.Query;
//import org.hibernate.SQLQuery;
//import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.ConstClass;
//import acp.db.utils.DbUtils;
import acp.utils.DialogUtils;

public class ConstManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(ConstManagerEdit.class);

  private SessionFactory sessionFactory;

  public ConstManagerEdit() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public ConstClass select(Long objId) {
    ConstClass obj = null; 
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ----------------------------------------
      obj = session.get(ConstClass.class, objId);
      // ----------------------------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      obj = null;
    } finally {
      session.close();
    }  
    return obj;
  }

  public Long insert(ConstClass newObj) {
    Long objId = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // Insert описан в XML -------------
      // session.save(newObj);
      objId = (Long) session.save(newObj);
      // ---------------------------------
      tx.commit();
      // objId = newObj.getId();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      objId = null;
    } finally {
      session.close();
    }  
    return objId;
  }
  
/*
  public Long insert2(ConstClass newObj) {
    Long objId = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      ConstClass obj = new ConstClass();
      obj.setName(newObj.getName().toUpperCase());
      obj.setValue(newObj.getValue());
      // ---------------------------------
      objId = (Long) session.save(obj);
      // ---------------------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      objId = null;
    } finally {
      session.close();
    }  
    return objId;
  }
 
  public Long insert3(ConstClass newObj) {
    Long objId = DbUtils.getValueL("select mssc_seq.nextval from dual");
    // ---------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("insert into mss_const ");
    sbQuery.append("(mssc_id, mssc_name, mssc_value) ");
    sbQuery.append("values (:id, upper(:name), :value)");
    String strQuery = sbQuery.toString();
    // ---------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SQLQuery query = session.createSQLQuery(strQuery);
      query.setLong("id", objId);
      query.setString("name", newObj.getName());
      query.setString("value", newObj.getValue());
      logger.info("\nQuery string: " + query.getQueryString());
      // --------------------
      query.executeUpdate();
      // --------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      objId = null;
    } finally {
      session.close();
    }  
    return objId;
  }
*/  
  
  public boolean update(ConstClass newObj) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // --- Update описан в XML ---
      session.update(newObj);
      // ---------------------------
      tx.commit();
      res = true;
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      res = false;
    } finally {
      session.close();
    }  
    return res;
  }

/*
  public boolean update2(ConstClass newObj) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      ConstClass obj = session.get(ConstClass.class, newObj.getId());
      obj.setName(newObj.getName().toUpperCase());
      obj.setValue(newObj.getValue());
      // --------------------
      session.update(obj);
      // --------------------
      tx.commit();
      res = true;
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      res = false;
    } finally {
      session.close();
    }  
    return res;
  }

  public boolean update3(ConstClass newObj) {
    boolean res = false;
    // --------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("update ConstClass");
    sbQuery.append(" set name=upper(:name)");
    sbQuery.append(", value=:value");
    sbQuery.append(" where id=:id");
    String strQuery = sbQuery.toString();
    // --------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // --------------------
      Query query = session.createQuery(strQuery);
      query.setLong("id",newObj.getId());
      query.setString("name",newObj.getName());
      query.setString("value",newObj.getValue());
      logger.info("\nQuery string: " + query.getQueryString());
      // --------------------
      query.executeUpdate();
      // --------------------
      tx.commit();
      res = true;
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      res = false;
    } finally {
      session.close();
    }  
    return res;
  }
*/
  
  public boolean delete(Long objId) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ---------------------------------------------------
      session.delete(session.load(ConstClass.class, objId));
      // ---------------------------------------------------
      tx.commit();
      res = true;
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      res = false;
    } finally {
      session.close();
    }  
    return res;
  }

}
