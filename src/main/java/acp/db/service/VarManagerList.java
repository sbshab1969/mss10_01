package acp.db.service;

import java.util.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.domain.*;
import acp.db.utils.*;
import acp.utils.*;

public class VarManagerList extends ManagerList {
  private static Logger logger = LoggerFactory.getLogger(VarManagerList.class);

  protected List<VarClass> cacheObj = new ArrayList<>();
  private int modeQuery = 1;

  public VarManagerList() {
    headers = new String[] { 
        "ID"
      , Messages.getString("Column.Name")
      , Messages.getString("Column.Type")
      , Messages.getString("Column.Number")
      , Messages.getString("Column.Varchar")
      , Messages.getString("Column.Date") };
    types = new Class<?>[] { 
        Long.class
      , String.class
      , String.class
      , Double.class
      , String.class
      , Date.class
    };
    cntColumns = headers.length;

    fields = new String[] { "id", "name", "type", "valuen", "valuev", "valued" };
    if (modeQuery == 0) {
      strFields = null;
    } else {
      strFields = StrSqlUtils.buildSelectFields(fields, null);
    }

    tableName = "VarClass";
    pkColumn = "id";
    strAwhere = null;
    seqId = 1000L;

    strFrom = tableName;
    strWhere = strAwhere;
    strOrder = pkColumn;
    // ------------
    prepareQuery(null);
    // ------------
  }

  @Override
  public void prepareQuery(Map<String,String> mapFilter) {
    if (mapFilter != null) {
      setWhere(mapFilter);
    } else {
      strWhere = strAwhere;
    }
    strQuery = StrSqlUtils.buildQuery(strFields, strFrom, strWhere, strOrder);
    strQueryCnt = StrSqlUtils.buildQuery("select count(*) from " + strFrom, strWhere, null);
  }

  private void setWhere(Map<String,String> mapFilter) {
    // ----------------------------------
    String vName = mapFilter.get("name"); 
    // ----------------------------------
    String phWhere = null;
    String str = null;
    // ---
    if (!StrUtils.emptyString(vName)) {
      str = "upper(name) like upper('" + vName + "%')";
      phWhere = StrSqlUtils.strAddAnd(phWhere, str);
    }
    // ---
    strWhere = StrSqlUtils.strAddAnd(strAwhere, phWhere);
  }

  @Override
  public long countRecords() {
    Long cnt = -1L;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try { 
      // -----------------------------------------------------------------
      Query query = session.createQuery(strQueryCnt);
      cnt = (Long) query.uniqueResult();
      // -----------------------------------------------------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
      cnt = -1L;
    } finally {
      session.close();
    }  
    return cnt;    
  }

  @Override
  public List<VarClass> queryAll() {
    cacheObj = fetchPage(-1,-1);
    return cacheObj;    
  }

  @Override
  public List<VarClass> fetchPage(int startPos, int cntRows) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try { 
      // HQL -------------------------------------
      Query query = session.createQuery(strQuery);
      if (startPos>0) {
        query.setFirstResult(startPos-1);  // Hibernate начинает с 0
      }
      if (cntRows>0) {
        query.setMaxResults(cntRows);
      }  
      // ==============
      fillCache(query);
      // ==============
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
    } finally {
      session.close();
    }  
    return cacheObj;    
  }  

  @SuppressWarnings("unchecked")
  private void fillCache(Query query) {
    logger.info("\nQuery string: " + query.getQueryString());
    // ============================
    List<?> objList = query.list();
    // ============================
    if (modeQuery == 0) {
      cacheObj = (List<VarClass>) objList;
    } else {  
      cacheObj = new ArrayList<>();
      for (int i=0; i < objList.size(); i++) {
        Object[] obj = (Object[]) objList.get(i);
        cacheObj.add(getObject(obj));
      }
    }   
  }
  
  private VarClass getObject(Object[] obj) {
    //---------------------------------------
    Long rsId = (Long) obj[0];
    String rsName = (String) obj[1];
    String rsType = (String) obj[2];
    Double rsValuen = (Double) obj[3];
    String rsValuev = (String) obj[4];
    Date rsValued = (Date) obj[5];
    //---------------------------------------
    VarClass newObj = new VarClass();
    newObj.setId(rsId);
    newObj.setName(rsName);
    newObj.setType(rsType);
    newObj.setValuen(rsValuen);
    newObj.setValuev(rsValuev);
    newObj.setValued(rsValued);
    //---------------------------------------
    return newObj;
  }
  
}
