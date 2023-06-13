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

    <div class="col-12 mb-2">

        <div class="row p-0 py-2 m-0 border rounded">

            <div class="col-12 col-md-8">

                <#if ride.publishedStatus != 'PUBLISHED'>
                    <div class="p-1 alert alert-warning" role="alert">Ce ride n'est pas publié.<br/> <span class="small text-muted">Publication le ${ride.publishedAt.format(_date_formatter)} à ${ride.publishedAt.format(_time_formatter)} (UTC)</span></div>
                </#if>
                <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>

                <ul class="list-unstyled">
                    <li>${ride.groups?size} groupe<#if ride.groups?size gt 1>s</#if> - <a class="link-dark" href="#group-section">Détails</a></li>
                    <#if ride.sortedGroups?first.meetingTime != ride.sortedGroups?last.meetingTime>
                        <li>Départ de ${ride.sortedGroups?first.meetingTime} à ${ride.sortedGroups?last.meetingTime}<#if ride.startPlace??> - <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${ride.startPlace.name}</a></#if></li>
                        <#else>
                        <li>Départ à ${ride.sortedGroups?first.meetingTime}<#if ride.startPlace??> - ${ride.startPlace.name}</#if></li>
                    </#if>
                    <#if ride.endPlace??>
                        <li>Arrivée : <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${ride.endPlace.name}</a></li>
                    </#if>
                </ul>

                <hr/>

                <p class="wrap-content">${ride.description}</p>

            </div>

            <div class="col-12 col-md-4">

                   <#if ride.imaged>
                     <img src="<@common.teamUrl team.id '/rides/${ride.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.id} image">
                 </#if>

            </div>

        </div>

    </div>


   <#assign colors=["#566B13", "#1d32a8", "#732C7B", "#bdbd22", "#c90808", "#b81491", "#628de3", "#6dcc5c", "#c694d4", "#e3a209"]>
   <#assign textColors=["#ffffff", "#ffffff", "#ffffff", "#000000", "#ffffff", "#ffffff", "#000000", "#000000", "#000000", "#000000"]>
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

    <div class="col-12 col-xl-4 mb-4">

              <div id="group-section">
                <#list ride.sortedGroups as group>

                    <div style="oveflow:hidden;" class="gap-1 gap-md-3 d-flex flex-row justify-content-between align-items-center border rounded mb-1 py-1 px-2" <#if group.map??> onmouseover="highlightMap(${groupMaps?seq_index_of(group.map.id)})"</#if>>

                        <div class="d-flex flex-column flex-grow-1 gap-1">

                            <div class="gap-2 d-flex flex-row justify-content-start align-items-center">
                                <strong>${group.meetingTime}</strong>
                                <span class="short-label text-nowrap text-truncate">${group.name}</span>
                            </div>

                            <div class="d-flex flex-row justify-content-start align-items-center small gap-1 gap-md-2 gap-xxl-3">
                                <#if group.map??>
                                       <span style="color: ${textColors[groupMaps?seq_index_of(group.map.id)%10]}; background-color: ${colors[groupMaps?seq_index_of(group.map.id)%10]};" class="text-nowrap badge border px-1 py-1"><@common.displayMapType group.map.type /></span>
                                </#if>
                                <span class="text-nowrap"><i class="bi bi-speedometer2 d-none d-md-inline-block"></i> ${group.averageSpeed}km/h</span>
                                <#if group.map??>
                                    <span class="text-nowrap"><i class="bi bi-arrow-left-right d-none d-md-inline-block"></i> ${group.map.length?floor}km</span>
                                    <span class="text-nowrap"><i class="bi bi-arrow-up d-none d-md-inline-block"></i> ${group.map.positiveElevation?floor}m</span>
                               </#if>
                            </div>

                        </div>

                        <div class="d-flex flex-column gap-1 align-items-end">

                            <div class="d-flex flex-row align-items-start justify-content-end gap-1 gap-md-2">
                                <div class="d-flex profile-images">
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
                                <div>
                                    <#if _authenticated>
                                        <#if group.hasParticipant(_user.id)>
                                            <a class="btn btn-sm btn-success py-0 px-2" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/remove-participant/${group.id}' />" ><i class="bi bi-person-dash"></i></a>
                                        <#else>
                                            <#if !ride.isParticipantInAnotherGroup(group.id, _user.id)>
                                                <a class="btn btn-sm btn-outline-success py-0 px-2" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />"><i class="bi bi-person-add"></i></a>
                                            </#if>
                                         </#if>
                                    <#else>
                                          <a class="btn btn-sm btn-outline-success py-0 px-2" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />"><i class="bi bi-person-add"></i></a>
                                    </#if>
                                </div>
                            </div>

                            <div>
                                <div class="btn-group">
                                     <#if group.map??>
                                        <a class="btn btn-sm btn-link link-dark py-0 px-2" download="${ride.title}-${group.name}.gpx" href="<@common.teamUrl team.id '/maps/${group.map.id}/gpx' />"><i class="bi bi-download"></i></a>
                                        <a class="btn btn-sm btn-link link-dark py-0 px-2" href="<@common.teamUrl team.id '/maps/${group.map.id}/garmin' />"><i class="bi bi-triangle-fill"></i></a>
                                        <a class="btn btn-sm btn-link link-dark py-0 px-2" href="<@common.teamUrl team.id '/maps/${group.map.permalink!group.map.id}' />"><i class="bi bi-map"></i></a>
                                     </#if>
                                </div>
                            </div>

                        </div>

                    </div>
                </#list>
              </div>

       </div>

        <div class="col-12 col-xl-8 mb-4">

               <div class="border rounded">

              <div class="position-relative w-100" style="z-index:1; height:500px;" id="map-wrapper">
                    <div class="w-100 w-sm-100 w-md-40 position-absolute bg-white top end-0" style="z-index:2000; height:150px;">
                          <canvas class="w-100" style="height:150px;" id="chart-wrapper"></canvas>
                     </div>
              </div>

        </div>

    </div>


