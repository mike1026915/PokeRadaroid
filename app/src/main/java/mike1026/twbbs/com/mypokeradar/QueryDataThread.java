package mike1026.twbbs.com.mypokeradar;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Created by mike on 2016/9/4.
 */
public class QueryDataThread extends Thread {

    private GoogleMap map = null;
    private double minLatitude = 0.0;
    private double maxLatitude = 0.0;
    private double minLongtitude = 0.0;
    private double maxLongtitude = 0.0;
    private String DEBUG = "__QUERY__";
    public static String QUERY_RESULT = "__QUERY_RESULT__";
    private MapsActivity mainActivity;

    public QueryDataThread(MapsActivity mapActivity)
    {
        mainActivity = mapActivity;
    }

    public void run()
    {

        while (true) {
            String url = "https://www.pokeradar.io/api/v1/submissions?";
            String paraMinLatitude = "minLatitude=" + String.valueOf(MapsActivity.minLatitude);
            String paraMaxLatitude = "maxLatitude=" + String.valueOf(MapsActivity.maxLatitude);
            String paraMinLongitude = "minLongitude=" + String.valueOf(MapsActivity.minLongtitude);
            String paraMaxLongtitude = "maxLongitude=" + String.valueOf(MapsActivity.maxLongtitude);
            String pokemonId = "pokemonId=0";
            String parameters = TextUtils.join("&", new String[]{paraMinLatitude, paraMaxLatitude, paraMinLongitude,
                    paraMaxLongtitude, pokemonId});
            url = new StringBuilder(url).append(parameters).toString();
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
                StringBuilder result = new StringBuilder();
                Scanner sc = new Scanner(con.getInputStream());
                while (sc.hasNextLine())
                    result.append(sc.nextLine() + "\n");
                Log.d(DEBUG, result.toString());
                try {
                    JSONObject json_data = new JSONObject(result.toString());
                    JSONArray pokemons = new JSONArray();
                    JSONArray error = new JSONArray();
                    try {
                        pokemons = json_data.getJSONArray("data");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {

                        error = json_data.getJSONArray("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    boolean success = json_data.getBoolean("success");
                    ArrayList<PokeMon> queryResult = new ArrayList<PokeMon>();
                    for (int i = 0; i < pokemons.length(); i++) {
                        JSONObject pokemon = pokemons.getJSONObject(i);

                        double monLatitute = pokemon.getDouble("latitude");
                        double monLontitude = pokemon.getDouble("longitude");
                        int monId = pokemon.getInt("pokemonId");
                        Log.d(DEBUG, ""+monId);
                        queryResult.add(new PokeMon(monId, monLontitude, monLatitute));
                    }
                    sendData(queryResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sc.close();
                Thread.sleep(5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendData(ArrayList<PokeMon> queryResult) {
        Message msg = new Message();
        Bundle bundleData = new Bundle();
        bundleData.putSerializable(QUERY_RESULT, queryResult);
        Log.d(DEBUG,"sendData" + bundleData.toString());
        msg.setData(bundleData);
        mainActivity.UiHandler.sendMessage(msg);
    }
}
