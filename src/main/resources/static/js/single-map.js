// neutral hue (blue)
const neutralHue = 210;
// hue for lowest gradient
const minHue = 85;
// hue for max gradient
const maxHue = -105;
// const saturation/lightness
const saturation = "86%";
const lightness = "62%";

// neutral color
const singleMapNeutralColor = "hsl(" + neutralHue + "," + saturation + "," + lightness + ")";

// get color from point
function getColor(data, p) {
    if (p.climbIndex !== null && p.climbPartIndex !== null) {
        // in climb
        // grade of climb part (rounded)
        var grade = Math.round(data.climbs[p.climbIndex].parts[p.climbPartIndex].grade);
        // hue (0% : minHue, 18% : maxHue)
        hue = Math.round(minHue + (grade / 18.0) * (maxHue - minHue));
        // bounded
        hue = Math.min(minHue, Math.max(maxHue, hue));
    } else {
        // default color
        return singleMapNeutralColor;
    }
    if (hue < 0) {
        // modulo 360
        hue = hue + 360;
    }
    // CSS HSL color
    return "hsl(" + hue + "," + saturation + "," + lightness + ")";
}

// get featureGroup with lines per grade
function getLineWithGradesFromData(data) {

    // get color for each point
    var colors = data.points.map(p => getColor(data, p));

    // lines per color (color => array (lines) of array (line) of array (coords))
    var polylines = new Map();
    var previousColor = null;
    for (let i = 0; i < data.points.length - 1; i++) {
        var color = colors[i];

        // init lines for color
        if (!polylines.has(color)) {
            polylines.set(color, []);
        }
        var polyline = polylines.get(color);

        // array of points
        var line = null;
        if (color !== previousColor) {
            // start a new line
            line = [[data.points[i].lat, data.points[i].lon]];
            polyline.push(line);
            previousColor = color;
        } else {
            // use previous line
            line = polyline[polyline.length - 1];
        }
        // add point to line
        line.push([data.points[i + 1].lat, data.points[i + 1].lon]);
    }

    // polyline list
    var layers = [];

    // for each color
    for (const [color, polylineLatLngs] of polylines.entries()) {
        // polyline
        layers.push(new L.polyline(
            polylineLatLngs,
            {
                interactive: false,
                color: color,
                weight: 8,
                opacity: 0.8,
                lineCap: 'butt',
                renderer: lineRenderer
            }
        ));
    }

    // build result
    return L.featureGroup(layers, {interactive: false});
}

function initSingleMapView(mapContainerId, raw, dataUrl, elevationProfileContainer) {
    // chart is zoomable
    mapState.zoomableChart = true;
    //  mapLayer to add to map
    var mapLayer = L.layerGroup();

    // init map
    initLeafletMap(mapContainerId, 47, 3, 5, 'OpenStreetMap', {
        layersControl: true,
        zoomControl: true,
        positionControl: true,
        positionOnLoad: raw,
        trackDisplayCallBacks: raw ? false : [() => { leafletMap.addLayer(mapLayer) }, () => { leafletMap.removeLayer(mapLayer); }],
        streetViewControl: true,
        fullScreenControl: true
    });

    if (dataUrl != null) {
        // retrieve data
        loadJsonContent(dataUrl, function(data) {

            // get layer from data
            var line = getLineWithGradesFromData(data);
            // compute data index
            computeIndex(data);

            // add line to mapLayer
            line.addTo(mapLayer);

            // for each marker
            data.markers.forEach(marker => {
                var latlon = [marker.lat, marker.lon];
                if (marker.type === "start") {
                    // start
                    L.marker(latlon, {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})})
                        .addTo(mapLayer);
                } else if (marker.type === "end") {
                    // end
                    L.marker(latlon, {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})})
                        .addTo(mapLayer);
                } else {
                    // distance marker
                    L.circleMarker(latlon, {
                        radius: 10,
                        fillColor: "#ffffff",
                        color: "#000000",
                        weight: 1,
                        opacity: 1,
                        fillOpacity: 0.8
                    }).addTo(mapLayer);

                    L.tooltip({
                        permanent: true,
                        direction: 'center',
                        className: 'circle-text'
                    })
                        .setContent(marker.label)
                        .setLatLng(latlon)
                        .addTo(mapLayer);
                }
            });

            // add mapLayer to map
            mapLayer.addTo(leafletMap);

            // init chart
            initChart(
                elevationProfileContainer,
                data,
                (p) => getColor(data, p),
                (p) => getColor(data, p)
            );

            // fit map
            leafletMap.fitBounds(line.getBounds());
        });
    }

}
