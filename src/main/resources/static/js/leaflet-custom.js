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
           chart.draw();
           return;
         }

         chart.corsair = {
           x,
           y,
           draw: true
         }

         chart.draw();

         const points = chart.getElementsAtEventForMode(evt.event, 'nearest', { intersect: false, axis : 'x' }, false);
         if(currentChartData !== null && points.length > 0) {

             var targetPoint = currentChartData[points[0].index];
             if(evt.event.type === 'mousemove') {
                if(mouseDownFlag) {
                    containingMap.panTo(new L.LatLng(targetPoint.lat, targetPoint.lng));
                }
                mouseHoverMarker.setLatLng(new L.LatLng(targetPoint.lat, targetPoint.lng));
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

var chartLoaded = false;
function initChart(containingMap, chartContainerId, elevationProfile, color, callback = null, segmentColor = false) {

    currentChartData = elevationProfile;
     const labels = elevationProfile.map(function(e) {
         return Math.round(e.x / 1000);
     });

     if(segmentColor === true) {
        color = 'rgb(160,176,70)';
     }

     const data = {
       labels: labels,
       datasets: [{
         fill: true,
         label: 'Elevation',
         backgroundColor: color,
         borderColor: 'rgb(0, 0, 0)',
         borderWidth: 1,
         pointRadius: 0,
         pointHoverBackgroundColor: '#000000',
         pointHoverBorderColor: '#000000',
         pointHoverBorderWidth: 1,
         pointHoverRadius: 3,
         segment: {
            backgroundColor: function(ctx) {
               if(segmentColor) {
                   return elevationProfile[ctx.p0DataIndex].color;
               }
               return undefined;
            },
             borderColor: function(ctx) {
                 if(segmentColor) {
                        return elevationProfile[ctx.p0DataIndex].color;
                   }
                return undefined;
             }
           },
         data: elevationProfile.map(function(e) {
               return e.y;
           }),
       }]
     };

     const config = {
       type: 'line',
       data: data,
       options: {
         responsive: true,
           plugins: {
               legend: {
                   display: false
               },
               tooltip: {
                 enabled: false
               }
           },
           layout: {
               padding: 0
           },
           animation: {
             onComplete: function() {
                if(!chartLoaded && callback !== null) {
                chartLoaded = true;
                callback();
                }
             }
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

function updateChart(elevationProfile, color) {
    currentChartData = elevationProfile;
     elevationChart.data.labels = elevationProfile.map(function(e) { return Math.round(e.x / 1000); });
     elevationChart.data.datasets[0].data = elevationProfile.map(function(e) { return e.y; });
     elevationChart.data.datasets[0].backgroundColor = color;
     elevationChart.update();
}