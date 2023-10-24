<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "trip_nav.ftlh">

    <#if trip.startPlace?? || trip.endPlace??>
            <div class="modal" tabindex="-1" id="placemodal">
              <div class="modal-dialog">
                <div class="modal-content">
                  <div class="modal-header">
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                  </div>
                  <div class="modal-body">

                      <#if trip.startPlace?? && trip.endPlace?? && trip.startPlace.id == trip.endPlace.id>
                            <@common.displayPlace "Départ/Arrivée" trip.startPlace />
                      <#else>

                        <div class="row">

                        <#if trip.startPlace??>
                        <div class="col-12 col-md-6">
                            <@common.displayPlace "Départ" trip.startPlace />
                        </div>
                        </#if>

                        <#if trip.endPlace??>
                        <div class="col-12 col-md-6">
                        <@common.displayPlace "Arrivée" trip.endPlace />
                        </div>
                        </#if>

                        </div>


                       </#if>

                  </div>
                </div>
              </div>
            </div>
        </#if>

     <#if trip.sortedParticipants?size gt 0>
       <div class="modal" tabindex="-1" id="participantsmodal">
         <div class="modal-dialog">
           <div class="modal-content">
             <div class="modal-header">
                 <h5>Participants</h5>
                 <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
             </div>
             <div class="modal-body">

                  <div class="d-flex flex-wrap justify-content-center">
                     <#list trip.sortedParticipants as participant>
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

    <#assign colors=["#566B13", "#1d32a8", "#732C7B", "#bdbd22", "#c90808", "#b81491", "#628de3", "#6dcc5c", "#c694d4", "#e3a209"]>
   <#assign textColors=["#ffffff", "#ffffff", "#ffffff", "#000000", "#ffffff", "#ffffff", "#000000", "#000000", "#000000", "#000000"]>
   <#assign stageMaps = []>
   <#assign totalDistance = 0>
   <#assign totalPositiveElevation = 0>
   <#assign mapCenter = [0.0,0.0]>
   <#if trip.startPlace?? && trip.startPlace.point??>
        <#assign mapCenter = [trip.startPlace.point.lat,trip.startPlace.point.lng]>
   </#if>
   <#list trip.sortedStages as stage>
      <#if stage.map??>
        <#assign totalDistance = totalDistance + stage.map.length>
        <#assign totalPositiveElevation = totalPositiveElevation + stage.map.positiveElevation>
        <#if mapCenter[0] == 0.0>
            <#assign mapCenter = [stage.map.startPoint.lat, stage.map.startPoint.lng]>
        </#if>
        <#assign mapId = stage.map.id>
          <#if stageMaps?seq_contains(mapId)>
          <#else>
            <#assign stageMaps = stageMaps + [mapId]>
            <div class="modal fade" id="modal-map-${stage.map.id}" tabindex="-1">
                  <div class="modal-dialog">
                    <div class="modal-content">
                      <div class="modal-header">
                        <h5 class="modal-title">${stage.map.name}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                      </div>
                      <div class="modal-body">
                        <div class="d-grid gap-2 col-6 mx-auto">
                            <a download="${stage.map.name}.gpx" href="<@common.teamUrl team.id '/maps/${stage.map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> GPX</a>
                            <a download="${stage.map.name}.fit" href="<@common.teamUrl team.id '/maps/${stage.map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> FIT</a>
                            <a href="<@common.teamUrl team.id '/maps/${stage.map.id}/garmin' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-triangle-fill"></i> Garmin</a>
                        </div>
                      </div>
                      <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
                      </div>
                    </div>
                  </div>
                </div>
          </#if>
      </#if>
   </#list>

    <div class="col-12 mb-2">

        <div class="row p-0 py-2 m-0 border rounded">

            <div class="col-12 col-md-8">

            <#if trip.publishedStatus != 'PUBLISHED'>
                <div class="p-1 alert alert-warning" role="alert">Ce trip n'est pas publié.<br/> <span class="small text-muted">Publication le ${trip.publishedAt.format(_date_formatter)} à ${trip.publishedAt.format(_time_formatter)} (UTC)</span></div>
            </#if>

            <h5 class="text-muted">${trip.startDate.format(_date_formatter)} <i class="bi bi-arrow-right-short"></i> ${trip.endDate.format(_date_formatter)}</h5>

            <ul class="list-unstyled">
                <li >${trip.stages?size} étape<#if trip.stages?size gt 1>s</#if> - <a class="link-dark" href="#stage-section">Détails</a></li>
                <li>Départ à ${trip.meetingTime}<#if trip.startPlace??> - <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${trip.startPlace.name}</a></#if></li>
                <#if trip.endPlace??>
                    <li>Arrivée : <a href="#" class="link-dark" data-bs-toggle="modal" data-bs-target="#placemodal">${trip.endPlace.name}</a></li>
                </#if>

                <#if totalDistance gt 0>
                 <li><div class="d-flex flex-row justify-content-start align-items-center gap-1 gap-md-2 gap-xxl-3">
                       <span class="text-nowrap"><i class="bi bi-arrow-left-right d-none d-md-inline-block"></i> ${totalDistance?floor}km</span>
                       <span class="text-nowrap"><i class="bi bi-arrow-up d-none d-md-inline-block"></i> ${totalPositiveElevation?floor}m</span>
                   </div></li>
                 </#if>

            </ul>

            <div class="d-flex flex-row align-items-center justify-content-between">
                <div>Participants :</div>
                <div>
                    <div class="d-flex flex-row align-items-center justify-content-between">
                        <div class="d-flex flex-row align-items-start justify-content-start">
                            <div class="d-flex profile-images me-2">
                                <#if trip.sortedParticipants?size gt 0>
                                    <#list trip.sortedParticipants?chunk(6)[0] as participant>
                                            <div class="profile-images-item" style="width:24px; height:24px;" class="d-inline-block"><a href="#" class="text-decoration-none" data-bs-toggle="modal" data-bs-target="#participantsmodal"><img class="w-100 h-100 d-block" src="<@common.teamUrl '' '/users/${participant.id}/image' />" alt="${participant.identity}"></a></div>
                                    </#list>
                                </#if>
                            </div>
                            <div>
                                <#if trip.sortedParticipants?size gt 6>
                                    <a href="#" class="text-decoration-none" data-bs-toggle="modal" data-bs-target="#participantsmodal"><span class="text-muted small fst-italic">+${trip.sortedParticipants?size - 6}</span></a>
                                <#elseif trip.sortedParticipants?size == 0>
                                    <span class="text-muted small fst-italic">Aucun participant</span>
                                </#if>
                            </div>
                        </div>
                        <div class="d-block ms-2">
                            <#if _authenticated>
                                <#if trip.hasParticipant(_user.id)>
                                    <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}/remove-participant' />" class="py-0 px-2 d-block btn btn-sm btn-success"><i class="bi-person-dash bi"></i></a>
                                <#else>
                                    <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}/add-participant' />" class="py-0 px-2 d-block btn btn-sm btn-outline-success"><i class="bi-person-add bi"></i></a>
                                 </#if>
                            <#else>
                                   <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}/add-participant' />" class="py-0 px-2 d-block btn btn-sm btn-outline-success"><i class="bi-person-add bi"></i></a>
                            </#if>
                        </div>
                    </div>
                </div>
            </div>


            <hr/>

            <p class="mt-4 wrap-content">${trip.description}</p>

            </div>

            <div class="col-12 col-md-4">

            <#if trip.imaged>
              <img src="<@common.teamUrl team.id '/trips/${trip.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.id} image">
            </#if>

         </div>

        </div>

    </div>



    <div class="col-12 col-xl-4 mb-4">

            <div id="stage-section">

               <#list trip.sortedStages as stage>

                   <div class="gap-1 gap-md-3 d-flex flex-row justify-content-between align-items-center border rounded mb-1 py-1 px-2" <#if stage.map??> onmouseover="highlightMap(${stageMaps?seq_index_of(stage.map.id)})"</#if>>

                       <div class="d-flex flex-column flex-grow-1 gap-1">
                           <div class="gap-2 d-flex flex-row justify-content-start align-items-center">
                                <span>${stage.date.format(_date_short_formatter)}</span>
                               <span class="short-label text-nowrap text-truncate">${stage.name}</span>
                           </div>

                           <div class="d-flex flex-row justify-content-start align-items-center small gap-1 gap-md-2 gap-xxl-3">
                               <#if stage.map??>
                                   <span style="color: ${textColors[stageMaps?seq_index_of(stage.map.id)%10]}; background-color: ${colors[stageMaps?seq_index_of(stage.map.id)%10]};" class="text-nowrap badge border px-1 py-1"><@common.displayMapType stage.map.type /></span>
                                   <span class="text-nowrap"><i class="bi bi-arrow-left-right d-none d-md-inline-block"></i> ${stage.map.length?floor}km</span>
                                   <span class="text-nowrap"><i class="bi bi-arrow-up d-none d-md-inline-block"></i> ${stage.map.positiveElevation?floor}m</span>
                              </#if>
                           </div>

                       </div>

                       <div class="d-flex flex-column gap-1">

                           <div>
                               <div class="btn-group">
                                    <#if stage.map??>
                                        <button class="btn btn-sm btn-link link-dark py-0 px-2" type="button" data-bs-toggle="modal" data-bs-target="#modal-map-${stage.map.id}"><i class="bi bi-download"></i></button>
                                       <a class="btn btn-sm btn-link link-dark py-0 px-2" href="<@common.teamUrl team.id '/maps/${stage.map.permalink!stage.map.id}' />"><i class="bi bi-map"></i></a>
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

    <#if stageMaps?size != 0>

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


            <#list stageMaps as map>

                loadJsonContent('<@common.teamUrl team.id '/maps/${map}/geojson' />', function(geojsonFeatures) {

                    var geojsonFeature = geojsonFeatures.features.find(f => f.geometry.type === 'LineString');

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
                                }, false);
                            }
                         </#if>
                    });

                    <#if map?is_first>

                         var firstPoint = geojsonFeature.geometry.coordinates[0];

                         L.marker([firstPoint[1], firstPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapStartIcon'})}).addTo(targetMap);

                    </#if>

                    <#if map?is_last>

                            var lastPoint = geojsonFeature.geometry.coordinates[geojsonFeature.geometry.coordinates.length - 1];

                         L.marker([lastPoint[1], lastPoint[0]], {clickable: false, icon : L.divIcon({className: 'mapEndIcon'})}).addTo(targetMap);


                    </#if>

                });


            </#list>

            <#if trip.startPlace?? || trip.endPlace??>
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

</div>

<#include "_includes_tail.ftlh">