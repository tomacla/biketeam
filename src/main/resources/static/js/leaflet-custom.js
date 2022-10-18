function initMap(mapContainerId, lat, lng, zoom, defaultLayer, layersControl, zoomControl) {
    var newMap = L.map(mapContainerId, { zoomControl: false, layers: [layers[defaultLayer]] }).setView([lat, lng], zoom);
    if(layersControl) {
        L.control.layers(layers, null, {position: 'bottomleft'}).addTo(newMap);
    }
    if(zoomControl) {
        L.control.zoom({position: 'bottomright'}).addTo(newMap);
    }
    return newMap;
}

var layers = {
    "Cyclo OSM": L.tileLayer('https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png', {
        minZoom: 1,
        maxZoom: 17,
        attribution: '<a href="https://github.com/cyclosm/cyclosm-cartocss-style/releases" title="CyclOSM - Open Bicycle render">CyclOSM</a> | Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }),
    "ESRI Satellite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
        attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
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
    "Stamen" :  L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}', {
        attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        minZoom: 0,
        maxZoom: 20,
        ext: 'png'
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
function initChart(containingMap, chartContainerId, elevationProfile, color, callback = null) {

    currentChartData = elevationProfile;
     const labels = elevationProfile.map(function(e) {
         return Math.round(e.x / 1000);
     });

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

     document.getElementById(chartContainerId).addEventListener('mouseout', function () {
         containingMap.dragging.enable();
         if(mouseHoverMarker !== null) {
            containingMap.removeLayer(mouseHoverMarker);
         }
     });

     document.getElementById(chartContainerId).addEventListener('touchstart', function () {
          containingMap.dragging.disable();
      });

      document.getElementById(chartContainerId).addEventListener('touchend', function () {
          containingMap.dragging.enable();
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