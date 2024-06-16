package Stepdef;

import Reusable.Reusable;
import com.aventstack.extentreports.ExtentTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CreateNewAccount
{
    public Connection connection;
    private ExtentTest logger;

    Reusable RA;

    @BeforeClass
    public void setUp()
    {
        RA=new Reusable();
    }

    @Test(priority = 1)
    public boolean testAadharRecordMatching()
    {
        boolean flag=false;


        //Read properties file
        try {
            Properties properties = new Properties();
            FileInputStream input = new FileInputStream("Capstone_data.properties");
            properties.load(input);
            input.close();

            //Connect to database
            connection =DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "1234"); //driverURL,username,password
            if (connection != null)
            {
                System.out.println("Aadhar Database connection is connected!");
            }
            Statement smt=connection.createStatement();
            smt.execute("use aadhar_db");


            //Retrieve data from database
            String query = "SELECT * FROM Aadhar WHERE Aadhar_No = '" + properties.getProperty("Aadhar_No") + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);


            //Compare data
            if (resultSet.next())
            {
                String aadharNo = resultSet.getString("Aadhar_No");
                // Assert that data matches with properties file

                Assert.assertEquals(aadharNo, properties.getProperty("Aadhar_No"));
                System.out.println("Record found in database for Aadhar Number: " +aadharNo);
                flag=true;
            }
            else
            {
                Assert.fail("Record not found in database for Aadhar Number: " + properties.getProperty("Aadhar_No"));
                flag=false;
            }

            // Close database connections
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return flag;
    }

    @Parameters({"posturl"})
    @Test(priority = 2)
    public void postCall(String url) {
        boolean a = testAadharRecordMatching();

        if (a == true) {
            try {
                SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-dd");

                connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "1234"); //driverURL,username,password
                if (connection != null) {
                    System.out.println("Connected!");
                }
                Statement smt = connection.createStatement();
                smt.execute("use aadhar_db");

                //Retrieve data from database
                String query = "SELECT * FROM Aadhar WHERE Aadhar_No = '" + RA.readPropertiesfile("Aadhar_No") + "'";


                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    // Retrieve data from the result set
                    String fname = resultSet.getString("Fname");
                    String lname = resultSet.getString("Lname");
                    String aadharNo = resultSet.getString("Aadhar_No");
                    String address = resultSet.getString("Address");
                    String phone = resultSet.getString("Phone");
                    Map<String, String> DBMap = new HashMap<>();

                    // Put the variables into the map
                    DBMap.put("Fname", fname);
                    DBMap.put("Lname", lname);
                    DBMap.put("Aadhar_No", aadharNo);
                    DBMap.put("Address", address);
                    DBMap.put("Phone", phone);

                    Response response = given().header("Content-type", "application/json").
                            body(RA.CreatePostJson(fname, lname, aadharNo, address, phone)).
                            when().
                            post(url);

                    String responseBody = response.getBody().asString();
                    System.out.println("POST Response Body: " + responseBody);

                    String createdDate=RA.date_extractor(response.getBody().jsonPath().getString("createdAt"));


                    //Read the response and match the response Fname,Lname,Aadhar_No,Address,Phone
                    //data with DB.
                    JsonPath jsonPath = response.jsonPath();
                    Map<String, Object> responseMap = jsonPath.getMap("");

                    for (Map.Entry<String, String> entry : DBMap.entrySet()) {
                        String key = entry.getKey();
                        String value1 = entry.getValue();
                        Object value2 = responseMap.get(key);

                        if (value2 == null) {
                            Assert.fail("Key '" + key + "' not found in responseMap");
                        } else {
                            Assert.assertEquals(value1, value2, "Values for key '" + key + "' do not match");
                        }
                    }



                    for (Map.Entry<String, Object> entry : responseMap.entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());

                        if (entry.getKey().equals("Fname")) {   //Response coming
                            if (entry.getValue().equals(lname)) {   //
                                Assert.assertEquals(entry.getValue(), fname, "Data is matching");
                            } else {
                                Assert.assertEquals(entry.getValue(), lname, "Data is not matching");
                            }
                        }

                        //Validating if Account ID is numeric
                        if (entry.getKey().equals("id")) {
                            Assert.assertTrue(((String) entry.getValue()).matches("[0-9]+"), "Account Number is matching ");
                        }
                    }

                    //Validating if CreatedAt Field contains current Date
                    Date date = new Date();
                    String current_date = sf.format(date);

                    if(current_date.equals(createdDate))
                    {
                        logger.pass("For the field createdAt==> Current-Date: " + current_date + " API Value: " + createdDate + " || Value matching.");
                    }
                    else
                    {
                        logger.fail("For the field createdAt==> Current-Date: " + current_date + " API Value: " + createdDate + " || Value not matching.");
                    }
                }
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }

        else {
            System.out.println("Aadhar No is not matching !");

        }
    }

}