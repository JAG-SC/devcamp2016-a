package me.takanakahiko.apitestapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText edit_search_word;    // 検索する文字を入力するためのエディットビューです
    private Button button_search;       // 検索ボタンのビューです
    private ListView lv;
    private AsyncSearchStationTask task_search_station; // 駅を検索する非同期タスクです
    private AsyncSearchPostalCodeTask task_search_postal_code; // 郵便番号を検索する非同期タスクです

    private String[] stations = new String[]{};
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_search_word = (EditText) findViewById(R.id.edit_search_word); // エディット検出
        button_search    = (Button)   findViewById(R.id.button_search); // ボタン検出
        lv               = (ListView) findViewById(R.id.listView1);

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1);

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_search.setEnabled(false);

                String serach_word = edit_search_word.getText().toString();

                if(serach_word.isEmpty()) {
                    print("検索する文字を入力してください。");
                    button_search.setEnabled(true); // ボタンを有効に戻します
                } else {
                    task_search_station = new AsyncSearchStationTask(); // 道（非同期タスク）を作る
                    task_search_station.execute(serach_word);            // 実行する
                }
            }
        });

        // 駅名をタップすると下部に"(駅名) clicked"と表示される
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                task_search_postal_code = new AsyncSearchPostalCodeTask(); // 道（非同期タスク）を作る
                task_search_postal_code.execute(item);            // 実行する

            }
        });
    }

    // 非同期タスク（station/light API）
    private class AsyncSearchStationTask extends AsyncTask<String, Void, String> {
        private String error_message = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                String url_str = "http://api.ekispert.jp/v1/json/station/light?";
                url_str += "key=" + APIKEY.ekispert;
                url_str += "&type=train";
                url_str += "&name=" + strings[0];
                URL api_request_url = new URL( url_str);
                return HTTP.request(api_request_url);
            } catch (IOException e) {
                e.printStackTrace();
                error_message = e.toString();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);

            if (results != null) {
                try {
                    JSONObject json = new JSONObject(results);
                    JSONObject result_set = json.getJSONObject("ResultSet");
                    if(result_set.has("Point")) {
                        JSONArray points = result_set.optJSONArray("Point");
                        if (points == null) {
                            points = new JSONArray();
                            points.put(result_set.getJSONObject("Point"));
                        }
                        resetList();
                        for (int i = 0; i < points.length(); ++i) {
                            JSONObject station = points
                                    .getJSONObject(i)
                                    .getJSONObject("Station");
                            println(station.getString("Name"));
                            addList(station.getString("Name"));
                        }
                        setList();
                    } else {
                        println("検索結果は0件でした。");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorOnTask(e.toString());
                }
            } else {
                errorOnTask(error_message);
            }
            button_search.setEnabled(true);
        }
    }

    // 非同期タスク（station API & geo API）
    private class AsyncSearchPostalCodeTask extends AsyncTask<String, Void, String> {
        private String error_message = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                String url_str = "http://api.ekispert.jp/v1/json/station?";
                url_str += "key=" + APIKEY.ekispert;
                url_str += "&type=train";
                url_str += "&name=" + strings[0];
                URL api_request_url = new URL( url_str);
                String results = HTTP.request(api_request_url);

                JSONObject json = new JSONObject(results);
                JSONObject result_set = json.getJSONObject("ResultSet");
                JSONObject point = result_set.getJSONObject("Point");
                JSONObject geopoint = point.getJSONObject("GeoPoint");
                String keido = geopoint.getString("longi_d");
                String ido = geopoint.getString("lati_d");

                url_str = "http://geoapi.heartrails.com/api/json";
                url_str += "?method=searchByGeoLocation";
                url_str += "&x="+ keido + "&y=" + ido;
                api_request_url = new URL( url_str);
                return HTTP.request(api_request_url);
            } catch (IOException e) {
                e.printStackTrace();
                error_message = e.toString();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return  null;
            }

        }

        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);

            if (results != null) {
                try {
                    JSONObject json = new JSONObject(results);
                    JSONObject reaponse = json.getJSONObject("response");
                    JSONArray locations = reaponse.getJSONArray("location");
                    String postal = locations.getJSONObject(0).getString("postal");
                    String toast = "T " + postal.substring(0,3) + "-" + postal.substring(3);
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorOnTask(e.toString());
                }
            } else {
                errorOnTask(error_message);
            }
            button_search.setEnabled(true);
        }
    }

    private void print(String text) {
        System.out.print(text);
    }
    private void println(String text) {
        System.out.println(text);
    }

    private void addList(String text){ adapter.add(text); }
    private void setList(){ lv.setAdapter(adapter);}
    private void resetList(){
        adapter.clear();
    }

    private void errorOnTask(String message){
        println("エラーが発生しました: ");
        println(message);
        println("スタックトレースを確認してください。");
        button_search.setEnabled(true);
    }
}
