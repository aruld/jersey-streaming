# Jersey Media Streaming Application

Example showing how to implement a media streaming resource in Jersey JAX-RS application.

Partial content using range headers is the recommended approach to stream media. Here is a simple implementation
that streams an audio file. The core of the logic lies in detecting whether clients support range requests.
Some browsers send range headers when they detect specific media type such as "audio/mp3", "video/mp4" etc.
response from the server. Chrome and Safari made use of these range headers when streaming media perfectly.

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

Run App in your favorite IDE.

Access the streaming resource
---------------------

The media will be streamed at the following URL: <http://localhost:9998/media/listen>.

Request and responses logged for various browsers.

Chrome
------

![ScreenShot](https://raw.github.com/aruld/jersey-streaming/master/src/main/resources/chrome-network-trace.png)

```
    Jan 22, 2013 9:29:54 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 1 * Server in-bound request
    1 > GET /listen
    1 > Host: localhost:9998
    1 > Connection: keep-alive
    1 > Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
    1 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17
    1 > Accept-Encoding: gzip,deflate,sdch
    1 > Accept-Language: en-US,en;q=0.8
    1 > Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
    1 >

    Jan 22, 2013 9:29:55 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 1 * Server out-bound response
    1 < 200
    1 < Content-Length: 2836624
    1 < Content-Type: audio/mp3
    1 <

    Jan 22, 2013 9:29:55 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 2 * Server in-bound request
    2 > GET /listen
    2 > Host: localhost:9998
    2 > Connection: keep-alive
    2 > Accept-Encoding: identity;q=1, *;q=0
    2 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17
    2 > Accept: */*
    2 > Referer: http://localhost:9998/media/listen
    2 > Accept-Language: en-US,en;q=0.8
    2 > Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
    2 > Range: bytes=0-
    2 >

    Jan 22, 2013 9:29:55 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 2 * Server out-bound response
    2 < 206
    2 < Accept-Ranges: bytes
    2 < Content-Range: bytes 0-1048576/2836624
    2 < Content-Length: 1048577
    2 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    2 < Content-Type: audio/mp3
    2 <

    Jan 22, 2013 9:30:57 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 4 * Server in-bound request
    3 > GET /listen
    3 > Host: localhost:9998
    3 > Connection: keep-alive
    3 > Accept-Encoding: identity;q=1, *;q=0
    3 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17
    3 > Accept: */*
    3 > Referer: http://localhost:9998/media/listen
    3 > Accept-Language: en-US,en;q=0.8
    3 > Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
    3 > Range: bytes=1048577-
    3 >

    Jan 22, 2013 9:30:57 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 4 * Server out-bound response
    3 < 206
    3 < Accept-Ranges: bytes
    3 < Content-Range: bytes 1048577-2097153/2836624
    3 < Content-Length: 1048577
    3 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    3 < Content-Type: audio/mp3
    3 <

    Jan 22, 2013 9:32:02 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 5 * Server in-bound request
    4 > GET /listen
    4 > Host: localhost:9998
    4 > Connection: keep-alive
    4 > Accept-Encoding: identity;q=1, *;q=0
    4 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17
    4 > Accept: */*
    4 > Referer: http://localhost:9998/media/listen
    4 > Accept-Language: en-US,en;q=0.8
    4 > Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
    4 > Range: bytes=2097154-
    4 >

    Jan 22, 2013 9:32:02 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 5 * Server out-bound response
    4 < 206
    4 < Accept-Ranges: bytes
    4 < Content-Range: bytes 2097154-2836623/2836624
    4 < Content-Length: 739470
    4 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    4 < Content-Type: audio/mp3
    4 <

```


Safari
------
```
    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 1 * Server in-bound request
    1 > GET /listen
    1 > Host: localhost:9998
    1 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17
    1 > Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
    1 > Accept-Language: en-us
    1 > Accept-Encoding: gzip, deflate
    1 > Connection: keep-alive
    1 >

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 1 * Server out-bound response
    1 < 200
    1 < Content-Length: 2836624
    1 < Content-Type: audio/mp3
    1 <

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 2 * Server in-bound request
    2 > GET /listen
    2 > Host: localhost:9998
    2 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17
    2 > Accept: */*
    2 > Range: bytes=0-1
    2 > Accept-Encoding: identity
    2 > Referer: http://localhost:9998/media/listen
    2 > X-Playback-Session-Id: 3F19F6F4-681F-47DC-8E4F-884AF0D6E88B
    2 > Connection: keep-alive
    2 >

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 2 * Server out-bound response
    2 < 206
    2 < Accept-Ranges: bytes
    2 < Content-Range: bytes 0-1/2836624
    2 < Content-Length: 2
    2 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    2 < Content-Type: audio/mp3
    2 <

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 3 * Server in-bound request
    3 > GET /listen
    3 > Host: localhost:9998
    3 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17
    3 > Accept: */*
    3 > Range: bytes=0-2836623
    3 > Accept-Encoding: identity
    3 > Referer: http://localhost:9998/media/listen
    3 > X-Playback-Session-Id: 3F19F6F4-681F-47DC-8E4F-884AF0D6E88B
    3 > Connection: keep-alive
    3 >

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 3 * Server out-bound response
    3 < 206
    3 < Accept-Ranges: bytes
    3 < Content-Range: bytes 0-2836623/2836624
    3 < Content-Length: 2836624
    3 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    3 < Content-Type: audio/mp3
    3 <

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 4 * Server in-bound request
    4 > GET /listen
    4 > Host: localhost:9998
    4 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17
    4 > Accept: */*
    4 > Range: bytes=131072-2836623
    4 > Accept-Encoding: identity
    4 > Referer: http://localhost:9998/media/listen
    4 > X-Playback-Session-Id: 3F19F6F4-681F-47DC-8E4F-884AF0D6E88B
    4 > Connection: keep-alive
    4 >

    Jan 22, 2013 9:48:00 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 4 * Server out-bound response
    4 < 206
    4 < Accept-Ranges: bytes
    4 < Content-Range: bytes 131072-2836623/2836624
    4 < Content-Length: 2705552
    4 < Last-Modified: Wed, 23 Jan 2013 07:29:16 GMT
    4 < Content-Type: audio/mp3
    4 <

```

Firefox
------
```
    Jan 22, 2013 9:41:24 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 1 * Server in-bound request
    1 > GET /listen
    1 > Host: localhost:9998
    1 > User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:18.0) Gecko/20100101 Firefox/18.0
    1 > Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
    1 > Accept-Language: en-US,en;q=0.5
    1 > Accept-Encoding: gzip, deflate
    1 > Connection: keep-alive
    1 >

    Jan 22, 2013 9:41:24 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 1 * Server out-bound response
    1 < 200
    1 < Content-Length: 2836624
    1 < Content-Type: audio/mp3
    1 <


```

IE 10
------
```
    Jan 22, 2013 9:53:19 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 1 * Server in-bound request
    1 > GET /listen
    1 > Accept: application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*
    1 > Accept-Language: en-US
    1 > User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/6.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)
    1 > Accept-Encoding: gzip, deflate
    1 > Host: 192.168.1.4:9998
    1 > DNT: 1
    1 > Connection: Keep-Alive
    1 >

    Jan 22, 2013 9:53:19 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 1 * Server out-bound response
    1 < 200
    1 < Content-Length: 2836624
    1 < Content-Type: audio/mp3
    1 <

    Jan 22, 2013 9:53:22 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 2 * Server in-bound request
    2 > GET /listen
    2 > Accept: */*
    2 > User-Agent: Windows-Media-Player/12.0.7601.17514
    2 > Accept-Encoding: gzip, deflate
    2 > Host: 192.168.1.4:9998
    2 > Connection: Keep-Alive
    2 >

    Jan 22, 2013 9:53:22 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 2 * Server out-bound response
    2 < 200
    2 < Content-Length: 2836624
    2 < Content-Type: audio/mp3
    2 <

    Jan 22, 2013 9:53:22 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 3 * Server in-bound request
    3 > GET /listen
    3 > Cache-Control: no-cache
    3 > Connection: Keep-Alive
    3 > Pragma: getIfoFileURI.dlna.org
    3 > Accept: */*
    3 > User-Agent: NSPlayer/12.00.7601.17514 WMFSDK/12.00.7601.17514
    3 > GetContentFeatures.DLNA.ORG: 1
    3 > Host: 192.168.1.4:9998
    3 >

    Jan 22, 2013 9:53:22 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 3 * Server out-bound response
    3 < 200
    3 < Content-Length: 2836624
    3 < Content-Type: audio/mp3
    3 <


```

Opera
------
```
    Jan 22, 2013 9:43:38 PM com.sun.jersey.api.container.filter.LoggingFilter filter
    INFO: 1 * Server in-bound request
    1 > GET /listen
    1 > User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X 10.8.2) Presto/2.12.388 Version/12.12
    1 > Host: localhost:9998
    1 > Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/webp, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
    1 > Accept-Language: en,en-US;q=0.9,ja;q=0.8,fr;q=0.7,de;q=0.6,es;q=0.5,it;q=0.4,pt;q=0.3,pt-PT;q=0.2,nl;q=0.1
    1 > Accept-Encoding: gzip, deflate
    1 > Connection: Keep-Alive
    1 >

    Jan 22, 2013 9:43:38 PM com.sun.jersey.api.container.filter.LoggingFilter$Adapter writeStatusAndHeaders
    INFO: 1 * Server out-bound response
    1 < 200
    1 < Content-Length: 2836624
    1 < Content-Type: audio/mp3
    1 <


```


Mp3 Credit
----------
http://publicdomain4u.com/scott-joplin-the-entertainer/mp3-download