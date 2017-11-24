package com.di.mesa.metric.client.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.di.mesa.metric.model.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class handles the communication between the application and a Restful API
 * based web server.
 *
 * @param : type of the returning response object. Note: the idea of this
 *          abstract class is to provide a wrapper for the logic around HTTP
 *          layer communication so development work can take this as a black
 *          box and focus on processing the result. With that said the
 *          abstract class will be provided as a template, which ideally can
 *          support different types of returning object (Dictionary, xmlDoc ,
 *          text etc.)
 */
public abstract class RestfulApiClient<T> {
    protected static final Logger logger = LoggerFactory.getLogger(RestfulApiClient.class);

    /**
     * Method to transform the response returned by the httpClient into the type
     * specified. Note: Method need to handle case such as failed request. Also
     * method is not supposed to pass the response object out via the returning
     * value as the response will be closed after the execution steps out of the
     * method context.
     *
     * @throws HttpResponseException
     * @throws IOException
     * @throws ParseException
     **/
    protected abstract T parseResponse(HttpResponse response) throws HttpResponseException, IOException;

    protected abstract T sendAndReturn(HttpUriRequest request, String mode) throws IOException;

    /**
     * function to perform a Get http request.
     *
     * @param uri           the URI of the request.
     * @param headerEntries extra entries to be added to request header.
     * @return the response object type of which is specified by user.
     * @throws IOException
     */
    public T httpGet(URI uri, List<NameValuePair> headerEntries) throws IOException {
        // shortcut if the passed url is invalid.
        if (null == uri) {
            logger.error(" unable to perform httpGet as the passed uri is null");
            return null;
        }

        HttpGet get = new HttpGet(uri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();// 设置请求和传输超时时间
        get.setConfig(requestConfig);

        return this.sendAndReturn((HttpGet) completeRequest(get, headerEntries));
    }

    public Map<String, String> httpGet(URI uri) throws IOException {
        Map<String, String> resultMap = Maps.newHashMap();

        HttpGet get = new HttpGet(uri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();// 设置请求和传输超时时间
        get.setConfig(requestConfig);

        CloseableHttpClient client = HttpClients.createDefault();

        HttpResponse response = client.execute(get);

        resultMap.put("status", String.valueOf(response.getStatusLine().getStatusCode()));
        resultMap.put("reponseBody", response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "");

        client.close();

        return resultMap;
    }

    public T httpGet(URI uri, List<NameValuePair> headerEntries, String mode) throws IOException {
        // shortcut if the passed url is invalid.
        if (null == uri) {
            logger.error(" unable to perform httpGet as the passed uri is null");
            return null;
        }

        HttpGet get = new HttpGet(uri);
        return sendAndReturn((HttpGet) completeRequest(get, headerEntries), mode);
    }

    public T httpGet(URI uri, List<NameValuePair> headerEntries, int timeout) throws IOException {
        if (null == uri) {
            logger.error(" unable to perform httpGet as the passed uri is null");
            return null;
        }

        HttpGet get = new HttpGet(uri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
                .build();
        get.setConfig(requestConfig);
        return this.sendAndReturn((HttpGet) completeRequest(get, headerEntries));
    }

    /**
     * function to perform a Post http request.
     *
     * @param uri           the URI of the request.
     * @param headerEntries extra entries to be added to request header.
     * @param postingBody   the content to be posted , optional.
     * @return the response object type of which is specified by user.
     * @throws UnsupportedEncodingException , IOException
     */
    public T httpPost(URI uri, List<NameValuePair> headerEntries, String postingBody)
            throws UnsupportedEncodingException, IOException {
        // shortcut if the passed url is invalid.
        if (null == uri) {
            logger.error(" unable to perform httpPost as the passed uri is null.");
            return null;
        }

        HttpPost post = new HttpPost(uri);
        return this.sendAndReturn(completeRequest(post, headerEntries, postingBody));
    }

    /**
     * function to perform a Delete http request.
     *
     * @param uri           the URI of the request.
     * @param headerEntries extra entries to be added to request header.
     * @return the response object type of which is specified by user.
     * @throws IOException
     */
    public T httpDelete(URI uri, List<NameValuePair> headerEntries) throws IOException {
        // shortcut if the passed url is invalid.
        if (null == uri) {
            logger.error(" unable to perform httpDelete as the passed uri is null.");
            return null;
        }

        HttpDelete delete = new HttpDelete(uri);
        return this.sendAndReturn((HttpDelete) completeRequest(delete, headerEntries));
    }

    /**
     * function to perform a Put http request.
     *
     * @param uri           the URI of the request.
     * @param headerEntries extra entries to be added to request header.
     * @param postingBody   the content to be posted , optional.
     * @return the response object type of which is specified by user.
     * @throws UnsupportedEncodingException , IOException
     */
    public T httpPut(URI uri, List<NameValuePair> headerEntries, String postingBody)
            throws UnsupportedEncodingException, IOException {
        // shortcut if the passed url is invalid.
        if (null == uri) {
            logger.error(" unable to perform httpPut as the passed url is null or empty.");
            return null;
        }

        HttpPut put = new HttpPut(uri);
        return this.sendAndReturn(completeRequest(put, headerEntries, postingBody));
    }

    /**
     * function to dispatch the request and pass back the response.
     */
    protected T sendAndReturn(HttpUriRequest request) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            return this.parseResponse(client.execute(request));
        } finally {
            client.close();
        }
    }

    protected static CloseableHttpAsyncClient httpAsyncclient = null;

    public static CloseableHttpAsyncClient getHttpAsyncClient() {
        if (null == httpAsyncclient) {
            httpAsyncclient = HttpAsyncClients.createDefault();
            httpAsyncclient.start();
        }
        return httpAsyncclient;
    }

    public static void closeHttpAsyncClient() throws IOException {
        if (null != httpAsyncclient) {
            httpAsyncclient.close();
        }
    }

    private String parseHttpResponse(HttpResponse response) throws HttpResponseException, IOException {
        final StatusLine statusLine = response.getStatusLine();
        String responseBody = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

        if (statusLine.getStatusCode() >= 300) {
            logger.error(String.format("unable to parse response as the response status is %s",
                    statusLine.getStatusCode()));
            throw new HttpResponseException(statusLine.getStatusCode(), responseBody);
        }

        return responseBody;
    }

    /**
     * helper function to build a valid URI.
     *
     * @param host   host name.
     * @param port   host port.
     * @param path   extra path after host.
     * @param isHttp indicates if whether Http or HTTPS should be used.
     * @param params extra query parameters.
     * @return the URI built from the inputs.
     * @throws IOException
     */
    public static URI buildUri(String host, int port, String path, boolean isHttp, Pair<String, String>... params)
            throws IOException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(isHttp ? "http" : "https").setHost(host).setPort(port);

        if (null != path && path.length() > 0) {
            builder.setPath(path);
        }

        if (params != null) {
            for (Pair<String, String> pair : params) {
                builder.setParameter(pair.getFirst(), pair.getSecond());
            }
        }

        URI uri = null;
        try {
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        return uri;
    }

    /**
     * helper function to build a valid URI.
     *
     * @param uri    the URI to start with.
     * @param params extra query parameters to append.
     * @return the URI built from the inputs.
     * @throws IOException
     */
    public static URI BuildUri(URI uri, Pair<String, String>... params) throws IOException {
        URIBuilder builder = new URIBuilder(uri);

        if (params != null) {
            for (Pair<String, String> pair : params) {
                builder.setParameter(pair.getFirst(), pair.getSecond());
            }
        }

        URI returningUri = null;
        try {
            returningUri = builder.build();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        return returningUri;
    }

    /**
     * helper function to fill the request with header entries .
     */
    private static HttpMessage completeRequest(HttpMessage request, List<NameValuePair> headerEntries) {
        if (null == request) {
            logger.error("unable to complete request as the passed request object is null");
            return request;
        }

        // dump all the header entries to the request.
        if (null != headerEntries && headerEntries.size() > 0) {
            for (NameValuePair pair : headerEntries) {
                request.addHeader(pair.getName(), pair.getValue());
            }
        }
        return request;
    }

    /**
     * helper function to fill the request with header entries and posting body
     * .
     */
    private static HttpEntityEnclosingRequestBase completeRequest(HttpEntityEnclosingRequestBase request,
                                                                  List<NameValuePair> headerEntries, String postingBody) throws UnsupportedEncodingException {
        if (null != completeRequest(request, headerEntries)) {
            // dump the post body UTF-8 will be used as the default encoding
            // type.
            if (null != postingBody && postingBody.length() > 0) {
                HttpEntity entity = new ByteArrayEntity(postingBody.getBytes("UTF-8"));
                request.setHeader("Content-Length", Long.toString(entity.getContentLength()));
                request.setEntity(entity);
            }
        }
        request.removeHeaders(HTTP.CONTENT_LEN);
        return request;
    }
}
