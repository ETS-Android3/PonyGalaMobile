package net.scadsdnd.ponygala;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Process;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements WebRequest.webUICatIf
{

    Integer lvl = 0;
    Boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StrictMode.ThreadPolicy mypolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(mypolicy);

        SharedPreferences shPrf = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        isAdmin = shPrf.getBoolean("admin_mode", false);

        WebRequest catWebRq = new WebRequest();

        catWebRq.UIContext = this;
        catWebRq.regCatCb(this);
        catWebRq.StatusUI = (TextView) findViewById(R.id.subtitle);
        catWebRq.pbIndicator = (ProgressBar) findViewById(R.id.pbWaitMain);

        catWebRq.execute(1);

        if (catWebRq.getStatus() == AsyncTask.Status.FINISHED){
            Log.v("!!!!!!" , "Finished");
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        lvl-=1;
        switch (lvl){
            case -1:
                Toast.makeText(MainActivity.this, "Нажмите ещё раз 'Назад', чтобы выйти.", Toast.LENGTH_LONG).show();
                break;
            case -2:
                finish();
                System.exit(0);
                break;
            default:
                Log.i("SYS", "Level: " + Integer.toString(lvl));
                break;
        }

    }


    @Override
    public void pCategoryListLoaded(JSONArray jRows) {

        Log.v("!","Got Categories");

        final String[] catID = new String[jRows.length()];
        String[] catName = new String[jRows.length()];
        String[] catCounts = new String[jRows.length()];
        String[][] catThumbs = new String[][]{
                new String[jRows.length()],new String[jRows.length()],
                new String[jRows.length()],new String[jRows.length()],
                new String[jRows.length()]
        };

        JSONObject jData = null;

        Map<String, String[]> srvData = new HashMap<>();
        Map<String, String> imgQuery = new HashMap<>();

        //String[] imgQuery = new String[jRows.length()*5];

        try {
            for (int i = 0; i < jRows.length(); i++) {
                jData = jRows.getJSONObject(i);

                catID[i] = jData.getString("cat_id");
                catName[i] = jData.getString("cat_name");
                catCounts[i] = jData.getString("count");

                if (Integer.valueOf(jData.getString("count")) > 1) {
                    for (int j = 0; j < 5; j++) {
                        catThumbs[j][i] = jData.getString("thumb_" + (j+1));
                        imgQuery.put("thumb_"+(i+j), jData.getString("thumb_" + (j+1)));
                    }
                }

                //Log.v("JSON", jData.getString("cat_name"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        srvData.put("ids", catID);
        srvData.put("names", catName);
        srvData.put("counters", catCounts);
        for(int j=0; j<5; j++){
            srvData.put("img_"+j, catThumbs[j]);
        }


        // https://developer.android.com/reference/android/widget/ListView
        //ArrayAdapter<String> myAdapt = new ArrayAdapter<String>(UIContext, android.R.layout.simple_list_item_1, catName);
        //OutputView.setAdapter(myAdapt);

        ListView OutputListVW = (ListView) findViewById(R.id.catListView);

        CatAdapter listAdapter = new CatAdapter(this, catName);
        listAdapter.catData = srvData;

        OutputListVW.setAdapter(listAdapter);

        OutputListVW.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(parent.getContext(), "db_id:" + catID[position], Toast.LENGTH_SHORT).show();

                // Creating new activity on click
                // https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intGala = new Intent(parent.getContext(), GalleryActivity.class);
                intGala.putExtra("catId", catID[position]);
                parent.getContext().startActivity(intGala);

            }
        });

        //OutputListVW.setRecyclerListener(new );
    }

}
