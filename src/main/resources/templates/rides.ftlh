<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-4 mb-4">

        <form id="rides-filter" action="<@spring.url '/rides' />" method="POST" class="border rounded p-2">

            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="rides-filter-from" class="form-label">Du</label>
                </div>
                <div class="col-9">
                    <input autocomplete="off" type="date" name="from" value="${formdata.from}" required="true" class="form-control" id="rides-filter-from">
                </div>
            </div>
            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="rides-filter-to" class="form-label">Au</label>
                </div>
                <div class="col-9">
                    <input autocomplete="off" type="date" name="to" value="${formdata.to}" required="true" class="form-control" id="rides-filter-to">
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
                          <h4>${ride.title}</h4>
                          <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>
                          <p>Groupes : <#list ride.groups as group><span class="badge bg-secondary">${group.name}</span><#if group_has_next> </#if></#list></p>
                          <p class="small"><@common.displayContent ride.description /></p>
                          <#if ride.imaged>
                            <img src="<@spring.url '/api/rides/${ride.id}/image' />" class="d-block shadow rounded w-50 h-auto mx-auto" alt="${ride.id} image">
                          </#if>
                        </div>
                        <div class="card-footer text-center">
                            <a href="<@spring.url '/rides/${ride.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i> Voir plus</a>
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