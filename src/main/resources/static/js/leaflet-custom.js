var mouseDownFlag = 0;

function initMap(mapContainerId, lat, lng, zoom, defaultLayer, providedOptions = {}) {

    var newMap = L.map(mapContainerId, { zoomControl: false, layers: [layers[defaultLayer]] }).setView([lat, lng], zoom);

    var options = {
        layersControl: providedOptions.layersControl || false,
        zoomControl: providedOptions.zoomControl || false,
        positionControl: providedOptions.positionControl || false,
        positionOnLoad: providedOptions.positionOnLoad || false,
        trackDisplayCallBacks: providedOptions.trackDisplayCallBacks || false,
        streetViewControl: providedOptions.streetViewControl || false
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

var overlayLayers = {
    "Voies cyclables": L.tileLayer('https://tile.waymarkedtrails.org/cycling/{z}/{x}/{y}.png', {
        maxNativeZoom: 18,
        maxZoom: 24,
        attribution: '&copy; <a href="https://www.waymarkedtrails.org" target="_blank">Waymarked Trails</a>'
    }),
    "Itinéraires VTT": L.tileLayer('https://tile.waymarkedtrails.org/mtb/{z}/{x}/{y}.png', {
        maxNativeZoom: 18,
        maxZoom: 24,
        attribution: '&copy; <a href="https://www.waymarkedtrails.org" target="_blank">Waymarked Trails</a>'
    })
}

var layers = {
    "Cyclo OSM": L.tileLayer('https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png', {
        maxNativeZoom: 17,
        maxZoom: 22,
        attribution: '<a href="https://github.com/cyclosm/cyclosm-cartocss-style/releases" title="CyclOSM - Open Bicycle render">CyclOSM</a> | Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }),
    "OpenStreeMap": L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
        maxNativeZoom: 19,
        maxZoom: 22
    }),
    "ESRI Satellite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
        attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
    }),
    "IGN Scan": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&TILEMATRIXSET=PM&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}&LAYER=GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN25TOUR&FORMAT=image/jpeg&STYLE=normal', {
        maxNativeZoom: 16,
        maxZoom: 22,
        apikey: 'f3ugilx7vq27vhzn887x8ds2',
        attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>'
    }),
    "IGN Satellite": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE={style}&TILEMATRIXSET=PM&FORMAT={format}&LAYER=ORTHOIMAGERY.ORTHOPHOTOS&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}', {
        attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>',
        bounds: [[-75, -180], [81, 180]],
        minZoom: 2,
        maxZoom: 19,
        apikey: 'ortho',
        format: 'image/jpeg',
        style: 'normal'
    }),
    "IGN Plan": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE={style}&TILEMATRIXSET=PM&FORMAT={format}&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}', {
        attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>',
        bounds: [[-75, -180], [81, 180]],
        minZoom: 2,
        maxZoom: 18,
        apikey: 'cartes',
        format: 'image/png',
        style: 'normal'
    }),
     "Carto" : L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
     	attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
     	maxZoom: 20
     })
};

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