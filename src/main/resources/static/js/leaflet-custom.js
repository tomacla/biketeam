// renderer with a lot of padding (show lines when paning)
var lineRenderer = L.svg({ padding: 1.0 });

// Leaflet map instance
var leafletMap = null;

// Marker for mouse
var mousePoint = L.marker([0.0, 0.0], {clickable: false, interactive: false, icon : L.divIcon({className: 'mapHoverIcon'})});

// Chart.js instance
var elevationChart = null;

// current displayed data
var currentData = null;

var mapState = {
    // index of selected point in currentData
    selected: -1,
    // true if chart is panable/zoomable
    zoomableChart: false,
    // true if mouse on chart
    onChart: false,
    // true if chart is moving (pan/zoom)
    chartMoving: false,
    // true if map is moving (pan/zoom)
    mapMoving: false,
    // prevent zooming chart after map update on a chart zoom
    chartMovingFalseTimer: null,
};

// show selected point
function doUpdatePoint() {
    if (leafletMap === null || currentData === null || elevationChart === null) {
        // invalid state
        return;
    }
    // selected point
    var p = null;

    const index = mapState.selected;
    if (index && index >= 0 && currentData && currentData.points && index < currentData.points.length) {
        // point selected
        p = currentData.points[index];
    }

    // handle map marker

    if (p === null) {
        if (leafletMap.hasLayer(mousePoint)) {
            // hide point
            leafletMap.removeLayer(mousePoint);
        }
    } else {
        var currentPosition = mousePoint.getLatLng();
        var newPosition = new L.LatLng(p.lat, p.lon)
        if (!currentPosition.equals(newPosition)) {
            // set latlng if changed
            mousePoint.setLatLng(newPosition);
        }
        if (!leafletMap.hasLayer(mousePoint)) {
            // show point
            leafletMap.addLayer(mousePoint);
        }
    }

    // Chart.js tooltip structure
    const tooltip = elevationChart.tooltip;

    // index to show
    var tooltipIndex = null;
    // dist for corshair
    var mousePointDist = null;

    if (p === null || p.dist < elevationChart.scales.x.min || p.dist > elevationChart.scales.x.max) {
        // no selected point
        tooltipIndex = -1;
        mousePointDist = -1;
    } else {
        tooltipIndex = p.index;
        mousePointDist = p.dist;
    }

    // should tooltip be set (null : do nothing, -1 : remove tooltip, >=0 : show tooltip)
    var newTooltipIndex = null;

    if (mousePointDist !== elevationChart.mousePointDist) {
        // corshair changed, force corshair/tooltip update
        newTooltipIndex = tooltipIndex;
    } else if (mapState.chartMoving || mapState.mapMoving) {
        // moving, force corshair/tooltip update
        newTooltipIndex = tooltipIndex;
    } else {
        // current tooltip shown
        var currentActiveElements = tooltip.getActiveElements();
        if (tooltipIndex === -1 && currentActiveElements.length > 0) {
            // tooltip shown but should be hidden
            newTooltipIndex = -1;
        }
        if (tooltipIndex >= 0) {
            // tooltip should be visible
            if (currentActiveElements.length === 0) {
                // no tooltip visible, force corshair/tooltip update
                newTooltipIndex = tooltipIndex;
            } else {
                var currentTooltipIndex = currentActiveElements[0].index;
                if (currentTooltipIndex != tooltipIndex) {
                    // not the good tooltip shown, force corshair/tooltip update
                    newTooltipIndex = tooltipIndex;
                }
            }
        }

    }

    if (newTooltipIndex !== null) {
        // update corshair
        elevationChart.mousePointDist = mousePointDist;

        if (newTooltipIndex === -1) {
            // hide tooltip
            tooltip.setActiveElements([], {x: 0, y: 0});
        } else {
            // show tooltip
            tooltip.setActiveElements([
                {
                    datasetIndex: 0,
                    index: newTooltipIndex
                }
            ],
                null);
        }
        // render chart
        elevationChart.render();
    }
}

// update selected point every 30ms
setInterval(doUpdatePoint, 30);

