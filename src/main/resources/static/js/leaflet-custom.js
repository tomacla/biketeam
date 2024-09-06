var mouseDownFlag = 0;

function initMap(mapContainerId, lat, lng, zoom, defaultLayer, providedOptions = {}) {

    var newMap = L.map(mapContainerId, { zoomControl: false, layers: [layers[defaultLayer]] }).setView([lat, lng], zoom);

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
        L.control.layers(layers, overlayLayers, {position: 'bottomleft'}).addTo(newMap);
    }
    if(options.zoomControl) {
        L.control.zoom({position: 'bottomright'}).addTo(newMap);
    }
    if(options.positionControl) {
        var lc = L.control.locate({position: 'bottomright', drawCircle : false, showPopup : false}).addTo(newMap);
        if(options.positionOnLoad) {
            lc.start();
        }
    }
    if(options.fullScreenControl) {
        newMap.addControl(new L.Control.Fullscreen({
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
        }).addTo( newMap );
    }

    if(options.streetViewControl) {
        L.easyButton({
            position:'bottomright',
            states: [{
                    stateName: 'button-street-view-start',        // name the state
                    icon:      'bi bi-person-walking fs-6',               // and define its properties
                    title:     'Streetview',      // like its title
                    onClick: function(btn, map) {       // and its callback
                        newMap.on('click', activateStreetView);
                        btn.button.style.color = 'orange';
                        btn.state('button-street-view-stop');    // change state on click!
                    }
                }, {
                    stateName: 'button-street-view-stop',
                    icon:      'bi bi-person-walking fs-6',
                    title:     'Streetview',
                    onClick: function(btn, map) {
                        newMap.off('click', activateStreetView);
                        btn.button.style.color = 'black';
                        btn.state('button-street-view-start');
                    }
            }]
        }).addTo( newMap );
    }

    return newMap;
}

const neutralHue = 210;
const minHue = 85;
const maxHue = -105;
const saturation = "86%";
const lightness = "62%";

function getColor(data, p, neutralColor) {
    if (p.climbIndex !== null && p.climbPartIndex !== null) {
        var grade = Math.round(data.climbs[p.climbIndex].parts[p.climbPartIndex].grade);
        hue = Math.round(minHue + (grade / 18.0) * (maxHue - minHue));
        hue = Math.min(minHue, Math.max(maxHue, hue));
    } else {
        return neutralColor;
    }
    if (hue < 0) {
        hue = hue + 360;
    }
    return "hsl(" + hue + "," + saturation + "," + lightness + ")";
}

function getLineFromData(data, neutralColor, interactive, showGrades) {
    if (showGrades) {
        var colors = data.points.map(p => getColor(data, p, neutralColor));

        var polylines = new Map();
        var previousColor = null;
        for (let i = 0; i < data.points.length - 1; i++) {
          var color = colors[i];

          if (!polylines.has(color)) {
              polylines.set(color, []);
          }
          var polyline = polylines.get(color);

          var line = null;
          if (color !== previousColor) {
            // start a new line
            line = [[data.points[i].lat, data.points[i].lon]];
            polyline.push(line);
            previousColor = color;
          } else {
            line = polyline[polyline.length - 1];
          }
          line.push([data.points[i + 1].lat, data.points[i + 1].lon]);
        }

        var layers = [];

        for (const [color, polylineLatLngs] of polylines.entries()) {
          layers.push(
            new L.polyline(
                        polylineLatLngs,
                        {
                            interactive: interactive,
                            color: color,
                            weight: 8,
                            opacity: 0.6,
                            lineCap: 'butt'
                        }
                    )
          );
        }

        return L.featureGroup(layers, {interactive: interactive});
    } else {
        var latLngs = data.points.map(p => [p.lat, p.lon]);
        return new L.polyline(
                        latLngs,
                        {
                            interactive: interactive,
                            color: neutralColor,
                            weight: 8,
                            opacity: 0.6,
                            lineCap: 'butt'
                        }
                    );
    }
}

