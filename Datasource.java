package bank;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kevin on 4/29/20
 */

public class Datasource {

    public static final String url = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&serverTimezone=UTC";
    public static final String user = "bank";
    public static final String password = "password";


    SignIn signIn = new SignIn();
    AccountInfo accountInfo = new AccountInfo();
    Customers customers = new Customers();


    private Scanner scan = new Scanner(System.in);

    public static final String QUERY_SIGNIN = "SELECT emailAdd, password FROM signIn WHERE emailAdd = ? AND password = ?";
    public static final String QUERY_INSERT_UA = "INSERT INTO USER_ACCOUNT (FName, LName, PhoneNo, emailAdd, BANumber, Balance, SSN)" +
                                                " VALUES (?, ?, ?, ?, ?, ?, ?);";
    public static final String QUERY_INSERT_SignIn = "INSERT INTO signIn (emailAdd, password, SSN) VALUE (?, ?, ?);";
    public static final String SELECT_ACCOUNT = "SELECT FName, LName, PhoneNo, BANumber, Balance, SSN " +
                                                        "FROM USER_ACCOUNT " +
                                                        "WHERE emailAdd = ?";
    public static final String SELECT_USER_CUSTOMER = "SELECT CFName, CEmailAdd, CPhoneNO, UEmail FROM CUSTOMERS C " +
                                                      "INNER JOIN USER_ACCOUNT U ON C.UEmail = U.emailAdd " +
                                                      "WHERE UEmail = ?";
    public static final String INSERT_CUSTOMER = "INSERT INTO CUSTOMERS (CFName, CLName, CEmailAdd, CPhoneNO, UEmail) VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_USER_BALANCE = "UPDATE USER_ACCOUNT SET Balance = Balance - ? WHERE emailAdd = ?";
    public static final String UPDATE_CUSTOMER_BALANCE = "UPDATE CUSTOMERS SET Balance = Balance + ? WHERE CEmailAdd = ?";

    private PreparedStatement querySignIn;
    private PreparedStatement queryInsertUA;
    private PreparedStatement queryInsertSignIn;
    private PreparedStatement selectAccount;
    private PreparedStatement selectUserCustomer;
    private PreparedStatement insertCustomer;
    private PreparedStatement updateUserBalance;
    private PreparedStatement updateCustomerBalance;


    private Connection conn;

    public boolean open() {
       try {
           conn = DriverManager.getConnection(url, user, password);
           querySignIn = conn.prepareStatement(QUERY_SIGNIN);
           queryInsertUA = conn.prepareStatement(QUERY_INSERT_UA);
           queryInsertSignIn = conn.prepareStatement(QUERY_INSERT_SignIn);
           selectAccount = conn.prepareStatement(SELECT_ACCOUNT);

           selectUserCustomer = conn.prepareStatement(SELECT_USER_CUSTOMER);
           insertCustomer = conn.prepareStatement(INSERT_CUSTOMER);
           updateUserBalance = conn.prepareStatement(UPDATE_USER_BALANCE);
           updateCustomerBalance = conn.prepareStatement(UPDATE_CUSTOMER_BALANCE);

               return true;
           } catch (SQLException e) {
           System.out.println("Couldn't connect to database: " + e.getMessage());
           return false;
       }
    }

