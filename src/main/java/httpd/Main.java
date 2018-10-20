package httpd;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.activation.MimetypesFileTypeMap;

public class Main {

    public static void main(String[] args)   {
        try {
            Properties prop = new Properties();
            String propFileName = args[0]+".properties";
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = loader.getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
                InetAddress localHost = InetAddress.getLocalHost();

                HttpLog logs = new HttpLog("logFile.txt");

                ServerSocket listen = new ServerSocket(Integer.parseInt(prop.getProperty("Port")));
                System.out.println("Listening on port : " + listen.getLocalPort());

                for(;;) {
                    Socket client = listen.accept();
                    System.out.println(client.toString());
                    HttpConnection connec = new HttpConnection(client, logs);
                    new Thread(connec).start();
                }
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in resources folder");
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
