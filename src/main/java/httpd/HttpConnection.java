package httpd;

import javax.activation.MimetypesFileTypeMap;
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

class HttpConnection implements Runnable {

    Socket client;
    HttpLog logan;

    public HttpConnection(Socket sock, HttpLog leFichierLog) {
        client = sock;
        logan = leFichierLog;
    }

    public void run() {
        try {
            Date today = new Date();
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String query = in.readLine();
            System.out.println(query);

            List<String> tokens = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(query, " ");
            while (tokenizer.hasMoreElements()) {
                tokens.add(tokenizer.nextToken());
            }
            //                    System.out.println("Chemin : " + "/" + prop.getProperty("WebRoot")+tokens.get(1));

            //                    File f = new File(loader.getResource(tokens.get(1)).getFile());
            File leFichier = new File(tokens.get(1).substring(1));

//            System.out.println("Chemin absolu : " + leFichier.getAbsolutePath());
//            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            if (leFichier.isDirectory() || leFichier.exists()) {
                System.out.println("ok");

                System.out.println("Mime Type of " + leFichier.getName() + " is " + new MimetypesFileTypeMap().getContentType(leFichier));

                if (leFichier.isDirectory()) {
                    ArrayList<File> files = new ArrayList<File>(Arrays.asList(leFichier.listFiles()));
                    ArrayList<String> filesNames = new ArrayList<String>(Arrays.asList(leFichier.list()));
                    for (String nom : filesNames) {
                        if (nom.contains("index")) {
                            leFichier = new File(tokens.get(1).substring(1)+"/index.html");
                        }
                    }
                }

                MimetypesFileTypeMap mimeType = new MimetypesFileTypeMap();
                String contentType = mimeType.getContentType(tokens.get(1));

                String successMessage = "HTTP/1.0 200 OK\r\n" +
                        "Date: " + today + "\r\n" +
                        "Server: JavaHttpd/1.0\r\n" +
                        "Content-type: " + contentType + "\r\n\n";

                BufferedReader htmlReader = new BufferedReader(new FileReader(leFichier));
                if (contentType.contains("text") || contentType.contains("htm")) {
                    String htmLine = null;
                    while ((htmLine = htmlReader.readLine()) != null) {
                        successMessage += htmLine;
                    }
                } else if (leFichier.isDirectory()) {
                    String contentsDirectory = "<HEAD><TITLE>Contents of the directory</TITLE></HEAD\r\n>" +
                            "<BODY><H1>Contents of the directory : </H1>\r\n";
                    ArrayList<File> files = new ArrayList<File>(Arrays.asList(leFichier.listFiles()));
                    ArrayList<String> filesNames = new ArrayList<String>(Arrays.asList(leFichier.list()));
                    for (int i = 0 ; i < files.size() ; i++) {
                        contentsDirectory+="<P>"+filesNames.get(i)+" : " + files.get(i)+" .</P>\r\n";
                    }


                    contentsDirectory += "</BODY>\r\n";
                    successMessage+=contentsDirectory;
                }

                logan.add(client.getLocalAddress().toString(), query, 200);
                out.write(successMessage);

            } else if (tokens.size() != 3 || (!tokens.get(0).equals("GET"))) {
                System.out.println("400");
                String errorMessage = "HTTP/1.0 400 Bad Request\r\n" +
                        "Date: " + today + "\r\n" +
                        "Server: JavaHttpd/1.0\r\n" +
                        "Content-type: text/html\r\n\n" +
                        "<HEAD><TITLE>Bad Request</TITLE></HEAD\r\n>" +
                        "<BODY><H1>Bad Request</H1>\r\n" +
                        "Votre navigateur Internet a envoyé une requête que ce serveur ne peut pas traiter.\r\n" +
                        "<P>\r\n" +
                        "</BODY>\r\n";

                logan.add(client.getLocalAddress().toString(), query, 400);
                out.write(errorMessage);
            } else {
                System.out.println("404");
                String errorMessage = "HTTP/1.0 404 Not found\r\n" +
                        "Date: " + today + "\r\n" +
                        "Server: JavaHttpd/1.0\r\n" +
                        "Content-type: text/html\r\n\n" +
                        "<HEAD><TITLE>File not found </TITLE></HEAD\r\n>" +
                        "<BODY><H1>File not found </H1>\r\n" +
                        "The resource " + tokens.get(1) + " is not present on this server..\r\n" +
                        "<P>\r\n" +
                        "</BODY>\r\n";

                logan.add(client.getLocalAddress().toString(), query, 404);
                out.write(errorMessage);
            }
            out.flush();
            client.close();
        } catch (IOException IOe) {
            System.out.println(IOe);
        }
    }
}
