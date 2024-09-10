<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#assign colors=["#566B13", "#1d32a8", "#732C7B", "#bdbd22", "#c90808", "#b81491", "#628de3", "#6dcc5c", "#c694d4", "#e3a209"]>
   <#assign textColors=["#ffffff", "#ffffff", "#ffffff", "#000000", "#ffffff", "#ffffff", "#000000", "#000000", "#000000", "#000000"]>
   <#assign stageMaps = []>
   <#assign mapCenter = [0.0,0.0]>
   <#list trip.sortedStages as stage>
      <#if stage.map??>
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
                            <a download="${stage.map.name}.gpx" href="<@common.teamUrl teamId '/maps/${stage.map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> GPX</a>
                            <a download="${stage.map.name}.fit" href="<@common.teamUrl teamId '/maps/${stage.map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> FIT</a>
                            <a href="<@common.teamUrl teamId '/maps/${stage.map.id}/garmin' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-triangle-fill"></i> Garmin</a>
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

            <h5 class="text-muted">${trip.title}</h5>

            <ul class="list-unstyled">
                <li >${trip.stages?filter(st -> !st.alternative)?size} étape<#if trip.stages?filter(st -> !st.alternative)?size gt 1>s</#if> - <a class="link-dark" href="#stage-section">Détails</a></li>

                <#if trip.distance gt 0>
                 <li><div class="d-flex flex-row justify-content-start align-items-center gap-1 gap-md-2 gap-xxl-3">
                       <span class="text-nowrap"><i class="bi bi-arrow-left-right d-none d-md-inline-block"></i> ${trip.distance?floor}km</span>
                       <span class="text-nowrap"><i class="bi bi-arrow-up d-none d-md-inline-block"></i> ${trip.elevation?floor}m</span>
                   </div></li>
                 </#if>

            </ul>


            <hr/>

            <p class="mt-4 wrap-content">${trip.description}</p>

            </div>

            <div class="col-12 col-md-4">

            <#if trip.imaged>
              <img src="<@common.teamUrl teamId '/trips/${trip.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.id} image">
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
                               <span class="short-label text-nowrap text-truncate">${stage.name} <#if stage.alternative><span class="text-nowrap badge bg-light text-dark border px-1 py-1">alt</span></#if></span>
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
                                       <a class="btn btn-sm btn-link link-dark py-0 px-2" href="<@common.teamUrl teamId '/maps/${stage.map.permalink!stage.map.id}' />"><i class="bi bi-map"></i></a>
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

              <div class="position-relative w-100" style="z-index:1; height:500px;">
                    <div class="w-100 position-absolute bg-white top end-0" style="z-index:1; height:500px;" id="map-wrapper">
                    </div>
                    <div class="w-100 w-sm-100 w-md-40 position-absolute bg-white top end-0" style="z-index:2000; height:150px;">
                          <canvas class="w-100" style="height:150px;" id="chart-wrapper"></canvas>
                     </div>
              </div>

         </div>

            </div>


        </div>

        <div class="col-12 mb-2">

                <div class="row p-0 py-2 m-0 border rounded">
                        <div class="markdown-content"><#if trip.markdownPage??>${trip.markdownPage}</#if></div>
                </div>
        </div>

    <#if stageMaps?size != 0>

    <script type="text/javascript">

            var mapCenter = [${mapCenter[0]?c}, ${mapCenter[1]?c}];
            var colors = [
            <#list colors as color>
                '${color}',
            </#list>
            ];
            var maps = [
            <#list stageMaps as map>
                { id: '${map}', url: '<@common.teamUrl teamId '/maps/${map}/data' />' },
            </#list>
            ];
            initMaps(mapCenter, maps, colors, true);

            <#if trip.startPlace?? || trip.endPlace??>
            resetPlacemodal();
            </#if>

        </script>

        </#if>

</div>

<#include "_includes_tail.ftlh">