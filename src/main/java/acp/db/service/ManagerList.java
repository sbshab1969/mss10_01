package acp.db.service;

import acp.db.DbConnect;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

public abstract class ManagerList {
  protected SessionFactory sessionFactory;

  protected String[] headers;
  protected Class<?>[] types;
  protected int cntColumns = 0;

  protected String[] fields;
  protected String strFields;

  protected String tableName;
  protected String pkColumn;
  protected String strAwhere;
  protected Long seqId = 0L;

  protected String strFrom;
  protected String strWhere;
  protected String strOrder;
  
  protected String strQuery;
  protected String strQueryCnt;

  public ManagerList() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public String[] getHeaders() {
    return headers;    
  }

  public Class<?>[] getTypes() {
    return types;    
  }

  public Long getSeqId() {
    return seqId;
  }

  protected abstract void prepareQuery(Map<String,String> mapFilter);
  protected abstract long countRecords();
  protected abstract List<?> queryAll();
  protected abstract List<?> fetchPage(int startPos, int cntRows);

  public void openQueryAll() {
  }  

  public void openQueryPage() {
  }  

  public void closeQuery() {
  }

}
