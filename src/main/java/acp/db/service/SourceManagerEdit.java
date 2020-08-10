package acp.db.service;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.SourceClass;
import acp.db.utils.DbUtils;
import acp.utils.DialogUtils;

public class SourceManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(SourceManagerEdit.class);

  private SessionFactory sessionFactory;

  public SourceManagerEdit() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public List<String[]> getSources() {
    String strQuery = "select msss_id, msss_name from mss_source order by msss_name";
    List<String[]> arrayString = DbUtils.getListString(strQuery);
    return arrayString;
  }
  
  public SourceClass select(Long objId) {
    SourceClass obj = null; 
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ----------------------------------------
      obj = session.get(SourceClass.class, objId);
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

  public Long insert(SourceClass newObj) {
    Long objId = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SourceClass obj = new SourceClass();
      obj.setName(newObj.getName());
      obj.setDateCreate(DbUtils.getSysdate());
      obj.setDateModify(DbUtils.getSysdate());
      obj.setOwner(DbUtils.getUser());
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

  public boolean update(SourceClass newObj) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SourceClass obj = session.get(SourceClass.class, newObj.getId());
      obj.setName(newObj.getName());
      obj.setDateModify(DbUtils.getSysdate());
      obj.setOwner(DbUtils.getUser());
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

  public boolean delete(Long objId) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ---------------------------------------------------
      session.delete(session.load(SourceClass.class, objId));
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
