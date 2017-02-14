package me.takanakahiko.apitestapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by takan on 2016/12/19.
 */
class HTTP {
    static String request(URL url) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader     = null;
        InputStream in;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            try {
                in = connection.getInputStream();
            } catch (IOException e){
                in = connection.getErrorStream();
            }
            if(in == null) {
                throw new IOException("The stream could not be opened.");
            }

            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            return response.toString();
        } finally {
            if(connection != null) connection.disconnect();
            if (reader != null) reader.close();
        }
    }
}