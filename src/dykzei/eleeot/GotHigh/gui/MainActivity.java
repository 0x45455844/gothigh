package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost();        
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("_board")
	        .setIndicator("/b/", null)
	        .setContent(new Intent(this, BoardActivity.class));        
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("_pool")
	        .setIndicator(getString(R.string.pool), null)
	        .setContent(new Intent(this, PoolActivity.class));        
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("_settings")
	        .setIndicator(getString(R.string.settings), null)
	        .setContent(new Intent(this, BoardActivity.class));        
        tabHost.addTab(tabSpec);
    }
}