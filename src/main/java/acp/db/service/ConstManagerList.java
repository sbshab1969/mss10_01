package acp.db.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Transaction;

import acp.db.domain.*;
import acp.db.utils.*;
import acp.utils.*;

public class ConstManagerList extends ManagerList {
  private static Logger logger = LoggerFactory.getLogger(ConstManagerList.class);

  protected List<ConstClass> cacheObj = new ArrayList<>();
  private int modeQuery = 0;

  public ConstManagerList() {
    headers = new String[] {
        "ID"
      , Messages.getString("Column.Name")
      , Messages.getString("Column.Value") 
    };
    types = new Class<?>[] { 
        Long.class
      , String.class
      , String.class
    };
    cntColumns = headers.length;
    
    fields = new String[] { "id", "name", "value" };
    if (modeQuery == 0) {
      strFields = null;
    } else {
      strFields = StrSqlUtils.buildSelectFields(fields, null);
    }

    tableName = "ConstClass";
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
  public List<ConstClass> queryAll() {
    cacheObj = fetchPage(-1,-1);
    return cacheObj;    
  }

  @Override
  public List<ConstClass> fetchPage(int startPos, int cntRows) {
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
      cacheObj = (List<ConstClass>) objList;
    } else {  
      cacheObj = new ArrayList<>();
      for (int i=0; i < objList.size(); i++) {
        Object[] obj = (Object[]) objList.get(i);
        cacheObj.add(getObject(obj));
      }
    }   
  }
  
  private ConstClass getObject(Object[] obj) {
    //---------------------------------------
    Long rsId = (Long) obj[0];
    String rsName = (String) obj[1];
    String rsValue = (String) obj[2];
    //---------------------------------------
    ConstClass newObj = new ConstClass();
    newObj.setId(rsId);
    newObj.setName(rsName);
    newObj.setValue(rsValue);
    //---------------------------------------
    return newObj;
  }

}
