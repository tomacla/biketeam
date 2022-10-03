<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "trip_nav.ftlh">

    <div class="col-12 col-md-4 mb-4">

        <div class="border rounded p-2">

            <div class="d-flex flex-row justify-content-between">
                <h4>${trip.title}</h4>
            </div>
            <#if trip.publishedStatus != 'PUBLISHED'>
                <div class="p-1 alert alert-warning" role="alert">Ce trip n'est pas publié.<br/> <span class="small text-muted">Publication le ${trip.publishedAt.format(_date_formatter)} à ${trip.publishedAt.format(_time_formatter)} (UTC)</span></div>
            </#if>

            <h5 class="text-muted">${trip.startDate.format(_date_formatter)} <i class="bi bi-arrow-right-short"></i> ${trip.endDate.format(_date_formatter)}</h5>

            <hr/>

            <ul class="list-unstyled">
                <li>Allure : ${trip.lowerSpeed}/${trip.upperSpeed} km/h</li>
                <li>Lieu de rendez vous : <#if (trip.meetingPoint)??><a class="link-dark" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${trip.meetingPoint.lat?c},${trip.meetingPoint.lng?c}"></#if>${trip.meetingLocation}<#if (trip.meetingPoint)??></a></#if></li>
                <li>Heure de départ : ${trip.meetingTime}</li>
            </ul>

            <hr/>

            <div class="d-flex flex-row align-items-start justify-content-between">
                <div class="d-block pe-2">
                    <#if _authenticated>
                        <#if trip.hasParticipant(_user.id)>
                            <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}/remove-participant' />" class="text-nowrap d-block btn btn-sm btn-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                        <#else>
                            <a tabindex="-1" href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}/add-participant' />" class="text-nowrap d-block btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                         </#if>
                    <#else>
                            <a href="<@common.teamUrl '' '/login?requestUri=/${team.id}/trips/${trip.permalink!trip.id}/add-participant' />" class="text-nowrap d-block btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                    </#if>
                </div>
                <div class="p-0 m-0 flex-wrap">
                    <#list trip.sortedParticipants as participant>
                        <#if participant.profileImage??>
                            <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="${participant.profileImage}" alt="${participant.identity}"></div>
                        <#else>
                            <div style="width:30px; height:30px; background:grey;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block rounded"></div>
                        </#if>
                    </#list>
                </div>
            </div>

            <hr/>

            <p class="mt-4 wrap-content">${trip.description}</p>

            <#if trip.imaged>
              <img src="<@common.teamUrl team.id '/trips/${trip.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.id} image">
            </#if>


        </div>

    </div>

    <#assign colors=["#566B13", "#4682B4", "#732C7B", "#FF0", "#F00", "#FF005E", "#3366CC"]>
       <#assign themes=["lime-theme", "steelblue-theme", "purple-theme", "yellow-theme", "red-theme", "magenta-theme", "lightblue-theme"]>
       <#assign stageMaps = []>
       <#assign mapCenter = [0.0,0.0]>
       <#list trip.sortedStages as stage>
          <#if stage.map??>
            <#assign mapCenter = [stage.map.startPoint.lat, stage.map.startPoint.lng]>
            <#assign mapId = stage.map.id>
              <#if stageMaps?seq_contains(mapId)>
              <#else>
                <#assign stageMaps = stageMaps + [mapId]>
              </#if>
          </#if>
       </#list>

    <div class="col-12 col-md-8 mb-4">

        <div class="border rounded p-2">


            <div class="table-responsive-sm">
            <table class="table table-sm table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th>Etape</th>
                        <th>Date</th>
                        <th class="d-none d-md-table-cell">KM</th>
                        <th class="d-none d-md-table-cell">D+</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <#list trip.sortedStages as stage>
                        <tr>
                            <td><#if stage.map??><span style="height: 10px; width: 10px; background-color: ${colors[stageMaps?seq_index_of(stage.map.id)%7]}; border-radius: 50%; display: inline-block;"></span></#if></td>
                            <td>${stage.name}</td>
                            <td>${stage.date.format(_date_formatter)}</td>
                            <td class="d-none d-md-table-cell"><#if stage.map??>${stage.map.length?floor} km</#if></td>
                            <td class="d-none d-md-table-cell"><#if stage.map??>${stage.map.positiveElevation?floor} m</#if></td>
                            <td>
                                <#if stage.map??>
                                    <a class="link-dark me-2" download="${trip.title}-${stage.name}.gpx" href="<@common.teamUrl team.id '/maps/${stage.map.id}/gpx' />"><i class="bi bi-download"></i></a>
                                    <a class="link-dark" href="<@common.teamUrl team.id '/maps/${stage.map.permalink!stage.map.id}' />"><i class="bi bi-map"></i></a>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </tbody>
                </table>
          </div>

          <div style="width:100%; height:500px;" id="map-wrapper"></div>
          <div class="d-none" id="chart-wrapper"></div>



        </div>

    </div>

    <#if stageMaps?size != 0>

    <script type="text/javascript">

            var layers = {
                 "main" : L.tileLayer('https://tiles.stadiamaps.com/tiles/alidade_smooth/{z}/{x}/{y}{r}.png', {
                    maxZoom: 20,
                    attribution: '&copy; <a href="https://stadiamaps.com/">Stadia Maps</a>, &copy; <a href="https://openmaptiles.org/">OpenMapTiles</a> &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'
                 })
            };

            function initMap(mapContainerId) {
                var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layers['main']] }).setView([${mapCenter[0]?c}, ${mapCenter[1]?c}], 12);
                L.control.zoom({position: 'bottomright'}).addTo(targetMap);
                return targetMap;
            }

            function initGpx(targetMap, gpxUrl, theme) {

               var elevation_options = {
                   theme: theme,
                   detached: true,
                   elevationDiv: "#chart-wrapper",
                   markerIcon:null,
                   followMarker: false,
                   imperial: false,
                   reverseCoords: false,
                   acceleration: false,
                    autofitBounds: false,
                   slope: false,
                   speed: false,
                   time: false,
                   summary: false,
                   altitude: false,
                   distanceMarkers: false,
                   downloadLink: false,
                   ruler: false,
                   legend: false,
                   marker:false
                 };

                 var controlElevation = L.control.elevation(elevation_options).addTo(targetMap);

                 controlElevation.load(gpxUrl);

                 return controlElevation;

            }

            var targetMap = initMap('map-wrapper');

            <#list trip.sortedStages as stage>
                <#if stage.map??>
                    initGpx(targetMap, '<@common.teamUrl team.id '/maps/${stage.map.id}/gpx' />', '${themes[stageMaps?seq_index_of(stage.map.id)%7]}');
                </#if>
            </#list>

        </script>

        </#if>

</div>

<#include "_includes_tail.ftlh">