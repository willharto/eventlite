package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Entity
@Table(name="venues")
public class Venue implements Comparable<Venue>{

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message="The venue name cannot be empty.")
    @Size(max=50, message="The venue name can not exceed 50 characters.")
    private String name;

    @NotEmpty(message="The venue capacity cannot be empty.")
    @Min(value=5, message="The venue can not have a capacity less than 5 people.")
    private int capacity;

    @NotEmpty(message="The venue roadname cannot be empty.")
    @Size(max=500, message="The venue roadname can not exceed 500 characters.")
    private String roadname;

    @NotEmpty(message="The venue postcode cannot be empty.")
    @Size(max=7, message="The venue postcode must be in the format XXX XXX.")
    private String postcode;

    private double latitude;
    private double longitude;
	
    public Venue() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s", name, roadname, postcode);
    }
	
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
	
    public String getRoadname() {
        return roadname;
    }

    public void setRoadname(String roadname) {
        this.roadname = roadname;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int compareTo(Venue other) {
        return this.name.compareTo(other.name);
    }
}
