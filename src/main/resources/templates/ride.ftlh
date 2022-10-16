<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "ride_nav.ftlh">

    <#if ride.startPlace?? || ride.endPlace??>
        <div class="modal" tabindex="-1" id="placemodal">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">

                  <#if ride.startPlace?? && ride.endPlace?? && ride.startPlace.id == ride.endPlace.id>
                        <@common.displayPlace "Départ/Arrivée" ride.startPlace />
                  <#else>

                    <div class="row">

                    <#if ride.startPlace??>
                    <div class="col-12 col-md-6">
                        <@common.displayPlace "Départ" ride.startPlace />
                    </div>
                    </#if>

                    <#if ride.endPlace??>
                    <div class="col-12 col-md-6">
                    <@common.displayPlace "Arrivée" ride.endPlace />
                    </div>
                    </#if>

                    </div>


                   </#if>

              </div>
            </div>
          </div>
        </div>
    </#if>

    <div class="col-12 col-md-4 mb-4">

        <div class="border rounded p-2">

            <div class="d-flex flex-row justify-content-between">
                <h4>${ride.title}</h4>
            </div>
            <#if ride.publishedStatus != 'PUBLISHED'>
                <div class="p-1 alert alert-warning" role="alert">Ce ride n'est pas publié.<br/> <span class="small text-muted">Publication le ${ride.publishedAt.format(_date_formatter)} à ${ride.publishedAt.format(_time_formatter)} (UTC)</span></div>
            </#if>
            <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>

            <#if ride.startPlace?? || ride.endPlace??>
            <ul class="list-unstyled">
                <#if ride.startPlace??>
                    <li>Départ : <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${ride.startPlace.name}</a></li>
                </#if>
                <#if ride.endPlace??>
                    <li>Arrivée : <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${ride.endPlace.name}</a></li>
                </#if>
            </ul>
            </#if>

            <hr/>

            <p class="wrap-content">${ride.description}</p>

               <#if ride.imaged>
                 <img src="<@common.teamUrl team.id '/rides/${ride.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.id} image">
             </#if>

        </div>

    </div>


   <#assign colors=["#566B13", "#4682B4", "#732C7B", "#FF0", "#F00", "#FF005E", "#3366CC"]>
   <#assign groupMaps = []>
   <#assign mapCenter = [0.0,0.0]>
   <#if ride.startPlace?? && ride.startPlace.point??>
       <#assign mapCenter = [ride.startPlace.point.lat,ride.startPlace.point.lng]>
  </#if>
   <#list ride.sortedGroups as group>
      <#if group.map??>
          <#if mapCenter[0] == 0.0>
            <#assign mapCenter = [group.map.startPoint.lat, group.map.startPoint.lng]>
          </#if>
          <#assign mapId = group.map.id>
          <#if groupMaps?seq_contains(mapId)>
          <#else>
            <#assign groupMaps = groupMaps + [mapId]>
          </#if>
      </#if>

    <#if group.sortedParticipants?size gt 0>
      <div class="modal" tabindex="-1" id="groupmodal-${group.id}">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
                <h5>Participants</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">

                 <div class="d-flex flex-wrap justify-content-center">
                    <#list group.sortedParticipants as participant>
                        <div class="d-flex align-items-center border rounded pe-2 me-2 mb-2">
                            <div style="width:30px; height:30px;" class="me-2"><img class="w-100 h-100 d-block rounded" src="<@common.teamUrl '' '/users/${participant.id}/image' />" alt="${participant.identity}"></div> <span class="small text-muted">${participant.identity}</span>
                        </div>
                    </#list>
                 </div>

            </div>
          </div>
        </div>
      </div>
      </#if>

   </#list>

    <div class="col-12 col-md-8 mb-4">

        <div class="border rounded p-2">




            <div class="table-responsive-sm mb-4">

                <table class="table table-sm table-striped">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Départ</th>
                            <th>Groupe</th>
                            <th class="d-none d-md-table-cell">Allure</th>
                            <th class="d-none d-md-table-cell">KM</th>
                            <th class="d-none d-md-table-cell">D+</th>
                            <th><span class="d-none d-md-inline">Participants</span></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list ride.sortedGroups as group>
                            <tr<#if group.map??> onmouseover="highlightMap(${groupMaps?seq_index_of(group.map.id)})" onmouseout="restoreMap(${groupMaps?seq_index_of(group.map.id)})"</#if>>
                                <td><#if group.map??><span style="height: 10px; width: 10px; background-color: ${colors[groupMaps?seq_index_of(group.map.id)%7]}; border-radius: 50%; display: inline-block;"></span></#if></td>
                                <td>${group.meetingTime}</td>
                                <td>${group.name}</td>
                                <td class="d-none d-md-table-cell">${group.lowerSpeed}/${group.upperSpeed} km/h</td>
                                <td class="d-none d-md-table-cell"><#if group.map??>${group.map.length?floor} km</#if></td>
                                <td class="d-none d-md-table-cell"><#if group.map??>${group.map.positiveElevation?floor} m</#if></td>
                                <td>
                                    <div class="d-flex flex-row align-items-start justify-content-start">
                                        <div class="d-flex profile-images me-2">
                                            <#if group.sortedParticipants?size gt 0>
                                                <#list group.sortedParticipants?chunk(3)[0] as participant>
                                                        <div class="profile-images-item" style="width:24px; height:24px;" class="d-inline-block"><a href="#" class="text-decoration-none" data-bs-toggle="modal" data-bs-target="#groupmodal-${group.id}"><img class="w-100 h-100 d-block" src="<@common.teamUrl '' '/users/${participant.id}/image' />" alt="${participant.identity}"></a></div>
                                                </#list>
                                            </#if>
                                        </div>
                                        <div>
                                            <#if group.sortedParticipants?size gt 3>
                                                <a href="#" class="text-decoration-none link-secondary" data-bs-toggle="modal" data-bs-target="#groupmodal-${group.id}"><span class="text-muted small fst-italic">+${group.sortedParticipants?size - 3}</span></a>
                                            </#if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <div class="btn-group">
                                        <#if _authenticated>
                                            <#if group.hasParticipant(_user.id)>
                                                <a class="btn btn-sm btn-success py-0 px-2" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/remove-participant/${group.id}' />" ><i class="bi bi-person-check"></i></a>
                                            <#else>
                                                <a class="btn btn-sm btn-outline-success py-0 px-2 <#if ride.isParticipantInAnotherGroup(group.id, _user.id)>disabled</#if>" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />"><i class="bi bi-person"></i></a>
                                             </#if>
                                        <#else>
                                                <a class="btn btn-sm btn-outline-success py-0 px-2" href="<@common.teamUrl '' '/login?requestUri=/${team.id}/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />"><i class="bi bi-person"></i></a>
                                        </#if>
                                        <#if group.map??>
                                            <a class="btn btn-sm btn-outline-secondary py-0 px-2" download="${ride.title}-${group.name}.gpx" href="<@common.teamUrl team.id '/maps/${group.map.id}/gpx' />"><i class="bi bi-download"></i></a>
                                            <a class="btn btn-sm btn-outline-secondary py-0 px-2" href="<@common.teamUrl team.id '/maps/${group.map.permalink!group.map.id}' />"><i class="bi bi-map"></i></a>
                                        </#if>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                    </table>
              </div>

              <div style="width:100%; height:500px;" id="map-wrapper"></div>

                <div class="mt-2 p-1" style="width:100%; height:280px;">
                    <canvas class="w-100 h-100" id="chart-wrapper"></canvas>
               </div>

        </div>

    </div>


