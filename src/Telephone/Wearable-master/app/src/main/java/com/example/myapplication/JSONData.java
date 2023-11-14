package com.example.myapplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JSONData{

    /**
     * Example :
     * {
     *    "latitudes": {
     *          "115856776547": "12.3",
     *          ...
     * }
     */

    Map<String, Double> latitudes = new HashMap<String, Double>();
    Map<String, Double> longitudes = new HashMap<String, Double>();
    Map<String, Double> luminosities = new HashMap<String, Double>();
    Map<String, Double> pedometers = new HashMap<String, Double>();

    public JSONData() {

    }

    /**
     * Add a value to the JSONData object
     * @param type The type of the value (latitude, longitude, luminosity, podometer)
     * @param date The date of the value in milliseconds since 1970 (System.currentTimeMillis())
     * @param value The value associated to the date
     * @return void
     */

    public void addValue(String type, String date, double value) {
        switch(type) {
            case "latitude":
                latitudes.put(date, value);
                break;
            case "longitude":
                longitudes.put(date, value);
                break;
            case "luminosity":
                luminosities.put(date, value);
                break;
            case "podometer":
                pedometers.put(date, value);
                break;
        }
    }

    boolean isEmpty(){
        return latitudes.isEmpty() && longitudes.isEmpty() && luminosities.isEmpty() && pedometers.isEmpty();
    }

    /**
     * Empty the JSONData object
     */
    void empty(){
        latitudes = new HashMap<String, Double>();
        longitudes = new HashMap<String, Double>();
        luminosities = new HashMap<String, Double>();
        pedometers = new HashMap<String, Double>();
    }

    /**
     * Get the JSONData object as a JSONObject
     * @return JSONObject
     */
    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("latitudes", latitudes);
            obj.put("longitudes", longitudes);
            obj.put("luminosities", luminosities);
            obj.put("pedometers", pedometers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Get the JSONData object as a String
     * @return String
     */
    public String getJSONString() {
        return getJSONObject().toString();
    }

}