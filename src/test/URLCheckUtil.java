package test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * URL 체크하는 유틸 클래스
 *
 * @author coreawin
 * @since 2020.12.18
 * @version 3.2.0.x 이후 지원
 */
public class URLCheckUtil {

    public static void main(String[] args) {
    	URLValidationUtil.ignoreSSLCheck();
    new URLCheckUtil().check("https://www.diquest.com");
    new URLCheckUtil().check("https://www.diquest.com");
    new URLCheckUtil().check("https://www.diquest.com");
    new URLCheckUtil().check("https://www.diquest.com");
    }

    public static void check(String url){
        {
        	UrlValidator urlValidator = new UrlValidator();
            boolean isUrl = urlValidator.isValid(url);
            String validation = null;
            String tp = "etc";
            // URL Validation CHECK...
            if (isUrl) {
                HttpURLConnection connection = null;
                try {
                    connection = httpUrlConnectTest(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int code = 0;
                try {
                    code = connection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String contentType = connection.getContentType();

                System.out.println(contentType);
                if (contentType.contains("image")) {
                    tp = "image";
                } else if (contentType.contains("video")) {
                    tp = "video";
                } else if (contentType.contains("application")) {
                    tp = "application";
                } else if (contentType.contains("text/html")) {
                    tp = "html";
                } else if (contentType.contains("application/json")) {
                    tp = "json";
                } else if (contentType.contains("application/xml")) {
                    tp = "xml";
                } else {
                    tp = "etc";
                }
            } else {
            }
        }
    }

    public static HttpURLConnection httpUrlConnectTest(String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int code = connection.getResponseCode();
        // System.out.println("reqUrl : " + reqUrl);
        // System.out.println("code : " + code);
        // System.out.println("=================== HEADER ======================");
        // // 응답 헤더의 정보를 모두 출력
        // for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
        // for (String value : header.getValue()) {
        // System.out.println(header.getKey() + " : " + value);
        // }
        // }

        if (code == HttpURLConnection.HTTP_MULT_CHOICE || code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_SEE_OTHER
                || code == HttpURLConnection.HTTP_NOT_MODIFIED || code == HttpURLConnection.HTTP_USE_PROXY)
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
        // 300(여러 선택항목) , 301(영구 이동), 302(임시 이동) ,303(기타 위치 보기) , 304(수정되지 않음) ,305(프록시 사용)
        {
            String Location = connection.getHeaderField("Location");
            return httpUrlConnectTest(Location);
        }
        return connection;
    }
}
