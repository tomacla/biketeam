<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="col-12 mb-4">

        <div class="row border rounded p-2">

            <div class="col-12 col-md-8">

                <form id="teams-filter" action="<@common.teamUrl '' '/teams' />" method="GET">

                    <input type="hidden" id="teams-filter-page" name="page" value="${page}">
                    <input type="hidden" id="teams-filter-pageSize" name="pageSize" value="${pageSize}">

                    <div class="row gy-2">

                          <div class="col-8 col-md-6">
                              <div class="input-group">
                                  <label class="input-group-text" for="teams-filter-name">Nom</label>
                                  <input autocomplete="off" type="text" value="${name}" name="name" class="form-control" id="teams-filter-name">
                               </div>
                            </div>

                        <div class="col-4 col-md-2 text-center">
                            <button type="submit" class="btn btn-secondary">Valider</button>
                        </div>

                    </div>
                 </form>

             </div>

             <div class="col-12 col-md-4 mt-2 mt-md-0">
                <div class="d-flex flew-row justify-content-center justify-content-md-end">
                    <p class="lead m-0 me-2">${matches} groupe<#if matches gt 1>s</#if></p>
                    <#if pages gt 1><div><@common.pagination page pages 'teams-filter-page' 'teams-filter' /></div></#if>
                </div>
             </div>

         </div>



    </div>

    <div class="col-12">

        <#if teams?size == 0>
             <div class="alert alert-warning" role="alert">
               Aucun groupe à afficher.
             </div>
        <#else>
             <div class="row row-cols-1 row-cols-md-4 g-4">
                 <#list teams as team>
                     <div class="col">
                         <div class="card h-100">
                             <div class="card-header bg-white px-0 py-2 mb-3">
                             <div style="height:180px; width:100%;  background-image: url('<@common.teamUrl team.id '/image' />');background-repeat: no-repeat;background-position: 50% 50%; background-size: contain;"></div>
                             </div>
                           <div class="card-body">
                             <h5 class="card-title"><a class="link-dark" href="<@common.teamUrl team.id '' />"><#if team.visibility == 'PRIVATE'><i class="bi bi-lock-fill"></i> </#if>${team.name}</a></h5>
                             <h6 class="text-muted">${team.city}, ${team.country}</h6>
                           </div>
                         </div>
                     </div>
                 </#list>
             </div>
        </#if>
    </div>

</div>

<#include "_includes_tail.ftlh">