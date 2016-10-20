package module.xyz;

import java.sql.*;
//import env.Agent;
import env.Spot;



public class Connect {
    public String driver;
    public String server;
    public String dbname;
    public String url;
    public String user;
    public String password;

    public Connect() {
        // JDBCドライバの登録
        driver = "org.postgresql.Driver";
        // データベースの指定
        server   = "192.168.33.10";   // PostgreSQL サーバ ( IP または ホスト名 )
        dbname   = "soars";         // データベース名
        url = "jdbc:postgresql://" + server + "/" + dbname;
        user     = "postgres";         //データベース作成ユーザ名
        password = "password";     //データベース作成ユーザパスワード
    }


    public void connectAndInit() {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();
            String sql = "drop schema public cascade; create schema public;"
                    + "CREATE TABLE leaderspots (id serial, name varchar(200));"
                    + "CREATE TABLE clients (id serial, client_name varchar(200), stage_name varchar(200), waiting boolean);"
                    + "CREATE TABLE spots (id serial, leaderspot_id integer, name varchar(200), x int, y int, z int, celltype varchar(200));"
                    + "CREATE TABLE agents (id serial, name varchar(200));";
//            ResultSet rs = stmt.executeQuery(sql);
            stmt.executeUpdate(sql);

            // テーブル照会結果を出力
//            while(rs.next()){
//                System.out.println("name：" + rs.getString("name"));
//            }
            // データベースのクローズ
//            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public int insertLeaderSpot(Spot leaderSpot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();

            String leaderSpotName = leaderSpot.getName();

            String sql = "INSERT INTO leaderspots (name)"
                + "SELECT ('" + leaderSpotName
                + "') WHERE NOT EXISTS (SELECT id FROM leaderspots WHERE name ='" + leaderSpotName + "');";
            stmt.executeUpdate(sql);

            sql = "SELECT id FROM leaderspots WHERE name = '" + leaderSpotName + "';";
            ResultSet rs = stmt.executeQuery(sql);

            int leaderspotsId = 0;
            if (rs.next()) {
                leaderspotsId = rs.getInt(1);
            }
            rs.close();

            // データベースのクローズ
            stmt.close();
            con.close();
            return leaderspotsId;
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
            return 0;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    // argument needs serverSpot and return clientId
    public int insertClient(Spot serverSpot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();
            String sql = "select count(*) from clients";
            ResultSet rs = stmt.executeQuery(sql);

            int clientId = 0;
            if (rs.next()) {
                clientId = rs.getInt(1) + 1;
            }
            rs.close();

            String nextStageName = serverSpot.getKeyword("NextStageName");

            if (clientId == 1) {
                sql = "insert into clients (client_name, stage_name, waiting) values ('latestClient', '" + nextStageName + "', True);";
                stmt.executeUpdate(sql);
            }

            String clientName = "client" + clientId;
            sql = "insert into clients (client_name, stage_name, waiting) values ('" + clientName + "', '" + nextStageName + "', True);";
            stmt.executeUpdate(sql);
            serverSpot.setKeyword("ClientName", clientName);

            // データベースのクローズ
            stmt.close();
            con.close();
            return clientId;
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
            return 0;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public int insertSpot(Spot spot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();

            int leaderSpotId = spot.getSpotVariable("LeaderSpot").getIntVariable("ID");
            String spotName = spot.getName();
            String cellType = spot.getKeyword("CellType");
            String sql = "insert into spots (leaderspot_id, name, x, y, z, celltype) values ("
                    + leaderSpotId + ","
                    + "'" + spotName + "',"
                    + spot.getIntVariable("XCoordinate") + ","
                    + spot.getIntVariable("YCoordinate") + ","
                    + spot.getIntVariable("ZCoordinate") + ","
                    + "'" + cellType + "');";

            stmt.executeUpdate(sql);

            sql = "SELECT id FROM spots WHERE name = '" + spotName + "';";
            ResultSet rs = stmt.executeQuery(sql);

            int spotId = 0;
            if (rs.next()) {
                spotId = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            con.close();
            return spotId;
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
            return 0;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public void updateStageName(Spot serverSpot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();
            String nextStageName = serverSpot.getKeyword("NextStageName");
            String clientName = serverSpot.getKeyword("ClientName");

            if (clientName.equals("client1")) {
                String sql = "UPDATE clients SET stage_name = '" + nextStageName + "', waiting = true WHERE client_name = 'latestClient' or client_name = '" + clientName + "';";
                stmt.executeUpdate(sql);
            } else {
                String sql = "UPDATE clients SET stage_name = '" + nextStageName + "', waiting = true WHERE client_name = '" + clientName + "';";
                stmt.executeUpdate(sql);
//
//                sql = "SELECT stage_name FROM clients WHERE client_name = 'latestClient';";
//                ResultSet rs = stmt.executeQuery(sql);
//
//                if (rs.next()) {
//                    String latestStageName = rs.getString(1);
//                }
            }
            System.out.println("nextStageName : " + nextStageName);

            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public Boolean shouldNextStage(String nextStageName) {
        return true;
    }

    public Boolean shouldNextStage(Spot serverSpot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();

            String nextStageName = serverSpot.getKeyword("NextStageName");
            String clientName = serverSpot.getKeyword("ClientName");

            if (clientName.equals("client1")) {
                while (true) {
                    String sql = "SELECT waiting FROM clients";
                    ResultSet rs = stmt.executeQuery(sql);
                    Boolean flag = true;

                    while (rs.next()) {
                        if (!rs.getBoolean(1)) {
                            Object lock = new Object();
                            synchronized (lock) {
                                lock.wait(2000); // wait 2 second if all clients haven't finished the same stage
                            }
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        stmt.close();
                        con.close();
                        return true;
                    }
                }
            } else {
                while (true) {
                    String sql = "SELECT waiting FROM clients WHERE client_name = '" + clientName + "';";
                    ResultSet rs = stmt.executeQuery(sql);

                    if (rs.next()) {
                        if (!rs.getBoolean(1)) {
                            stmt.close();
                            con.close();
                            return true;
                        }

                        Object lock = new Object();
                        synchronized (lock) {
                            lock.wait(2000); // wait 2 second if all clients haven't finished the same stage
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void progressNextStage(Spot serverSpot) {
        try {
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();

//            String nextStageName = serverSpot.getKeyword("NextStageName");
            String clientName = serverSpot.getKeyword("ClientName");

            if (clientName.equals("client1")) {
                String sql = "UPDATE clients SET waiting = false;";
                stmt.executeQuery(sql);
            }
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}