    public void close() {
        try {
            if(updateCustomerBalance != null) {
                updateCustomerBalance.close();
            }
            if(updateUserBalance != null) {
                updateUserBalance.close();
            }
            if(insertCustomer != null) {
                insertCustomer.close();
            }
            if(selectUserCustomer != null) {
                selectUserCustomer.close();
            }
            if(selectAccount != null) {
                selectAccount.close();
            }
            if(querySignIn != null) {
                querySignIn.close();
            }
            if(queryInsertUA != null) {
                queryInsertUA.close();
            }
            if(queryInsertSignIn != null) {
                queryInsertSignIn.close();
            }
            if(conn != null) {
                conn.close();
            }
        } catch(SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    /******************************************************************************************************************
     *********************************************** Main Menu ********************************************************
     ******************************************************************************************************************/

    public void runMenu() {
        boolean exit = false;
        printHeader();
        while(!exit) {
            printMenu();
            int choice = getInput();
            switch(choice) {
                case 0:
                    System.out.println("Exit");
                    exit = true;
                    break;
                case 1:
                    signIn();
                    break;
                case 2:
                    createAccount();
                    break;
                default:
                    System.out.println("Unknown error has occurred.");
            }
        }
    }

    private void printHeader() {
        System.out.println("+------------------------------------+");
        System.out.println("|       Welcome to Wallet App        |");
        System.out.println("+------------------------------------+");
    }

    private void printMenu() {
        System.out.println("+---------------------------+");
        System.out.println("| Please make a selection:  |");
        System.out.println("| 0) Exit                   |");
        System.out.println("| 1) Sign in                |");
        System.out.println("| 2) Create a new account   |");
        System.out.println("+---------------------------+");

    }

    private int getInput() {
        int choice = -1;
        do {
            System.out.println("Enter your choice: ");
            try {
                choice = Integer.parseInt(scan.nextLine());
            } catch(NumberFormatException e) {
                System.out.println("Invalid selection. Numbers only please.");
            }

            if(choice < 0 || choice > 2) {
                System.out.println("Choice outside of range. Please choice again.");
            }
        } while (choice < 0 || choice > 2);
        return choice;
    }

    /******************************************************************************************************************
     *********************************************** Sign in **********************************************************
     ******************************************************************************************************************/




    public void signIn() {
        if(isValid()) {
            System.out.println("+-----------------------------------------+");
            System.out.println("|    Your are now login into the account  |");
            System.out.println("+-----------------------------------------+");

            try {
                selectAccount.setString(1, signIn.getEmailAdd());

                selectUserCustomer.setString(1, signIn.getEmailAdd());

                ResultSet rs = selectAccount.executeQuery();
                ResultSet rs2 = selectUserCustomer.executeQuery();
                while(rs.next()) {
                    accountInfo.setFirstName(rs.getString(1));
                    accountInfo.setLastName(rs.getString(2));
                    accountInfo.setPhoneNo(rs.getString(3));
                    accountInfo.setBalance(rs.getDouble(5));
                    signIn.setSsn(rs.getString(6));
                }
                while(rs2.next()) {
                    customers.setCFirstName(rs2.getString(1));
                    customers.setCPhoneNo(rs2.getString(2));
                    customers.setCEmailAdd(rs2.getString(3));
                }
                rs.close();
                rs2.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            runAccountMenu();
        }
        else {
            System.out.println("+-----------------------------------------+");
            System.out.println("|  Your User ID or password is incorrect  |");
            System.out.println("|           Please try again!!!           |");
            System.out.println("+-----------------------------------------+");
        }

    }

    // check if the email address and password are correct.
    public boolean isValid() {
        try {
            System.out.println("Please enter your Email Address: ");
            signIn.setEmailAdd(scan.nextLine());
            System.out.println("Please enter your Password: ");
            signIn.setPassword(scan.nextLine());

            querySignIn.setString(1, signIn.getEmailAdd());
            querySignIn.setString(2, signIn.getPassword());
            ResultSet rs = querySignIn.executeQuery();
            if(rs.next()) {
                rs.close();
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }

        return false;
    }

    public void createAccount() {

        System.out.println("Please enter your SSN: ");
        signIn.setSsn(scan.nextLine());
        System.out.println("Please enter your First Name: ");
        accountInfo.setFirstName(scan.nextLine());
        System.out.println("Please enter your Last Name: ");
        accountInfo.setLastName(scan.nextLine());
        System.out.println("Please enter your Email Address: ");
        signIn.setEmailAdd(scan.nextLine());
        System.out.println("Please enter your password: ");
        signIn.setPassword(scan.nextLine());
        System.out.println("Please enter your Phone Number: ");
        accountInfo.setPhoneNo(scan.nextLine());
        System.out.println("Please enter your Bank Account Number: ");
        accountInfo.setBankAccountNumber(scan.nextLine());
        System.out.println("Please enter How much you want to deposit: ");
        accountInfo.setBalance(Double.parseDouble(scan.nextLine()));


        try {

            queryInsertSignIn.setString(1, signIn.getEmailAdd());
            queryInsertSignIn.setString(2, signIn.getPassword());
            queryInsertSignIn.setString(3, signIn.getSsn());
            queryInsertUA.setString(1, accountInfo.getFirstName());
            queryInsertUA.setString(2, accountInfo.getLastName());
            queryInsertUA.setString(3, accountInfo.getPhoneNo());
            queryInsertUA.setString(4, signIn.getEmailAdd());
            queryInsertUA.setString(5, accountInfo.getBankAccountNumber());
            queryInsertUA.setDouble(6, accountInfo.getBalance());
            queryInsertUA.setString(7, signIn.getSsn());
            queryInsertUA.execute();
            queryInsertSignIn.execute();


            runAccountMenu();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("+-----------------------------------+");
            System.out.println("|  There is a problem on your info. |");
            System.out.println("|          Please try again.        |");
            System.out.println("+-----------------------------------+");
        }

    }

    /******************************************************************************************************************
     ******************************************** Account Menu ********************************************************
     ******************************************************************************************************************/

    public void runAccountMenu() {
        boolean exit = false;
        while(!exit) {
            printAccountMenu();
            int choice = chooseNumber();
            switch(choice) {
                case 0 :
                    System.out.println("Sign Out");
                    exit = true;
                    break;
                case 1 :
                    printAccountInfoHeader();
                    break;
                case 2 :
                    sendMoney();
                    break;
                case 3 :
//                requestMoney();
                    break;
                case 4 :
//                statements();
                    break;
                case 5 :
//                searchTransactions();
                case 6 :
                    runCustomerMenu();
                    break;
                case 7 :
//                changeUserInfo();
                default:
                    System.out.println("Unknown error has occurred.");
            }

        }
    }

    private void printAccountMenu() {
        System.out.println("+-------------------------- +");
        System.out.println("| Please make a selection:  |");
        System.out.println("| 0) Sign Out               |");
        System.out.println("| 1) Show Account Info      |");
        System.out.println("| 2) Send Money             |");
        System.out.println("| 3) Request Money          |");
        System.out.println("| 4) Statements             |");
        System.out.println("| 5) Add Or Delete Customer |");
        System.out.println("| 6) Search Transactions    |");
        System.out.println("+---------------------------+");

    }

    private void printAccountInfoHeader() {
        try {
            selectAccount.setString(1, signIn.getEmailAdd());
            ResultSet rs = selectAccount.executeQuery();

            while(rs.next()) {
                System.out.println("+-------------Account Info----------------+");
                System.out.println("| First Name: " + rs.getString(1));
                System.out.println("| Last Name : " + rs.getString(2));
                System.out.println("| Phone Number: " + rs.getString(3));
                System.out.println("| Bank Account Number: " + rs.getString(4));
                System.out.println("| Balance : " + rs.getString(5));
                System.out.println("+-------------Account Info----------------+");
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }

    }

    private int chooseNumber() {
        int choice = -1;
        do {
            System.out.println("Enter your choice: ");
            try {
                choice = scan.nextInt();
                scan.nextLine();
            } catch(NumberFormatException e) {
                System.out.println("Invalid selection. Numbers only please.");
            }

            if(choice < 0 || choice > 7) {
                System.out.println("Choice outside of range. Please choose again.");
            }
        } while (choice < 0 || choice > 7);
        return choice;
    }

    //add bank account
//    private void addBankAccount() {
//        try{
//            System.out.println("Please enter your First Name: ");
//            accountInfo.setFirstName(scan.nextLine());
//            System.out.println("Please enter your Last Name: ");
//            accountInfo.setLastName(scan.nextLine());
//            System.out.println("Please enter your Email Address: ");
//            signIn.setEmailAdd(scan.nextLine());
//            System.out.println("Please enter your Bank Account Number: ");
//            accountInfo.setBankAccountNumber(scan.nextLine());
//            System.out.println("Please enter your Amount: ");
//            accountInfo.setBalance(Double.parseDouble(scan.nextLine()));
//
//            insertBankInfo.setString(1, accountInfo.getBankAccountNumber());
//            insertBankInfo.setString(2, signIn.getEmailAdd());
//            insertBankInfo.setString(3, accountInfo.getFirstName());
//            insertBankInfo.setString(4, accountInfo.getLastName());
//            insertBankInfo.setDouble(5, accountInfo.getBalance());
//            boolean isSuccess = insertBankInfo.execute();
//            if(isSuccess) {
//                System.out.println("+-----------------+");
//                System.out.println("|    Successful   |");
//                System.out.println("+-----------------+");
//            }
//            return;
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        } catch (NumberFormatException ex) {
//            System.out.println("Amount Should be numbers only");
//        }
//    }

    /******************************************************************************************************************
     ******************************************** Customer Menu *******************************************************
     ******************************************************************************************************************/

    private void runCustomerMenu() {
        boolean exit = false;

        while(!exit) {
            System.out.println("+---------------------+");
            System.out.println("| 0) Exit             |");
            System.out.println("| 1) Show Customers   |");
            System.out.println("| 2) Add Customer     |");
            System.out.println("| 3) Delete Customer  |");
            System.out.println("+---------------------+");
            int choice = getInputCustomer();

            switch(choice) {
                case 0:
                    System.out.println("Exit");
                    exit = true;
                    break;
                case 1:
                    printCustomer();
                    break;
                case 2:
                    addCustomer();
                    break;
                case 3:
                     deleteCustomer();
                    break;
                default :
                    System.out.println("Unknown error has occurred.");
            }
        }
    }

    private int getInputCustomer() {
        int choice = -1;
        do {
            System.out.println("Enter your choice: ");
            try {
                choice = Integer.parseInt(scan.nextLine());
            } catch(NumberFormatException e) {
                System.out.println("Invalid selection. Numbers only please.");
            }

            if(choice < 0 || choice > 3) {
                System.out.println("Choice outside of range. Please choice again.");
            }
        } while (choice < 0 || choice > 3);
        return choice;
    }

    /******************************************************************************************************************
     ******************************************** Show Customers ******************************************************
     ******************************************************************************************************************/


    private void printCustomer(){

        try {
            selectUserCustomer.setString(1, signIn.getEmailAdd());
            ResultSet rs = selectUserCustomer.executeQuery();

            List<Customers> customerList = new ArrayList<>();

            while(rs.next()) {
                Customers customer = new Customers();
                customer.setCFirstName(rs.getString(1));
                customer.setCEmailAdd(rs.getString(2));
                customer.setCPhoneNo(rs.getString(3));
                customer.setUEmail(rs.getString(4));
                customerList.add(customer);
            }
            if(customerList != null) {
                for(Customers c : customerList) {

                    System.out.println("First Name: " + c.getCFirstName() + ", Email Address: " + c.getCEmailAdd() +
                                       ", Phone Number: " + c.getCPhoneNo());
                    System.out.println("==============================================================================");

                }
            }
            rs.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /******************************************************************************************************************
     ******************************************** Add Customer ********************************************************
     ******************************************************************************************************************/


    private void addCustomer() {

        System.out.println("Please enter customer's First Name: ");
        customers.setCFirstName(scan.nextLine());
        System.out.println("Please enter customer's Last Name: ");
        customers.setCLastName(scan.nextLine());
        System.out.println("Please enter customer's Email Address: ");
        customers.setCEmailAdd(scan.nextLine());
        System.out.println("Please enter customer's Phone Number: ");
        customers.setCPhoneNo(scan.nextLine());


        try {
            insertCustomer.setString(1, customers.getCFirstName());
            insertCustomer.setString(2, customers.getCLastName());
            insertCustomer.setString(3, customers.getCEmailAdd());
            insertCustomer.setString(4, customers.getCPhoneNo());
            insertCustomer.setString(5, signIn.getEmailAdd());
            insertCustomer.execute();

            System.out.println("+-------------------------+");
            System.out.println("| Customer is been added  |");
            System.out.println("+-------------------------+");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("+--------------------------------+");
            System.out.println("| The Email address you enter    |");
            System.out.println("| is incorrect or already exist! |");
            System.out.println("| Please try again.              |");
            System.out.println("+--------------------------------+");
        }

    }

    /******************************************************************************************************************
     ******************************************** Delete Customer *****************************************************
     ******************************************************************************************************************/



    private void deleteCustomer() {
        try {
            System.out.println("Please enter a customer's First Name: ");
            String name = scan.nextLine();


            PreparedStatement selectCustomer = conn.prepareStatement("SELECT CFName FROM CUSTOMERS C INNER JOIN USER_ACCOUNT U " +
                                                                    "ON C.UEmail = U.emailAdd WHERE U.emailAdd = ? AND CFName = ?");
            PreparedStatement deleteCustomer = conn.prepareStatement("DELETE FROM CUSTOMERS WHERE CFName = ? AND UEmail = ?");
            selectCustomer.setString(1, signIn.getEmailAdd());
            selectCustomer.setString(2, name);
            ResultSet rs = selectCustomer.executeQuery();
            if(rs.next()) {
                deleteCustomer.setString(1, rs.getString(1));
                deleteCustomer.setString(2, signIn.getEmailAdd());
                deleteCustomer.execute();
            }
            else {
                System.out.println("+-----------------------------------------+");
                System.out.println("| The customer you enter does not exist.  |");
                System.out.println("| Please try again!                       |");
                System.out.println("+-----------------------------------------+");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /******************************************************************************************************************
     ********************************************** Send Money ********************************************************
     ******************************************************************************************************************/

    private void sendMoney() {
        printCustomer();

        try {

            PreparedStatement currentBalance = conn.prepareStatement("SELECT Balance FROM USER_ACCOUNT WHERE emailAdd = ?");
            currentBalance.setString(1, signIn.getEmailAdd());
            ResultSet rs = currentBalance.executeQuery();
            rs.next();
            accountInfo.setBalance(rs.getDouble(1));



            System.out.println("Please enter receiver's email: ");
            String email = scan.nextLine();
            System.out.println("Please enter amount you want to send: ");
            double amount = scan.nextDouble();
            boolean isEnough = false;

            while(!isEnough) {
                if(amount > accountInfo.getBalance() || amount < 0) {
                    System.out.println("You have not enough money to send.");
                    System.out.println("Your balance is: " + accountInfo.getBalance());
                    System.out.println("Please enter an amount");
                    amount = scan.nextDouble();

                }
                else {
                    isEnough = true;
                }

            }

            updateUserBalance.setDouble(1, amount);
            updateUserBalance.setString(2, signIn.getEmailAdd());
            updateCustomerBalance.setDouble(1, amount);
            updateCustomerBalance.setString(2, email);
            updateUserBalance.execute();
            updateCustomerBalance.execute();

            rs.close();

            // Record to transaction history
            PreparedStatement storeTransaction = conn.prepareStatement("insert into transaction_history (sender, receiver, amount, ussn) values ( ?, ?, ?, ?)");
            storeTransaction.setString(1, accountInfo.getFirstName());
            storeTransaction.setString(2, customers.getCFirstName());
            storeTransaction.setDouble(3, amount);
            storeTransaction.setString(4, signIn.getSsn());
            storeTransaction.execute();

            System.out.println("+-------------------------+");
            System.out.println("|  Transfer successfully  |");
            System.out.println("|  Transaction stored     |");
            System.out.println("+-------------------------+");


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /******************************************************************************************************************
     ******************************************* Transaction History **************************************************
     ******************************************************************************************************************/

    private void statements(){
        boolean exit = false;
        while(!exit) {
            System.out.println("+-----------------------------+");
            System.out.println("| 1) |");
            System.out.println("+-----------------------------+");

        }

    }













































}
