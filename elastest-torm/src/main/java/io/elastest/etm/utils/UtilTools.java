package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UtilTools {

    private static final Logger logger = LoggerFactory
            .getLogger(UtilTools.class);

    private static String hostIp;

    @Value("${os.name}")
    private static String windowsSO;

    @Value("${et.etm.incontainer}")
    private static String inContainer;

    /*
     * public boolean pingHost(String host, int port, int timeout) { try (Socket
     * socket = new Socket()) { socket.connect(new InetSocketAddress(host,
     * port), timeout); return true; } catch (IOException e) { return false; //
     * Either timeout or unreachable or failed DNS lookup. } }
     */

    public static String getElasTestHostOnWin() {
        return "localhost";
    }

    /**
     * Returns the docker-host's url on Windows
     * 
     * @return
     */
    public static String getDockerHostUrlOnWin() {
        BufferedReader reader = null;
        String dockerHostUrl = "";

        try {
            Process child = Runtime.getRuntime().exec("docker-machine url");
            reader = new BufferedReader(
                    new InputStreamReader(child.getInputStream()));
            dockerHostUrl = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return dockerHostUrl;
    }

    /**
     * Returns the docker-host's ip.
     * 
     * @return
     */
    public static String getDockerHostIpOnWin() {
        BufferedReader reader = null;
        String dockerHostIp = "";
        if (windowsSO != null && windowsSO.toLowerCase().contains("win")) {
            try {
                Process child = Runtime.getRuntime().exec("docker-machine ip");
                reader = new BufferedReader(
                        new InputStreamReader(child.getInputStream()));
                dockerHostIp = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return dockerHostIp;
    }

    public static String getHostIp() throws Exception {
        if (hostIp == null) {
            if (inContainer != null && inContainer.equals("true")) {
                try {
                    String ipRoute = Shell.runAndWait("sh", "-c",
                            "/sbin/ip route");
                    String[] tokens = ipRoute.split("\\s");
                    hostIp = tokens[2];
                } catch (Exception e) {
                    throw new Exception("Exception executing /sbin/ip route",
                            e);
                }
            } else {
                hostIp = "127.0.0.1";
            }
        }
        logger.debug("Host IP is {}", hostIp);
        return hostIp;
    }

    public static String getDockerHostIp() throws Exception {
        if (windowsSO != null && windowsSO.toLowerCase().contains("win")) {
            return getDockerHostIpOnWin();
        } else
            return getHostIp();
    }

    public static String getMyIp() {
        String myIp = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            if (hostIp != null) {
                logger.debug("Current IP address : " + hostIp);
            }
            myIp = ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return myIp;
    }

    // Obj to Json

    public static String convertJsonString(Object obj,
            Class<?> serializationView, JsonInclude.Include inclusion) {
        String jsonString = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(inclusion);

            jsonString = objectMapper.writerWithView(serializationView)
                    .writeValueAsString(obj);

        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return jsonString;
    }

    public static String convertJsonString(Object obj,
            Class<?> serializationView) {
        return convertJsonString(obj, serializationView, Include.ALWAYS);
    }

    // Json to Obj
    @SuppressWarnings("unchecked")
    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, JsonInclude.Include inclusion,
            boolean failOnUnknownProperties) {
        T object = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(inclusion);
            objectMapper.configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            object = (T) objectMapper.readValue(json, serializationView);
        } catch (IOException e) {
            logger.error("Error during conversion: " + e.getMessage());
        }
        return object;
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView) {
        return convertJsonStringToObj(json, serializationView, Include.ALWAYS,
                true);
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, JsonInclude.Include inclusion) {
        return convertJsonStringToObj(json, serializationView, inclusion, true);
    }

    public static <T> T convertJsonStringToObj(String json,
            Class<?> serializationView, boolean failOnUnknownProperties) {
        return convertJsonStringToObj(json, serializationView, Include.ALWAYS,
                failOnUnknownProperties);
    }

    /**
     * Generate an unique id.
     * 
     * @return the new unique id
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static int findRandomOpenPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public static boolean checkIfUrlIsUp(String urlToCheck) {
        boolean up = false;
        URL url;
        try {
            url = new URL(urlToCheck);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            up = (responseCode >= 200 && responseCode <= 299);
            if (!up) {
                return up;
            }
        } catch (IOException e) {
            return false;
        }

        return up;
    }

    public static String doPing(String ip) throws IOException {
        InetAddress ping;
        ping = InetAddress.getByName(ip);
        if (ping.isReachable(5000)) {
            return ping.getHostAddress().toString();
        } else {
            throw new IOException("Ip " + ip + " non reachable");
        }
    }

    @SuppressWarnings({ "unchecked" })
    // Example: If wants name value of {container: {name: value}}, tree should
    // be [docker,container,name]
    public static Object getMapFieldByTreeList(Map<String, Object> dataMap,
            List<String> tree) {
        if (tree.size() > 0) {
            String field = tree.get(0);

            if (dataMap.get(field) != null) {
                if (tree.size() > 1) {
                    List<String> subTree = tree.subList(1, tree.size());
                    return getMapFieldByTreeList(
                            (Map<String, Object>) dataMap.get(field), subTree);
                } else {
                    return dataMap.get(field);
                }

            }

        }
        return null;

    }

    public static Object cloneObject(Object obj) {
        try {
            Object clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(obj) == null
                        || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (field.getType().isPrimitive()
                        || field.getType().equals(String.class)
                        || field.getType().getSuperclass().equals(Number.class)
                        || field.getType().equals(Boolean.class)) {
                    field.set(clone, field.get(obj));
                } else {
                    Object childObj = field.get(obj);
                    if (childObj == obj) {
                        field.set(clone, clone);
                    } else {
                        field.set(clone, cloneObject(field.get(obj)));
                    }
                }
            }
            return clone;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> objectToMap(Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }
}
