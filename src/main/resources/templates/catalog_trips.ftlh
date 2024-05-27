<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mt-2">

        <div class="row g-2 pb-4">
            <#if trips?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Aucun trip à afficher.
                 </div>
            </#if>
            <#list trips as trip>

                  <div class="col-12 col-md-4">
                    <div class="card">
                        <div class="card-body">
                          <h5 class="card-title"><a class="link-dark" href="<@common.teamUrl '' '/catalog/trips/${trip.id}' />">${trip.title}</a></h5>
                          <p class="card-text">
                            ${trip.stages?filter(st -> !st.alternative)?size} étape<#if trip.stages?filter(st -> !st.alternative)?size gt 1>s</#if>
                          </p>
                        </div>
                    </div>
                  </div>
            </#list>
        </div>

      </div>

</div>

<#include "_includes_tail.ftlh">