</div>


<#if groupMaps?size != 0>

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
            "main" :  L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}', {
                     	attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                     	subdomains: 'abcd',
                     	minZoom: 0,
                     	maxZoom: 20,
                     	ext: 'png'
                     })
        };

        function initMap(mapContainerId) {
            var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layers['main']] }).setView([${mapCenter[0]?c}, ${mapCenter[1]?c}], 11);
            L.control.zoom({position: 'bottomright'}).addTo(targetMap);
            return targetMap;
        }

        function highlightMap(mapIndex) {
            const layer = geoJsonLayers.find(function(l) {
                return l.mapIndex === mapIndex;
            });
            if(layer) {
                layer.layer.setStyle({
                 weight: 8,
                 opacity: 1
                });
                layer.layer.bringToFront();

                currentChartData = layer.elevationProfile;
                if(elevationChart == null) {
                    initChart(layer.elevationProfile, layer.color);
                } else {
                    updateChart(layer.elevationProfile, layer.color);
                }

            }
        }

        function restoreMap(mapIndex) {
            const layer = geoJsonLayers.find(function(l) {
                return l.mapIndex === mapIndex;
            });
            if(layer) {
                layer.layer.setStyle({
                  weight: 5,
                  opacity: 0.8
                });
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

        function updateChart(elevationProfile, color) {

            elevationChart.data.labels = elevationProfile.map(function(e) {
                                                    return Math.round(e.x / 1000);
                                                });
            elevationChart.data.datasets[0].data = elevationProfile.map(function(e) {
                                                                         return e.y;
                                                                     });
             elevationChart.data.datasets[0].backgroundColor = color;
            elevationChart.update();
        }

        var targetMap = initMap('map-wrapper');
        var geoJsonLayers = [];

        <#list groupMaps as map>

            loadJsonContent('<@common.teamUrl team.id '/maps/${map}/geojson' />', function(geojsonFeature) {
                var layer = L.geoJSON(geojsonFeature,
                    {
                      interactive:false,
                      color: '${colors[map?index%7]}',
                      weight: 5,
                      opacity: 0.8
                    });

                layer.addTo(targetMap);

                loadJsonContent('<@common.teamUrl team.id '/maps/${map}/elevation-profile' />', function(elevationProfile) {
                    geoJsonLayers.push({
                        layer: layer,
                        mapIndex: ${map?index},
                        color: '${colors[map?index%7]}',
                        elevationProfile: elevationProfile
                    });
                });

                <#if map?is_first>

                    var firstPoint = geojsonFeature.geometry.coordinates[0];
                    var lastPoint = geojsonFeature.geometry.coordinates[geojsonFeature.geometry.coordinates.length - 1];

                     L.marker([firstPoint[1], firstPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})}).addTo(targetMap);

                     L.marker([lastPoint[1], lastPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})}).addTo(targetMap);

                </#if>

            });

        </#list>


        <#if ride.startPlace?? || ride.endPlace??>
            const myModalEl = document.getElementById('placemodal')
            myModalEl.addEventListener('show.bs.modal', event => {
                leafletModalMaps.forEach(map => {
                 setTimeout(function() {
                        map.invalidateSize();
                    }, 10);
                });
            })
        </#if>

    </script>

</#if>

<#include "_includes_tail.ftlh">