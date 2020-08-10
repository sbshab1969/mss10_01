package acp.db.service;

import java.sql.Timestamp;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.ConfigClass;
import acp.db.utils.*;
import acp.utils.*;

public class ConfigManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(ConfigManagerEdit.class);

  private SessionFactory sessionFactory;

  public ConfigManagerEdit() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public ConfigClass select(Long objId) {
    ConfigClass obj = null; 
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ----------------------------------------
      obj = session.get(ConfigClass.class, objId);
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

  public String getCfgName(Long objId) {
    // ------------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("select name from ConfigClass where id=:id");
    String strQuery = sbQuery.toString();
    // ------------------------------------------------------
    String configName = "";
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createQuery(strQuery);
      query.setLong("id",objId);
      logger.info("\nQuery string: " + query.getQueryString());
      // --------------------
      configName = (String) query.uniqueResult();
      // --------------------
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e,logger);
      configName = "";
    }
    // ------------------------------------------------------
    return configName;
  }

  public String getCfgStr(Long objId) {
    // ------------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("select configR from ConfigClass where id=:id");
    String strQuery = sbQuery.toString();
    // ------------------------------------------------------
    String configStr = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createQuery(strQuery);
      query.setLong("id",objId);
      logger.info("\nQuery string: " + query.getQueryString());
      // --------------------
      configStr = (String) query.uniqueResult();
      // --------------------
      tx.commit();
    } catch (HibernateException e) {
      DialogUtils.errorPrint(e,logger);
      configStr = null;
    }
    // ------------------------------------------------------
    return configStr;
  }
 
  public Long insert(ConfigClass newObj) {
    Long objId = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      String emptyXml = "<?xml version=\"1.0\"?><config><sverka.ats/></config>";
      Timestamp sysdt = DbUtils.getSysdate();
      String usr = DbUtils.getUser();
      newObj.setConfigW(emptyXml);
      newObj.setDateCreate(sysdt);
      newObj.setDateModify(sysdt);
      newObj.setOwner(usr);
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
  public Long insert2(ConfigClass newObj) {
    // ------------------------------------------------------
    Long objId = DbUtils.getValueL("select msso_seq.nextval from dual");
    // ------------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("insert into mss_options (");
    sbQuery.append("msso_id, msso_name, msso_config");
    sbQuery.append(",msso_dt_begin, msso_dt_end, msso_comment");
    sbQuery.append(",msso_dt_create, msso_dt_modify, msso_owner, msso_msss_id)");
    sbQuery.append(" values (:id, :name, :conf, :dtBegin, :dtEnd, :comment");  // OK
//    sbQuery.append(" values (:id, :name, XMLType(:conf), :dtBegin, :dtEnd, :comment");  // OK   
    sbQuery.append(", sysdate, sysdate, user, :source)");
    String strQuery = sbQuery.toString();
    // ------------------------------------------------------
    String emptyXml = "<?xml version=\"1.0\"?><config><sverka.ats/></config>";
    Timestamp tsBegin = StrSqlUtils.util2ts(newObj.getDateBegin());
    Timestamp tsEnd = StrSqlUtils.util2ts(newObj.getDateEnd());
    // ------------------------------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SQLQuery query = session.createSQLQuery(strQuery);
      query.setLong("id", objId);
      query.setString("name", newObj.getName());
      query.setString("conf", emptyXml);
      query.setTimestamp("dtBegin", tsBegin);
      query.setTimestamp("dtEnd", tsEnd);
      query.setString("comment", newObj.getComment());
      query.setLong("source", newObj.getSourceId());
      // --------------------------
      query.executeUpdate();
      // --------------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      objId = null;
    } finally {
      session.close();
    }  
    // -----------------------------------------------------
    return objId;
  }
  */

  public boolean update(ConfigClass newObj) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      ConfigClass obj = session.get(ConfigClass.class, newObj.getId());
      obj.setName(newObj.getName());
      // -----------
      obj.setConfigW(obj.getConfigR());  // !!!! Перезапись XMLType 
      // -----------
      obj.setDateBegin(newObj.getDateBegin());
      obj.setDateEnd(newObj.getDateEnd());
      obj.setComment(newObj.getComment());
      //obj.setDateCreate(newObj.getDateCreate());
      obj.setDateModify(DbUtils.getSysdate());
      obj.setOwner(DbUtils.getUser());
      obj.setSourceId(newObj.getSourceId());
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

  public boolean updateCfgStr(Long objId, String txtConf) {
    boolean res = false;
    // -----------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("update mss_options");
    //sbQuery.append(" set msso_config=:conf"); // OK
    sbQuery.append("   set msso_config=XMLType(:conf)");  // OK
    sbQuery.append(", msso_dt_modify=sysdate");
    sbQuery.append(", msso_owner=user");
    sbQuery.append(" where msso_id=:id");
    String strQuery = sbQuery.toString();
    // -----------------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SQLQuery query = session.createSQLQuery(strQuery);
      query.setLong("id", objId);
      query.setString("conf", txtConf);
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
    // -----------------------------------------------------
    return res;
  }

  public boolean delete(Long objId) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ---------------------------------------------------
      session.delete(session.load(ConfigClass.class, objId));
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
 
  public boolean copy(Long objId) {
    boolean res = false;
    // -----------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("insert into mss_options");
    sbQuery.append(" (select msso_seq.nextval, msso_name || '_copy'");
    sbQuery.append(", msso_config");
    sbQuery.append(", msso_dt_begin, msso_dt_end, msso_comment");
    sbQuery.append(", sysdate, sysdate, user, msso_msss_id");
    sbQuery.append(" from mss_options where msso_id=:id)");
    String strQuery = sbQuery.toString();
    // -----------------------------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SQLQuery query = session.createSQLQuery(strQuery);
      query.setLong("id", objId);
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
    // -----------------------------------------------------
    return res;
  }

  /*
  public boolean copy2(Long objId) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      ConfigClass obj = session.get(ConfigClass.class, objId);
      ConfigClass newObj = new ConfigClass();
      newObj.setName(obj.getName() + "_copy");
      newObj.setConfigW(obj.getConfigR());  // !!!!!!
      newObj.setDateBegin(obj.getDateBegin());
      newObj.setDateEnd(obj.getDateEnd());
      newObj.setComment(obj.getComment());
      newObj.setDateCreate(DbUtils.getSysdate());
      newObj.setDateModify(DbUtils.getSysdate());
      newObj.setOwner(DbUtils.getUser());
      newObj.setSourceId(obj.getSourceId());
      // --------------------
      session.save(newObj);
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

}