</div>


<#if groupMaps?size != 0>

 <script type="text/javascript">

        var targetMap = initMap('map-wrapper', ${mapCenter[0]?c} + 0.02, ${mapCenter[1]?c}, 11, "Carto", false, false);

        var geoJsonLayers = [];

        function highlightMap(mapIndex) {
            restoreMap();
            const layer = geoJsonLayers.find(function(l) {
                return l.mapIndex === mapIndex;
            });
            if(layer) {
                layer.layer.setStyle({
                 weight: 8,
                 opacity: 1
                });
                layer.layer.bringToFront();

                updateChart(layer.elevationProfile, layer.color);
            }
        }

        function restoreMap() {
            geoJsonLayers.forEach(function(layer) {
                layer.layer.setStyle({
                  weight: 5,
                  opacity: 0.8
                });
            });
        }

        <#list groupMaps as map>

            loadJsonContent('<@common.teamUrl team.id '/maps/${map}/geojson' />', function(geojsonFeature) {
                var layer = L.geoJSON(geojsonFeature,
                    {
                      interactive:true,
                      color: '${colors[map?index%10]}',
                      weight: 5,
                      opacity: 0.8
                    }).on('click', function(e) {
                        highlightMap(${map?index});
                    });

                layer.addTo(targetMap);

                loadJsonContent('<@common.teamUrl team.id '/maps/${map}/elevation-profile' />', function(elevationProfile) {

                    geoJsonLayers.push({
                        layer: layer,
                        mapIndex: ${map?index},
                        color: '${colors[map?index%10]}',
                        elevationProfile: elevationProfile
                    });

                    <#if map?is_first>
                        if(elevationChart === null) {
                            initChart(targetMap, 'chart-wrapper', elevationProfile, '${colors[map?index%10]}', function() {
                                highlightMap(${map?index});
                            });
                        }
                    </#if>

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