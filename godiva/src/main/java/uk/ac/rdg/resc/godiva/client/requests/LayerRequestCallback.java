package uk.ac.rdg.resc.godiva.client.requests;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Parses the JSON response and exposes the layer details
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class LayerRequestCallback implements RequestCallback {

    private static final NumberFormat format2Digits = NumberFormat.getFormat("00");
    private static final NumberFormat format4Digits = NumberFormat.getFormat("0000");

    private ErrorHandler err;

    private LayerDetails layerDetails;

    public LayerRequestCallback(String layerId, ErrorHandler err) {
        this.err = err;
        layerDetails = new LayerDetails(layerId);
    }

    @Override
    public void onError(Request request, Throwable exception) {
        err.handleError(exception);
    }

    public LayerDetails getLayerDetails() {
        return layerDetails;
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        JSONValue jsonMap = JSONParser.parseLenient(response.getText());
        JSONObject parentObj = jsonMap.isObject();

        JSONValue unitsJson = parentObj.get("units");
        // We don't care if no units are specified
        if (unitsJson != null) {
            layerDetails.setUnits(unitsJson.isString().stringValue());
        }

        JSONValue bboxJson = parentObj.get("bbox");
        if (bboxJson != null) {
            JSONArray bboxArr = bboxJson.isArray();
            if (bboxArr.size() != 4) {
                layerDetails.setExtents(null);
                err.handleError(new IndexOutOfBoundsException(
                        "Wrong number of elements for bounding box: " + bboxArr.size()));
            } else {
                layerDetails.setExtents(bboxArr.get(0).isString().stringValue() + ","
                        + bboxArr.get(1).isString().stringValue() + ","
                        + bboxArr.get(2).isString().stringValue() + ","
                        + bboxArr.get(3).isString().stringValue());
            }
        }

        JSONValue scaleRangeJson = parentObj.get("scaleRange");
        if (scaleRangeJson != null) {
            JSONArray scaleRangeArr = scaleRangeJson.isArray();
            if (scaleRangeArr.size() != 2) {
                err.handleError(new IndexOutOfBoundsException(
                        "Wrong number of elements for scale range: " + scaleRangeArr.size()));
            } else {
                layerDetails.setScaleRange(scaleRangeArr.get(0).isString().stringValue() + ","
                        + scaleRangeArr.get(1).isString().stringValue());
            }
        }

        JSONValue nColorBandsJson = parentObj.get("numColorBands");
        // Set a default value
        if (nColorBandsJson != null) {
            JSONNumber nColorBandsNum = nColorBandsJson.isNumber();
            layerDetails.setNColorBands((int) nColorBandsNum.doubleValue());
        }

        JSONValue logScalingJson = parentObj.get("logScaling");
        if (logScalingJson != null) {
            layerDetails.setLogScale(logScalingJson.isBoolean().booleanValue());
        }

        JSONValue supportedStylesJson = parentObj.get("supportedStyles");
        if (supportedStylesJson != null) {
            JSONArray supportedStylesArr = supportedStylesJson.isArray();
            List<String> supportedStyles = new ArrayList<String>();
            for (int i = 0; i < supportedStylesArr.size(); i++) {
                supportedStyles.add(supportedStylesArr.get(i).isString().stringValue());
            }
            layerDetails.setSupportedStyles(supportedStyles);
        } else {
            err.handleError(new NullPointerException("No styles listed"));
        }

        JSONValue moreInfoJson = parentObj.get("moreInfo");
        if (moreInfoJson != null) {
            layerDetails.setMoreInfo(moreInfoJson.isString().stringValue());
        }

        JSONValue copyrightJson = parentObj.get("copyright");
        if (copyrightJson != null) {
            layerDetails.setCopyright(copyrightJson.isString().stringValue());
        }

        JSONValue palettesJson = parentObj.get("palettes");
        if (palettesJson != null) {
            JSONArray palettesArr = palettesJson.isArray();
            List<String> availablePalettes = new ArrayList<String>();
            for (int i = 0; i < palettesArr.size(); i++) {
                availablePalettes.add(palettesArr.get(i).isString().stringValue());
            }
            layerDetails.setAvailablePalettes(availablePalettes);
            JSONValue defaultPaletteJson = parentObj.get("defaultPalette");
            if (defaultPaletteJson != null) {
                layerDetails.setSelectedPalette(defaultPaletteJson.isString().stringValue());
            }
        }

        // If we have different times, we may (will?) have a nearest
        // time string.
        JSONValue nearestTimeJson = parentObj.get("nearestTimeIso");
        if (nearestTimeJson != null) {
            String nearestTime = nearestTimeJson.isString().stringValue();
            if(!nearestTime.equals("") && nearestTime.length() > 10) {
                layerDetails.setNearestTime(nearestTime);
                layerDetails.setNearestDate(nearestTimeJson.isString().stringValue().substring(0, 10));
            }
        }

        boolean multiFeature = false;
        JSONValue multiFeatureJson = parentObj.get("multiFeature");
        if (multiFeatureJson != null) {
            multiFeature = multiFeatureJson.isBoolean().booleanValue();
        }
        layerDetails.setMultiFeature(multiFeature);

        if (multiFeature) {
            JSONValue startTimeJson = parentObj.get("startTime");
            if (startTimeJson != null) {
                layerDetails.setStartTime(startTimeJson.isString().stringValue());
            }
            JSONValue endTimeJson = parentObj.get("endTime");
            if (endTimeJson != null) {
                layerDetails.setEndTime(endTimeJson.isString().stringValue());
            }

            JSONValue startZJson = parentObj.get("startZ");
            if (startZJson != null) {
                layerDetails.setStartZ(startZJson.isString().stringValue());
            }
            JSONValue endZJson = parentObj.get("endZ");
            if (endZJson != null) {
                layerDetails.setEndZ(endZJson.isString().stringValue());
            }
            JSONValue zUnitsJson = parentObj.get("zUnits");
            if (zUnitsJson != null) {
                layerDetails.setZUnits(zUnitsJson.isString().stringValue());
            }
            JSONValue zPositiveJson = parentObj.get("zPositive");
            if (zPositiveJson != null) {
                layerDetails.setZPositive(zPositiveJson.isBoolean().booleanValue());
            }
        } else {
            JSONValue datesJson = parentObj.get("datesWithData");
            if (datesJson != null) {
                JSONObject datesObj = datesJson.isObject();
                List<String> availableDates = new ArrayList<String>();
                for (String yearString : datesObj.keySet()) {
                    int year = Integer.parseInt(yearString);
                    JSONObject yearObj = datesObj.get(yearString).isObject();
                    for (String monthString : yearObj.keySet()) {
                        // Months start from zero
                        int month = Integer.parseInt(monthString);
                        JSONArray daysArr = yearObj.get(monthString).isArray();
                        for (int iDay = 0; iDay < daysArr.size(); iDay++) {
                            int day = (int) daysArr.get(iDay).isNumber().doubleValue();
                            availableDates.add(format4Digits.format(year) + "-"
                                    + format2Digits.format(month + 1) + "-"
                                    + format2Digits.format(day));
                        }
                    }
                }
                layerDetails.setAvailableDates(availableDates);
            }

            JSONValue zvalsJson = parentObj.get("zaxis");
            if (zvalsJson != null) {
                JSONObject zvalsObj = zvalsJson.isObject();
                layerDetails.setZUnits(zvalsObj.get("units").isString().stringValue());
                layerDetails.setZPositive(zvalsObj.get("positive").isBoolean().booleanValue());
                List<String> availableZs = new ArrayList<String>();
                JSONArray zvalsArr = zvalsObj.get("values").isArray();
                for (int i = 0; i < zvalsArr.size(); i++) {
                    availableZs.add(zvalsArr.get(i).isNumber().toString());
                }
                layerDetails.setAvailableZs(availableZs);
            }
        }
    }

}
