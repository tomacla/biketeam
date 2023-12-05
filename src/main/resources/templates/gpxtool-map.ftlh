<#include "_includes_head.ftlh">

<div id="gpxtool" class="container-fluid w-100 h-100 p-0 m-0">

    <div class="modal fade" id="modal-map-${gpxuuid}" tabindex="-1">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">Télécharger</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                <div class="d-grid gap-2 col-6 mx-auto">
                    <a download="${gpxuuid}.gpx" href="<@common.teamUrl '' '/gpxtool/${gpxuuid}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> GPX</a>
                    <a download="${gpxuuid}.fit" href="<@common.teamUrl '' '/gpxtool/${gpxuuid}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> FIT</a>
                    <a href="<@common.teamUrl '' '/gpxtool/${gpxuuid}/garmin' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-triangle-fill"></i> Garmin</a>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
              </div>
            </div>
          </div>
        </div>

<div class="row p-0 m-0 justify-content-center w-100 h-100">

    <#if gpxuuid != 'raw'>
        <div id="map-toolbar" class="w-100 col-12 p-0 m-0 py-2">
            <div class="d-flex flex-row justify-content-around w-100 h-100 align-items-center flex-wrap">
                <div class="d-flex flex-row my-1 my-md-0">
                    <span><i class="bi bi-arrow-left-right"></i> ${gpx.length}km</span>
                    <span><i class="bi bi-arrow-up"></i> ${gpx.positiveElevation}m</span>
                    <span><i class="bi bi-arrow-down"></i> ${gpx.negativeElevation}m</span>
                </div>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-secondary" type="button" data-bs-toggle="modal" data-bs-target="#modal-map-${gpxuuid}"><i class="bi bi-download"></i><span class="d-none d-md-inline"> Télécharger</span></button>
                    <button onclick="copyURLtoClipboard();" class="btn btn-outline-secondary" role="button"><i class="bi bi-share"></i><span class="ps-1 d-none d-md-inline-block">Partager</span></a>
                </div>
            </div>
        </div>
    </#if>

    <div id="gpxtool<#if gpxuuid == 'raw'>raw</#if>map-wrapper" class="col-12 p-0 m-0">

        <div class="w-100 h-100" id="map-container"></div>

    </div>

    <#if gpxuuid != 'raw'>
        <div style="height:150px" class="col-12 p-0 m-0">
            <canvas class="w-100" style="height:150px;" id="chart-wrapper"></canvas>
        </div>
    </#if>


    <script type="text/javascript">


             <#if gpxuuid != 'raw'>

                function showTrack() {
                           targetMap.addLayer(geoJsonLayer);
                       }

                       function hideTrack() {
                           targetMap.removeLayer(geoJsonLayer);
                       }

            </#if>

            var targetMap = initMap('gpxtool<#if gpxuuid == 'raw'>raw</#if>map-wrapper', 51.505, -0.09, 11, 'Cyclo OSM',
                {
                     layersControl: true,
                     zoomControl: true,
                     positionControl: true,
                     positionOnLoad: <#if gpxuuid != 'raw'>false<#else>true</#if>,
                     trackDisplayCallBacks: <#if gpxuuid != 'raw'>[showTrack, hideTrack]<#else>false</#if>,
                     streetViewControl: true,
                     fullScreenControl: true
                 });

             <#if gpxuuid != 'raw'>
                var geojsonFeature = ${gpx.geojson?no_esc};
                var elevationProfile = ${gpx.elevationProfile?no_esc};

                var geoJsonLayer = L.layerGroup();
                var layer = L.geoJSON(geojsonFeature,
                    {
                      interactive:false,
                      color: '#ff0000',
                      weight: 5,
                      opacity: 0.8,
                      pointToLayer: function (feature, latlng) {

                          if(feature.properties.name === 'start') {
                              return L.marker(latlng, {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})})

                          } else if(feature.properties.name === 'end') {
                              return L.marker(latlng, {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})});
                           } else {

                              var circle = L.circleMarker(latlng, {
                                   radius: 10,
                                   fillColor: "#ffffff",
                                   color: "#000000",
                                   weight: 1,
                                   opacity: 1,
                                   fillOpacity: 0.8
                               });

                              var text = L.tooltip({
                                      permanent: true,
                                      direction: 'center',
                                      className: 'circle-text'
                                  })
                                  .setContent(feature.properties.name)
                                  .setLatLng(latlng);
                                  text.addTo(geoJsonLayer);

                                  return circle;
                            }

                        }
                    });

                layer.addTo(geoJsonLayer);
                geoJsonLayer.addTo(targetMap);
                initChart(targetMap, 'chart-wrapper', elevationProfile, '#ff0000', null, true);

                targetMap.fitBounds(layer.getBounds());

             </#if>

        </script>


</div>

<#include "_includes_tail.ftlh">

