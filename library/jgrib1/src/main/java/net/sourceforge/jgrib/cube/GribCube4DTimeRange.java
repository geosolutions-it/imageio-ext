/*
 * Created on Aug 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.jgrib.cube;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordPDS;


/**
 * This class is used here to hold information about the time range for a 
 * 4D cube inside a grib file.
 * It is able to tell the user the time for the forecast times of this cubes, basing 
 * its decision on the first forecast time, and the base time for this cube.
 * All the time values sotred here are in GMT timezone.
 *
 * @author Simone Giannecchini 
 */
final class GribCube4DTimeRange {
    /** Base time for this 4D cube. */
    protected GregorianCalendar baseTime = null;

    /** First forecast time for this 4D cube. */
    protected GregorianCalendar firstForecast = null;

    /** Last forecast time for this 4D cube. */
    protected GregorianCalendar lastForecast = null;

    /** Tau for this time list. */
    protected int TAU = -1;

    /**Number of time dwells for this cube.*/
    protected int numberOfDwells=0;

    /**
     * Default constructor.
     */
    public GribCube4DTimeRange() {
    }


    /**
     * When adding a new record to the cube this method
     * updates the information held by this class about
     * the time span of this 4D cube.
     *
     * @param record the record being added.
     * @return False in case an error occurred, true for success.
     */
    public boolean add(final GribRecord record) {

        //adding
        final GribRecordPDS pds = record.getPDS();
        final GregorianCalendar time1 = (GregorianCalendar) pds
            .getGMTForecastTime();
        final GregorianCalendar baseTime= (GregorianCalendar) pds.getGMTBaseTime();
        //this is the first record we add to this cube.
        if (this.baseTime == null) {
        	//base time
            this.baseTime =   new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            this.baseTime.clear();
            this.baseTime.set(baseTime.get(Calendar.YEAR),
            		baseTime.get(Calendar.MONTH),
            		baseTime.get(Calendar.DAY_OF_MONTH),
            		baseTime.get(Calendar.HOUR_OF_DAY),
            		baseTime.get(Calendar.MINUTE),
            		0);
            //first forecast time
            this.firstForecast =  new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            this.firstForecast.clear();//REMEBER THIS ALWAYS
            this.firstForecast.set(time1.get(Calendar.YEAR),
            		time1.get(Calendar.MONTH),
            		time1.get(Calendar.DAY_OF_MONTH),
            		time1.get(Calendar.HOUR_OF_DAY),
            		time1.get(Calendar.MINUTE),
            		0);
            this.firstForecast.set(Calendar.DST_OFFSET, 0);
            
            //last forecast time
            this.lastForecast =    new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            this.lastForecast.clear();
            this.lastForecast.set(time1.get(Calendar.YEAR),
            		time1.get(Calendar.MONTH),
            		time1.get(Calendar.DAY_OF_MONTH),
            		time1.get(Calendar.HOUR_OF_DAY),
            		time1.get(Calendar.MINUTE),
            		0);
            this.lastForecast.set(Calendar.DST_OFFSET, 0);
            
            //updating the number of elements in the time range
            this.numberOfDwells++;
            return true;
        }

        /**
         * 
         * If we ge there we do already have some records in this cube.
         * 
         */
        //setting min and max
        if (this.firstForecast.after(time1)) {
            this.firstForecast =   new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            this.firstForecast.clear();
            this.firstForecast.set(time1.get(Calendar.YEAR),
            		time1.get(Calendar.MONTH),
            		time1.get(Calendar.DAY_OF_MONTH),
            		time1.get(Calendar.HOUR_OF_DAY),
            		time1.get(Calendar.MINUTE),
            		0);
            this.firstForecast.set(Calendar.DST_OFFSET, 0);
        }

        if (this.lastForecast.before(time1)) {
            this.lastForecast =   new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            this.lastForecast.clear();
            this.lastForecast.set(time1.get(Calendar.YEAR),
            		time1.get(Calendar.MONTH),
            		time1.get(Calendar.DAY_OF_MONTH),
            		time1.get(Calendar.HOUR_OF_DAY),
            		time1.get(Calendar.MINUTE),
            		0);
            this.lastForecast.set(Calendar.DST_OFFSET, 0);
        }


        this.numberOfDwells++;
        return true;
    }

    /**
     * This method is used to check if a record is compatible with this cube. 
     * More specifically it checks whether or not the base time for the provided record
     * is the same of the base time for this cube, in such a case we should have 
     * compatibility.
     *
     * @param record Record to check compatibility for.
     *
     * @return True for compatible, false for not compatible.
     */
    public boolean isCompatible(GribRecord record) {
        //if the year has not been set that means that we
        //have not initialized this field yet.
        if (this.baseTime == null) {
            return true;
        }

        //field initialized, let's check the base time
        final GregorianCalendar cal = (GregorianCalendar) record.getPDS()
                                                                .getGMTBaseTime();

        if (cal.after(this.baseTime) || cal.before(this.baseTime)) {
            return false;
        }


        return true;
    }
	public GregorianCalendar getBaseTime() {
		return baseTime;
	}
	public GregorianCalendar getFirstForecast() {
		return firstForecast;
	}
	public GregorianCalendar getLastForecast() {
		return lastForecast;
	}
	public int getNumberOfDwells() {
		return numberOfDwells;
	}
}
