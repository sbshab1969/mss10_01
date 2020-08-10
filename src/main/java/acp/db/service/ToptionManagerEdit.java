package acp.db.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.ToptionClass;
import acp.utils.DialogUtils;

public class ToptionManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(ToptionManagerEdit.class);

  private SessionFactory sessionFactory;

  private String path;
  private ArrayList<String> attrs;
  private int attrSize;
  private int attrMax = 5;
  private String attrPrefix;

  public ToptionManagerEdit(String path, ArrayList<String> attrs) {
    sessionFactory = DbConnect.getSessionFactory();

    this.path = path;
    this.attrs = attrs;
    this.attrSize = attrs.size();
    String[] pathArray = path.split("/");
    this.attrPrefix = pathArray[pathArray.length - 1];
  }

  public String getPath() {
    return path;
  }

  public ArrayList<String> getAttrs() {
    return attrs;
  }

  public int getAttrSize() {
    return attrSize;
  }

  public int getAttrMax() {
    return attrMax;
  }

  public String getAttrPrefix() {
    return attrPrefix;
  }

  public ToptionClass select(Long objId) {
    // ------------------------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("select t.* from table(mss.spr_option_id(?,?,?,?,?,?,?)) t");
    String strQuery = sbQuery.toString();
    // ------------------------------------------------------
    ToptionClass toptObj = null;
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    int j = 0;
    try {
      // --------------------
      Query query = session.createSQLQuery(strQuery);
      // --------------------
      // j++;
      query.setLong(j, objId);
      j++;
      query.setString(j, path);
      // ----------------------------------
      for (int i = 0; i < attrSize; i++) {
        j++;
        query.setString(j, attrs.get(i));
      }
      for (int i = attrSize; i < attrMax; i++) {
        j++;
        query.setString(j, "");
      }
      logger.info("\nQuery string: " + query.getQueryString());
      // --------------------
      Object[] obj = (Object[]) query.uniqueResult();
      toptObj = getObject(obj);       
      // --------------------
      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
      DialogUtils.errorPrint(e,logger);
    } finally {
      session.close();
    }  
    // ------------------------------------------------------
    return toptObj;
  }

  private ToptionClass getObject(Object[] obj) {
    //---------------------------------------
    BigDecimal idBigDec = (BigDecimal) obj[0];
    Long rsId = idBigDec.longValue();
    // ----------------------
    int j = 0;
    ArrayList<String> pArr = new ArrayList<>();
    for (int i = 0; i < attrSize; i++) {
      j = i + 1;
      String pj = (String) obj[j];
      pArr.add(pj);
    }
    // ----------------------
    Date rsDateBegin = (Date) obj[++j];
    Date rsDateEnd = (Date) obj[++j];
    //---------------------------------------
    ToptionClass newObj = new ToptionClass();
    newObj.setId(rsId);
    newObj.setArrayP(pArr);
    newObj.setDateBegin(rsDateBegin);
    newObj.setDateEnd(rsDateEnd);
    //---------------------------------------
    return newObj;
  }

  public boolean updatePar(ToptionClass objOld, ToptionClass objNew) {
    boolean res = false;
    Long objId = objOld.getId();
    ArrayList<String> recOldValue = objOld.getPArray();
    ArrayList<String> recNewValue = objNew.getPArray();
    // -----------------------------------------
    String where = "";
    for (int i = 0; i < attrSize; i++) {
      if (recOldValue.get(i) != null) {
        where += "[@" + attrs.get(i) + "=\"" + recOldValue.get(i) + "\"]";
      }
    }
    // -----------------------------------------
    StringBuilder sbQuery = new StringBuilder();
    sbQuery.append("update mss_options set ");
    sbQuery.append("msso_config=updateXml(msso_config");
    for (int i = 0; i < attrSize; i++) {
      if (!recNewValue.get(i).equals("")) {
        String param = path + where + "/@" + attrs.get(i);
        sbQuery.append(",'" + param + "',?");
      }
    }
    sbQuery.append(") where msso_id=?");
    String strQuery = sbQuery.toString();
    // -----------------------------------------
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      SQLQuery query = session.createSQLQuery(strQuery);
      int j = 0;
      for (int i = 0; i < attrSize; i++) {
        if (!recNewValue.get(i).equals("")) {
//          query.setString(++j, recNewValue.get(i));
          query.setString(j++, recNewValue.get(i));
        }
      }
      query.setLong(j, objId);
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

}
