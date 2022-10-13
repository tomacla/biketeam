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
   <#assign themes=["lime-theme", "steelblue-theme", "purple-theme", "yellow-theme", "red-theme", "magenta-theme", "lightblue-theme"]>
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
                            <tr>
                                <td><#if group.map??><span style="height: 10px; width: 10px; background-color: ${colors[groupMaps?seq_index_of(group.map.id)%7]}; border-radius: 50%; display: inline-block;"></span></#if></td>
                                <td>${group.meetingTime}</td>
                                <td>${group.name}</td>
                                <td class="d-none d-md-table-cell">${group.lowerSpeed}/${group.upperSpeed} km/h</td>
                                <td class="d-none d-md-table-cell"><#if group.map??>${group.map.length?floor} km</#if></td>
                                <td class="d-none d-md-table-cell"><#if group.map??>${group.map.positiveElevation?floor} m</#if></td>
                                <td>
                                    <div class="d-flex flex-row align-items-start justify-content-start">
                                        <div class="d-none d-md-flex profile-images me-2">
                                            <#if group.sortedParticipants?size gt 0>
                                                <#list group.sortedParticipants?chunk(3)[0] as participant>
                                                        <div class="profile-images-item" style="width:24px; height:24px;" class="d-inline-block"><img class="w-100 h-100 d-block" src="<@common.teamUrl '' '/users/${participant.id}/image' />" alt="${participant.identity}"></div>
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
              <div class="d-none" id="chart-wrapper"></div>

        </div>

    </div>


</div>


<#if groupMaps?size != 0>

 <script type="text/javascript">

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
            var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layers['main']] }).setView([${mapCenter[0]?c}, ${mapCenter[1]?c}], 12);
            L.control.zoom({position: 'bottomright'}).addTo(targetMap);
            return targetMap;
        }

        var startendloaded = false;
        function initGpx(targetMap, gpxUrl, theme) {

           var elevation_options = {
               theme: theme,
               detached: true,
               elevationDiv: "#chart-wrapper",
               markerIcon:null,
               followMarker: false,
               imperial: false,
               autofitBounds: false,
               reverseCoords: false,
               acceleration: false,
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

                if(!startendloaded) {
                    startendloaded = true;

                    controlElevation.on('eledata_loaded', (data) => {


                    var startPoint = data.data[0].geometry.coordinates[0];
                    var endpoint = data.data[0].geometry.coordinates[data.data[0].geometry.coordinates.length - 1];

                    L.marker([startPoint[1], startPoint[0]], {clickable: false, icon : L.icon({
                        iconUrl: '<@common.teamUrl '' '/css/pin-icon-start.png' />',
                        iconSize: [33, 45],
                        iconAnchor: [16, 45],
                        shadowUrl: '<@common.teamUrl '' '/css/pin-shadow.png' />',
                        shadowSize: [50, 50],
                        shadowAnchor: [25, 50]
                    })}).addTo(targetMap);
                    L.marker([endpoint[1], endpoint[0]], {clickable: false, icon : L.icon({
                       iconUrl: '<@common.teamUrl '' '/css/pin-icon-end.png' />',
                       iconSize: [33, 45],
                       iconAnchor: [16, 45],
                       shadowUrl: '<@common.teamUrl '' '/css/pin-shadow.png' />',
                       shadowSize: [50, 50],
                       shadowAnchor: [25, 50]
                   })}).addTo(targetMap);

                 });

             }

             controlElevation.load(gpxUrl);

             return controlElevation;

        }

        var targetMap = initMap('map-wrapper');

        <#list ride.sortedGroups as group>
            <#if group.map??>
                initGpx(targetMap, '<@common.teamUrl team.id '/maps/${group.map.id}/gpx' />', '${themes[groupMaps?seq_index_of(group.map.id)%7]}');
            </#if>
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