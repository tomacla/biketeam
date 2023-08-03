<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="modal fade" id="modal-map-${map.id}" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">${map.name}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="d-grid gap-2 col-6 mx-auto">
                <a download="${map.name}.gpx" href="<@common.teamUrl team.id '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> GPX</a>
                <a download="${map.name}.fit" href="<@common.teamUrl team.id '/maps/${map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> FIT</a>
                <a href="<@common.teamUrl team.id '/maps/${map.id}/garmin' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-triangle-fill"></i> Garmin</a>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
          </div>
        </div>
      </div>
    </div>

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
                    <#if _authenticated>
                        <#if _user.mapFavorites?filter(mp -> mp.id == map.id)?size == 0>
                            <a href="<@common.teamUrl team.id '/maps/${map.id}/add-favorite' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-star"></i> Favori</a>
                        <#else>
                            <a href="<@common.teamUrl team.id '/maps/${map.id}/remove-favorite' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-star-fill"></i>  Favori</a>
                        </#if>
                    </#if>
                    <button class="btn btn-outline-secondary" type="button" data-bs-toggle="modal" data-bs-target="#modal-map-${map.id}"><i class="bi bi-download"></i> Télécharger</button>
                    <button id="toggle-track-button" onclick="toggleTrack();" class="btn btn-outline-secondary" role="button"><i class="bi bi-eye-slash-fill"></i> Masquer</button>
                    <button onclick="showLocation();" class="btn btn-outline-secondary" role="button"><i class="bi bi-geo-fill"></i> Position</button>
                </div>
             </#if>
        </div>

    </div>

    <div class="col-12">
        <div style="width:100%; height:<#if !_embed>500px<#else>31vh</#if>;" id="map-wrapper"></div>
    </div>

    <div class="col-12 mt-2 p-1 w-100" style="height:290px;">
        <canvas class="w-100" style="height:290px;" id="chart-wrapper"></canvas>
    </div>

</div>

    <script type="text/javascript">


        var trackDisplayed = true;

        function showLocation() {

            activateLocation(targetMap);


        }

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

        var geoJsonLayer = null;
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
                initChart(targetMap, 'chart-wrapper', elevationProfile, '#ff0000', null);
            });

            var firstPoint = geojsonFeature.geometry.coordinates[0];
            var lastPoint = geojsonFeature.geometry.coordinates[geojsonFeature.geometry.coordinates.length - 1];

             targetMap.panTo(new L.LatLng(firstPoint[1], firstPoint[0]));
             L.marker([firstPoint[1], firstPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})}).addTo(targetMap);

             L.marker([lastPoint[1], lastPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})}).addTo(targetMap);


        });


    </script>

<#include "_includes_tail.ftlh">