<#include "_includes_head.ftlh">

<div id="gpxtool" class="container-fluid w-100 h-100 p-0 m-0" >

<div class="row p-0 m-0 justify-content-center w-100 h-100">

    <div id="map-wrapper" style="height:calc(100% - 150px)" class="col-12 p-0 m-0">

        <div class="w-100 h-100" id="map-container">

            <div class="overlay top-center pt-2">
                <div class="btn-group">
                  <a download="${gpxuuid}.gpx" href="<@common.teamUrl '' '/gpxtool/${gpxuuid}/gpx' />" class="btn btn-secondary text-white" role="button"><i class="bi bi-file-arrow-down-fill"></i><span class="ps-1 d-none d-md-inline-block">Télécharger</span></a>
                  <button onclick="copyURLtoClipboard();" class="btn btn-secondary  text-white" role="button"><i class="bi bi-share-fill"></i><span class="ps-1 d-none d-md-inline-block">Partager</span></a>
                  <button id="toggle-track-button" onclick="toggleTrack();" class="d-none d-md-inline-block btn btn-secondary  text-white" role="button"><i class="bi bi-eye-slash-fill"></i><span class="ps-1 d-none d-md-inline-block">Masquer</span></a>
                  <button onclick="displayPosition();" class="btn btn-secondary  text-white" role="button"><i class="bi bi-geo-fill"></i><span class="ps-1 d-none d-md-inline-block">Position</span></a>
                </div>
            </div>

            <div class="overlay bottom-center pb-4 fs-5 text-center">
                <span class="badge rounded-pill bg-secondary me-1"><i class="bi bi-bicycle"></i> ${gpx.length}km</span>
                <span class="badge rounded-pill bg-secondary me-1"><i class="bi bi-arrow-up-right"></i> ${gpx.positiveElevation} m</span>
                <span class="badge rounded-pill bg-secondary"><i class="bi bi-arrow-down-right"></i> ${gpx.negativeElevation} m</span>
            </div>

        </div>

    </div>

    <div style="height:150px" class="col-12 p-0 m-0">
        <canvas class="w-100" style="height:150px;" id="chart-wrapper"></canvas>
    </div>


    <script type="text/javascript">

            function displayPosition() {
                activateLocation(targetMap);
            }

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

            var targetMap = initMap('map-wrapper', 51.505, -0.09, 11, 'Cyclo OSM', true, true);

            var geojsonFeature = ${gpx.geojson?no_esc};
            var elevationProfile = ${gpx.elevationProfile?no_esc};

            var geoJsonLayer = L.geoJSON(geojsonFeature,
                {
                  interactive:false,
                  color: '#ff0000',
                  weight: 5,
                  opacity: 0.8
                });

            geoJsonLayer.addTo(targetMap);
            initChart(targetMap, 'chart-wrapper', elevationProfile, '#ff0000', null);

            var firstPoint = geojsonFeature.geometry.coordinates[0];
            var lastPoint = geojsonFeature.geometry.coordinates[geojsonFeature.geometry.coordinates.length - 1];

             targetMap.panTo(new L.LatLng(firstPoint[1], firstPoint[0]));
             L.marker([firstPoint[1], firstPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})}).addTo(targetMap);

             L.marker([lastPoint[1], lastPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})}).addTo(targetMap);


        </script>


</div>

<#include "_includes_tail.ftlh">

