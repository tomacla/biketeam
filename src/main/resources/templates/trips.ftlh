<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-4 mb-4">

        <form id="trips-filter" action="<@common.teamUrl team.id '/trips' />" method="GET" class="border rounded p-2">

            <#if _team_admin>
                <div class="text-end">
                    <a href="<@common.teamUrl team.id '/admin/trips' />" class="btn btn-secondary btn-sm"><i class="bi bi-plus-circle"></i> Nouveau</a>
                 </div>
             </#if>

            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="trips-filter-from" class="form-label">Départ du</label>
                </div>
                <div class="col-9">
                    <input onchange="setFieldValue('trips-filter-page', 0);" autocomplete="off" type="date" name="from" value="${formdata.from}" required class="form-control" id="trips-filter-from">
                </div>
            </div>
            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="trips-filter-to" class="form-label">Au</label>
                </div>
                <div class="col-9">
                    <input onchange="setFieldValue('trips-filter-page', 0);" autocomplete="off" type="date" name="to" value="${formdata.to}" required class="form-control" id="trips-filter-to">
                </div>
            </div>

            <input type="hidden" id="trips-filter-page" name="page" value="${formdata.page}">
            <input type="hidden" id="trips-filter-pageSize" name="pageSize" value="${formdata.pageSize}">

            <div class="d-flex mt-3 justify-content-center">
                <button type="submit" class="btn btn-secondary btn-sm">Valider</button>
            </div>

        </form>

    </div>

    <div class="col-12 col-md-8">

        <div class="row g-2 pb-4">

             <#if trips?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Aucun trip à afficher.
                 </div>
            </#if>

            <#list trips as trip>
                  <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                          <h4><a class="link-dark" href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}' />">${trip.title}</a></h4>
                          <h5 class="text-muted">Du ${trip.startDate.format(_date_formatter)} au ${trip.endDate.format(_date_formatter)}</h5>
                          <p>Etapes : <#list trip.sortedStages as stage><span class="badge bg-secondary">${stage.name}</span><#if stage_has_next> </#if></#list></p>
                          <p class="small wrap-content">${trip.description}</p>
                          <#if trip.imaged>
                            <div class="row justify-content-center">
                                <div class="col-12 col-md-6">
                                     <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}' />"><img src="<@common.teamUrl team.id '/trips/${trip.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.id} image"></a>
                                </div>
                            </div>
                          </#if>
                        </div>
                        <div class="card-footer text-center">
                            <a href="<@common.teamUrl team.id '/trips/${trip.permalink!trip.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye-fill"></i> Voir</a>
                        </div>
                    </div>
                  </div>
            </#list>

        </div>

        <#if pages gt 1>
            <div class="pb-4 d-flex justify-content-center">
                <@common.pagination formdata.page pages 'trips-filter-page' 'trips-filter' />
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">