<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mb-4">

        <div class="border rounded p-2">
            <div class="d-flex flex-row justify-content-between">
                <h4>${map.name}<small class="text-muted"> - Ajoutée le ${map.postedAt.format(_date_formatter)}</small></h4>
                <div>
                    <#if _team_admin>
                        <a href="<@spring.url '/' + team.id + '/admin/maps/${map.id}' />" class="btn btn-sm btn-secondary"><i class="bi-pencil bi"></i> Editer</a>
                    </#if>
                </div>
            </div>
            <ul class="list-inline">
                <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${map.length}km</li>
                <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${map.positiveElevation}m</li>
                <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${map.negativeElevation}m</li>
            </ul>

            <#if map.tags?size gt 0>
                <p><strong>Tags</strong> : <#list map.tags as tag><span class="badge bg-secondary">${tag}</span><#if tag_has_next> </#if></#list></p>
            </#if>

            <div class="btn-group" role="group">
                <a download="${map.name}.gpx" href="<@spring.url '/api/' + team.id + '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> GPX</a>
                <a download="${map.name}.fit" href="<@spring.url '/api/' + team.id + '/maps/${map.id}/fit' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> FIT</a>
            </div>
        </div>

    </div>

    <div class="col-12">

        <div style="width:100%; height:500px;" id="map-container"></div>

    </div>

    <script type="text/javascript">

        function initMap(mapContainerId) {

            var target = L.map(mapContainerId).setView([51.505, -0.09], 13);

            <#if (_integ.mapBoxAPIKey)??>
                L.tileLayer('https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=${_integ.mapBoxAPIKey}', {
                    attribution: '&copy; <a target="_blank" href="https://openstreetmap.org/copyright">Carte de base et données de OpenStreetMap</a>',
                    minZoom: 1,
                    maxZoom: 17
                }).addTo(target);
            <#else>
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; <a target="_blank" href="https://openstreetmap.org/copyright">Carte de base et données de OpenStreetMap</a>'
                }).addTo(target);
            </#if>

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

        var gpxUrl = '<@spring.url '/api/' + team.id + '/maps/${map.id}/gpx' />';
        var embedMap = initMap('map-container');
        displayElevation(embedMap, gpxUrl);


    </script>

<#include "_includes_tail.ftlh">