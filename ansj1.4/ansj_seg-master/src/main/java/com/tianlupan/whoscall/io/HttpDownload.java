package com.tianlupan.whoscall.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.tianlupan.whoscall.TextUtils;

public class HttpDownload {

    private boolean mGzip = true;
    //private String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13";
    private String userAgent="Googlebot/2.1 (+http://www.google.com/bot.html)";
   // private   String userAgent="Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.133 Safari/534.16";
    private static final String default_charset = "utf-8";
    private static final String headerCharset = "Content-Type";
    
    
    public void setGzipEnable(boolean gzip) {
        mGzip = true;
    }

    //private static final CookieStore cookieStore = new BasicCookieStore();

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }

    public HttpDownload() {

    }

   

    private String getCharSetFromHtml(String html) {
        if (TextUtils.isEmpty(html)) {
            System.out
                    .println("be care : getCharSetFromHtml parse html eqausl null");
            return default_charset;
        }
        String content = html;
        final int checkSize = 256;
        if (content.length() > checkSize)
            content = content.substring(0, checkSize);
        content = content.toLowerCase();

        if (content.contains("utf-8")) {
            return default_charset;
        } else if (content.contains("gb2312")) {
            return "gb2312";
        } else if (content.contains("gbk")) {
            return "gbk";
        } else {
            return default_charset;
        }

    }

    
    /**
     * 
     * Download through HttpURLConnection
     */
    public String getHtml(String downloadURL) {

        InputStream urlStream = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(downloadURL);
            HttpURLConnection cumtConnection = (HttpURLConnection) url
                    .openConnection();

            cumtConnection.setRequestProperty("User-Agent", userAgent);
            cumtConnection.setRequestProperty("Pragma", "no-cache");
           // cumtConnection.setRequestProperty("Proxy-Connection", "Keep-Alive");
            //cumtConnection.setRequestProperty("Host", url.getHost());
            cumtConnection
                    .setRequestProperty("Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            cumtConnection.setRequestProperty("Accept-Language",
                    "zh-cn,zh;q=0.5");
            cumtConnection.setRequestProperty("Pragma", "no-cache");
            cumtConnection.setRequestProperty("Accept-Charset",
                    "GB2312,utf-8;q=0.7,*;q=0.7");
            cumtConnection.setRequestProperty("Accept-Encoding",
                    mGzip ? "gzip,deflate" : "deflate");
  
/*            if( cumtConnection.getResponseCode()!=200)
                {
                System.out.println("请示网址返回错误:"+cumtConnection.getResponseCode()+", url="+downloadURL);
                return null;
                }*/
           

            String encoding = cumtConnection.getContentEncoding();
            int contentLength = cumtConnection.getContentLength();
            if (contentLength < 0)
                contentLength = 0;

           // System.out.println("headers:" + cumtConnection.getHeaderFields());

            List<String> charsetStrings = cumtConnection.getHeaderFields().get(
                    headerCharset);

            String charset = default_charset;
            
            
            
            
            boolean charsetNotExists = false;
            if (charsetStrings != null && charsetStrings.size() == 1) {
                String responseCharset = charsetStrings.get(0);
                if (!TextUtils.isEmpty(responseCharset)) {
                    String lowerCharset = responseCharset.toLowerCase();
                    if (lowerCharset.contains("utf-8")) {
                        charset = "utf-8";
                    } else if (lowerCharset.contains("gb2312")) {
                        charset = "gb2312";
                    } else if (lowerCharset.contains("gbk")) {
                        charset = "gbk";
                    }
                    else if(downloadURL.startsWith("http://cache.baiducontent.com/"))
                    {
                    	//百度缓存是gb2312编码，当前使用硬编码无误
                    	//TODO 现在只是最基本的硬编码，根据html中的charset转变编码，如：Response Header
                    	//中未设置charset,并且采用了压缩，如果以utf-8解压缩并且强制根据html 中的charset gb2312
                    	//转换，会造成乱码                   	
                      	charset="gb2312";
                    } else {
                        charset = default_charset;
                        charsetNotExists = true;
                    }
                }
            }

            boolean useGZIP = false;

            if (!TextUtils.isEmpty(encoding) && encoding.contains("gzip"))
                useGZIP = true;

            urlStream = useGZIP ? new GZIPInputStream(
                    cumtConnection.getInputStream()) : cumtConnection
                    .getInputStream();

            reader = new BufferedReader(new InputStreamReader(urlStream,
                    charset));

            String lineTag = "\r\n";

            StringBuilder builder = new StringBuilder(
                    useGZIP ? contentLength * 2 : contentLength);

            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line + lineTag);
            }

            reader.close();
            urlStream.close();

            String html = builder.toString();
            if (charsetNotExists) {
                String htmlCharset = getCharSetFromHtml(html);
                if (!htmlCharset.equals(charset)) {
                    // 转码成html中的编码
                    if (html != null) {
                        System.out.println("changecharset from " + charset
                                + " to " + htmlCharset);
                        // 用源字符编码解码字符串
                        byte[] bs = html.getBytes(Charset.forName(charset));
                        return new String(bs, htmlCharset);
                    }
                }
            }
            return html;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;

    }

}
