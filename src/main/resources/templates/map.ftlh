<#include "_includes_head.ftlh">

<div id="gpxtool" class="container-fluid w-100 h-100 p-0 m-0">

<div class="row p-0 m-0 justify-content-center w-100 h-100">

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

    <div id="map-toolbar" class="w-100 col-12 p-0 m-0">
        <div class="d-flex flex-row justify-content-around w-100 h-100 align-items-center flex-wrap">
            <div class="order-0 d-flex flex-column">
                <div>
                    <span>${map.name}</span>
                    <div class="d-none d-md-inline-block">
                    <#if map.tags?size gt 0>
                         <#list map.tags as tag><span class="badge bg-secondary">${tag}</span><#if tag_has_next> </#if></#list>
                     </#if>
                     </div>
                 </div>
                <span class="small text-muted">${map.postedAt.format(_date_formatter)}</span>
            </div>
            <div class="order-2 order-md-1 d-flex flex-row my-1 my-md-0">
                <span><i class="bi bi-arrow-left-right"></i> ${map.length}km</span>
                <span><i class="bi bi-arrow-up"></i> ${map.positiveElevation}m</span>
                <span><i class="bi bi-arrow-down"></i> ${map.negativeElevation}m</span>
            </div>
            <#if !_embed>
            <div class="order-1 order-md-2 btn-group btn-group-sm" role="group">
                <#if _authenticated>
                    <#if _user.mapFavorites?filter(mp -> mp.id == map.id)?size == 0>
                        <a href="<@common.teamUrl team.id '/maps/${map.id}/add-favorite' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-star"></i><span class="d-none d-md-inline"> Favori</span></a>
                    <#else>
                        <a href="<@common.teamUrl team.id '/maps/${map.id}/remove-favorite' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-star-fill"></i><span class="d-none d-md-inline">  Favori</span></a>
                    </#if>
                </#if>
                <button class="btn btn-outline-secondary" type="button" data-bs-toggle="modal" data-bs-target="#modal-map-${map.id}"><i class="bi bi-download"></i><span class="d-none d-md-inline"> Télécharger</span></button>
                <#if _team_admin>
                    <a href="<@common.teamUrl team.id '/admin/maps/${map.id}' />" class="btn btn-sm btn-outline-secondary"><i class="bi-pencil bi"></i><span class="d-none d-md-inline"> Editer</span></a>
                </#if>
            </div>
            </#if>
        </div>
    </div>

    <div id="map-wrapper" class="col-12 p-0 m-0 w-100">
        <div class="w-100 h-100" id="map-container"></div>
    </div>

    <div id="chart-wrapper" style="height:150px" class="col-12 p-0 m-0">
            <canvas class="w-100" style="height:150px;" id="chart-container"></canvas>
       </div>

</div>
</div>

    <script type="text/javascript">
        initMapView(
            'map-wrapper',
            false,
            '<@common.teamUrl team.id '/maps/${map.id}/data' />',
            'chart-container'
        );
    </script>

<#include "_includes_tail.ftlh">