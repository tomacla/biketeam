<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <a class="btn btn-secondary" href="<@common.teamUrl team.id '/admin/trips/new' />">Nouveau trip</a>

        <hr/>

        <form id="trips-filter" action="<@common.teamUrl team.id '/admin/trips' />" method="GET" class="mb-2">

            <input type="hidden" id="trips-filter-page" name="page" value="${page}">
            <input type="hidden" id="trips-filter-pageSize" name="pageSize" value="${pageSize}">

            <div class="row gy-2">

                  <div class="col-8 col-md-6">
                      <div class="input-group">
                          <label class="input-group-text" for="trips-filter-title">Nom</label>
                          <input autocomplete="off" type="text" value="${title}" name="title" class="form-control" id="trips-filter-title">
                       </div>
                    </div>

                <div class="col-4 col-md-2 text-center">
                    <button type="submit" class="btn btn-secondary">Filtrer</button>
                </div>

            </div>
         </form>

        <#if trips?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Trip<span class="text-muted"> - Date de publication</span></div>
                     <div>
                            <@common.pagination page pages 'trips-filter-page' 'trips-filter' />
                        </div>
                    <div>Actions</div>
                </li>
                <#list trips as trip>
                    <li class="d-flex list-group-item list-group-item-trip justify-content-between align-items-center">
                        <div><span class="text-<#if trip.publishedStatus == 'PUBLISHED'>success<#else>danger</#if>"><i class="bi bi-calendar-<#if trip.publishedStatus == 'PUBLISHED'>check<#else>x</#if>"></i></span> ${trip.title} (${trip.startDate.format(_date_formatter)})<span class="text-muted"> - ${trip.publishedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/trips/${trip.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/trips/${trip.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/trips/delete/${trip.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>

        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun trip.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">