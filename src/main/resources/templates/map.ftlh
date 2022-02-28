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
                <#if map.crossing>
                    <li class="list-inline-item"><i class="bi bi-x"></i> Ca croise !</li>
                </#if>
            </ul>

            <#if map.tags?size gt 0>
                <p><strong>Tags</strong> : <#list map.tags as tag><span class="badge bg-secondary">${tag}</span><#if tag_has_next> </#if></#list></p>
            </#if>

             <#if !_embed>
                <div class="btn-group btn-group-sm" role="group">
                    <a download="${map.name}.gpx" href="<@common.teamUrl team.id '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> GPX</a>
                    <a download="${map.name}.fit" href="<@common.teamUrl team.id '/maps/${map.id}/fit' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> FIT</a>
                </div>
             </#if>
        </div>

    </div>

    <div class="col-12">
        <div style="width:100%; height:<#if !_embed>500px<#else>31vh</#if>;" id="map-container-md"></div>
    </div>

    <script type="text/javascript">

        var map = null;

        function initMap(mapContainerId) {

            var target = L.map(mapContainerId).setView([51.505, -0.09], 13);

            L.tileLayer('https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=${mapBoxAPIKey}', {
                attribution: '&copy; <a target="_blank" href="https://openstreetmap.org/copyright">Carte de base et données de OpenStreetMap</a>',
                minZoom: 1,
                maxZoom: 17
            }).addTo(target);

            // show the scale bar on the lower left corner
            L.control.scale().addTo(target);

            return target;
        }

        function displayElevation(target, gpxUrl) {

            var elevation_options = {
                theme: "purple-theme",
                detached: true,
                elevationDiv: "#map-elevation-container",
                followMarker: true,
                imperial: false,
                reverseCoords: false,
                acceleration: false,
                slope: false,
                speed: false,
                time: false,
                summary: false,
                downloadLink: false,
                ruler: false,
                legend: false
              };

              var controlElevation = L.control.elevation(elevation_options).addTo(target);
              controlElevation.load(gpxUrl);
        }

        setTimeout(function() {
            var gpxUrl = '<@common.teamUrl team.id '/maps/${map.id}/gpx' />';

            map = initMap('map-container-md');
            displayElevation(map, gpxUrl);

        }, 500);


    </script>

<#include "_includes_tail.ftlh">