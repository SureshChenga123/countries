package com.accenture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class CountryInfo {

    public static void main(String[] args) {
        try {
            int maxDifferentBorders = 0;
            String countryWithMostDifferentBorders = "";
            String apiUrl = "https://restcountries.com/v3.1/all";
            //Fetch data from restcountries api
            String responseData = getDataFromRestCountriesApi(apiUrl);
            JSONArray countriesData = new JSONArray(responseData);

            List<JSONObject> countriesList = IntStream
                    .range(0, countriesData.length())
                    .mapToObj(countriesData::getJSONObject)
                    .collect(Collectors.toList());

            // Sorted list of countries by population density in descending order.
            countriesList.sort((country1, country2) ->
                    Double.compare(calculatePopulationDensity(country2), calculatePopulationDensity(country1)));


            for (JSONObject country : countriesList) {
                System.out.println(country.getJSONObject("name").getString("common") +
                        " - Population Density: " + calculatePopulationDensity(country));

                // Country in Asia containing the most bordering countries of a different region
                if (country.getString("region").equals("Asia")) {
                    int differentBorders = countDifferentRegionBorders(country, countriesData);
                    if (differentBorders > maxDifferentBorders) {
                        maxDifferentBorders = differentBorders;
                        countryWithMostDifferentBorders = country.getJSONObject("name").getString("common");
                    }
                }
            }

            System.out.println("Country in Asia with most bordering countries of a different region:" + countryWithMostDifferentBorders);

        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    private static String getDataFromRestCountriesApi(String apiUrl) throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private static double calculatePopulationDensity(JSONObject country) {
        double population = country.getDouble("population");
        double area = country.getDouble("area");

        if (population == 0 || area == 0) {
            return 0;
        }

        return population / area;
    }

    private static int countDifferentRegionBorders(JSONObject country, JSONArray countriesData) {

        int count = 0;
        if (country.isNull("borders")) {
            return count;
        }
        JSONArray borders = country.getJSONArray("borders");

        for (int i = 0; i < borders.length(); i++) {
            String borderCountryCode = borders.getString(i);
            JSONObject borderCountry = findCountryByCode(borderCountryCode, countriesData);

            if (borderCountry != null && !borderCountry.getString("region").equals(country.getString("region"))) {
                count++;
            }
        }

        return count;
    }

    private static JSONObject findCountryByCode(String countryCode, JSONArray countriesData) {
        for (int i = 0; i < countriesData.length(); i++) {
            JSONObject country = countriesData.getJSONObject(i);
            if (country.getString("cca3").equals(countryCode)) {
                return country;
            }
        }
        return null;
    }
}


