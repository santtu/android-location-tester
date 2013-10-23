/*
 * Copyright (c) 2013 Santeri Paavolainen <santtu@iki.fi>
 *
 * This file is part of LocationTester Android sample application.
 *
 * LocationTester is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocationTester is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LocationTester.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.iki.santtu.android.locationtester;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements LocationListener {
    String provider = LocationManager.GPS_PROVIDER;
    private TextView text;
    private LocationManager lm;
    private int set = 0;

    private Object formatLocation(Location l) {
        if (l == null)
            return "none";

        return String.format("%.6f/%.6f+%.1fm@%.1fm",
                l.getLatitude(), l.getLongitude(),
                l.getAltitude(), l.getAccuracy());
    }

    private void add(String format, Object... args) {
        String formatted = String.format(format, args);
        text.setText(text.getText() + "\n" + formatted);
        Log.i("LocationTester", "DATA: " + formatted);
    }

    @Override
    protected void onPause() {
        Log.i("LocationTester", "onPause()");
        super.onPause();
        lm.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        Log.i("LocationTester", "onResume()");
        super.onResume();

        if (((ToggleButton) findViewById(R.id.toggleButton)).isChecked())
            lm.requestLocationUpdates(provider, 0, 0, this);
    }

    @Override
    protected void onDestroy() {
        Log.i("LocationTester", "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("LocationTester", "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        text = (TextView) findViewById(R.id.status);
        text.setText("");

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        // check which providers are enabled, and remove bad ones from view
        if (!lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
            ((RadioGroup) findViewById(R.id.providerGroup)).removeView(findViewById(R.id.passiveButton));

        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            ((RadioGroup) findViewById(R.id.providerGroup)).removeView(findViewById(R.id.networkButton));

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            ((RadioGroup) findViewById(R.id.providerGroup)).removeView(findViewById(R.id.gpsButton));
    }

    public void onPassiveProviderSelected(View view) {
        provider = LocationManager.PASSIVE_PROVIDER;
        add("Selected passive provider");
    }

    public void onNetworkProviderSelected(View view) {
        provider = LocationManager.NETWORK_PROVIDER;
        add("Selected network provider");
    }

    public void onGpsProviderSelected(View view) {
        provider = LocationManager.GPS_PROVIDER;
        add("Selected GPS provider");
    }

    public void onTrackingToggled(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        Log.i("LocationTester", "onTrackingToggled: on=" + on);

        if (findViewById(R.id.passiveButton) != null)
            findViewById(R.id.passiveButton).setEnabled(!on);

        if (findViewById(R.id.networkButton) != null)
            findViewById(R.id.networkButton).setEnabled(!on);

        if (findViewById(R.id.gpsButton) != null)
            findViewById(R.id.gpsButton).setEnabled(!on);

        findViewById(R.id.cachedButton).setEnabled(!on);

        if (on) {
            set++;
            add("%s-%d: Starting tracking", provider, set);
            lm.requestLocationUpdates(provider, 0, 0, this);
        } else {
            lm.removeUpdates(this);
            add("%s-%d: Stopping tracking", provider, set);
        }
    }

    public void onGetCachedLocationClick(View view) {
        set++;
        add("%s-%d: Cached: %s", provider, set, formatLocation(lm.getLastKnownLocation(provider)));
    }


    @Override
    public void onLocationChanged(Location location) {
        add("%s-%d: Location: %s", provider, set, formatLocation(location));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        add("%s: Status: %d", provider, status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        add("%s: Enabled", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        add("%s: Disabled", provider);
    }
}
