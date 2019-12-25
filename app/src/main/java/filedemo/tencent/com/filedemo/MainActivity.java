package filedemo.tencent.com.filedemo;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity
        implements QbSdk.PreInitCallback, ValueCallback<String> {
    private static final String TAG = "MainActivity";
    public String mFilePath = null;

    private ListView mListView = null;

    private ArrayList<String> mFileList = new ArrayList<String>();

    private FileListAdapter mAdapter = null;
    private File fileN;
//    private String Url = "http://39.99.180.62:9050/files/1577153205496高旭12.14问题.xlsx";
//    private String newFileName = Url.substring(Url.lastIndexOf("/")+1);
    private String Url = "https://wkbjcloudbos.bdimg.com/v1/wenku97//1a07c5117451296b710ee725c14aa199?responseContentDisposition=attachment%3B%20filename%3D%222018%25E5%2585%25A8%25E5%259B%25BD%25E2%2585%25A1%25E5%258D%25B7%25E7%2590%2586%25E7%25A7%2591%25E7%25BB%25BC%25E5%2590%2588%25E9%25AB%2598%25E8%2580%2583%25E7%259C%259F%25E9%25A2%2598.docx%22%3B%20filename%2A%3Dutf-8%27%272018%25E5%2585%25A8%25E5%259B%25BD%25E2%2585%25A1%25E5%258D%25B7%25E7%2590%2586%25E7%25A7%2591%25E7%25BB%25BC%25E5%2590%2588%25E9%25AB%2598%25E8%2580%2583%25E7%259C%259F%25E9%25A2%2598.docx&responseContentType=application%2Foctet-stream&responseCacheControl=no-cache&authorization=bce-auth-v1%2Ffa1126e91489401fa7cc85045ce7179e%2F2019-12-25T02%3A38%3A48Z%2F3000%2Fhost%2Fd4cc5927ee753005d0c677399ad9d00acedf4ca743feba40acbe798fbd61cc08&token=eyJ0eXAiOiJKSVQiLCJ2ZXIiOiIxLjAiLCJhbGciOiJIUzI1NiIsImV4cCI6MTU3NzI0NDUyOCwidXJpIjp0cnVlLCJwYXJhbXMiOlsicmVzcG9uc2VDb250ZW50RGlzcG9zaXRpb24iLCJyZXNwb25zZUNvbnRlbnRUeXBlIiwicmVzcG9uc2VDYWNoZUNvbnRyb2wiXX0%3D.wcGa39GRpehjJt%2BTJV8z7HJlk6gz8SfE0Lj2vy7i1yM%3D.1577244528";
    private String newFileName = "2018全国Ⅱ卷理科综合高考真题.docx";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_miniqb_file);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
//        QbSdk.forceSysWebView();
        String[] list = new String[2];
        list[0] = "android.permission.WRITE_EXTERNAL_STORAGE";
        list[1] = "android.permission.READ_PHONE_STATE";
        MainActivity.this.requestPermissions(list, 105);

        QbSdk.initX5Environment(this, this);

        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mAdapter = new FileListAdapter(this, R.layout.vlist, mFileList);
        mListView = (ListView) findViewById(R.id.miniqb_file_list_view);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File("/sdcard/edit.pptx");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("local", "true");
                params.put("entryId", "2");
                params.put("allowAutoDestory", "true");

                JSONObject Object = new JSONObject();
                try {
                    Object.put("pkgName", MainActivity.this.getApplication().getPackageName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put("menuData", Object.toString());
                openFileReader(MainActivity.this, file.getAbsolutePath());
                //QbSdk.openFileReader(MainActivity.this,"/sdcard/edit.pptx",params,MainActivity.this);
            }
        });

        getFileFromSD(mFilePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCoreInitFinished() {
        Log.d("test", "onCoreInitFinished");
    }

    @Override
    public void onViewInitFinished(boolean isX5Core) {
        Log.d("test", "onViewInitFinished,isX5Core =" + isX5Core);
    }

    @Override
    public void onReceiveValue(String val) {
        Log.d("test", "onReceiveValue,val =" + val);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

    }

    private void getFileFromSD(String filePath) {
        System.out.println("========" + filePath);
        File mfile = new File(filePath);
        File[] files = mfile.listFiles();
        System.out.println("========" + files);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
//                getFileFromSD(file.getPath());
            } else {
                checkIsTestFile(file.getPath());
            }
        }
        mFileList.add("网络下载");
        mAdapter.notifyDataSetChanged();
    }

    private void checkIsTestFile(final String fName) {
        System.out.println("==========" + fName);
        QbSdk.canOpenFile(this, fName, new ValueCallback<Boolean>() {

            @Override
            public void onReceiveValue(Boolean arg0) {
                if (arg0 == true) {
                    mFileList.add(fName);
                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.d("test", "unsupported file:" + fName);
                }
            }
        });
    }

    public void openFileReader(Context context, String pathName) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("local", "true");
        JSONObject Object = new JSONObject();
        try {
            Object.put("pkgName", context.getApplicationContext().getPackageName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put("menuData", Object.toString());
        QbSdk.getMiniQBVersion(context);
        System.out.println("=========" + params);
        System.out.println("=========" + pathName);
        int ret = QbSdk.openFileReader(context, pathName, params, this);
    }

    private void downLoadFromNet(final String url) {
        //1.网络下载、存储路径、
        File cacheFile = new File(Environment.getExternalStorageDirectory(), newFileName);
        Log.d(TAG, "缓存文件 = " + cacheFile.toString());
        if (cacheFile.exists()) {
            if (cacheFile.length() <= 0) {
                Log.d(TAG, "删除空文件！！");
                cacheFile.delete();
                return;
            }
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(2000, TimeUnit.MILLISECONDS)
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "下载文件-->onResponse");
                ResponseBody responseBody = response.body();
                createFile(responseBody);

                if (fileN == null) return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openFileReader(MainActivity.this, fileN.getAbsolutePath());
                    }
                });
            }
        });
    }

    private void createFile(ResponseBody responseBody) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = responseBody.byteStream();
            long total = responseBody.contentLength();

            fileN = new File(Environment.getExternalStorageDirectory(), newFileName);
            Log.d(TAG, "创建缓存文件： " + fileN.toString());
            if (!fileN.exists()) {
                fileN.createNewFile();
            }
            fos = new FileOutputStream(fileN);
            long sum = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                Log.d(TAG, "写入缓存文件" + fileN.getName() + "进度: " + progress);
            }
            fos.flush();
            Log.d(TAG, "文件下载成功,准备展示文件。");
        } catch (Exception e) {
            Log.d(TAG, "文件下载异常 = " + e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
            }
        }
    }

    public class FileListAdapter extends ArrayAdapter<String>
            implements AdapterView.OnItemClickListener {

        Context mContext = null;
        int mRes = 0;

        List<String> mObjects = null;

        public FileListAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            mContext = context;
            mRes = textViewResourceId;
            mObjects = objects;
        }


        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.vlist, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.title);
            final CharSequence text = (CharSequence) mObjects.get(position);
            textView.setText(text);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);

            return convertView;
        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mObjects.get(position).equals("网络下载")) {
                downLoadFromNet(Url);
            } else {
                File file = new File(mObjects.get(position));
                openFileReader(mContext, file.getAbsolutePath());
            }
        }

    }

}
