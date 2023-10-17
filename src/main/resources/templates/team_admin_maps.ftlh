<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl team.id '/admin/maps/new' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-map-gpx" class="form-label">Fichier GPX</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouvelle Map">
            </div>
        </form>
        <hr/>

        <form id="maps-filter" action="<@common.teamUrl team.id '/admin/maps' />" method="GET" class="mb-2">

            <input type="hidden" id="maps-filter-page" name="page" value="${page}">
            <input type="hidden" id="maps-filter-pageSize" name="pageSize" value="${pageSize}">

            <div class="row gy-2">

                  <div class="col-8 col-md-6">
                      <div class="input-group">
                          <label class="input-group-text" for="maps-filter-title">Nom</label>
                          <input autocomplete="off" type="text" value="${name}" name="name" class="form-control" id="maps-filter-title">
                       </div>
                    </div>

                <div class="col-4 col-md-2 text-center">
                    <button type="submit" class="btn btn-secondary">Filtrer</button>
                </div>

            </div>
         </form>

        <#if maps?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Map<span class="text-muted"> - Date d'ajout</span></div>
                     <div>
                        <@common.pagination page pages 'maps-filter-page' 'maps-filter' />
                    </div>
                    <div>Actions</div>
                </li>
                <#list maps as map>
                    <li class="d-flex list-group-item list-group-item-map justify-content-between align-items-center">
                        <div>${map.name}<span class="text-muted"> - ${map.postedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/maps/${map.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/maps/${map.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/maps/delete/${map.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>


         <#else>
             <div class="alert alert-warning" role="alert">
               Aucune map.
             </div>
         </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">