<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-4 mb-4">

        <form id="rides-filter" action="<@common.teamUrl team.id '/rides' />" method="GET" class="border rounded p-2">

            <#if _team_admin>
                <div class="text-end">
                    <a href="<@common.teamUrl team.id '/admin/rides' />" class="btn btn-secondary btn-sm"><i class="bi bi-plus-circle"></i> Nouveau</a>
                 </div>
             </#if>

            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="rides-filter-from" class="form-label">Du</label>
                </div>
                <div class="col-9">
                    <input onchange="setFieldValue('rides-filter-page', 0);" autocomplete="off" type="date" name="from" value="${formdata.from}" required class="form-control" id="rides-filter-from">
                </div>
            </div>
            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="rides-filter-to" class="form-label">Au</label>
                </div>
                <div class="col-9">
                    <input onchange="setFieldValue('rides-filter-page', 0);" autocomplete="off" type="date" name="to" value="${formdata.to}" required class="form-control" id="rides-filter-to">
                </div>
            </div>

            <input type="hidden" id="rides-filter-page" name="page" value="${formdata.page}">
            <input type="hidden" id="rides-filter-pageSize" name="pageSize" value="${formdata.pageSize}">

            <div class="d-flex mt-3 justify-content-center">
                <button type="submit" class="btn btn-secondary btn-sm">Valider</button>
            </div>

        </form>

    </div>

    <div class="col-12 col-md-8">

        <div class="row g-2 pb-4">

             <#if rides?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Aucun ride à afficher.
                 </div>
            </#if>

            <#list rides as ride>
                  <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                          <h4><a class="link-dark" href="<@common.teamUrl team.id '/rides/${ride.id}' />">${ride.title}</a></h4>
                          <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>
                          <p>Groupes : <#list ride.sortedGroups as group><span class="badge bg-secondary">${group.name}</span><#if group_has_next> </#if></#list></p>
                          <p class="small wrap-content">${ride.description}</p>
                          <#if ride.imaged>
                            <div class="row justify-content-center">
                                <div class="col-12 col-md-6">
                                     <a href="<@common.teamUrl team.id '/rides/${ride.id}' />"><img src="<@common.teamUrl team.id '/rides/${ride.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.id} image"></a>
                                </div>
                            </div>
                          </#if>
                        </div>
                        <div class="card-footer text-center">
                            <a href="<@common.teamUrl team.id '/rides/${ride.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye-fill"></i> Voir</a>
                        </div>
                    </div>
                  </div>
            </#list>

        </div>

        <#if pages gt 1>
            <div class="pb-4 d-flex justify-content-center">
                <@common.pagination formdata.page pages 'rides-filter-page' 'rides-filter' />
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">