package com.aruld.jersey.streaming;

/**
 * HTTP Range Headers
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
public interface HttpRangeHeaders {
    /**
     * See {@link <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.5">HTTP/1.1 documentation</a>}.
     */
    String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * See {@link <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.16">HTTP/1.1 documentation</a>}.
     */
    String CONTENT_RANGE = "Content-Range";

    /**
     * See {@link <a href="https://tools.ietf.org/html/rfc7233#section-3.2">HTTP/1.1 documentation</a>}.
     */
    String IF_MATCH = "If-Range";

    /**
     * See {@link <a href="https://tools.ietf.org/html/rfc7233#section-3.1">HTTP/1.1 documentation</a>}.
     */
    String RANGE = "Range";
}
