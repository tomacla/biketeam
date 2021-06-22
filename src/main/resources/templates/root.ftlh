<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="col-12">

        <div class="text-center mt-4">
            <h1>Biketeam</h1>
            <p class="fs-5 text-muted">Créer un site unique pour votre groupe de vélo.</p>
            <a href="<@spring.url '/new' />" class="btn btn-secondary">Créer ma team</a>
        </div>

        <hr/>

        <#if feed?? && feed?size != 0>
            <div class="row">
                <div class="col-12 col-md-8 border-end">
                    <@common.displayFeed true />
                </div>
                <div class="col-12 col-md-4">
        </#if>

            <h6 class="text-center lead my-4">Déjà sur Biketeam</h6>

            <div class="row row-cols-1 row-cols-md-<#if feed?? && feed?size != 0>1<#else>4</#if> g-4">
                <#list teams as team>
                    <div class="col">
                        <div class="card h-100">
                            <div class="card-header bg-white px-0 py-2 mb-3">
                            <div style="height:180px; width:100%;  background-image: url('<@spring.url '/api/' + team.id + '/image' />');background-repeat: no-repeat;background-position: 50% 50%; background-size: contain;"></div>
                            </div>
                          <div class="card-body">

                            <h5 class="card-title"><a class="link-dark" href="<@spring.url '/' + team.id />">${team.name}</a></h5>
                            <h6 class="text-muted">${team.city}, ${team.country}</h6>
                          </div>
                          <div class="card-footer text-center">
                              <a href="<@spring.url '/' + team.id />" class="btn btn-secondary btn-sm">Voir</a>
                            </div>
                        </div>
                    </div>
                </#list>
            </div>

        <#if feed?? && feed?size != 0>
            </div>
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">