// handle all map/chart events for showing selected point
function changeMapState(eventType, event) {
    if (!currentData) {
        return;
    }

    if (eventType === "mapmousemove" || eventType === "mapclick") {
        // disable event when moving map or chart
        if (!mapState.mapMoving && !mapState.chartMoving) {
            // update selected point from nearest point
            mapState.selected = getPointFromLatLng(event.latlng);
        }
    }
    if (eventType === "mapmouseout") {
        // disable event when moving map or chart
        if (!mapState.mapMoving && !mapState.chartMoving) {
            // out of map -> no point selected
            mapState.selected = -1;
        }
    }
    if (eventType === "mapmovestart") {
        // map is moving
        mapState.mapMoving = true;
    }
    if (eventType === "mapmove") {
        // disable event when moving chart
        if (mapState.zoomableChart && !mapState.chartMoving) {
            // set chart view linked to map view
            updateChartViewFromMap();
        }
    }
    if (eventType === "mapmoveend") {
        // map moving is finished
        mapState.mapMoving = false;
        // disable event when moving chart
        if (mapState.zoomableChart && !mapState.chartMoving) {
            // set chart view linked to map view
            updateChartViewFromMap();
        }
    }

    if (eventType === "chartmousemove") {
        // mouse on chart
        mapState.onChart = true;
    }
    // this event comes from chartjs tooltip, triggered anytime tooltip is displayed
    if (eventType === "charttooltip") {
        // mouse on chart, disable event when moving chart
        if (mapState.onChart && !mapState.mapMoving && !mapState.chartMoving) {
            // set selected point
            mapState.selected = event;
        }
    }
    if (eventType === "chartmouseout") {
        // mouse not on chart
        mapState.onChart = false;
        // disable this when moving map or chart
        if (!mapState.mapMoving && !mapState.chartMoving) {
            // no point selected
            mapState.selected = -1;
        }
    }
    if (eventType == "chartmovestart") {
        // chart is starting to pan/zoom
        mapState.chartMoving = true;
        // cancel timer setting chartMoving to false
        clearTimeout(mapState.chartMovingFalseTimer);
    }
    // if chart is zoomable
    if (eventType === "chartmovecomplete" && mapState.zoomableChart) {
        // update map view from chart
        updateMapViewFromChart();
        // cancel timer setting chartMoving to false
        clearTimeout(mapState.chartMovingFalseTimer);
        // new timer setting chartMoving to false
        // 300ms for letting the time to map to be set (mapmoveend will trigger during this)
        mapState.chartMovingFalseTimer = setTimeout(() => {
            mapState.chartMoving = false;
        }, 300);
    }
}

// update chart zoom based on map
function updateChartViewFromMap() {
    var bounds = leafletMap.getBounds();
    // visible points
    var points = currentData.points
        .filter(p => bounds.contains(L.latLng(p.lat, p.lon)));
    var pMin;
    var pMax;
    if (points.length === 0) {
        // no point, full extent for chart
        pMin = 0;
        pMax = currentData.info.dist;
    } else {
        // min/max value
        pMin = points.reduce((prev, curr) => prev.dist < curr.dist ? prev : curr);
        pMax = points.reduce((prev, curr) => prev.dist > curr.dist ? prev : curr);
    }
    elevationChart.options.scales.x.min = pMin.dist;
    elevationChart.options.scales.x.max = pMax.dist;
    // update chart
    elevationChart.update();
}

// update map bounds based on chart extent
function updateMapViewFromChart() {
    var minDist = elevationChart.scales.x.min;
    var maxDist = elevationChart.scales.x.max;

    // points in visible chart
    var points = currentData.points
        .filter(p => (minDist <= p.dist) && (p.dist <= maxDist))
        .map(p => [p.lat, p.lon]);
    // set map bounds (L.latLngBounds compute bounds based on point list)
    leafletMap.fitBounds(L.latLngBounds(points));
}

function getPointFromLatLng(latlng) {
    // map bounds
    var bounds = leafletMap.getBounds();
    // corners
    var sw = bounds.getSouthWest();
    var ne = bounds.getNorthEast();
    // distance between corners (km)
    var mapViewSize = distance(sw.lng, sw.lat, ne.lng, ne.lat);
    // get nearest point, max dist is mapViewSize / 20.0
    const nearestIds = around(currentData.index, latlng.lng, latlng.lat, 1, mapViewSize / 20.0);
    if (nearestIds.length === 1) {
        // return the id
        return nearestIds[0];
    } else {
        // no valid point
        return -1;
    }
}

function activateStreetView(e) {
    window.open('http://maps.google.com/maps?q=&layer=c&cbll='+e.latlng.lat+','+e.latlng.lng+'&cbp=11,0,0,0,0', '_blank');
}

// compute spatial index for data
function computeIndex(data) {

    // remove negative elevation
    data.points.forEach(p => {
        if (p.ele < 0) {
            p.ele = 0;
        }
    });

    const index = new KDBush(data.points.length);
    data.points.forEach(p => index.add(p.lon, p.lat));
    index.finish();
    data.index = index;
}

