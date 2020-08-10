package acp.db.service;

import java.sql.Timestamp;
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

public class FileOtherManagerList extends ManagerList {
  private static Logger logger = LoggerFactory.getLogger(FileOtherManagerList.class);

  protected List<FileOtherClass> cacheObj = new ArrayList<>();
  private int modeQuery = 1;
  private Long fileId;

  public FileOtherManagerList(Long file_id) {
    headers = new String[] { "ID"
      , Messages.getString("Column.Time")
      , Messages.getString("Column.Desc") 
    };
    types = new Class<?>[] { 
        Long.class
      , Timestamp.class
      , String.class
    };
    cntColumns = headers.length;
    
    fileId = file_id;

    fields = new String[] { "id", "dateEvent", "descr" };
    if (modeQuery == 0) {
      strFields = null;
    } else {
      strFields = StrSqlUtils.buildSelectFields(fields, null);
    }

    tableName = "FileOtherClass";
    pkColumn = "id";
    strAwhere = "refId=" + fileId;
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
    strWhere = strAwhere;
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
  public List<FileOtherClass> queryAll() {
    cacheObj = fetchPage(-1,-1);
    return cacheObj;    
  }

  @Override
  public List<FileOtherClass> fetchPage(int startPos, int cntRows) {
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
      cacheObj = (List<FileOtherClass>) objList;
    } else {  
      cacheObj = new ArrayList<>();
      for (int i=0; i < objList.size(); i++) {
        Object[] obj = (Object[]) objList.get(i);
        cacheObj.add(getObject(obj));
      }
    }   
  }
  
  private FileOtherClass getObject(Object[] obj) {
    //---------------------------------------
    Long rsId = (Long) obj[0];
    Timestamp rsDateEvent = (Timestamp) obj[1];
    String rsDescr = (String) obj[2];
    //---------------------------------------
    FileOtherClass newObj = new FileOtherClass();
    newObj.setId(rsId);
    newObj.setDateEvent(rsDateEvent);
    newObj.setDescr(rsDescr);
    //---------------------------------------
    return newObj;
  }

}
