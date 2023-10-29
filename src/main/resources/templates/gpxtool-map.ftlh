<#include "_includes_head.ftlh">

<div id="gpxtool" class="container-fluid w-100 h-100 p-0 m-0" >

<div class="row p-0 m-0 justify-content-center w-100 h-100">

    <div id="map-wrapper" style="<#if gpxuuid != 'raw'>height:calc(100% - 150px)<#else>height:100%</#if>" class="col-12 p-0 m-0">

        <div class="w-100 h-100" id="map-container">

            <div class="overlay top-center pt-2">
                <div class="btn-group">
                 <#if gpxuuid != 'raw'>
                      <a download="${gpxuuid}.gpx" href="<@common.teamUrl '' '/gpxtool/${gpxuuid}/gpx' />" class="btn btn-secondary text-white" role="button"><i class="bi bi-file-arrow-down-fill"></i><span class="ps-1 d-none d-md-inline-block">Télécharger</span></a>
                      <button onclick="copyURLtoClipboard();" class="btn btn-secondary  text-white" role="button"><i class="bi bi-share-fill"></i><span class="ps-1 d-none d-md-inline-block">Partager</span></a>
                      <button id="toggle-track-button" onclick="toggleTrack();" class="d-none d-md-inline-block btn btn-secondary  text-white" role="button"><i class="bi bi-eye-slash-fill"></i><span class="ps-1 d-none d-md-inline-block">Masquer</span></a>
                  </#if>
                  <button onclick="displayPosition();" class="btn btn-secondary  text-white" role="button"><i class="bi bi-geo-fill"></i><span class="ps-1 d-none d-md-inline-block">Position</span></a>
                </div>
            </div>

            <#if gpxuuid != 'raw'>
                <div class="overlay bottom-center pb-4 fs-5 text-center">
                    <span class="badge rounded-pill bg-secondary me-1"><i class="bi bi-bicycle"></i> ${gpx.length}km</span>
                    <span class="badge rounded-pill bg-secondary me-1"><i class="bi bi-arrow-up-right"></i> ${gpx.positiveElevation} m</span>
                    <span class="badge rounded-pill bg-secondary"><i class="bi bi-arrow-down-right"></i> ${gpx.negativeElevation} m</span>
                </div>
            </#if>

        </div>

    </div>

    <#if gpxuuid != 'raw'>
        <div style="height:150px" class="col-12 p-0 m-0">
            <canvas class="w-100" style="height:150px;" id="chart-wrapper"></canvas>
        </div>
    </#if>


    <script type="text/javascript">

            function displayPosition() {
                activateLocation(targetMap);
            }

             <#if gpxuuid != 'raw'>
            var trackDisplayed = true;
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
            </#if>

            var targetMap = initMap('map-wrapper', 51.505, -0.09, 11, 'Cyclo OSM', true, true);

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

             <#else>

             displayPosition();

             </#if>

        </script>


</div>

<#include "_includes_tail.ftlh">