function initLeafletMap(mapContainerId, lat, lng, zoom, defaultLayer, providedOptions = {}) {
    leafletMap = L.map(mapContainerId, { zoomControl: false, layers: [layers[defaultLayer]] }).setView([lat, lng], zoom);

    var options = {
        layersControl: providedOptions.layersControl || false,
        zoomControl: providedOptions.zoomControl || false,
        positionControl: providedOptions.positionControl || false,
        positionOnLoad: providedOptions.positionOnLoad || false,
        trackDisplayCallBacks: providedOptions.trackDisplayCallBacks || false,
        streetViewControl: providedOptions.streetViewControl || false,
        fullScreenControl : providedOptions.streetViewControl || false,
    }
    if(options.layersControl) {
        L.control.layers(layers, overlayLayers, {position: 'bottomleft'}).addTo(leafletMap);
    }
    if(options.zoomControl) {
        L.control.zoom({position: 'bottomright'}).addTo(leafletMap);
    }
    if(options.positionControl) {
        var lc = L.control.locate({position: 'bottomright', drawCircle : false, showPopup : false}).addTo(leafletMap);
        if(options.positionOnLoad) {
            lc.start();
        }
    }
    if(options.fullScreenControl) {
        leafletMap.addControl(new L.Control.Fullscreen({
            title: {
                'false': 'Plein écran',
                'true': 'Sortie'
            }
        }));
    }
    if(options.trackDisplayCallBacks) {
        L.easyButton({
            position:'bottomright',
            states: [{
                stateName: 'button-hide-track',        // name the state
                icon:      'bi bi-eye-slash-fill fs-6',               // and define its properties
                title:     'Masquer',      // like its title
                onClick: function(btn, map) {       // and its callback
                    options.trackDisplayCallBacks[1]();
                    btn.state('button-show-track');    // change state on click!
                }
            }, {
                stateName: 'button-show-track',
                icon:      'bi bi-eye-fill fs-6',
                title:     'Afficher',
                onClick: function(btn, map) {
                    options.trackDisplayCallBacks[0]();
                    btn.state('button-hide-track');
                }
            }]
        }).addTo( leafletMap );
    }

    if(options.streetViewControl) {
        L.easyButton({
            position:'bottomright',
            states: [{
                stateName: 'button-street-view-start',        // name the state
                icon:      'bi bi-person-walking fs-6',               // and define its properties
                title:     'Streetview',      // like its title
                onClick: function(btn, map) {       // and its callback
                    leafletMap.on('click', activateStreetView);
                    btn.button.style.color = 'orange';
                    btn.state('button-street-view-stop');    // change state on click!
                }
            }, {
                stateName: 'button-street-view-stop',
                icon:      'bi bi-person-walking fs-6',
                title:     'Streetview',
                onClick: function(btn, map) {
                    leafletMap.off('click', activateStreetView);
                    btn.button.style.color = 'black';
                    btn.state('button-street-view-start');
                }
            }]
        }).addTo( leafletMap );
    }

    // events

    leafletMap.on('click', e => changeMapState('mapclick', e));
    leafletMap.on('mousemove', e => changeMapState('mapmousemove', e));
    leafletMap.on('mouseout', e => changeMapState('mapmouseout', e));

    leafletMap.on('movestart', e => changeMapState('mapmovestart', e));
    leafletMap.on('move', e => changeMapState('mapmove', e));
    leafletMap.on('moveend', e => changeMapState('mapmoveend', e));
}

// compute ticks for chart
function getTicks(ticks) {
    let values = ticks.map(t => t.value);
    let min = Math.min(...values);
    let max = Math.max(...values);

    var step;
    if (max - min > 6) {
        // range > 6km => 1 tick every km
        step = 1.0;
    } else if (max - min > 2) {
        // range > 2km => 1 tick every 500m
        step = 0.5;
    } else {
        // 1 tick every 200m
        step = 0.2;
    }

    // start/end tick
    let minI = Math.ceil(min / step);
    let maxI = Math.floor(max / step);
    // 1 tick per step between start and end
    newTicks = [];
    for (let i = minI; i <= maxI; i++) {
        newTicks.push(i * step);
    }
    return newTicks.map(t => ({value: t}));
}

