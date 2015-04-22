package uk.ac.shef.wit.feeds.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.zip.DataFormatException;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.CompressionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.wit.feeds.DataServer;
import uk.ac.shef.wit.feeds.FeedDocument;
import uk.ac.shef.wit.feeds.FeedUser;

import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.QueryUtil;


/**
 * MySqlDataServer class
 */
public class MySqlDataServer
        implements DataServer
{
    /**
     * Field logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MySqlDataServer.class);
//    int limit = 1000000; // 1 Mb
//    int numLogFiles = 100;
//    FileHandler fh = new FileHandler("TestLog.log", limit, numLogFiles);
    
//    public static final String TRIDSRAWDOCUMENTS = "tridsRawDocuments";
    public static final String WIKIPEDIA = "testw";

    private static final int DEFAULT_PORT = 3336;

    String host = "localhost";
    int port = DEFAULT_PORT;
    String path = null;
    String username = "root";
    String password = "";
    boolean overwrite = false;

    
    
    public static final String dumpSqlStr = "source ";
    
    public static final String createSchemaStr= "CREATE database IF NOT EXISTS ";
    
    public static final String useSchemaStr= "USE ";
   
    private static Connection rawDocumentConnection;
    private MysqldResource mysqldResource;
    private PreparedStatement addRawDocPrepdStmt;


    private int addRawDocumentsInsertCount = 0;
    private int addRawDocumentsBatchSize = 1000;

    private boolean shutdown = false;
    private String executeAfterShutdown = null;

    /**
     * Main method for command line interface.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        MySqlDataServer server = null;
        try
        {
            server = createInstance();
            ShutdownUtils.addShutdownListener(server);
            server.init(getOptions(), args);

            server.init(server.host, server.port, server.path, server.username, server.password, server.overwrite);

//            for (int i = 0; !server.isShutdown() && i < 10; i++)
//            {
//                server.addRawDocument("a", CompressionTools.compressString("she's adorable one"));
//                server.addRawDocument("b", CompressionTools.compressString("she's so beautiful one"));
//                server.addRawDocument("c", CompressionTools.compressString("she's my cute little pie"));
//                try
//                {
//                    Thread.sleep(1000);
//                }
//                catch (InterruptedException ignore)
//                {
//                }
//            }
            
            ScriptRunner sr = new ScriptRunner(rawDocumentConnection, true, false);
            FileReader fr = new FileReader(new File(args[0]));  

            sr.runScript(rawDocumentConnection, fr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (server != null)
            {
                server.shutdown();
            }
        }
        logger.info("Done.");
        System.exit(0);
    }

    public static MySqlDataServer createInstance()
            throws IOException
    {
        return new MySqlDataServer();
    }

    private MySqlDataServer()
    {
    }

    public void init(Options options, final String[] args)
            throws ParseException
    {
    	
    	
        // create the parser
        CommandLineParser parser = new GnuParser();
        try
        {
            // parse the command line arguments
            CommandLine commandLine = parser.parse(options, args);

            path = commandLine.getOptionValue("data-server-path");
            if (commandLine.hasOption("data-server-host"))
            {
                host = commandLine.getOptionValue("data-server-host");
            }
            if (commandLine.hasOption("data-server-port"))
            {
                port = Integer.parseInt(commandLine.getOptionValue("data-server-port"));
            }
            if (commandLine.hasOption("data-server-username"))
            {
                username = commandLine.getOptionValue("data-server-username");
            }
            if (commandLine.hasOption("data-server-password"))
            {
                password = commandLine.getOptionValue("data-server-password");
            }

            overwrite = commandLine.hasOption("overwrite");
            if (commandLine.hasOption("execute-batch-size"))
            {
                addRawDocumentsBatchSize = Integer.parseInt(commandLine.getOptionValue("execute-batch-size"));
            }
            executeAfterShutdown = commandLine.getOptionValue("execute-after-shutdown");
        }
        catch (ParseException e)
        {
            logger.info(e.getLocalizedMessage());
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(180);
            formatter.printHelp(MySqlDataServer.class.getName(), options);
            throw e;
        }
    }

    

    public  void resetDatabase(String sqlFile) throws SQLException  
    {  
        String s = new String();  
        StringBuffer sb = new StringBuffer();  
  
        try  
        {  
            FileReader fr = new FileReader(new File(sqlFile));  
            // be sure to not have line starting with "--" or "/*" or any other non aplhabetical character  
  
            BufferedReader br = new BufferedReader(fr);  
  
            while((s = br.readLine()) != null)  
            {  
                sb.append(s);  
            }  
            br.close();  
  
            // here is our splitter ! We use ";" as a delimiter for each request  
            // then we are sure to have well formed statements  
            String[] inst = sb.toString().split(";");  
  
            java.sql.Statement st = rawDocumentConnection.createStatement();  
  
            for(int i = 0; i<inst.length; i++)  
            {  
                // we ensure that there is no spaces before or after the request string  
                // in order to not execute empty statements  
                if(!inst[i].trim().equals(""))  
                {  
                    st.executeUpdate(inst[i]);  
                    System.out.println(">>"+inst[i]);  
                }  
            }  
    
        }  
        catch(Exception e)  
        {  
            System.out.println("*** Error : "+e.toString());  
            System.out.println("*** ");  
            System.out.println("*** Error : ");  
            e.printStackTrace();  
            System.out.println("################################################");  
            System.out.println(sb.toString());  
        }  
  
    } 
    
//
//    /**
//     * @throws java.io.IOException on failure
//     */
//    public void init(String host, int port, String path, String username, String password, boolean dropTable)
//            throws IOException
//    {
//        String className = "com.mysql.jdbc.Driver";
//        if (port <= 0)
//        {
//            port = DEFAULT_PORT;
//        }
//
//        File databaseDir = new File(path);
//        mysqldResource = startDatabase(databaseDir, port, username, password);
//        try
//        {
//            Class.forName(className);
//            String dbName = TRIDSRAWDOCUMENTS;
//            String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
//                    + "?createDatabaseIfNotExist=true"
//                    + "&rewriteBatchedStatements=true"
//                    + "&server.storage_engine=MYISAM"
//                    + "&server.skip-locking"
//                    + "&server.skip-innodb";
//
//
//            rawDocumentConnection = DriverManager.getConnection(connectionUrl, username, password);
//            rawDocumentConnection.setAutoCommit(false); // turn off for batch processing
//            addRawDocPrepdStmt = rawDocumentConnection.prepareStatement(
//                    "INSERT INTO tridsRawDocuments (id, content) VALUES (?, ?)");
//            
//            
//            String sql = "SELECT VERSION()";
//            String queryForString = new QueryUtil(rawDocumentConnection).queryForString(sql);
//            StringBuilder buffer = new StringBuilder();
//            buffer.append("\n------------------------\n");
//            buffer.append(sql);
//            buffer.append("\n------------------------\n");
//            buffer.append(queryForString);
//            buffer.append("\n------------------------\n");
//            logger.info(buffer.toString());
//
//            if (dropTable)
//            {
//                logger.info("Dropping table tridsRawDocuments...");
//                rawDocumentConnection.prepareStatement("drop table if exists tridsRawDocuments;").execute();
//            }
//            if (getCount(rawDocumentConnection, TRIDSRAWDOCUMENTS) == -1)
//            {
//                logger.info("Creating table tridsRawDocuments...");
//                rawDocumentConnection.prepareStatement(
//                        "create table if not exists tridsRawDocuments ( id VARCHAR(64), content BLOB) ENGINE = MYISAM;").execute();
//            }
//
//            try
//            {
//                Thread.sleep(100);
//            }
//            catch (InterruptedException ignore)
//            {
//            }
//        }
//        catch (ClassNotFoundException e)
//        {
//            throw new IOException("Failed to initialise data server", e);
//        }
//        catch (SQLException e)
//        {
//            throw new IOException("Failed to initialise data server", e);
//        }
//    }

    
    /**
     * @throws java.io.IOException on failure
     */
    public void init(String host, int port, String path, String username, String password, boolean dropTable)
            throws IOException
    {
        String className = "com.mysql.jdbc.Driver";
        if (port <= 0)
        {
            port = DEFAULT_PORT;
        }

        File databaseDir = new File(path);
        mysqldResource = startDatabase(databaseDir, port, username, password);
        try
        {
            Class.forName(className);
            String dbName = WIKIPEDIA;
            String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?createDatabaseIfNotExist=true"
                    + "&rewriteBatchedStatements=true"
                    + "&server.storage_engine=MYISAM"
                    + "&server.skip-locking"
                    + "&server.skip-innodb";


            rawDocumentConnection = DriverManager.getConnection(connectionUrl, username, password);
            rawDocumentConnection.setAutoCommit(false); // turn off for batch processing
            addRawDocPrepdStmt = rawDocumentConnection.prepareStatement(
                    "INSERT INTO tridsRawDocuments (id, content) VALUES (?, ?)");
            
            
            String sql = "SELECT VERSION()";
            String queryForString = new QueryUtil(rawDocumentConnection).queryForString(sql);
            StringBuilder buffer = new StringBuilder();
            buffer.append("\n------------------------\n");
            buffer.append(sql);
            buffer.append("\n------------------------\n");
            buffer.append(queryForString);
            buffer.append("\n------------------------\n");
            logger.info(buffer.toString());

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignore)
            {
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Failed to initialise data server", e);
        }
        catch (SQLException e)
        {
            throw new IOException("Failed to initialise data server", e);
        }
    }

    
    public static MysqldResource startDatabase(File databaseDir, int port, String userName, String password)
            throws IOException
    {
        MysqldResource mysqldResource = new MysqldResource(databaseDir);
        Map<String, String> database_options = new HashMap<String, String>();
        database_options.put(MysqldResourceI.PORT, Integer.toString(port));
        database_options.put(MysqldResourceI.INITIALIZE_USER, "true");
        database_options.put(MysqldResourceI.INITIALIZE_USER_NAME, userName);
        database_options.put(MysqldResourceI.INITIALIZE_PASSWORD, password);
        database_options.put("default-storage-engine", "MYISAM");
        database_options.put("skip-innodb", null);
        database_options.put("table_cache", "4");
        database_options.put("max_connections", "4");

        mysqldResource.start("mysqld-thread", database_options);
        if (!mysqldResource.isRunning())
        {
            throw new IOException("MySQL did not start.");
        }
        logger.info("MySQL is running.");
        return mysqldResource;
    }

    /**
     * Share method to get a resultSet from an SQL string
     *
     * @param connection Connection
     * @param sqlString  SQL Statement
     * @return ResultSet
     */
    protected ResultSet queryExecute(Connection connection, String sqlString)
    {
        try
        {
            PreparedStatement stmt = connection.prepareStatement(sqlString);
            return queryExecute(stmt);
        }
        catch (SQLException e)
        {
            int code = e.getErrorCode();
            logger.info("queryExecute building prepStatement " + sqlString + " returns " + code + " " + e);
            return null;
        }
    }

    /**
     * General method to execute a query using a PreparedStatement
     *
     * @param stmt PreparedStatement
     * @return ResultSet
     */
    protected ResultSet queryExecute(PreparedStatement stmt)
    {
        logger.info("DbTable.queryExecute(" + stmt + ")");
        try
        {
            return stmt.executeQuery();
        }
        catch (SQLException e)
        {
            logger.info("queryExecute: " + stmt + " returns " + e);
            return null;
        }
    }

    /**
     * General method to do a statement.execute from a String. Returns the number of rows affected
     *
     * @param connection Connection
     * @param sql        SQL Statement
     * @return int
     */
    int statementExecute(Connection connection, String sql)
    {
        int nb = -1;
        try
        {
            PreparedStatement stmt = connection.prepareStatement(sql);     // on prepare le statement
            nb = statementExecute(stmt);
        }
        catch (SQLException e)
        {
            logger.info("StatementExecute building prepStatement for " + sql + " returns: " + e);
        }
        logger.info("statementExecute(" + sql + ") returns " + nb);
        return nb;
    }

    /**
     * General method to do a statementExecute from a PreparedStatement. Returns the number of rows affected
     *
     * @param stmt PreparedStatement
     * @return int
     */
    int statementExecute(PreparedStatement stmt)
    {
        int nb = -1;
        try
        {
            nb = stmt.executeUpdate();                      // la commande passee en parametre
        }
        catch (SQLException e)
        {
            int code = e.getErrorCode();
            logger.info("statementExecute: " + stmt);
            logger.info("status: " + code + ">> " + e);
        }
        return nb;
    }

    /*
    * returns the row count from a table whose name is passed as parameter
    * returns -1 if the table does not exist
    */
    int getCount(Connection connection, String tableName)
    {
        int nb = -1;
        String sql = "SELECT COUNT(*) FROM " + tableName + ";";

        // call our queryExcute method
        ResultSet rs = queryExecute(connection, sql);
        // if ResultSet is null returns -1
        if (rs == null)
        {
            logger.info("GetCount() on " + tableName + " returns " + nb);
            return nb;
        }
        // fetch rows count
        try
        {
            rs.next();
            nb = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("DbTable.getCount() rs.getNext() or rs.getInt Exception: " + e);
        }

        logger.info("GetCount() on " + tableName + " returns " + nb);
        return nb;
    }


    public static Options getOptions()
    {
        // create Options object
        Options options = new Options();

        // add help option
        options.addOption(new Option("?", "help", false, "Print this message"));

        options.addOption(OptionBuilder.withArgName("name")
                                       .withLongOpt("data-server-host")
                                       .hasArg()
                                       .withDescription("Host for the data server, default: localhost")
                                       .create("dshost"));

        options.addOption(OptionBuilder.withArgName("int")
                                       .withLongOpt("data-server-port")
                                       .hasArg()
                                       .withDescription("Port of the data server, default 3336")
                                       .create("dsport"));

        options.addOption(OptionBuilder.withArgName("path")
                                       .withLongOpt("data-server-path")
                                       .hasArg()
                                       .isRequired()
                                       .withDescription("Path to the data server (e.g. /data/path)")
                                       .create("dspath"));

        options.addOption(OptionBuilder.withArgName("name")
                                       .withLongOpt("data-server-user")
                                       .hasArg()
                                       .withDescription("Name of the data server user, default: root")
                                       .create("dsuser"));

        options.addOption(OptionBuilder.withArgName("password")
                                       .withLongOpt("data-server-password")
                                       .hasArg()
                                       .withDescription("Password of the data server user, default: \"\"")
                                       .create("dspass"));

        options.addOption(OptionBuilder.withArgName("int")
                                       .withLongOpt("execute-batch-size")
                                       .hasArg()
                                       .withDescription(
                                               "Number of inserts before executeBatch is called. Default: 1000")
                                       .create("ebs"));

        options.addOption(OptionBuilder.withLongOpt("overwrite")
                                       .withDescription("Overwrite the output")
                                       .create("ow"));

        options.addOption(OptionBuilder.withArgName("command")
                                       .withLongOpt("execute-after-shutdown")
                                       .hasArg()
                                       .withDescription("The command to execute after shutdown is called")
                                       .create("eas"));
;
        
        return options;
    }

    @Override
    public void addDocument(FeedDocument feedDocument, FeedUser feedUser)
            throws IOException
    {

    }

    
    public void dumpSql(String dbname, String sqlFile) throws SQLException, IOException{
    	
        PreparedStatement createSchemaPrepdStmt =  rawDocumentConnection.prepareStatement( createSchemaStr+dbname) ;
        PreparedStatement useSchemaPrepdStmt=  rawDocumentConnection.prepareStatement(useSchemaStr+dbname) ;
      createSchemaPrepdStmt.execute();
      useSchemaPrepdStmt.execute();
      
      
    	 BufferedReader br = new BufferedReader(new FileReader(sqlFile));
    	  java.sql.Statement statement = rawDocumentConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	  System.out.println("Reading SQL File...");
    	  String line="";
    	  StringBuilder sb = new StringBuilder();

    	  while( (line=br.readLine())!=null)
    	  {
    	     if(line.length()==0 || line.startsWith("--"))
    	     {
    	        continue;
    	     }else
    	     {
    	        sb.append(line);
    	     } 

    	     if(line.trim().endsWith(";"))
    	     {
    	        statement.execute(sb.toString());
    	        sb = new StringBuilder();
    	     }

    	  }
    	  br.close();
    }
    
    public void dumpSqlFile(String dbname, String sqlFile) throws Exception
    {
//    	
      PreparedStatement createSchemaPrepdStmt =  rawDocumentConnection.prepareStatement( createSchemaStr+dbname) ;
      PreparedStatement useSchemaPrepdStmt=  rawDocumentConnection.prepareStatement(useSchemaStr+dbname) ;
    createSchemaPrepdStmt.execute();
    useSchemaPrepdStmt.execute();
    	
   		java.sql.Statement stm = rawDocumentConnection.createStatement();
    	BufferedReader reader = new BufferedReader(new FileReader(new File(sqlFile)));
    	while (true) {
    	    String line = reader.readLine();
    	    if (line == null) {
    	        break;
    	    }
    	    if (line.equals("")) {
    	        continue;
    	    }
    	    // this is the trick -- you need to pass different SQL to different methods
    	    if (line.startsWith("SELECT")) {
    	        stm.executeQuery(line);
    	    } else if (line.startsWith("UPDATE") || line.startsWith("INSERT")
    	        || line.startsWith("DELETE")) {
    	        stm.executeUpdate(line);
    	    } else {
    	        stm.execute(line);
    	    }
    	}
    	stm.close();
    	
    	
    	
    	
//    	
//    	
//    	
//    	
//    	
//        try
//        {
//        	
//             PreparedStatement createSchemaPrepdStmt =  rawDocumentConnection.prepareStatement( createSchemaStr+dbname) ;
//             PreparedStatement useSchemaPrepdStmt=  rawDocumentConnection.prepareStatement(useSchemaStr+dbname) ;
//             PreparedStatement dumpSqlPrepdStmt=  rawDocumentConnection.prepareStatement(dumpSqlStr+sqlFile) ;
//
//
////        	createSchemaPrepdStmt.setObject(1, dbname);
//            logger.info(createSchemaPrepdStmt.toString());
//
////        	useSchemaPrepdStmt.setObject(1, dbname);
//            logger.info(useSchemaPrepdStmt.toString());
//
////            dumpSqlPrepdStmt.setObject(1, sqlFile);
//            logger.info(dumpSqlPrepdStmt.toString());
//
//            createSchemaPrepdStmt.execute();
//            useSchemaPrepdStmt.execute();
//            dumpSqlPrepdStmt.execute();
////            commitSqlDump();
//        }
//        catch (SQLException e)
//        {
//            throw new Exception("Failed to import sql file", e);
//        }
    }

    
    public void addRawDocument(String id, byte[] content)
    throws IOException
{
try
{
    addRawDocPrepdStmt.setString(1, id);
    addRawDocPrepdStmt.setBlob(2, new SerialBlob(content));
    addRawDocPrepdStmt.addBatch();
    if (++addRawDocumentsInsertCount % addRawDocumentsBatchSize == 0)
    {
        commitRawDocument();
        logger.info("Committed documents: {}", addRawDocumentsInsertCount);
    }
}
catch (SQLException e)
{
    throw new IOException("Failed to add document", e);
}
}
    
    @Override
    public void commitRawDocument()
            throws IOException
    {
        try
        {
            addRawDocPrepdStmt.executeBatch();
            rawDocumentConnection.commit();
            addRawDocPrepdStmt.clearParameters();
        }
        catch (SQLException e)
        {
            throw new IOException("Failed to commit.", e);
        }
    }

    
    
    @Override
    public void shutdown()
    {
        // if shutdown has already been called just return
        if (isShutdown())
        {
            return;
        }

        shutdown = true;
        logger.info("Shutdown {}...", this.getClass().getCanonicalName());
        try
        {
            if (rawDocumentConnection != null)
            {
//                outputRawDocuments(rawDocumentConnection, System.out);

                addRawDocPrepdStmt.close();
                rawDocumentConnection.close();

                addRawDocPrepdStmt = null;
                rawDocumentConnection = null;
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to shutdown rawDocument connection cleanly.", e);
        }

        try
        {
            if (mysqldResource != null)
            {
                mysqldResource.shutdown();
                mysqldResource = null;
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to shutdown mysql resource cleanly.", e);
        }

        if (executeAfterShutdown != null)
        {
            try
            {
                Runtime.getRuntime().exec(executeAfterShutdown);
            }
            catch (IOException e)
            {
                logger.error("Failed to execute: {}", executeAfterShutdown, e);
            }
            executeAfterShutdown = null;
        }
    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

    @Override
    public int getDocumentCount()
    {
        return getCount(rawDocumentConnection, WIKIPEDIA);
    }

    private static void outputRawDocuments(Connection rawDocumentConnection, PrintStream out)
            throws SQLException, IOException, DataFormatException
    {
        ResultSet rs = rawDocumentConnection.prepareStatement(
                "select * from "+WIKIPEDIA+";").executeQuery();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            out.print(rsmd.getColumnName(i) + "\t");
        }
        out.println();

        // Checking if the data is correct
        while (rs.next())
        {
            for (int i = 1; i <= rsmd.getColumnCount(); i++)
            {
                switch (i)
                {
                    case 2:
                        Blob blob = rs.getBlob(i);
                        InputStream blobIs = blob.getBinaryStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = blobIs.read(buffer, 0, 1024)) != -1)
                        {
                            baos.write(buffer, 0, bytesRead);
                        }

                        out.print(new String(CompressionTools.decompress(baos.toByteArray())) + "\t");
                        break;
                    default:
                        out.print(rs.getObject(i).toString() + "\t");
                }
            }
            out.println();
        }
    }

}