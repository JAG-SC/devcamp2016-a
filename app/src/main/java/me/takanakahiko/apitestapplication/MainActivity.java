package me.takanakahiko.apitestapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText edit_search_word;    // 検索する文字を入力するためのエディットビューです
    private TextView text_output;         // 出力を表示するためのテキストビューです
    private Button button_search;       // 検索ボタンのビューです
    private AsyncSearchStationTask task_search_station; // 駅を検索をする非同期タスクです


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_search_word = (EditText) findViewById(R.id.edit_search_word);
        text_output      = (TextView) findViewById(R.id.text_output);
        button_search    = (Button)   findViewById(R.id.button_search);

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_search.setEnabled(false);
                text_output.setText("");

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
                    println(json.toString(2));
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
        text_output.setText(text_output.getText() + text);
    }
    private void println(String text) {
        print(text + '\n');
    }

    private void errorOnTask(String message){
        println("エラーが発生しました: ");
        println(message);
        println("スタックトレースを確認してください。");
        button_search.setEnabled(true);
    }
}
