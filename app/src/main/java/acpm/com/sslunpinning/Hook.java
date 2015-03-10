package acpm.com.sslunpinning;

import java.net.Socket;

import android.os.Environment;

import com.saurik.substrate.MS;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class Hook {


    public static void initialize() {

        MS.hookClassLoad("android.app.ContextImpl", new MS.ClassLoadHook() {
            public void classLoaded(Class<?> resources) {
                getPackageName(resources);
            }
        });
    }

    public static void hookSSL(final String name) {

        // HttpsURLConnection.setSSLSocketFactory >> SSLContext with EmptyTrustManager
        MS.hookClassLoad("javax.net.ssl.HttpsURLConnection",
                new MS.ClassLoadHook() {
                    public void classLoaded(Class<?> resources) {
                        final String methodName = "setSSLSocketFactory";
                        Method method;
                        try {
                            method = resources.getMethod(methodName,
                                    javax.net.ssl.SSLSocketFactory.class);
                        } catch (NoSuchMethodException e) {
                            method = null;
                        }

                        if (method != null) {

                            final MS.MethodPointer old = new MS.MethodPointer();
                            if (name.equals(readFromFile())) {
                                MS.hookMethod(resources, method,
                                        new MS.MethodHook() {
                                            public Object invoked(Object resources, Object... args)
                                                    throws Throwable {
                                                SSLContext context = SSLContext.getInstance("TLS");
                                                context.init(null, EmptyTrustManager.getInstance(), null);
                                                old.invoke(resources, context.getSocketFactory());
                                                return null;
                                            }
                                        },old);
                            }
                        }
                    }
                });

        //TrustManagerFactory.getTrustManagers >> EmptyTrustManager
        MS.hookClassLoad("javax.net.ssl.TrustManagerFactory",
                new MS.ClassLoadHook() {

                    public void classLoaded(Class<?> resources) {
                        final String methodName = "getTrustManagers";
                        Method method;
                        try {
                            method = resources.getMethod(methodName);
                        } catch (NoSuchMethodException e) {
                            method = null;
                        }

                        if (method != null) {

                            final MS.MethodPointer old = new MS.MethodPointer();
                            if (name.equals(readFromFile())) {
                                MS.hookMethod(resources, method,
                                        new MS.MethodHook() {
                                            public Object invoked(Object resources,Object... args)
                                                    throws Throwable {

                                                return EmptyTrustManager.getInstance();
                                            }
                                        }, old);
                            }
                        }
                    }
                });

        // SSLContext.init >> with EmptyTrustManager
        MS.hookClassLoad("javax.net.ssl.SSLContext",
                new MS.ClassLoadHook() {
                    public void classLoaded(Class<?> resources) {
                        final String methodName = "init";
                        Method method;
                        try {
                            method = resources.getMethod(methodName,
                                    KeyManager[].class, TrustManager[].class, SecureRandom.class);
                        } catch (NoSuchMethodException e) {
                            method = null;
                        }

                        if (method != null) {

                            final MS.MethodPointer old = new MS.MethodPointer();
                            if (name.equals(readFromFile())) {
                                MS.hookMethod(resources, method,
                                        new MS.MethodHook() {
                                            public Object invoked(Object resources, Object... args)
                                                    throws Throwable {
                                                old.invoke(resources, null, EmptyTrustManager.getInstance(), null);
                                                return null;
                                            }
                                        }, old);
                            }
                        }
                    }
                });

        // HttpsURLConnection.setDefaultHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        MS.hookClassLoad("javax.net.ssl.HttpsURLConnection",
                new MS.ClassLoadHook() {
                    public void classLoaded(Class<?> resources) {
                        Method method;
                        try {
                            method = resources.getMethod("setDefaultHostnameVerifier", HostnameVerifier.class);
                        } catch (NoSuchMethodException e) {
                            method = null;
                        }

                        if (method != null) {
                            final MS.MethodPointer old = new MS.MethodPointer();

                            if (name.equals(readFromFile())) {
                                MS.hookMethod(resources, method,
                                        new MS.MethodHook() {
                                            public Object invoked(Object resources,Object... args)
                                                    throws Throwable {
                                                old.invoke(resources, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                                                return null;
                                            }
                                        }, old);
                            }
                        }
                    }
                });

    }

    public static void getPackageName(Class<?> resources) {

        String methodName = "getPackageName";
        Class<?>[] params = new Class<?>[]{};
        Method pMethod = null;

        try {
            pMethod = resources.getMethod(methodName, params);
        } catch (Exception e) {
            pMethod = null;
        }

        if (pMethod != null) {
            final MS.MethodPointer old = new MS.MethodPointer();
            MS.hookMethod(resources, pMethod, new MS.MethodHook() {
                public Object invoked(Object resources, Object... args) throws Throwable {
                    String packageName = (String) old.invoke(resources, args);
                    if (!packageName.equals("android")) {
                        hookSSL(packageName);
                    }
                    return packageName;
                }
            }, old);
        }
    }

    private static String readFromFile() {

        String packageName = "";
        try {
            File conf = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SSLUnpinning/app.conf");

            if (conf.exists() == false) {
                conf.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(conf));
            String line;
            while ((line = br.readLine()) != null) {
                packageName = line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }
}
