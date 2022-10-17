<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mb-4">

        <div class="border rounded p-2">
            <div class="d-flex flex-row justify-content-between">
                <h4>${map.name}</h4>
                <div>
                    <#if _team_admin && !_embed>
                        <a href="<@common.teamUrl team.id '/admin/maps/${map.id}' />" class="btn btn-sm btn-secondary"><i class="bi-pencil bi"></i> Editer</a>
                    </#if>
                </div>
            </div>
            <h6><small class="text-muted">Ajoutée le ${map.postedAt.format(_date_formatter)}</small></h6>
            <ul class="list-inline">
                <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${map.length}km</li>
                <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${map.positiveElevation}m</li>
                <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${map.negativeElevation}m</li>
            </ul>

            <#if map.tags?size gt 0>
                <p><strong>Tags</strong> : <#list map.tags as tag><span class="badge bg-secondary">${tag}</span><#if tag_has_next> </#if></#list></p>
            </#if>

             <#if !_embed>
                <div class="btn-group btn-group-sm" role="group">
                    <a download="${map.name}.gpx" href="<@common.teamUrl team.id '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> GPX</a>
                    <a download="${map.name}.fit" href="<@common.teamUrl team.id '/maps/${map.id}/fit' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> FIT</a>
                    <button id="toggle-track-button" onclick="toggleTrack();" class="btn btn-outline-secondary" role="button"><i class="bi bi-eye-slash-fill"></i> Masquer</button>
                </div>
             </#if>
        </div>

    </div>

    <div class="col-12">
        <div style="width:100%; height:<#if !_embed>500px<#else>31vh</#if>;" id="map-wrapper"></div>
    </div>


    <div class="col-12 mt-2 p-1" style="width:100%; height:300px;">
        <canvas class="w-100 h-100" id="chart-wrapper"></canvas>
   </div>

</div>

    <script type="text/javascript">

        var mouseHoverMarker = L.marker([0.0, 0.0], {clickable: false, icon : L.divIcon({className: 'mapHoverIcon'})});
        var currentChartData = null;

        var chartCorsairPlugin = {
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
                    targetMap.addLayer(mouseHoverMarker);
                 } else {
                    targetMap.removeLayer(mouseHoverMarker);
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
            })
        };

        function toggleTrack() {
            if(trackDisplayed) {

              targetMap.removeLayer(geoJsonLayer);

              trackDisplayed = false;
              document.getElementById('toggle-track-button').innerHTML = '<i class="bi bi-eye-fill"></i> Afficher';

            } else {

                targetMap.addLayer(geoJsonLayer);

              trackDisplayed = true;
              document.getElementById('toggle-track-button').innerHTML = '<i class="bi bi-eye-slash-fill"></i> Masquer';
            }
        }

        var elevationChart = null;
        function initChart(elevationProfile, color) {

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
                      padding: 5
                  },
                  scales: {
                      y: {
                        title: {
                          display: true,
                          text: 'Dénivelé (m)'
                        }
                      },
                      x: {
                          title: {
                            display: true,
                            text: 'Distance (km)'
                          }
                        }
                    }
              },
                plugins: [chartCorsairPlugin]
            };

            elevationChart = new Chart(
                document.getElementById('chart-wrapper'),
                config
              );
        }

        function initMap(mapContainerId) {

            var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layers['IGN Plan']] }).setView([51.505, -0.09], 11);
            L.control.layers(layers, null, {position: 'bottomleft'}).addTo(targetMap);
            L.control.zoom({position: 'bottomright'}).addTo(targetMap);

            return targetMap;

        }

        var targetMap = initMap('map-wrapper');
        var geoJsonLayer = null;
        var trackDisplayed = true;

        loadJsonContent('<@common.teamUrl team.id '/maps/${map.id}/geojson' />', function(geojsonFeature) {

            var layer = L.geoJSON(geojsonFeature,
                {
                  interactive:false,
                  color: '#ff0000',
                  weight: 5,
                  opacity: 0.8
                });

            layer.addTo(targetMap);

            loadJsonContent('<@common.teamUrl team.id '/maps/${map.id}/elevation-profile' />', function(elevationProfile) {
                geoJsonLayer = layer;
                initChart(elevationProfile, '#ff0000');
                currentChartData = elevationProfile;
            });


            var firstPoint = geojsonFeature.geometry.coordinates[0];
            var lastPoint = geojsonFeature.geometry.coordinates[geojsonFeature.geometry.coordinates.length - 1];

             targetMap.panTo(new L.LatLng(firstPoint[1], firstPoint[0]));
             L.marker([firstPoint[1], firstPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})}).addTo(targetMap);

             L.marker([lastPoint[1], lastPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})}).addTo(targetMap);


        });


    </script>

<#include "_includes_tail.ftlh">