// tooltip for point
function getTooltip(data, p) {
    // base info (dist, elevation, grade)
    var result = Math.round(p.dist * 10) / 10 + " km "
    + Math.round(p.ele) + " m ("
    + (Math.round(p.grade * 10) / 10) + " %)";
    if (p.climbIndex !== null && p.climbIndex < data.climbs.length) {
        // in climb
        var climb = data.climbs[p.climbIndex];
        if (p.climbPartIndex !== null && p.climbPartIndex < climb.parts.length) {
            // climb info (index/count, elevation, dist, grade)
            result = result + "\n"
            + "Montée (" + (p.climbIndex+1) + "/" + data.climbs.length + ") : "
            + Math.round(climb.elevation) + " m/"
            + Math.round(climb.dist / 100) / 10 + " km (" +
            + Math.round(climb.climbingGrade * 10) / 10 + " %)";
            if (climb.parts.length > 1) {
                // climb part info (index/count, dist, grade)
                var part = climb.parts[p.climbPartIndex];
                result = result + "\n"
                + "Partie (" + (p.climbPartIndex+1) + "/" + climb.parts.length + ") : " + Math.round(part.dist / 100) / 10 + " km (" +
                Math.round(part.grade * 10) / 10 + " %)";
            }
        }
    }
    return result;
}

function initChart(chartContainerId, data, getSegmentBackgroundColor, getSegmentBorderColor) {

    currentData = data;

    if (elevationChart !== null) {
        return;
    }

    const configData = {
        datasets: [{
            fill: true,
            label: 'Elevation',
            borderColor: 'rgb(0, 0, 0)',
            borderWidth: 1,
            pointRadius: 0,
            pointHoverBorderWidth: 0,
            pointHoverRadius: 0,
            segment: {
                backgroundColor: ctx => getSegmentBackgroundColor(ctx.p0.raw),
                borderColor: ctx => getSegmentBorderColor(ctx.p0.raw),
            },
            data: data.points
        }]
    };

    const corshair = corshairPlugin();
    // disable tooltip of chart is not zoomable
    var showTooltip = mapState.zoomableChart;
    const tooltipOptions = {
        enabled: showTooltip,
        animation: false,
        callbacks: {
            title: (items) => {
                if (showTooltip && items.length > 0) {
                    return getTooltip(data, items[0].raw);
                } else {
                    return "";
                }
            },
            label: (item) => ""
        },
        external: function(context) {
            if (context.tooltip.dataPoints &&
            context.tooltip.dataPoints.length > 0) {
                // point selected
                changeMapState('charttooltip', context.tooltip.dataPoints[0].dataIndex);
            } else {
                // no point selected
                changeMapState('charttooltip', -1);
            }
        }
    };

    var zoomOptions;
    if (mapState.zoomableChart) {
        zoomOptions = {
            zoom: {
                wheel: {
                    enabled: true,
                },
                pinch: {
                    enabled: true
                },
                mode: 'x',
                onZoomComplete: () => changeMapState('chartmovecomplete'),
                onZoomStart: () => changeMapState('chartmovestart'),
            },
            pan: {
                enabled: true,
                mode: 'x',
                onPanComplete: () => changeMapState('chartmovecomplete'),
                onPanStart: () => changeMapState('chartmovestart'),
            },
            limits: {
                x: {
                    min: 0,
                    max: data.info.dist,
                    minRange: 1.0
                }
            },
        };
    } else {
        // no zoom/pinch/pan
        zoomOptions = {
            zoom: {
                wheel: {
                    enabled: false,
                },
                pinch: {
                    enabled: false
                }
            },
            pan: {
                enabled: false
            }
        };
    }

    const config = {
        type: 'line',
        data: configData,
        options: {
            responsive: true,
            // aspect ratio can change in DOM
            maintainAspectRatio: false,
            parsing: {
                // attributes in points data
                xAxisKey: 'dist',
                yAxisKey: 'ele'
            },
            interaction: {
                intersect: false,
                axis: 'x',
                mode: 'nearest'
            },
            scales: {
                x: {
                    type: 'linear',
                    afterBuildTicks: axis => {
                        // custom ticks
                        axis.ticks = getTicks(axis.ticks);
                    },
                    ticks: {
                        stepSize: 1.0,
                        minRotation: 0,
                        maxRotation: 0,
                        callback: function(value, index, ticks) {
                            if (Math.round(value) === value) {
                                // do not show .0 in xxx.0
                                return Math.round(value);
                            }
                            return value.toFixed(1);
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: tooltipOptions,
                zoom: zoomOptions,
            },
            layout: {
                padding: 0
            }
        },
        plugins: [corshair]
    };

    var chartElement = document.getElementById(chartContainerId);

    // events
    chartElement.addEventListener('mousemove', e => changeMapState('chartmousemove', e));
    chartElement.addEventListener('mouseout', e => changeMapState('chartmouseout', e));

    elevationChart = new Chart(chartElement, config);
    elevationChart.options.animation.duration = 250;
}
