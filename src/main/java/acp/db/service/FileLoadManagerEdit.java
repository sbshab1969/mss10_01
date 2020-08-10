package acp.db.service;

import java.util.ArrayList;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.db.DbConnect;
import acp.db.domain.*;
import acp.utils.DialogUtils;

public class FileLoadManagerEdit {
  private static Logger logger = LoggerFactory.getLogger(FileLoadManagerEdit.class);

  private SessionFactory sessionFactory;

  public FileLoadManagerEdit() {
    sessionFactory = DbConnect.getSessionFactory();
  }

  public FileLoadClass select(Long objId) {
    FileLoadClass obj = null; 
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    try {
      // ----------------------------------------
      Query query = session.createQuery("from FileLoadClass fl left outer join fetch fl.config cfg where fl.id=:id");
      query.setLong("id", objId);
      logger.info("\nQuery string: " + query.getQueryString());
      obj = (FileLoadClass) query.uniqueResult();
      // ----------------------------------------
      ArrayList<String> statList = new ArrayList<>();
      statList.add(String.valueOf(obj.getRecAll()));
      statList.add(String.valueOf(obj.getRecErr()));
      obj.setStatList(statList);
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

}
