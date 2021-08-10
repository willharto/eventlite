package uk.ac.man.cs.eventlite.dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

    private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);
    private final static String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiZXZlbnRsaXRlZjEyIiwiYSI6ImNrODMwOWM3ODAxa3AzdW81YTFyaGp4cGkifQ.FzUdEKsz2vsaMLx2W69pXQ";
    private final static int MAPBOX_WAIT_INTERVAL = 500;
    private final static int MAPBOX_WAIT_TIME = 3000;
    public static Boolean isMapboxSuccess = null;

    @Autowired
    private VenueRepository venueRepository;

    @Override
    public long count() {
        return venueRepository.count();
    }

    @Override
    public Iterable<Venue> findAll() {
        Iterable<Venue> venues = venueRepository.findAll();
        Collections.sort((List<Venue>) venues);
        return venues;
    }

    @Override
    public void save(Venue venue) throws ServiceException {
        log.info(String.format("Querying [[ %s ]] via Mapbox API", venue.getFullAddress()));

        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
            .accessToken(MAPBOX_ACCESS_TOKEN)
            .country(java.util.Locale.UK) // Restrict query to only UK addresses.
            .query(venue.getFullAddress())
            .build();

        isMapboxSuccess = null;

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {
                    isMapboxSuccess = true;
                    Point firstResultPoint = results.get(0).center();
                    double lat = firstResultPoint.latitude();
                    double lon = firstResultPoint.longitude();
                    log.info(String.format("Point [[ %f , %f ]] received from Mapbox API", lat, lon));
                    venue.setLatitude(lat);
                    venue.setLongitude(lon);
                    return;
                }
                isMapboxSuccess = false;
                log.info("No result from Mapbox API");
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        for (int i = 0; i < MAPBOX_WAIT_TIME / MAPBOX_WAIT_INTERVAL && isMapboxSuccess == null; i++) {
            try {
              Thread.sleep(MAPBOX_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                log.info("Wait for Mapbox response has been interrupted");
            }
        }

        if (isMapboxSuccess == null || !isMapboxSuccess) {
            throw new ServiceException(String.format("Mapbox failed to return a point in %.1fs\n"
                + "%s was not saved", (double)MAPBOX_WAIT_TIME / 1000.0, venue));
        }

        venueRepository.save(venue);
        log.info(String.format("Saved [[ %s ]] to database.", venue));
    }

    @Override
    public Optional<Venue> findById(long id) {
        return venueRepository.findById(id);
    }

    @Override
    public Venue findOne(long id) {
        return venueRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(long id) {
        venueRepository.deleteById(id);
        log.info("Deleted venue with " + id + " from database.");
    }

    @Override
    public void delete(Venue venue) {
        venueRepository.delete(venue);
        log.info("Deleted [[ " + venue + " ]] from database.");
    }
}
