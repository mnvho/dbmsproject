/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
// import java.sql.ResultSet;
import java.sql.Date;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs.
   public double calculateDistance(double lat1, double long1, double lat2, double long2) {
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2);
   }

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   public static String userType = "";

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     Amazon.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      Amazon esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println(
                  "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            System.out.println("\t\t\t\t    MAIN MENU ");
            System.out.println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            System.out.println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");
            String authorisedUser = null;
            // boolean logIn = true;
            switch (readChoice(true)) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorisedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }// end switch
            if (authorisedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println(
                        "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
                  System.out.println("\t\t\t\t    MAIN MENU ");
                  System.out.println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
                  System.out.println("1. View Stores within 30 miles");
                  System.out.println("2. View Product List");
                  System.out.println("3. Place a Order");
                  System.out.println("4. View 5 recent orders");

                  // the following functionalities basically used by managers
                  if (userType.equals("manager   ") || userType.equals("admin     ")) {
                     System.out.println("5. Update Product");
                     System.out.println("6. View 5 recent Product Updates Info");
                     System.out.println("7. View 5 Popular Items");
                     System.out.println("8. View 5 Popular Customers");
                     System.out.println("9. Place Product Supply Request to Warehouse");
                  }

                  if (userType.equals("admin     ")) {
                     System.out.println("10. View user info: ");
                     System.out.println("11. Update user info: ");
                  }

                  System.out.println(".........................");
                  System.out.println("20. Log out");
                  System.out.println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");
                  switch (readChoice(false)) {
                     case 1:
                        viewStores(esql);
                        break;
                     case 2:
                        viewProducts(esql);
                        break;
                     case 3:
                        placeOrder(esql);
                        break;
                     case 4:
                        viewRecentOrders(esql);
                        break;
                     case 5:
                        updateProduct(esql);
                        break;
                     case 6:
                        viewRecentUpdates(esql);
                        break;
                     case 7:
                        viewPopularProducts(esql);
                        break;
                     case 8:
                        viewPopularCustomers(esql);
                        break;
                     case 9:
                        placeProductSupplyRequests(esql);
                        break;
                     case 10:
                        viewUser(esql);
                        break;
                     case 11:
                        updateUser(esql);
                        break;

                     case 20:
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "\t\tUser Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * 
    * @int
    **/
   public static int readChoice(boolean logIn) {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            if (!logIn) {
               if (userType.equals("customer  ")) {
                  if (input > 4 && input != 20) {
                     input = 69;
                  }
               }
               if (userType.equals("manager   ")) {
                  if (input > 9 && input != 20) {
                     input = 69;
                  }
               }
            }
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql) {
      try {
         System.out.print("\n\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");
         String latitude = in.readLine(); // enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: "); // enter long value between [0.0, 100.0]
         String longitude = in.readLine();

         String type = "customer";

         String query = String.format(
               "INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name,
               password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println("User successfully created!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }// end CreateUser

   public static int userID = 0;

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql) {
      try {
         System.out.print("\n\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);

         // Getting userID
         String userIDQuery = String.format("SELECT userID FROM USERS WHERE name = '%s' AND password = '%s'", name,
               password);
         List<List<String>> thing = esql.executeQueryAndReturnResult(userIDQuery);
         userID = Integer.parseInt(thing.get(0).get(0));

         // Getting userType
         String userTypeQuery = String.format("SELECT type FROM USERS WHERE name = '%s' AND password = '%s'", name,
               password);
         List<List<String>> thing2 = esql.executeQueryAndReturnResult(userTypeQuery);
         userType = thing2.get(0).get(0);

         if (userNum > 0)
            return name;
         return null;
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }// end

   // Rest of the functions definition go in here

   public static List<Integer> allowedStore = new ArrayList<Integer>();

   public static void viewStores(Amazon esql) {
      double latitude = 0;
      double longitude = 0;
      try {
         String storeIDQuery = "SELECT latitude, longitude " +
               "FROM Users " +
               "WHERE userID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         latitude = Double.parseDouble(thing.get(0).get(0));
         longitude = Double.parseDouble(thing.get(0).get(1));
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }

      try {
         // Fetch all stores from the database
         String query = "SELECT storeID, latitude, longitude FROM Store;";
         List<List<String>> stores = esql.executeQueryAndReturnResult(query);
         boolean foundStores = false;

         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t    STORES WITHIN 30 MILES OF YOUR LOCATION: ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");

         for (List<String> store : stores) {
            double storeLat = Double.parseDouble(store.get(1));
            double storeLong = Double.parseDouble(store.get(2));

            // Calculate distance using the provided method
            double distance = esql.calculateDistance(latitude, longitude, storeLat, storeLong);
            if (distance <= 30) {
               System.out.println(
                     "Store ID: " + store.get(0) + "\t\tDistance: " + String.format("%.2f", distance) + " miles");
               allowedStore.add(Integer.parseInt(store.get(0)));
               foundStores = true;
            }
         }
         if (!foundStores) {
            System.out.println("No stores found within 30 miles of your location.");
         }

         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewProducts(Amazon esql) {
      int numOfStores = 0;
      try {
         String storeLengthQuery = "SELECT * FROM Store;";
         numOfStores = esql.executeQuery(storeLengthQuery);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }

      try {
         String storeID = "";
         boolean storeIdGood = false;
         while (!storeIdGood) {
            System.out.print("\n\tEnter Store ID (0-" + numOfStores + "): ");
            storeID = in.readLine();

            if (!(Integer.parseInt(storeID) >= 0 && Integer.parseInt(storeID) <= numOfStores)) {
               System.out.println("Store Invalid! Please enter a store between 0 and " + numOfStores);
            } else {
               storeIdGood = true;
            }
         }

         String query = "SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = " + storeID + ";";

         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t    List of products in store #" + storeID + ": ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         int rowCount = thing.size();

         for (int i = 0; i < thing.size(); i++) {
            System.out.println("Product name: " + thing.get(i).get(0) +
                  "\t# of Units: " + thing.get(i).get(1) +
                  "\tPrice per unit: " + thing.get(i).get(2));
         }

         System.out.println("\nTotal product(s): " + rowCount);
         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void placeOrder(Amazon esql) {
      viewStores(esql);

      // Get storeID
      int storeID = 0;
      boolean storeIDGood = false;
      if (allowedStore.size() == 0) {
         System.out.println("You have no stores near you.");
         return;
      }

      while (!storeIDGood) {
         System.out.println("\nEnter one of the following storeID:");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (allowedStore.contains(storeID)) {
               storeIDGood = true;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      }

      viewProductsThing(esql, Integer.toString(storeID));
      boolean productNameGood = false;
      String productName = "";

      // Get productName
      while (!productNameGood) {
         System.out.println("Enter the product name:");
         try {
            productName = in.readLine();
         } catch (Exception e) {
            System.out.println("Invalid input" + e.getMessage());
         }

         int count = 0;
         // Check if the product exists
         String checkQuery = "SELECT * FROM Product WHERE ProductName = '" + productName + "'";
         try {
            count = esql.executeQuery(checkQuery);
         } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
         }

         if (count == 0) {
            System.out.println("Product does not exist.");
         } else {
            productNameGood = true;
         }
      }

      // Get unitsOrdered
      int unitsOrdered = 0;
      System.out.println("Enter the new number of units:");
      try {
         unitsOrdered = Integer.parseInt(in.readLine());
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      String query = "INSERT INTO Orders (customerID, storeID, productName, unitsOrdered, orderTime)" +
            "VALUES (" + userID + "," + storeID + ", '" + productName + "', " + unitsOrdered + ", CURRENT_TIMESTAMP);";

      String queryUpdateStore = "UPDATE Product " +
            "SET numberOfUnits = numberOfUnits - " + unitsOrdered + " " +
            "WHERE storeID = " + storeID + " " +
            "AND productName = '" + productName + "';";

      try {
         int row = esql.executeQuery(query);
      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }

      try {
         esql.executeUpdate(queryUpdateStore);
      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewRecentOrders(Amazon esql) {
      if (userType.equals("manager   ")) {
         try {
            // Get storeID
            int storeID = 0;
            boolean getStoreID = false;
            List<Integer> storeList = new ArrayList<Integer>();

            System.out.println(
                  "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            System.out.println("\t\t\t\t List of stores managing: ");
            System.out
                  .println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            try {
               String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
               List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
               if (thing.size() == 1) {
                  storeID = Integer.parseInt(thing.get(0).get(0));
                  System.out.println(storeID);
               } else {
                  getStoreID = true;
                  for (int i = 0; i < thing.size(); i++) {
                     System.out.println(thing.get(i).get(0));
                     storeList.add(Integer.parseInt(thing.get(i).get(0)));
                  }
               }
            } catch (Exception e) {
               System.err.println(e.getMessage());
            }
            System.out
                  .println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

            if (getStoreID) {
               System.out.println("Select one of the following storeID you manage: ");
               try {
                  storeID = Integer.parseInt(in.readLine());
                  if (storeList.contains(storeID)) {
                     getStoreID = false;
                  } else {
                     System.out.println("\nInvalid store option.");
                  }
               } catch (Exception e) {
                  System.out.println("\nInvalid input" + e.getMessage());
               }
            } else {
               System.out.println("\n" + storeID + " have been automatically selected.\n");
            }

            // Get 5 most recent orders from that store
            System.out.println(
                  "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            System.out.println("\t\t\t\t\t\t5 MOST RECENT ORDERS: ");
            System.out
                  .println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            String query = "SELECT * " +
                  "FROM Orders " +
                  "WHERE storeID = " + storeID + " " +
                  "ORDER BY orderTime DESC " +
                  "LIMIT 5;";
            List<List<String>> thing = esql.executeQueryAndReturnResult(query);
            for (int i = 0; i < thing.size(); i++) {
               String orderNum = thing.get(i).get(0);
               String customerID = thing.get(i).get(1);
               // String storeID = thing.get(i).get(2);
               String productName = thing.get(i).get(3);
               String unitsOrdered = thing.get(i).get(4);
               String orderTime = thing.get(i).get(5);
               System.out.println((i + 1) + ". Product name: " + productName +
                     "\t CustomerID: " + customerID +
                     "\t Units ordered: " + unitsOrdered +
                     // "\t Store: " + storeID +
                     "\t Order #: " + orderNum +
                     "\t Time: " + orderTime);
            }
            System.out.println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

         } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
         }
      } else {
         try {
            System.out.println(
                  "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            System.out.println("\t\t\t\t\t\t5 MOST RECENT ORDERS: ");
            System.out
                  .println(
                        "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
            // System.out.println("5 Most recent orders: ");
            String query = "SELECT * FROM Orders WHERE customerID = " + userID + "ORDER BY orderTime DESC LIMIT 5;";
            List<List<String>> thing = esql.executeQueryAndReturnResult(query);
            for (int i = 0; i < thing.size(); i++) {
               String orderNum = thing.get(i).get(0);
               String customerID = thing.get(i).get(1);
               String storeID = thing.get(i).get(2);
               String productName = thing.get(i).get(3);
               String unitsOrdered = thing.get(i).get(4);
               String orderTime = thing.get(i).get(5);
               System.out.println((i + 1) + ". Product name: " + productName +
               // "\t CustomerID: " + customerID +
                     "\t Units ordered: " + unitsOrdered +
                     "\t Store: " + storeID +
                     "\t Order #: " + orderNum +
                     "\t Time: " + orderTime);
            }
            System.out.println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

         } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
         }
      }
   }

   public static void updateProduct(Amazon esql) {
      int storeID = 0;
      boolean getStoreID = false;
      List<Integer> storeList = new ArrayList<Integer>();

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t\t List of stores managing: ");
      System.out
            .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      try {
         String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         if (thing.size() == 1) {
            storeID = Integer.parseInt(thing.get(0).get(0));
            System.out.println(storeID);
         } else {
            getStoreID = true;
            for (int i = 0; i < thing.size(); i++) {
               System.out.println(thing.get(i).get(0));
               storeList.add(Integer.parseInt(thing.get(i).get(0)));
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      System.out
            .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      if (getStoreID) {
         System.out.println("Select one of the following storeID you manage: ");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (storeList.contains(storeID)) {
               getStoreID = false;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      } else {
         System.out.println("\n" + storeID + " have been automatically selected.\n");
      }

      viewProductsThing(esql, Integer.toString(storeID));
      boolean productNameGood = false;
      String productName = "";
      // Get productName
      while (!productNameGood) {
         System.out.println("Enter the product name:");
         try {
            productName = in.readLine();
         } catch (Exception e) {
            System.out.println("Invalid input" + e.getMessage());
         }

         int count = 0;
         // Check if the product exists
         String checkQuery = "SELECT * FROM Product WHERE ProductName = '" + productName + "'";
         try {
            count = esql.executeQuery(checkQuery);
         } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
         }

         if (count == 0) {
            System.out.println("Product does not exist.");
         } else {
            productNameGood = true;
         }
      }

      // Get numberOfUnits
      int numberOfUnits = 0;
      System.out.println("Enter the new number of units:");
      try {
         numberOfUnits = Integer.parseInt(in.readLine());
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get pricePerUnit
      int pricePerUnit = 0;
      System.out.println("Enter the new price per unit:");
      try {
         pricePerUnit = Integer.parseInt(in.readLine());
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      String updateQuery = "UPDATE Product " +
            "SET numberOfUnits = " + numberOfUnits +
            ", pricePerUnit = " + pricePerUnit +
            "WHERE storeID = " + storeID +
            "AND productName = '" + productName + "';";

      try {
         esql.executeUpdate(updateQuery);

         System.out.println("Product updated successfully.");

      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewRecentUpdates(Amazon esql) {
      // Get storeID
      int storeID = 0;
      boolean getStoreID = false;
      List<Integer> storeList = new ArrayList<Integer>();

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t\t List of stores managing: ");
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      try {
         String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         if (thing.size() == 1) {
            storeID = Integer.parseInt(thing.get(0).get(0));
            System.out.println(storeID);
         } else {
            getStoreID = true;
            for (int i = 0; i < thing.size(); i++) {
               System.out.println(thing.get(i).get(0));
               storeList.add(Integer.parseInt(thing.get(i).get(0)));
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      if (getStoreID) {
         System.out.println("Select one of the following storeID you manage: ");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (storeList.contains(storeID)) {
               getStoreID = false;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      } else {
         System.out.println("\n" + storeID + " have been automatically selected.\n");
      }

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t    5 MOST RECENT PRODUCT UPDATES: ");
      System.out
            .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");

      try {
         // System.out.println("5 MOST RECENT PRODUCT UPDATES: ");
         String query = "SELECT * " +
               "FROM Orders " +
               "WHERE storeID = " + storeID + " " +
               "ORDER BY orderTime DESC " +
               "LIMIT 5;";
         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         for (int i = 0; i < thing.size(); i++) {
            String updateNumber = thing.get(i).get(0);
            String managerID = thing.get(i).get(1);
            // String storeID = thing.get(i).get(2);
            String productName = thing.get(i).get(3);
            String updatedOn = thing.get(i).get(4);
            System.out.println((i + 1) + ". Product name: " + productName +
            // "\t Store: " + storeID +
                  "\t Update #: " + updateNumber +
                  "\t Time: " + updatedOn);
         }

         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewPopularProducts(Amazon esql) {
      // Get storeID
      int storeID = 0;
      boolean getStoreID = false;
      List<Integer> storeList = new ArrayList<Integer>();

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t\t List of stores managing: ");
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      try {
         String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         if (thing.size() == 1) {
            storeID = Integer.parseInt(thing.get(0).get(0));
            System.out.println(storeID);
         } else {
            getStoreID = true;
            for (int i = 0; i < thing.size(); i++) {
               System.out.println(thing.get(i).get(0));
               storeList.add(Integer.parseInt(thing.get(i).get(0)));
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      if (getStoreID) {
         System.out.println("Select one of the following storeID you manage: ");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (storeList.contains(storeID)) {
               getStoreID = false;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      } else {
         System.out.println("\n" + storeID + " have been automatically selected.\n");
      }

      try {
         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t\t5 MOST POPULAR PRODUCTS: ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         String query = "SELECT productName, SUM(unitsOrdered) AS totalUnitsOrdered " +
               "FROM Orders " +
               "WHERE storeID = " + storeID + " " +
               "GROUP BY productName " +
               "ORDER BY totalUnitsOrdered DESC " +
               "LIMIT 5;";
         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         for (int i = 0; i < thing.size(); i++) {
            System.out.println((i + 1) + ". Product: " + thing.get(i).get(0) +
                  "\t\t Numbers sold: " + thing.get(i).get(1));
         }
         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewPopularCustomers(Amazon esql) {
      // Get storeID
      int storeID = 0;
      boolean getStoreID = false;
      List<Integer> storeList = new ArrayList<Integer>();

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t\t List of stores managing: ");
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      try {
         String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         if (thing.size() == 1) {
            storeID = Integer.parseInt(thing.get(0).get(0));
            System.out.println(storeID);
         } else {
            getStoreID = true;
            for (int i = 0; i < thing.size(); i++) {
               System.out.println(thing.get(i).get(0));
               storeList.add(Integer.parseInt(thing.get(i).get(0)));
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      System.out
            .println(
                  "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      if (getStoreID) {
         System.out.println("Select one of the following storeID you manage: ");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (storeList.contains(storeID)) {
               getStoreID = false;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      } else {
         System.out.println("\n" + storeID + " have been automatically selected.\n");
      }

      try {
         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t\t5 MOST POPULAR CUSTOMERS ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         String query = "SELECT U.name AS customer_name, COUNT(O.customerID) AS order_count " +
               "FROM Orders O " +
               "JOIN Users U ON O.customerID = U.userID " +
               "WHERE O.storeID = " + storeID + " " +
               "GROUP BY O.customerID, U.name " +
               "ORDER BY order_count DESC " +
               "LIMIT 5;";

         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         for (int i = 0; i < thing.size(); i++) {
            String name = thing.get(i).get(0).replace(".", " ").replace("_", " ");
            System.out.println((i + 1) + ". Name: " + name +
                  "\t Number of orders: " + thing.get(i).get(1));
         }
         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void placeProductSupplyRequests(Amazon esql) {
      // Get storeID
      int storeID = 0;
      boolean getStoreID = false;
      List<Integer> storeList = new ArrayList<Integer>();

      System.out.println(
            "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      System.out.println("\t\t\t\t List of stores managing: ");
      System.out
            .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
      try {
         String storeIDQuery = "SELECT storeID FROM Store WHERE managerID = " + userID + ";";
         List<List<String>> thing = esql.executeQueryAndReturnResult(storeIDQuery);
         if (thing.size() == 1) {
            storeID = Integer.parseInt(thing.get(0).get(0));
            System.out.println(storeID);
         } else {
            getStoreID = true;
            for (int i = 0; i < thing.size(); i++) {
               System.out.println(thing.get(i).get(0));
               storeList.add(Integer.parseInt(thing.get(i).get(0)));
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
      System.out
            .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      if (getStoreID) {
         System.out.println("Select one of the following storeID you manage: ");
         try {
            storeID = Integer.parseInt(in.readLine());
            if (storeList.contains(storeID)) {
               getStoreID = false;
            } else {
               System.out.println("\nInvalid store option.");
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      } else {
         System.out.println("\n" + storeID + " have been automatically selected.\n");
      }

      viewProductsThing(esql, Integer.toString(storeID));
      boolean productNameGood = false;
      String productName = "";
      // Get productName
      while (!productNameGood) {
         // String productName = "";
         System.out.println("Enter the product name:");
         try {
            productName = in.readLine();
         } catch (Exception e) {
            System.out.println("Invalid input" + e.getMessage());
         }

         int count = 0;
         // Check if the product exists
         String checkQuery = "SELECT COUNT(*) AS count_exists " +
               "FROM Store " +
               "WHERE storeID = " + storeID + " " +
               "AND EXISTS ( " +
               "SELECT 1 " +
               "FROM Product " +
               "WHERE Store.storeID = Product.storeID " +
               "AND Product.productName = '" + productName + "' );";
         try {
            count = esql.executeQuery(checkQuery);
         } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
         }

         if (count == 0) {
            System.out.println("Product does not exist.");
         } else {
            productNameGood = true;
         }
      }

      // Get warehouseID
      int warehouseID = 0;
      boolean warehouseIDgood = false;
      while (!warehouseIDgood) {
         System.out.println("\nEnter the warehouseID: ");
         try {
            warehouseID = Integer.parseInt(in.readLine());
         } catch (Exception e) {
            System.out.println("Invalid input" + e.getMessage());
         }

         int count = 0;
         // Check if warehouse exists
         String checkQuery = "SELECT COUNT(*) AS warehouse_count " +
               "FROM Warehouse " +
               "WHERE WarehouseID = " + warehouseID + ";";

         try {
            count = esql.executeQuery(checkQuery);
         } catch (SQLException e) {
            System.out.println("\nSQL Exception: " + e.getMessage());
         }

         if (count == 0) {
            System.out.println("\nWarehouse does not exist.");
         } else {
            warehouseIDgood = true;
         }
      }

      // Get unitsRequested
      int unitsRequested = 0;
      boolean unitsRequestedGood = false;
      while (!unitsRequestedGood) {
         System.out.println("\nEnter the number of units requesting:");
         try {
            unitsRequested = Integer.parseInt(in.readLine());
            if (unitsRequested <= 0) {
               System.out.println("\nNumber has to be larger than 0");
            } else {
               unitsRequestedGood = true;
            }
         } catch (Exception e) {
            System.out.println("\nInvalid input" + e.getMessage());
         }
      }

      String query = "INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) VALUES "
            + "(" + userID + ", " + warehouseID + ", " + storeID + ", '" + productName + "', " + unitsRequested + ");";

      String queryUpdateStore = "UPDATE Product " +
            "SET numberOfUnits = numberOfUnits + " + unitsRequested + " " +
            "WHERE storeID = " + storeID + " " +
            "AND productName = '" + productName + "';";

      try {
         int row = esql.executeQuery(query);
      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }

      try {
         esql.executeUpdate(queryUpdateStore);
      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewUser(Amazon esql) {
      String query = "SELECT * FROM Users;";

      try {
         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t    List all users: ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         int rowCount = thing.size();

         for (int i = 0; i < thing.size(); i++) {
            String userID = thing.get(i).get(0);
            String name = thing.get(i).get(1);
            name = name.replaceAll("\\s+", "");
            for (int j = name.length(); j < 25; j++)
               name += " ";
            String password = thing.get(i).get(2);
            String latitude = thing.get(i).get(3);
            String longitude = thing.get(i).get(4);
            String type = thing.get(i).get(5);
            System.out.println("UserID: " + userID +
                  "\tName: " + name +
                  "Password: " + password +
                  "\tLatitude: " + latitude +
                  "\tLongitude: " + longitude +
                  "\tType: " + type);
         }

         System.out.println("\nTotal product(s): " + rowCount);
         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateUser(Amazon esql) {
      // Get name
      String name = "";
      System.out.println("Enter the new name:");
      try {
         name = in.readLine();
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get password
      String password = "";
      System.out.println("Enter the new password:");
      try {
         password = in.readLine();
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get latitude
      int latitude = 0;
      System.out.println("Enter the new latitude:");
      try {
         latitude = Integer.parseInt(in.readLine());
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get longitude
      int longitude = 0;
      System.out.println("Enter the new longitutde:");
      try {
         longitude = Integer.parseInt(in.readLine());
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get type
      String type = "";
      System.out.println("Enter the new type:");
      try {
         type = in.readLine();
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      // get userID
      String userID = "";
      System.out.println("Enter the userID to change:");
      try {
         userID = in.readLine();
      } catch (Exception e) {
         System.out.println("Invalid input" + e.getMessage());
      }

      String updateQuery = "UPDATE Users " +
            "SET name = '" + name + "', " +
            "password = '" + password + "', " +
            "latitude = " + latitude + ", " +
            "longitude = " + longitude + ", " +
            "type = '" + type + "' " +
            "WHERE userID = " + userID + ";";

      try {
         esql.executeUpdate(updateQuery);

         System.out.println("User updated successfully.");

      } catch (SQLException e) {
         System.err.println("SQL Exception: " + e.getMessage());
      }
   }

   public static void viewProductsThing(Amazon esql, String storeID) {
      try {

         String query = "SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = " + storeID + ";";

         System.out.println(
               "\n=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         System.out.println("\t\t\t    List of products in store #" + storeID + ": ");
         System.out
               .println("=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
         List<List<String>> thing = esql.executeQueryAndReturnResult(query);
         int rowCount = thing.size();

         for (int i = 0; i < thing.size(); i++) {
            System.out.println("Product name: " + thing.get(i).get(0) +
                  "\t# of Units: " + thing.get(i).get(1) +
                  "\tPrice per unit: " + thing.get(i).get(2));
         }

         System.out.println("\nTotal product(s): " + rowCount);
         System.out.println(
               "=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=\n");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
}// end Amazon
