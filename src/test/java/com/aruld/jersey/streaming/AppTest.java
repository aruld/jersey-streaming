package com.aruld.jersey.streaming;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import sun.misc.IOUtils;

import javax.ws.rs.core.HttpHeaders;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Media client
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 */
public class AppTest {
    public static void main(String[] args) throws Exception {
        Client client = Client.create();
        WebResource target = client.resource("http://localhost:9998/listen");
        ClientResponse response = target.head();
        if (response.getClientResponseStatus().getStatusCode() == 206) {
            System.out.println("Range supported!");

            // Simulate Safari request
            response = target.header("Range", "bytes=0-1").get(ClientResponse.class);
            InputStream is = response.getEntityInputStream();
            String header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            int length = Integer.parseInt(header);
            System.out.println("Length = " + length);

            OutputStream os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();

            response = target.header("Range", "bytes=0-2836623").get(ClientResponse.class);

            header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            length = Integer.parseInt(header);
            System.out.println("Length = " + length);
            is = response.getEntityInputStream();
            os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();

            response = target.header("Range", "bytes=131072-2836623").get(ClientResponse.class);
            header = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            length = Integer.parseInt(header);
            System.out.println("Length = " + length);
            is = response.getEntityInputStream();
            os = new FileOutputStream("temp.mp3");
            os.write(IOUtils.readFully(is, -1, false));
            is.close();
            os.close();
        }
    }
}
