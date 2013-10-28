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
import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.Serializable;

public class MainActivity extends Activity implements LocationListener {
    private static final String STATE_KEY = MainActivity.class.getCanonicalName() + ".state";
    String provider = LocationManager.GPS_PROVIDER;
    private TextView text;
    private LocationManager lm;
    private int set = 0;
    private ScrollView scroll;
    private ToggleButton toggle;
    private boolean tracking;

    private Object formatLocation(Location l) {
        if (l == null)
            return "none";

        return String.format("%.6f/%.6f+%.1fm@%.1fm",
                l.getLatitude(), l.getLongitude(),
                l.getAltitude(), l.getAccuracy());
    }

    private void add(String formatted) {
        if (text.length() > 0)
            text.append("\n");

        text.append(formatted);

        Log.i("LocationTester", "DATA: " + formatted);

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
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

        if (tracking)
            lm.requestLocationUpdates(provider, 0, 0, this);
    }

    @Override
    protected void onDestroy() {
        Log.i("LocationTester", "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle bundle) {
        Log.i("LocationTester", "onCreate(): bundle=" + bundle);

        super.onCreate(bundle);
        setContentView(R.layout.main);

        text = (TextView) findViewById(R.id.status);
        scroll = (ScrollView) findViewById(R.id.scroll);
        toggle = (ToggleButton) findViewById(R.id.toggleButton);

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

    private boolean isTracking() {
        return tracking;
    }

    public void setTracking(boolean on) {
        tracking = on;

        if (findViewById(R.id.passiveButton) != null)
            findViewById(R.id.passiveButton).setEnabled(!on);

        if (findViewById(R.id.networkButton) != null)
            findViewById(R.id.networkButton).setEnabled(!on);

        if (findViewById(R.id.gpsButton) != null)
            findViewById(R.id.gpsButton).setEnabled(!on);

        findViewById(R.id.cachedButton).setEnabled(!on);

        if (on)
            lm.requestLocationUpdates(provider, 0, 0, this);
        else
            lm.removeUpdates(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putBoolean("tracking", tracking);
        bundle.putInt("set", set);
        bundle.putString("text", text.getText().toString());
        bundle.putString("provider", provider);
        bundle.putIntArray("position", new int[] { scroll.getScrollX(), scroll.getScrollY()});

        Log.i("LocationTester", "onSaveInstanceState: bundle=" + bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        Log.i("LocationTester", "onRestoreInstanceState: bundle=" + bundle);

        text.setText(bundle.getString("text"));
        set = bundle.getInt("set", 0);
        provider = bundle.getString("provider");
        final int[] position = bundle.getIntArray("position");
        if (position != null)
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.scrollTo(position[0], position[1]);
                }
            });

        setTracking(bundle.getBoolean("tracking", false));
    }

    public void onPassiveProviderSelected(View view) {
        provider = LocationManager.PASSIVE_PROVIDER;
        add(getString(R.string.selected_passive_text));
    }

    public void onNetworkProviderSelected(View view) {
        provider = LocationManager.NETWORK_PROVIDER;
        add(getString(R.string.selected_network_text));
    }

    public void onGpsProviderSelected(View view) {
        provider = LocationManager.GPS_PROVIDER;
        add(getString(R.string.selected_gps_text));
    }

    public void onTrackingToggled(View view) {
        boolean on = toggle.isChecked();

        Log.i("LocationTester", "onTrackingToggled: on=" + on);

        setTracking(on);

        if (on) {
            set++;
            add(getString(R.string.status_starting_format, provider, set));
        } else {
            add(getString(R.string.status_stopping_format, provider, set));
        }
    }

    public void onGetCachedLocationClick(View view) {
        set++;
        add(getString(R.string.status_cached_format, provider, set, formatLocation(lm.getLastKnownLocation(provider))));
    }

    @Override
    public void onLocationChanged(Location location) {
        add(getString(R.string.status_location_format, provider, set, formatLocation(location)));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        add(getString(R.string.status_changed_format, provider, status));
    }

    @Override
    public void onProviderEnabled(String provider) {
        add(getString(R.string.status_enabled_format, provider));
    }

    @Override
    public void onProviderDisabled(String provider) {
        add(getString(R.string.status_disabled_format, provider));
    }
}
