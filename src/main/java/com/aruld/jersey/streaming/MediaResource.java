package com.aruld.jersey.streaming;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

/**
 * Streaming resource
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
@Path("/")
public class MediaResource {

    private final File audio;
    private final File video;

    private final static String BYTES = "bytes";

    public MediaResource() {
        // serve media from file system
        String AUDIO_FILE = "/ScottJoplin-TheEntertainer1902.mp3";
        URL audioUrl = this.getClass().getResource(AUDIO_FILE);
        audio = new File(audioUrl.getFile());
        String VIDEO_FILE = "/bbb_sunflower_1080p_30fps_normal.mp4";
        URL videoUrl = this.getClass().getResource(VIDEO_FILE);
        video = new File(videoUrl.getFile());
    }

    // A simple way to verify if the server supports range headers (https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests)
    @HEAD
    @Produces("audio/mp3")
    @Path("audio")
    public Response audio() {
        return Response.ok().status(200).header(HttpRangeHeaders.ACCEPT_RANGES, BYTES).header(HttpHeaders.CONTENT_LENGTH, audio.length()).build();
    }

    @GET
    @Produces("audio/mp3")
    @Path("audio")
    public Response streamAudio(@HeaderParam(HttpRangeHeaders.RANGE) String range) throws Exception {
        return buildStream(audio, range);
    }

    // A simple way to verify if the server supports range headers (https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests)
    @HEAD
    @Produces("video/mp4")
    @Path("video")
    public Response video() {
        return Response.ok().status(200).header(HttpRangeHeaders.ACCEPT_RANGES, BYTES).header(HttpHeaders.CONTENT_LENGTH, video.length()).build();
    }

    @GET
    @Produces("video/mp4")
    @Path("video")
    public Response streamVideo(@HeaderParam(HttpRangeHeaders.RANGE) String range) throws Exception {
        return buildStream(video, range);
    }

    /**
     * Adapted from http://stackoverflow.com/questions/12768812/video-streaming-to-ipad-does-not-work-with-tapestry5/12829541#12829541
     * @param asset Media file
     * @param range range header
     * @return Streaming output
     * @throws Exception IOException if an error occurs in streaming.
     */
    private Response buildStream(final File asset, final String range) throws Exception {
        // range not requested : Firefox does not send range headers
        if (range == null) {
            StreamingOutput streamer = output -> {
                try (FileChannel inputChannel = new FileInputStream(asset).getChannel(); WritableByteChannel outputChannel = Channels.newChannel(output)) {
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            };
            return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
        }

        String[] ranges = range.split("=")[1].split("-");
        final int from = Integer.parseInt(ranges[0]);

        /*
          Chunk media if the range upper bound is unspecified. Chrome, Opera sends "bytes=0-"
         */
        int chunk_size = 1024 * 1024;
        int to = chunk_size + from;
        if (to >= asset.length()) {
            to = (int) (asset.length() - 1);
        }
        if (ranges.length == 2) {
            to = Integer.parseInt(ranges[1]);
            // check for out of bounds range request
            if (to > asset.length()) {
                return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
        }

        final String responseRange = String.format("%s %d-%d/%d", BYTES, from, to, asset.length());
        final RandomAccessFile raf = new RandomAccessFile(asset, "r");
        raf.seek(from);

        final int len = to - from + 1;
        final MediaStreamer streamer = new MediaStreamer(len, raf);
        Response.ResponseBuilder res = Response.ok(streamer)
                .status(Response.Status.PARTIAL_CONTENT)
                .header(HttpRangeHeaders.ACCEPT_RANGES, BYTES)
                .header(HttpRangeHeaders.CONTENT_RANGE, responseRange)
                .header(HttpHeaders.CONTENT_LENGTH, streamer.getLength())
                .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
        return res.build();
    }
}