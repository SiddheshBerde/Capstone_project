package Capstone_Project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class read_data_from_property_file
{
    public static String read_properties(String key)
    {
        Properties prop = new Properties();
        String value = null;
        try
        {
            prop.load(new FileInputStream(System.getProperty("user.dir")+"/capstone_project_data.properties"));
            value = prop.getProperty(key);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return value;
    }
}
