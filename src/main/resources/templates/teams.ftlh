<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="col-12 mb-4">

        <form id="teams-filter" action="<@common.teamUrl '' '/teams' />" method="GET" class="border rounded p-2">

            <input type="hidden" id="teams-filter-page" name="page" value="${formdata.page}">
            <input type="hidden" id="teams-filter-pageSize" name="pageSize" value="${formdata.pageSize}">

            <div class="row gy-2">
                  <div class="col-12 col-md-3">
                      <div class="input-group">
                          <label class="input-group-text" for="teams-filter-name">Nom</label>
                          <input autocomplete="off" type="text" value="${formdata.name}" name="name" class="form-control" id="teams-filter-name">
                       </div>
                    </div>
                  <div class="col-12 col-md-4">
                    <div class="input-group">
                        <label class="input-group-text" for="teams-filter-city">Ville</label>
                        <input autocomplete="off" type="text" value="${formdata.city}" name="city" class="form-control" id="teams-filter-city">
                     </div>
                  </div>
                  <div class="col-12 col-md-4">
                    <div class="input-group">
                        <label class="input-group-text" for="teams-filter-name">Pays</label>
                        <@common.countrySelect formdata.country 'country' 'teams-filter-country' false />
                     </div>
                  </div>
                <div class="col-12 col-md-1 text-center">
                    <button type="submit" class="btn btn-secondary">Valider</button>
                </div>
            </div>

         </form>

    </div>

    <div class="col-12">

        <#if teams?size == 0>
             <div class="alert alert-warning" role="alert">
               Aucune team à afficher.
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