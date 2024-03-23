package sql;

import org.junit.Test;


public class TestJDBC {
    @Test
    public void test() {
        String JDBC_Driver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(JDBC_Driver);
//            DriverManager.getConnection()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
