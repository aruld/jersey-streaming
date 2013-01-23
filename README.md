# Jersey Media Streaming Sample App


One of the best ways to stream media

Example showing how to implement a media streaming resource in Jersey JAX-RS application.

Here is how media can be streamed using partial:
```java
    // Firefox, Opera, IE do not send range headers
    if (range == null) {
        StreamingOutput streamer = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {

                final FileChannel inputChannel = new FileInputStream(asset).getChannel();
                final WritableByteChannel outputChannel = Channels.newChannel(output);
                try {
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                } finally {
                    // closing the channels
                    inputChannel.close();
                    outputChannel.close();
                }
            }
        };
        return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
    }

    String[] ranges = range.split("=")[1].split("-");
    final int from = Integer.parseInt(ranges[0]);
    /**
     * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
     */
    int to = chunk_size + from;
    if (to >= asset.length()) {
        to = (int) (asset.length() - 1);
    }
    if (ranges.length == 2) {
        to = Integer.parseInt(ranges[1]);
    }

    final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
    final RandomAccessFile raf = new RandomAccessFile(asset, "r");
    raf.seek(from);

    final int len = to - from + 1;
    final MediaStreamer streamer = new MediaStreamer(len, raf);
    Response.ResponseBuilder res = Response.ok(streamer).status(206)
            .header("Accept-Ranges", "bytes")
            .header("Content-Range", responseRange)
            .header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
            .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
```


Setup
------

* Clone this repository
* mvn install

Testing
-------

Access the streaming resource
---------------------

The application will be running at the following URL: <http://localhost:9998/media/listen>.



```
Jan 17, 2013 8:31:31 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer start
INFO: Starting GrizzlyTestContainer...
Jan 17, 2013 8:31:32 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [localhost:9998]
Jan 17, 2013 8:31:32 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer] Started.
Jan 17, 2013 8:31:32 PM org.glassfish.jersey.filter.LoggingFilter log
INFO: 1 * LoggingFilter - Request received on thread main
1 > POST http://localhost:9998/multipart
1 > Content-Type: multipart/form-data
--Boundary_1_1939873418_1358490692628
Content-Type: application/xml
Content-Disposition: form-data; filename="input.xml"; modification-date="Fri, 18 Jan 2013 06:31:22 GMT"; size=109; name="xml"

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<person>
    <name>Arul Dhesiaseelan</name>
</person>
--Boundary_1_1939873418_1358490692628--


Processing file # input.xml
Jan 17, 2013 8:31:33 PM org.glassfish.jersey.filter.LoggingFilter log
INFO: 2 * LoggingFilter - Response received on thread main
2 < 200
2 < Date: Fri, 18 Jan 2013 06:31:33 GMT
2 < Transfer-Encoding: chunked
2 < Content-Type: application/xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?><person><name>Arul Dhesiaseelan</name><uid>af462941-8580-400c-86a1-0a550c27a24a</uid></person>

Jan 17, 2013 8:31:33 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer stop
INFO: Stopping GrizzlyTestContainer...
Jan 17, 2013 8:31:33 PM org.glassfish.grizzly.http.server.NetworkListener stop
INFO: Stopped listener bound to [localhost:9998]
Jan 17, 2013 8:31:30 PM org.glassfish.jersey.server.ApplicationHandler initialize
INFO: Initiating Jersey application, version Jersey: 2.0-m11 2012-12-21 12:34:15...
```


Mp3 Credit
----------
http://publicdomain4u.com/scott-joplin-the-entertainer/mp3-download