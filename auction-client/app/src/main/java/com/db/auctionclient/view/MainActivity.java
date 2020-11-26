package com.db.auctionclient.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;

import com.db.auctionclient.R;
import com.db.auctionclient.model.worker.GlobalSingleton;
import com.db.auctionclient.model.worker.ClientConfiguration;
import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.view.auctionsoverview.AuctionsOverviewFragment;
import com.db.auctionclient.view.runningauctions.RunningAuctionsFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

/**
 * Entrance point of the application.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    /** Code of the request for permission to read from external storage.*/
    private static final int PERMISSION_REQUEST_CODE = 42;
    /** The relative path of the json file, which contains the auction data.*/
    private static final String AUCTIONS_PATH = "/Online-Auktion-App/auctions.json";
    /** The relative path of the json file, which contains the configuration.*/
    private static final String CONFIG_PATH = "/Online-Auktion-App/client_config.json";

    // Navigation view components.
    private DrawerLayout layoutDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView layoutNavView;

    /**
     * Starts a fragment.
     * @param fragment fragment, which should be started.
     */
    public void initFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.layoutContainer, fragment).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Checks, if the permission to read from external storage was already granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }else initApplication();

        layoutNavView = findViewById(R.id.layoutNavView);
        layoutNavView.setNavigationItemSelectedListener(this);

        layoutDrawer = findViewById(R.id.layoutDrawer);

        drawerToggle = new ActionBarDrawerToggle(this, layoutDrawer, R.string.nav_drawer_open, R.string.nav_drawer_close);
        layoutDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Starts the auction overview view.
        initFragment(AuctionsOverviewFragment.newInstance());
        layoutNavView.setCheckedItem(R.id.nav_auctions_all);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_auctions_all:
                initFragment(AuctionsOverviewFragment.newInstance());
                break;
            case R.id.nav_auctions_running:
                initFragment(RunningAuctionsFragment.newInstance());
                break;
        }
        layoutDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // disable the back button
        //super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initApplication();
            }
        }
    }

    /**
     * Loads the configuration and initializes an instance of the client-master. Also loads
     * auction data from a json file and inserts it to the database.
     */
    public void initApplication() {
        String actionsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUCTIONS_PATH;
        String configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CONFIG_PATH;
        try {
            Auction[] auctions = Auction.loadFromJson(actionsPath);
            AppDatabase database = AppDatabase.getInstance(getApplication());
            AppDatabase.executorService.execute(() -> {
                database.auctionDao().deleteAll();
                database.auctionTaskDao().deleteAllAuctionTasks();
                database.auctionDao().addAuctions(auctions);
            });
            ClientConfiguration config = ClientConfiguration.loadFromJson(configPath);
            GlobalSingleton.initInstance(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
