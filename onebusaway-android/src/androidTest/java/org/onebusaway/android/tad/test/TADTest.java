package org.onebusaway.android.tad.test;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.onebusaway.android.io.test.ObaTestCase;
import org.onebusaway.android.mock.Resources;
import org.onebusaway.android.tad.Segment;
import org.onebusaway.android.tad.TADNavigationServiceProvider;

import java.io.Reader;

/**
 * Created by azizmb on 3/18/16.
 */
public class TADTest extends ObaTestCase {

    static final String TAG = "TADTest";

    public void testTrip() {
        try {
            // Read test CSV.
            Reader reader = Resources.read(getContext(), Resources.getTestUri("tad_trip_coords_1"));
            String csv = IOUtils.toString(reader);

            String TRIP_ID = getTripId(csv);
            String STOP_ID = getDestinationId(csv);

            TADNavigationServiceProvider provider = new TADNavigationServiceProvider(TRIP_ID, STOP_ID);

            // Construct Destination & Second-To-Last Locations
            Location dest = getDestinationLocation(csv);
            Location last = getBeforeDestinationLocation(csv);

            Segment segment = new Segment(last, dest, null);


            // Begin navigation & simulation
            provider.navigate(new Segment[]{segment});

            //Location[] locations = getTrip(csv);

            int i = 0;
            for (Location l : getTrip(csv)) {
                provider.locationUpdated(l);
                Thread.sleep(500);
                Log.i(TAG, String.format("%d: (%f, %f, %f)\tR:%s  F:%s", i++,
                        l.getLatitude(), l.getLongitude(), l.getSpeed(),
                        Boolean.toString(provider.getGetReady()), Boolean.toString(provider.getFinished())
                ));
            }

            assertEquals(true, provider.getGetReady() && provider.getFinished());
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    /**
     * Takes a CSV string and returns an array of Locations built from CSV data.
     * The first line of the csv is assumed to be a header, and the columns as follows
     * time, lat, lng, elevation, accuracy, bearing, speed, provider.
     * Generated using GPS Logger for Android (https://github.com/mendhak/gpslogger)
     * (Also, available on the play store).
     *
     * @param csv
     * @return
     */
    private Location[] getTrip(String csv) {
        String[] lines = csv.split("\n");

        Location[] locations = new Location[lines.length - 1];

        // Skip header and run through csv.
        // Rows are formatted like this:
        // time,lat,lon,elevation,accuracy,bearing,speed,satellites,provider
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            /*double lat = Double.parseDouble(values[1]);
            double lng = Double.parseDouble(values[2]);
            double alt = Double.parseDouble(values[3]);
            float acc = Float.parseFloat(values[4]);
            float bearing = Float.parseFloat(values[5]);
            float speed = Float.parseFloat(values[6]);
            String provider = values[8];*/

            double lat = Double.parseDouble(values[1]);
            double lng = Double.parseDouble(values[2]);
            float speed = Float.parseFloat(values[3]);
            String provider = values[4];

            locations[i - 1] = new Location(provider);
            locations[i - 1].setLatitude(lat);
            locations[i - 1].setLongitude(lng);
            //locations[i-1].setBearing(bearing);
            locations[i - 1].setSpeed(speed);
            /*locations[i-1].setAccuracy(acc);
            locations[i-1].setAltitude(alt);*/
        }

        return locations;
    }

    private String getTripId(String csv) {
        String[] lines = csv.split("\n");
        String[] details = lines[0].split(",");
        return details[0];
    }

    private String getDestinationId(String csv) {
        String[] lines = csv.split("\n");
        String[] details = lines[0].split(",");
        return details[1];
    }

    private Location getDestinationLocation(String csv) {
        String[] lines = csv.split("\n");
        String[] details = lines[0].split(",");
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(Double.parseDouble(details[2]));
        loc.setLongitude(Double.parseDouble(details[3]));
        return loc;
    }

    private Location getBeforeDestinationLocation(String csv) {
        String[] lines = csv.split("\n");
        String[] details = lines[0].split(",");
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(Double.parseDouble(details[5]));
        loc.setLongitude(Double.parseDouble(details[6]));
        return loc;
    }
}
