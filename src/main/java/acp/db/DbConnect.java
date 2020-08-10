package acp.db;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
//import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jdbc.ReturningWork;
//import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnect {

  public final static String DB_PATH = "bin";

  public final static String DB_INDEX = "index";
  public final static String DB_NAME = "name";
  public final static String DB_USER = "hibernate.connection.username";
  public final static String DB_PASSWORD = "hibernate.connection.password";

  public final static String DB_CONN_STRING = "hibernate.connection.url";
  public final static String DB_DRIVER = "hibernate.connection.driver_class";

  private static Properties dbProp;
  private static Connection dbConn;
  private static SessionFactory sessionFactory;

  private static Logger logger = LoggerFactory.getLogger(DbConnect.class);

  public static Properties getParams() {
    return dbProp;
  }

  public static void setParams(Properties props) {
    dbProp = props;
  }

//  public static Connection getConnection() {
//    return dbConn;
//  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public static boolean testConnection() {
    if (sessionFactory != null) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean connect() {
    logger.info("connecting ...");
    // ---------------------------
    if (sessionFactory == null) {
      if (dbProp != null) {
        sessionFactory = buildSessionFactory(dbProp.getProperty(DB_NAME));
      } else {
        sessionFactory = buildSessionFactory();
      }
    }
    // ---------------------------
    if (sessionFactory != null) {
      dbConn = openConnection(sessionFactory);
    }
    // ---------------------------
    if (sessionFactory != null && dbConn != null) {
      return true;
    } else {
      return false;
    }
    // ---------------------------
  }

  public static boolean connectDefault() {
    boolean res = connect();
    return res;
  }

  public static boolean reconnect() {
    if (sessionFactory != null) {
      disconnect();
    }
    boolean res = connect();
    return res;
  }

  public static void disconnect() {
    logger.info("disconnecting ...");

    if (dbConn != null) {
      try {
        dbConn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      dbConn = null;
    }

    if (sessionFactory != null) {
      sessionFactory.close();
      sessionFactory = null;
    }
  }

  public static void readCfg(Properties props) {
    String fullFileName = props.getProperty(DB_NAME);
    // ------------------------------------------------------
    Configuration cnf = configure(fullFileName);
    // ------------------------------------------------------
    props.setProperty(DB_USER, cnf.getProperty(DB_USER));
    props.setProperty(DB_PASSWORD, cnf.getProperty(DB_PASSWORD));
    props.setProperty(DB_CONN_STRING,cnf.getProperty(DB_CONN_STRING));
    props.setProperty(DB_DRIVER,cnf.getProperty(DB_DRIVER));
    // ------------------------------------------------------
  }

  private static Configuration configure(String cfg) {
    // ---------------------------------------------------------------------
    Configuration conf = new Configuration();
    if (cfg != null) {
      conf.configure(cfg);
    } else {
      conf.configure();
    }
    // printCfg(cfg,conf);
    return conf;
  }

  @SuppressWarnings("unused")
  private static void printCfg(String cfg, Configuration conf) {
    System.out.println("");

    // logger.info("Configuration file = " + cfg);
    // logger.info(DB_CONN_STRING + " = " + conf.getProperty(DB_CONN_STRING));
    // logger.info(DB_DRIVER + " = " + conf.getProperty(DB_DRIVER));
    // logger.info(DB_USER + " = " + conf.getProperty(DB_USER));
    // logger.info(DB_PASSWORD + " = " + conf.getProperty(DB_PASSWORD));

    System.out.println("Configuration file = " + cfg);
    System.out.println(DB_CONN_STRING + " = " + conf.getProperty(DB_CONN_STRING));
    System.out.println(DB_DRIVER + " = " + conf.getProperty(DB_DRIVER));
    System.out.println(DB_USER + " = " + conf.getProperty(DB_USER));
    System.out.println(DB_PASSWORD + " = " + conf.getProperty(DB_PASSWORD));

    System.out.println("");
  }

  private static SessionFactory buildSessionFactory(String cfg) {
    Configuration conf = configure(cfg);
    // ----------------------------------------------
    if (dbProp != null) {
      conf.setProperty(DB_USER, dbProp.getProperty(DB_USER));
      conf.setProperty(DB_PASSWORD, dbProp.getProperty(DB_PASSWORD));
    }
    // ----------------------------------------------
    try {
      SessionFactory sf = conf.buildSessionFactory();
      return sf;
    } catch (Throwable e) {
      logger.error(e.getMessage());
      return null;
    }
    // ----------------------------------------------
  }

  private static SessionFactory buildSessionFactory() {
    return buildSessionFactory(null);
  }

  public static Connection openConnection(SessionFactory sFactory) {
    // SessionFactoryImplementor sessionFactoryImpl =
    // (SessionFactoryImplementor) sFactory;
    // ConnectionProvider connectionProvider =
    // sessionFactoryImpl.getConnectionProvider();

    ConnectionProvider connectionProvider = sFactory.getSessionFactoryOptions().getServiceRegistry()
        .getService(ConnectionProvider.class);

    Connection conn = null;
    try {
      conn = connectionProvider.getConnection();
    } catch (SQLException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    return conn;
  }

  public static Connection openConnection(Session sess) {
    // Connection conn = sess.connection();
    // ------------------------------------------
    SessionImpl sessionImpl = (SessionImpl) sess;
    Connection conn = sessionImpl.connection();
    // ------------------------------------------
    return conn;
  }

  public static Connection openConnectionDoWork(Session sess) {
    // sess.doWork(new Work() {
    // @Override
    // public void execute(Connection p_conn) throws SQLException {
    //   System.out.println("p_conn: " + p_conn);
    // }
    // });

    Connection conn = sess.doReturningWork(new ReturningWork<Connection>() {
      @Override
      public Connection execute(Connection p_conn) throws SQLException {
        return p_conn;
      }
    });
    return conn;
  }

  public static String[] getFileList() {
    File path = new File(DB_PATH);
    // String[] list = path.list();
    String[] list = path.list(filter(".*\\.cfg.xml"));
    Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
    return list;
  }

  public static FilenameFilter filter(final String regex) {
    return new FilenameFilter() {
      private Pattern pattern = Pattern.compile(regex);

      public boolean accept(File dir, String name) {
        return pattern.matcher(name).matches();
      }
    };
  }

}
