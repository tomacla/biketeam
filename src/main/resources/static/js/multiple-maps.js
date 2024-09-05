
var mapLayers = [];

function restoreMap() {
    mapLayers.forEach(function(layer) {
        layer.layer.setStyle({
          weight: 5,
          opacity: 0.8
        });
    });
}

function highlightMap(mapIndex) {
    restoreMap();
    const layer = mapLayers.find(function(l) {
        return l.mapIndex === mapIndex;
    });
    if(layer) {
        layer.layer.setStyle({
         weight: 8,
         opacity: 1
        });
        layer.layer.bringToFront();
        updateChart(layer.data, layer.color);
    }
}

function initMaps(mapCenter, maps, colors, trip) {
    var targetMap = initMap('map-wrapper', mapCenter[0] + 0.02, mapCenter[1], 11, "Carto");
    var featureGroup = L.featureGroup();
    var promisesCount = maps.length;
    maps.forEach((map, index) => {
        loadJsonContent(map.url, function(data) {

            var color = colors[index % 10];
            var layer = getLineFromData(data, color);
            layer.addTo(featureGroup);

            mapLayers.push({
                layer: layer,
                mapIndex: index,
                color: color,
                data: data
            });
            if (index === 0) {
                initChart(targetMap, 'chart-wrapper', data, color, false);
                var startPoint = data.points[0];
                L.marker([startPoint.lat, startPoint.lon], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})})
                    .addTo(featureGroup);
            }

            var endPoint = null;
            if (!trip && index === 0) {
                endPoint = data.points[data.points.length - 1];
            }
            if (trip && index === maps.length - 1) {
                endPoint = data.points[data.points.length - 1];
            }
            if (endPoint != null) {
                L.marker([endPoint.lat, endPoint.lon], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})})
                    .addTo(featureGroup);
            }

            promisesCount--;
            if(promisesCount === 0) {
                featureGroup.addTo(targetMap);
                targetMap.fitBounds(featureGroup.getBounds());
                highlightMap(0);
            }
        });
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