function initMapView(mapContainerId, raw, dataUrl, elevationProfileContainer) {
    const neutralColor = "hsl(" + neutralHue + "," + saturation + "," + lightness + ")";

    var targetMap = null;
    var mapLayer = L.layerGroup();

    targetMap = initMap(mapContainerId, 47, 3, 5, 'OpenStreetMap', {
        layersControl: true,
        zoomControl: true,
        positionControl: true,
        positionOnLoad: raw,
        trackDisplayCallBacks: raw ? false : [() => { targetMap.addLayer(mapLayer) }, () => { targetMap.removeLayer(mapLayer); }],
        streetViewControl: true,
        fullScreenControl: true
    });

    if (dataUrl != null) {
        loadJsonContent(dataUrl, function(data) {

           var line = getLineFromData(data, neutralColor, false, true);

           line.addTo(mapLayer);

           data.markers.forEach(marker => {
                var latlon = [marker.lat, marker.lon];
                if (marker.type === "start") {
                    L.marker(latlon, {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})})
                        .addTo(mapLayer);
                } else if (marker.type === "end") {
                    L.marker(latlon, {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})})
                        .addTo(mapLayer);
                } else {
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

            mapLayer.addTo(targetMap);

            initChart(targetMap, elevationProfileContainer, data, neutralColor, true, true, true);

            targetMap.fitBounds(line.getBounds());

        });
    }

}

function activateStreetView(e) {
    window.open('http://maps.google.com/maps?q=&layer=c&cbll='+e.latlng.lat+','+e.latlng.lng+'&cbp=11,0,0,0,0', '_blank');
}

var elevationChart = null;
var currentChartData = null;
var mouseHoverMarker = L.marker([0.0, 0.0], {clickable: false, icon : L.divIcon({className: 'mapHoverIcon'})});

function chartCorsairPlugin(containingMap) {
    return {
       id: 'corsair',
       afterInit: (chart) => {
         chart.corsair = {
           x: 0,
           y: 0
         }
       },
       afterEvent: (chart, evt) => {

         const {
           chartArea: {
             top,
             bottom,
             left,
             right
           }
         } = chart;
         const {
           event: {
             x,
             y
           }
         } = evt;
         if (x < left || x > right || y < top || y > bottom) {
           chart.corsair = {
             x,
             y,
             draw: false
           }
           chart.render();
           return;
         }

         chart.corsair = {
           x,
           y,
           draw: true
         }

         chart.render();

         const points = chart.getElementsAtEventForMode(evt.event, 'nearest', { intersect: false, axis : 'x' }, false);
         if(currentChartData !== null && points.length > 0) {

             var targetPoint = currentChartData.points[points[0].index];
             if(evt.event.type === 'mousemove') {
                if(mouseDownFlag) {
                    containingMap.panTo(new L.LatLng(targetPoint.lat, targetPoint.lon));
                }
                mouseHoverMarker.setLatLng(new L.LatLng(targetPoint.lat, targetPoint.lon));
                containingMap.addLayer(mouseHoverMarker);
             } else {
                containingMap.removeLayer(mouseHoverMarker);
             }

         }

       },
       afterDatasetsDraw: (chart, _, opts) => {
         const {
           ctx,
           chartArea: {
             top,
             bottom,
             left,
             right
           }
         } = chart;
         const {
           x,
           y,
           draw
         } = chart.corsair;

         if (!draw) {
           return;
         }

         ctx.lineWidth = opts.width || 0;
         ctx.setLineDash(opts.dash || []);
         ctx.strokeStyle = opts.color || 'black'

         ctx.save();
         ctx.beginPath();
         ctx.moveTo(x, bottom);
         ctx.lineTo(x, top);
         ctx.stroke();
         ctx.restore();
       }
    }
 }

var chartNeutralColor = null;

