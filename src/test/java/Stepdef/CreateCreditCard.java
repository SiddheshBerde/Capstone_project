package Stepdef;

import Reusable.Reusable;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;

public class CreateCreditCard
{
    public Connection connection;
    private ExtentSparkReporter spark;
    private ExtentReports extent;
    private ExtentTest logger;
    Response post_credit_response;
    Reusable RA;
    DataFormatter formatter = new DataFormatter();

    @BeforeClass
    public void report_and_db_setup() throws SQLException
    {
        RA = new Reusable();
        extent = new ExtentReports();
        spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/Report/Credit_Card_Validation.html");
        spark.config().setDocumentTitle("Credit Card Details Validation");
        spark.config().setReportName("Credit_Card_Details_Validation_Report");
        spark.config().setTheme(Theme.DARK);
        logger = extent.createTest("Validate Credit Card Details");
        extent.attachReporter(spark);
        extent.setSystemInfo("Build_Name", "Capstone Project - Create Credit Card");
        extent.setSystemInfo("Environment_Name", "QA");
        extent.setSystemInfo("Name", "Siddhesh Berde");

        connection =DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "1234"); //driverURL,username,password
        if (connection != null)
        {
            System.out.println("Credit Card Database connection is connected!");
        }
        Statement smt=connection.createStatement();
    }


    @Test(priority = 1)
    public void insert_credit_card_details() throws SQLException, IOException
    {
        System.out.println("Inserting Credit Card Details in DB.");
        Statement stmt = connection.createStatement();
        FileInputStream fis = new FileInputStream(new File("C:/Users/siddheshv/Documents/CapstoneProject/Creditcardinfo.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);


        for (Row row : sheet)
        {
            if(row.getRowNum() != 0)
            {

                String name = row.getCell(0).getStringCellValue();
                String year = formatter.formatCellValue(row.getCell(1));
                String credit_card_no = formatter.formatCellValue(row.getCell(2));
                String credit_card_limit = row.getCell(3).getStringCellValue();
                String expiry_date = row.getCell(4).getStringCellValue();
                String credit_card_type = row.getCell(5).getStringCellValue();
                String query = "insert into aadhar_db.Creditcard values (\""+name+"\","+year+","+credit_card_no+",\""+credit_card_limit+"\",\""+expiry_date+"\",\""+credit_card_type+"\")";
                stmt.execute(query);
            }


        }
        System.out.println("Credit Card Details Inserted Successfully");
        logger.info("Credit Card Details Inserted Successfully");
    }


    @Test(priority = 2)
    public void insert_credit_card_and_pan() throws SQLException, IOException
    {
        System.out.println("Inserting Credit Card & Pan Details in DB.");
        Statement stmt = connection.createStatement();
        FileInputStream fis = new FileInputStream(new File("C:/Users/siddheshv/Documents/CapstoneProject/Cardnumberandpan.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        for (Row row : sheet)
        {
            if(row.getRowNum() != 0)
            {
                String credit_card_no = formatter.formatCellValue(row.getCell(0));
                String pan_no = row.getCell(1).getStringCellValue();
                String query = "insert into aadhar_db.Creditcardpan values ("+credit_card_no+",\""+pan_no+"\")";
                stmt.execute(query);
            }
        }
        System.out.println("Credit Card & Pan Details Inserted Successfully");
        logger.info("Credit Card & Pan Details Inserted Successfully");
    }


    @Test(priority = 3)
    public void validate_credit_card_details() throws SQLException, IOException
    {
        System.out.println("Credit Card Valdiation starts!!");
        Statement stmt = connection.createStatement();
        Statement stmt2 = connection.createStatement();
        String post_url = "https://api.restful-api.dev/objects";
        FileInputStream fis = new FileInputStream(new File("C:/Users/siddheshv/Documents/CapstoneProject/Cardnumberandtype.xlsx"));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        for (Row row : sheet)
        {
            if (row.getRowNum() != 0)
            {
                logger.info("***** Validation for Record Number: " + row.getRowNum() + "*****");
                long credit_card_no = (long) row.getCell(0).getNumericCellValue();
                String query = "select * from aadhar_db.Creditcard where credit_card_no = " + credit_card_no + ";";
                ResultSet result = stmt.executeQuery(query);
                while (result.next()) {
                    // Storing Values from Database
                    String name_db = result.getString(1);
                    int year_db = result.getInt(2);
                    long credit_card_no_db = result.getLong(3);
                    String credit_limit_db = result.getString(4);
                    String expiry_date_db = result.getString(5);
                    String card_type_db = result.getString(6);

                    // Sending DB Values to Post Request Body
                    String post_request_body = RA.creditCardDetails(name_db, year_db, credit_card_no_db, credit_limit_db, expiry_date_db, card_type_db);
                    post_credit_response = given().contentType(ContentType.JSON).body(post_request_body).when().post(post_url);
                    System.out.println(post_credit_response.body().asString());



                    // Storing Values from Post Response
                    String name_api = post_credit_response.getBody().jsonPath().getString("name");
                    //int year_api = post_credit_response.getBody().jsonPath().getInt("data.year");
                    long credit_card_no_api = post_credit_response.getBody().jsonPath().getLong("data['Credit Card Number']");
                    String credit_limit_api = post_credit_response.getBody().jsonPath().getString("data.Limit");
                    String expiry_date_api = post_credit_response.getBody().jsonPath().getString("data['EXP Date']");
                    String card_type_api = post_credit_response.getBody().jsonPath().getString("data['Card Type']");

                    // Validating Post Response against DB Values
                    if (name_db.equals(name_api)) {
                        logger.pass("For the field Name==> DB value: " + name_db + " API Value: " + name_api + " || Value matching.");
                    } else {
                        logger.fail("For the field Name==> DB value: " + name_db + " API Value: " + name_api + " || Value not matching.");
                    }
//                    if (year_db == year_api) {
//                        logger.pass("For the field Year==> DB value: " + year_db + " API Value: " + year_api + " || Value matching.");
//                    } else {
//                        logger.fail("For the field Year==> DB value: " + year_db + " API Value: " + year_api + " || Value not matching.");
//                    }
                    if (credit_card_no_db == credit_card_no_api) {
                        logger.pass("For the field Credit Card No==> DB value: " + credit_card_no_db + " API Value: " + credit_card_no_api + " || Value matching.");
                    } else {
                        logger.fail("For the field Credit Card No==> DB value: " + credit_card_no_db + " API Value: " + credit_card_no_api + " || Value not matching.");
                    }
                    if (credit_limit_db.equals(credit_limit_api)) {
                        logger.pass("For the field Credit Card Limit==> DB value: " + credit_limit_db + " API Value: " + credit_limit_api + " || Value matching.");
                    } else {
                        logger.fail("For the field Credit Card Limit==> DB value: " + credit_limit_db + " API Value: " + credit_limit_api + " || Value not matching.");
                    }
                    if (expiry_date_db.equals(expiry_date_api)) {
                        logger.pass("For the field Expiry Date==> DB value: " + expiry_date_db + " API Value: " + expiry_date_api + " || Value matching.");
                    } else {
                        logger.fail("For the field Expiry Date==> DB value: " + expiry_date_db + " API Value: " + expiry_date_api + " || Value not matching.");
                    }
                    if (card_type_db.equals(card_type_api)) {
                        logger.pass("For the field Credit Card Type==> DB value: " + card_type_db + " API Value: " + card_type_api + " || Value matching.");
                    } else {
                        logger.fail("For the field Credit Card Type==> DB value: " + card_type_db + " API Value: " + card_type_api + " || Value not matching.");
                    }

                    // Validating whether Credit Card Number is mapped to a Pan Card
                    String query_1 = "select * from aadhar_db.Creditcardpan where Credit_Card_No = " + credit_card_no_api + ";";
                    ResultSet result2 = stmt2.executeQuery(query_1);
                    while (result2.next()) {
                        long credit_card_no_pan_db = result2.getLong("Credit_Card_No");
                        String pan_db = result2.getString("Pan_No");
                        if (!pan_db.isEmpty()) {
                            logger.pass("Credit Card No:" + credit_card_no_pan_db + " is mapped with Pan Card No:" + pan_db);
                        } else {
                            logger.fail("Credit Card No:" + credit_card_no_pan_db + " is not mapped with any Pan Card.");
                        }
                    }
                }
            }
        }

        System.out.println("Credit Card Validation completed.");
    }


    @AfterClass
    public void report_generation()
    {
        System.out.println("All Tests Executed. Extent Report is generated");
        extent.flush();
    }

}