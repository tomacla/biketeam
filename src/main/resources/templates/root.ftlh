<#include "_includes_head.ftlh">

<div class="text-center mt-4">
    <h1>Biketeam</h1>
    <p class="fs-5 text-muted">Créer un site unique pour votre groupe de vélo.</p>
    <a href="<@spring.url '/new' />" class="btn btn-secondary">Créer ma team</a>
</div>

<hr/>

<p class="small lead text-center">Déjà sur Biketeam</p>

<div class="row row-cols-1 row-cols-md-4 g-4">
    <#list teams as team>
        <div class="col">
            <div class="card">
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

<#include "_includes_tail.ftlh">