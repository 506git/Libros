package eco.libros.android.gps

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.content.ContextCompat

open class GpsTracker(context: Context?) : Service(), LocationListener {
    private var mContext : Context? = null
    private var location : Location? = null
    private var latitude : Double? = null
    private var longtitude : Double? =null
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
    private val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1

    protected var locationManager : LocationManager? = null

    init {
        this.mContext = context
        getLocation();
    }

    public fun getLocation(){
        locationManager = mContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isGpsEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var isNetworkEnabled: Boolean = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isGpsEnabled && isNetworkEnabled){
            val hasFineLocationPermission: Int = ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION);
            val hasCoarseLocationPermission: Int = ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            } else
                return
            if (isNetworkEnabled) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this@GpsTracker
                )
                if (locationManager != null) {
                    location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        latitude = location?.latitude
                        longtitude = location?.longitude
                    }
                }
            }
            if (isGpsEnabled){
                if (location == null){
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES, this@GpsTracker)
                    if (locationManager != null){
                        latitude = location?.latitude
                        latitude = location?.longitude
                    }
                }
            }
        }
    }

    public fun getLatitude() : Double? {
        if (location != null){
            latitude = location?.latitude
        }
        return latitude
    }

    fun getLongitude() : Double? {
        if(location != null){
            longtitude = location?.longitude
        }
        return longtitude
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onLocationChanged(p0: Location) {

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    public fun stopUsingGps(){
        if(locationManager !=null){
            locationManager?.removeUpdates(this@GpsTracker)
        }
    }
}