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
    private AsyncSearchStationTask task_search_station; // 駅を検索をする非同期タスクです

    private String[] stations = new String[]{};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_search_word = (EditText) findViewById(R.id.edit_search_word);
        button_search    = (Button)   findViewById(R.id.button_search);
        lv               = (ListView) findViewById(R.id.listView1);

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_search.setEnabled(false);

                String serach_word = edit_search_word.getText().toString();

                if(serach_word.isEmpty()) {
                    print("検索する文字を入力してください。");
                    button_search.setEnabled(true); // ボタンを有効に戻します
                } else {
                    task_search_station = new AsyncSearchStationTask();
                    task_search_station.execute(serach_word);
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item + " clicked",Toast.LENGTH_LONG).show();
            }
        });
    }

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

    private void print(String text) {
        System.out.print(text);
    }
    private void println(String text) {
        System.out.println(text);
    }

    private void addList(String text){
        int old_len = stations.length;
        String[] temp = new String[old_len+1];
        System.arraycopy(stations,0,temp, 0, old_len);
        temp[old_len] = text;
        stations = new String[old_len+1];
        System.arraycopy(temp,0,stations, 0, old_len+1);
    }

    private void setList(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1, stations);
        lv.setAdapter(adapter);
    }

    private void resetList(){
        stations = new String[]{};
    }

    private void errorOnTask(String message){
        println("エラーが発生しました: ");
        println(message);
        println("スタックトレースを確認してください。");
        button_search.setEnabled(true);
    }
}
