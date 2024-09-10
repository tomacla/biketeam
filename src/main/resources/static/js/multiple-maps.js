var mapLayers = [];

var chartNeutralColor = null;

// update chart data
function updateChart(data, color) {
    currentData = data;
    elevationChart.data.datasets[0].data = data.points;
    chartNeutralColor = color;
    elevationChart.update();
    elevationChart.resetZoom();
    elevationChart.render();
}

function restoreMap() {
    mapLayers.forEach(function(layer) {
        layer.layer.setStyle({
            weight: 5,
            opacity: 0.5
        });
    });
}

function highlightMap(mapIndex) {
    // reset all layers
    restoreMap();
    // find layer
    const layer = mapLayers.find(function(l) {
        return l.mapIndex === mapIndex;
    });
    if(layer) {
        // highlight layer
        layer.layer.setStyle({
            weight: 8,
            opacity: 0.7
        });
        layer.layer.bringToFront();
        // update chart
        updateChart(layer.data, layer.color);
    }
}

// simple polyline
function getSimpleLineFromData(data, color) {
    var latLngs = data.points.map(p => [p.lat, p.lon]);
    return new L.polyline(
        latLngs,
        {
            interactive: true,
            color: color,
            weight: 8,
            opacity: 0.8,
            lineCap: 'butt',
            renderer: lineRenderer
        }
    );
}

function initMapsChart(data, color) {
    chartNeutralColor = color;
    initChart(
        'chart-wrapper',
        data,
        (p) => chartNeutralColor,
        (p) => 'black'
    );
}

function initMaps(mapCenter, maps, colors, trip) {
    // init map
    initLeafletMap('map-wrapper', mapCenter[0], mapCenter[1], 11, "Carto");

    // contains all lines
    var featureGroup = L.featureGroup();

    // queries
    const queries = maps.map((map, index) => {
        return { index, map, url: map.url };
    });
    // query all data
    loadJsonContentMultiple(queries, results => {
        if (results.length > 0) {
            // at least one result
            // first result
            var data = results[0].result;
            // init chart
            initMapsChart(data, colors[0]);
            var startPoint = data.points[0];
            L.marker([startPoint.lat, startPoint.lon], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})})
                .addTo(featureGroup);
        }
        // for each result
        results.forEach(result => {
            var index = result.query.index;
            var data = result.result;

            // compute spatial index
            computeIndex(data);

            // get line
            var color = colors[index % 10];
            var layer = getSimpleLineFromData(data, color);
            // click event
            layer.on('click', e => highlightMap(index));

            // add to featureGroup
            layer.addTo(featureGroup);

            // store result
            mapLayers.push({
                layer: layer,
                mapIndex: index,
                color: color,
                data: data
            });

            // compute endpoint
            var endPoint = null;
            if (!trip && index === 0) {
                // not trip => end is end of first map
                endPoint = data.points[data.points.length - 1];
            }
            if (trip && index === maps.length - 1) {
                // trip => end is end of last map
                endPoint = data.points[data.points.length - 1];
            }
            // endpoint found ? add it to featureGroup
            if (endPoint != null) {
                L.marker([endPoint.lat, endPoint.lon], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})})
                    .addTo(featureGroup);
            }
        });
        // at least one result
        if (results.length > 0) {
            // add featureGroup to map
            featureGroup.addTo(leafletMap);
            // highlight first map
            highlightMap(0);
            // fit all maps
            leafletMap.fitBounds(featureGroup.getBounds());
        }
    });
}

function resetPlacemodal() {
    const myModalEl = document.getElementById('placemodal')
    myModalEl.addEventListener('show.bs.modal', event => {
        leafletModalMaps.forEach(map => {
            setTimeout(function() {
                map.invalidateSize();
            }, 10);
        });
    })
}
