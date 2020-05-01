import java.sql.*;
import java.util.Scanner;
import bank.AccountInfo;
import bank.Datasource;

public class Main {

    String url = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&serverTimezone=UTC";
    String user = "bank";
    String password = "password";
    AccountInfo acct = new AccountInfo();
    boolean exit;

    Scanner scan = new Scanner(System.in);
    PreparedStatement p;


    public static void main(String[] args) {
        Datasource datasource = new Datasource();
        if(!datasource.open()) {
            System.out.println("Can't open datasource");
            return;
        }
        datasource.runMenu();

        datasource.close();

    }






}
