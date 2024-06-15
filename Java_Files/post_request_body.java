package Capstone_Project;

public class post_request_body
{
    public static String aadhar_details(String firstname,String lastname,long aadhar,String address,long phone)
    {
        String post_request_body = "{\n" +
                "\"Fname\": \""+firstname+"\",\n" +
                "\"Lname\": \""+lastname+"\",\n" +
                "\"Aadhar_No\": "+aadhar+",\n" +
                "\"Address\": \""+address+"\",\n" +
                "\"Phone\": "+phone+"\n" +
                "}";
        return post_request_body;
    }
    public static String credit_card_details(String name,int year,long credit_card_no,String limit,String expiry_date,String card_type)
    {
      String post_request_body = "{\n" +
              "\"name\": \""+name+"\",\n" +
              "\"data\": {\n" +
              "\"year\": "+year+",\n" +
              "\"Credit Card Number\": "+credit_card_no+",\n" +
              "\"Limit\": \""+limit+"\",\n" +
              "\"EXP Date\": \""+expiry_date+"\",\n" +
              "\"Card Type\": \""+card_type+"\"\n" +
              "}\n" +
              "}";
      return post_request_body;
    }
}
