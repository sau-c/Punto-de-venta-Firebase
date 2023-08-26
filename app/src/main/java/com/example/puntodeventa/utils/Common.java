package com.example.puntodeventa.utils;

import com.example.puntodeventa.models.GoogleSheetsResponse;
import com.example.puntodeventa.models.IGoogleSheets;

public class Common {
    public static String BASE_URL = "https://script.google.com/macros/s/AKfycbyMJtnQzhkgxFxRVeS6Wyp87JyYJKWrDcOh3Md1ytIfwErZcXdRpnab7NzrcuZvTSX-DA/";
    public static String GOOGLE_SHEET_ID = "1PYFf0Dg6jKbR301lz0NLttWbWXbC9DMoyYUgn35KTDY";
    public static String SHEET_NAME = "accesorios";

    public static IGoogleSheets iGSGetMethodClient(String baseUrl) {
        return GoogleSheetsResponse.getClientGetMethod(baseUrl).create(IGoogleSheets.class);
    }
}