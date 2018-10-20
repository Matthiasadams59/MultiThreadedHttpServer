package httpd;

import java.io.*;
import java.util.Date;
import java.util.Properties;

class HttpLog {

    private File logging;

    HttpLog (String logFile)
    {
        try {
            File fichierLog = new File(logFile);

            if (fichierLog.exists()) {
                logging = fichierLog;
            } else {
                throw new FileNotFoundException("Log file '" + logFile + "' not found in resources folder");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    void add (String address, String request, int status)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(logging, true));
            out.newLine();
            Date today = new Date();
            out.write("["+address+"] ["+today+"] [" + request + "] [" + status + "]" );
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
