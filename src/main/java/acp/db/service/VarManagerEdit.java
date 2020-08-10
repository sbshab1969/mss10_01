package acp.db.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.VarClass;
import acp.db.utils.DbUtils;
import acp.utils.*;

public class VarManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(VarManagerEdit.class);

  private SessionFactory sessionFactory;

  public VarManagerEdit() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public VarClass select(Long objId) {
    VarClass obj = null; 
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ----------------------------------------
      obj = session.get(VarClass.class, objId);
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

  public Long insert(VarClass newObj) {
    Long objId = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      VarClass obj = new VarClass();
      obj.setName(newObj.getName().toUpperCase());
      obj.setType(newObj.getType());
      obj.setValuen(newObj.getValuen());
      obj.setValuev(newObj.getValuev());
      obj.setValued(newObj.getValued());
      obj.setLen(120);
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

  public boolean update(VarClass newObj) {
    boolean res = false;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      VarClass obj = session.get(VarClass.class, newObj.getId());
      obj.setName(newObj.getName().toUpperCase());
      obj.setType(newObj.getType());
      obj.setValuen(newObj.getValuen());
      obj.setValuev(newObj.getValuev());
      obj.setValued(newObj.getValued());
      obj.setLen(120);
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
      session.delete(session.load(VarClass.class, objId));
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

  @SuppressWarnings("unchecked")
  public void fillVars(Map<String, String> varMap) {
    // ------------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("from VarClass");
    sbQuery.append(" where upper(name) like 'CERT%'");
    sbQuery.append(" or upper(name) = 'VERSION_MSS' order by name");
    String strQuery = sbQuery.toString();
    // ------------------------------------------------------
    List<VarClass> objList = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      Query query = session.createQuery(strQuery);
      objList = query.list();
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
    } finally {
      session.close();
    }  
    // ---------------------------------------------
    for (VarClass vcls : objList) {
      String rsqName = vcls.getName().toUpperCase();
      String valuev = null;
      Date valued = null;
      if (rsqName.startsWith("CERT")) {
        valuev = vcls.getValuev();
        varMap.put(rsqName, valuev);
      } else if (rsqName.equals("VERSION_MSS")) {
        valuev = vcls.getValuev();
        valued = vcls.getValued();
        varMap.put("VERSION", valuev);
        varMap.put("VERSION_DATE", StrUtils.date2Str(valued));
      }
    }
    // ---------------------------------------------
  }
  
}
