package com.aruld.jersey.streaming;

import sun.misc.IOUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Media client
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
public class AppTest {
    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9998/listen");
        Response response = target.request().head();
        if (response.getStatus() == 206) {
            System.out.println("Range supported!");

            // Simulate Safari request
            response = target.request().header("Range", "bytes=0-1").get(Response.class);
            InputStream is = response.readEntity(InputStream.class);
            Object header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            int length = Integer.parseInt((String) header);
            System.out.println("Length = " + length);

            OutputStream os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();

            response = target.request().header("Range", "bytes=0-2836623").get(Response.class);

            header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            length = Integer.parseInt((String) header);
            System.out.println("Length = " + length);
            is = response.readEntity(InputStream.class);
            os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();

            response = target.request().header("Range", "bytes=131072-2836623").get(Response.class);
            header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            length = Integer.parseInt((String) header);
            System.out.println("Length = " + length);
            is = response.readEntity(InputStream.class);
            os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();
        }
    }
}
