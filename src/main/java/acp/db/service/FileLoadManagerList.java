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

import acp.db.domain.FileLoadClass;
import acp.db.utils.*;
import acp.utils.*;

public class FileLoadManagerList extends ManagerList {
  private static Logger logger = LoggerFactory.getLogger(FileLoadManagerList.class);

  protected List<FileLoadClass> cacheObj = new ArrayList<>();
  private int modeQuery = 1;

  public FileLoadManagerList() {
    headers = new String[] { 
        "ID"
      , Messages.getString("Column.FileName")
      , "MD5"
      , Messages.getString("Column.Owner")
      , Messages.getString("Column.DateWork")
      , Messages.getString("Column.RecordCount") 
    };
    types = new Class<?>[] { 
        Long.class
      , String.class
      , String.class
      , String.class
      , Timestamp.class
      , int.class
    };
    cntColumns = headers.length;

    fields = new String[] { "id", "name", "md5", "owner", "dateWork", "recAll" };
    if (modeQuery == 0) {
      strFields = null;
    } else {
      strFields = StrSqlUtils.buildSelectFields(fields, null);
    }

    tableName = "FileLoadClass";
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
    String vFileName = mapFilter.get("file_name");
    String vOwner = mapFilter.get("owner");
    String vDtBegin = mapFilter.get("dt_begin");
    String vDtEnd = mapFilter.get("dt_end");
    String vRecBegin = mapFilter.get("rec_begin");
    String vRecEnd = mapFilter.get("rec_end");
    // ----------------------------------
    String phWhere = null;
    String str = null;
    // ---
    if (!StrUtils.emptyString(vFileName)) {
      str = "upper(name) like upper('" + vFileName + "%')";
      phWhere = StrSqlUtils.strAddAnd(phWhere, str);
    }
    // ---
    if (!StrUtils.emptyString(vOwner)) {
      str = "upper(owner) like upper('" + vOwner + "%')";
      phWhere = StrSqlUtils.strAddAnd(phWhere, str);
    }
    //---
    String vField = "";
    String valueBeg = "";
    String valueEnd = "";
    //---
    vField = "trunc(dateWork)";
    valueBeg = "to_date('" + vDtBegin + "','dd.mm.yyyy')";
    valueEnd = "to_date('" + vDtEnd + "','dd.mm.yyyy')";
    if (!StrUtils.emptyString(vDtBegin) || !StrUtils.emptyString(vDtEnd)) {
      if (!StrUtils.emptyString(vDtBegin) && !StrUtils.emptyString(vDtEnd)) {
        str = vField + " between " + valueBeg + " and " + valueEnd;
      } else if (!StrUtils.emptyString(vDtBegin) && StrUtils.emptyString(vDtEnd)) {
        str = vField + " >= " + valueBeg;
      } else if (StrUtils.emptyString(vDtBegin) && !StrUtils.emptyString(vDtEnd)) {
        str = vField + " <= " + valueEnd;
      }
      phWhere = StrSqlUtils.strAddAnd(phWhere, str);
    }
    //---
    vField = "recAll";
    valueBeg = vRecBegin;
    valueEnd = vRecEnd;
    if (!StrUtils.emptyString(vRecBegin) || !StrUtils.emptyString(vRecEnd)) {
      if (!StrUtils.emptyString(vRecBegin) && !StrUtils.emptyString(vRecEnd)) {
        str = vField + " between " + valueBeg + " and " + valueEnd;
      } else if (!StrUtils.emptyString(vRecBegin) && StrUtils.emptyString(vRecEnd)) {
        str = vField + " >= " + valueBeg;
      } else if (StrUtils.emptyString(vRecBegin) && !StrUtils.emptyString(vRecEnd)) {
        str = vField + " <= " + valueEnd;
      }
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
  public List<FileLoadClass> queryAll() {
    cacheObj = fetchPage(-1,-1);
    return cacheObj;    
  }

  @Override
  public List<FileLoadClass> fetchPage(int startPos, int cntRows) {
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
      cacheObj = (List<FileLoadClass>) objList;
    } else {  
      cacheObj = new ArrayList<>();
      for (int i=0; i < objList.size(); i++) {
        Object[] obj = (Object[]) objList.get(i);
        cacheObj.add(getObject(obj));
      }
    }   
  }
  
  private FileLoadClass getObject(Object[] obj) {
    //---------------------------------------
    Long rsId = (Long) obj[0];
    String rsName = (String) obj[1];
    String rsMd5 = (String) obj[2];
    String rsOwner = (String) obj[3];
    Timestamp rsDateWork = (Timestamp) obj[4];
    Integer rsRecAll = (Integer) obj[5];
    //---------------------------------------
    FileLoadClass newObj = new FileLoadClass();
    newObj.setId(rsId);
    newObj.setName(rsName);
    newObj.setMd5(rsMd5);
    newObj.setOwner(rsOwner);
    newObj.setDateWork(rsDateWork);
    newObj.setRecAll(rsRecAll);
    //---------------------------------------
    return newObj;
  }

}