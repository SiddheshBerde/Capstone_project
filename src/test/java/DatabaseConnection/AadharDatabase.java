package DatabaseConnection;
import java.sql.*;

public class AadharDatabase
{
    Connection connection;
    String driverURL;

    public static void main(String[] args)
    {
        AadharDatabase dbconnect=new AadharDatabase();
        dbconnect.createAadharData();
    }

    public void createAadharData()
    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "1234"); //driverURL,username,password
            if (connection != null)
            {
                System.out.println("Aadhar Database connection is connected!");
            }

            Statement smt=connection.createStatement();
            //create a database
            //smt.execute("create database aadhar_db");
             //System.out.println("Aadhar Database created successfully");

            //use Database
            smt.execute("use aadhar_db");
            //smt.execute("drop table Aadhar");

            //create a table
            String createquery="CREATE TABLE Aadhar(Aadhar_No bigint(12) NOT NULL, Fname varchar(100) NOT NULL, Lname varchar(100), Address varchar(150) NOT NULL, Phone bigint(10) NOT NULL, PRIMARY KEY (Aadhar_No));";
            smt.execute(createquery);
            System.out.println("Aadhar table created successfully");

            //insert records into table
            String insertquery="INSERT INTO Aadhar VALUES (656545676586,'Vipul','Shravage','Pune',8767543212)";
            smt.execute("INSERT INTO Aadhar(Aadhar_No,Fname,Lname,Address,Phone) values ('656545676586','Vipul','Shravage','Pune','8767543212') ,\n" +
                    "('234378654309','Sunil','Kumar','Pune','7876565767'),\n" +
                    "('178657656784','Arpit','Patro','Pune','9767543212'),\n" +
                    "('988676547555','Om','Prakash','Pune','3767543212');");

            smt.execute(insertquery);
            System.out.println("Record inserted successfully");

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}