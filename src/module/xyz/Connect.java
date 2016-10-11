package module.xyz;

import java.sql.*;

//import env.Agent;


class Connect {
    public static void main(String[] args) {
        try {
            // JDBCドライバの登録
            String driver = "org.postgresql.Driver";
            // データベースの指定
            String server   = "192.168.33.10";   // PostgreSQL サーバ ( IP または ホスト名 )
            String dbname   = "soars";         // データベース名
            String url = "jdbc:postgresql://" + server + "/" + dbname;
            String user     = "postgres";         //データベース作成ユーザ名
            String password = "password";     //データベース作成ユーザパスワード
            Class.forName(driver);
            // データベースとの接続
            Connection con = DriverManager.getConnection(url, user, password);
            // テーブル照会実行
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM test";
            ResultSet rs = stmt.executeQuery(sql);

            // テーブル照会結果を出力
            while(rs.next()){
                System.out.println("name：" + rs.getString("name"));
            }
            // データベースのクローズ
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.err.println("SQL failed.");
            e.printStackTrace ();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace ();
        }
    }
}


//public class Connect {
//    public static void connect() {
////        int id;
////        String name;
//        try {
//            // JDBCドライバの登録
//            String driver = "org.postgresql.Driver";
//            // データベースの指定
//            String server   = "192.168.33.10";   // PostgreSQL サーバ ( IP または ホスト名 )
//            String dbname   = "soars";         // データベース名
//            String url = "jdbc:postgresql://" + server + "/" + dbname;
//            String user     = "postgres";         //データベース作成ユーザ名
//            String password = "password";     //データベース作成ユーザパスワード
//            Class.forName (driver);
//            // データベースとの接続
//            Connection con = DriverManager.getConnection(url, user, password);
//            // テーブル照会実行
//            Statement stmt = con.createStatement();
//            String sql = "SELECT * FROM test";
//            ResultSet rs = stmt.executeQuery(sql);
//
//
//            // JDBCドライバの登録
////            String driver = "org.postgresql.Driver";
////            // データベースの指定
////            //192.168.33.10
////            String server   = "postgres";   // PostgreSQL サーバ ( IP または ホスト名 )
////            String dbname   = "postgres";         // データベース名
////            String url = "jdbc:postgresql://" + server + "/" + dbname;
////            String user     = "postgres";         //データベース作成ユーザ名
////            String password = "password";     //データベース作成ユーザパスワード
////            Class.forName (driver);
////            // データベースとの接続
////            Connection con = DriverManager.getConnection(url, user, password);
////            // テーブル照会実行
////            Statement stmt = con.createStatement ();
////            String sql = "SELECT * FROM hello_world_table";
////            ResultSet rs = stmt.executeQuery (sql);
//
//
//            // テーブル照会結果を出力
//            while(rs.next()){
//                System.out.println("name：" + rs.getString("name"));
//            }
//            // データベースのクローズ
//            rs.close();
//            stmt.close();
//            con.close();
//        } catch (SQLException e) {
//            System.err.println("SQL failed.");
//            e.printStackTrace ();
//        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace ();
//        }
//    }
//}