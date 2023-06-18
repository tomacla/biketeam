<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="col-12 border-bottom pb-4">

        <h1 class="my-4 pb-4 border-bottom text-center">Un site unique pour les cyclistes</h1>

        <ul class="mt-4 lead text-center list-unstyled">
            <li>Rassemblez votre communauté</li>
            <li>Partagez vos maps</li>
            <li>Gérez vos rides</li>
            <li>Publiez des actualités</li>
        </ul>

        <div class="text-center">
            <a href="<@common.teamUrl '' '/new' />" class="btn btn-secondary">Créer mon groupe !</a>
        </div>

    </div>

    <div class="col-12">

        <h5 class="text-center lead mt-4">Groupes déjà créés</h5>
        <h6 class="text-center text-muted mb-4"><a class="link-secondary" href="<@common.teamUrl '' '/teams' />">Explorer les groupes</a></h6>

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

    </div>

</div>

<#include "_includes_tail.ftlh">