function getTooltip(data, p) {
    var result = Math.round(p.dist * 10) / 10 + " km "
            + Math.round(p.ele) + " m ("
            + (Math.round(p.grade * 10) / 10) + " %)";
    if (p.climbIndex !== null && p.climbIndex < data.climbs.length) {
        var climb = data.climbs[p.climbIndex];
        if (p.climbPartIndex !== null && p.climbPartIndex < climb.parts.length) {
            var part = climb.parts[p.climbPartIndex];
            result = result + "\n"
                + "Montée (" + (p.climbIndex+1) + "/" + data.climbs.length + ") : "
                + Math.round(climb.elevation) + " m/"
                + Math.round(climb.dist / 100) / 10 + " km (" +
                + Math.round(climb.climbingGrade * 10) / 10 + " %)";
            if (climb.parts.length > 1) {
                result = result + "\n"
                    + "Partie (" + (p.climbPartIndex+1) + "/" + climb.parts.length + ") : " + Math.round(part.dist / 100) / 10 + " km (" +
                    Math.round(part.grade * 10) / 10 + " %)";
            }
        }
    }
    return result;
}

function initChart(containingMap, chartContainerId, data, neutralColor, activateZoom, colorGrades, showTooltip) {

     currentChartData = data;
     chartNeutralColor = neutralColor;

     const configData = {
       datasets: [{
         fill: true,
         label: 'Elevation',
         borderColor: 'rgb(0, 0, 0)',
         borderWidth: 1,
         pointRadius: 0,
         pointHoverBackgroundColor: '#000000',
         pointHoverBorderColor: '#000000',
         pointHoverBorderWidth: 1,
         pointHoverRadius: 3,
         segment: {
            backgroundColor: function(ctx) {
                if (colorGrades) {
                    return getColor(data, ctx.p0.raw, chartNeutralColor);
                } else {
                    return chartNeutralColor;
                }
            },
             borderColor: function(ctx) {
                if (colorGrades) {
                    return getColor(data, ctx.p0.raw, chartNeutralColor);
                } else {
                    return chartNeutralColor;
                }
             }
           },
         data: data.points
       }]
     };

     const config = {
       type: 'line',
       data: configData,
       options: {
           responsive: true,
           parsing: {
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
                   ticks: {
                       stepSize: 1.0
                   }
               }
           },
           plugins: {
               legend: {
                   display: false
               },
               tooltip: {
                 enabled: showTooltip,
                 callbacks: {
                    title: (items) => getTooltip(data, items[0].raw),
                    label: (item) => ""
                 }
               },
                zoom: {
                    zoom: {
                      wheel: {
                        enabled: activateZoom,
                      },
                      pinch: {
                        enabled: activateZoom
                      },
                      mode: 'x',
                    },
                    limits: {
                      x: {
                        min: 0,
                        max: data.info.dist,
                        minRange: 1.0
                      }
                    },
                },
           },
           layout: {
               padding: 0
           }
       },
         plugins: [chartCorsairPlugin(containingMap)]
     };

     document.getElementById(chartContainerId).addEventListener('mouseover', function () {
         containingMap.dragging.disable();
     });

     document.getElementById(chartContainerId).addEventListener('mouseup', function () {
          mouseDownFlag=0;
      });

     document.getElementById(chartContainerId).addEventListener('mouseout', function () {
         containingMap.dragging.enable();
         if(mouseHoverMarker !== null) {
            containingMap.removeLayer(mouseHoverMarker);
         }
     });

     document.getElementById(chartContainerId).addEventListener('mousedown', function () {
           mouseDownFlag=1;
       });

     document.getElementById(chartContainerId).addEventListener('touchstart', function () {
          containingMap.dragging.disable();
          mouseDownFlag=1;
      });

      document.getElementById(chartContainerId).addEventListener('touchend', function () {
          containingMap.dragging.enable();
          mouseDownFlag=0;
          if(mouseHoverMarker !== null) {
             containingMap.removeLayer(mouseHoverMarker);
          }
      });


     elevationChart = elevationChart === null ? new Chart(
         document.getElementById(chartContainerId),
         config
       ) : elevationChart;

}

function updateChart(data, color) {
     currentChartData = data;
     elevationChart.data.datasets[0].data = data.points;
     chartNeutralColor = color;
     elevationChart.options.plugins.zoom.limits.x.max = data.info.dist;
     elevationChart.update();
     elevationChart.resetZoom();
     elevationChart.render();
}