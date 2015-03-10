package acpm.com.sslunpinning;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class AppListActivity extends ActionBarActivity {

    ArrayList<PackageInfo> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        ListView appList= loadListView();

        appList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {
                PackageInfo app = apps.get(position);
                Toast.makeText(getApplicationContext(), "Bypass applied to " + app.getAppName(), Toast.LENGTH_LONG).show();
                writeToFile(app.getPckName());

                loadListView();
            }
        });

        try {
            Class.forName("com.saurik.substrate");
        } catch (Throwable ignore) {
            // ignored
        }
        Hook sslUnpinning = new Hook();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        writeToFile("");
        loadListView();
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<PackageInfo> getInstalledApps() {
        ArrayList<PackageInfo> appsList = new ArrayList<PackageInfo>();
        List<android.content.pm.PackageInfo> packs = getPackageManager().getInstalledPackages(0);

        String packBypassed = readFromFile();
        for(int i=0;i<packs.size();i++) {

            android.content.pm.PackageInfo p = packs.get(i);
            PackageInfo pInfo = new PackageInfo();
            pInfo.setAppName(p.applicationInfo.loadLabel(getPackageManager()).toString());
            pInfo.setPckName(p.packageName);
            pInfo.setIcon(p.applicationInfo.loadIcon(getPackageManager()));

            if(p.packageName.trim().equals(packBypassed.trim()))
            {
                pInfo.setBypassed(true);
            }
            // Installed by user
            if ((p.applicationInfo.flags & 129) == 0) {
                appsList.add(pInfo);
            }
        }
        return appsList;
    }

    private void writeToFile(String data) {

        try {

            File conf = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/SSLUnpinning/app.conf");

            if(conf.exists() == false) {
                File path = new File(String.valueOf(conf.getParentFile()));
                if (path.mkdirs()) {
                    conf.createNewFile();
                }
            }
            FileOutputStream fOut = new FileOutputStream(conf,false);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(data);
            myOutWriter.close();
            fOut.close();

        } catch (Exception e) {
            e.printStackTrace();
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

    private ListView loadListView()
    {
        ListView appList=(ListView)findViewById(R.id.apps_view);
        apps = getInstalledApps();
        appList.setAdapter(new AppsAdapter(this,apps));
        return appList;
    }
}
