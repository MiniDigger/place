package me.minidigger.place;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Martin on 01.04.2017.
 */
public class Download {

    private static final String URL = "https://abra.me/place-snaps/";
    private static final File folder = new File("./images");
    private static final Set<String> brokenImages = new HashSet<>();

    public static void main(String[] args) throws Exception {
        brokenImages.add("04-01%2001%3a41%3a03.png");
        brokenImages.add("04-01%2009%3a52%3a44.png");
        brokenImages.add("04-01%2013%3a16%3a15.png");
        brokenImages.add("04-01%2015%3a46%3a40.png");

        readURL(new URL(URL))
                .filter(s -> s.startsWith("<a")).forEach(s -> {
            String file = s.split("\"")[1];
            try {
                download(new URL(URL + file), file);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }

    private static Stream<String> readURL(URL url) throws Exception {
        // doesn't work, LE cert...
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
//            return reader.lines();
//        }
        return Files.readAllLines(new File("./file").toPath()).parallelStream();
    }

    private static URL makeURL(String file) {
        try {
            return new URL(URL + file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void download(URL url, String name) {
        File targetFile = new File(folder, name);
        if (targetFile.exists()) return;
        if (brokenImages.contains(name)) return;
        System.out.println("download " + name);
        try {
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            targetFile.mkdirs();
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(is);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        // Create a new trust manager that trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Activate the new trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}
