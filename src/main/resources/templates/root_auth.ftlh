<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="d-none d-md-block col-4">

        <div class="border rounded p-2">
                <img class="d-block mt-2 mx-auto rounded" src="<@common.teamUrl '' '/users/${_user.id}/image' />" alt="profile">
            <h5 class="text-center mt-2 pb-2">${user.identity}</h5>

           <div class="text-center my-4">
               <div class="btn-group mx-auto" role="group">
                   <a class="btn btn-sm btn-outline-secondary" href="<@common.teamUrl '' '/new' />"><i class="bi bi-plus-circle"></i> Créer un groupe</a>
                   <a class="btn btn-sm btn-outline-secondary" href="<@common.teamUrl '' '/teams' />"><i class="bi bi-search"></i> Explorer les groupes</a>
               </div>
           </div>

            <#if userTeams?size != 0>
                <div class="list-group">
                    <div class="list-group-item list-group-item-secondary">Mes groupes</div>
                    <#list userTeams as userTeam>
                        <a href="<@common.teamUrl userTeam.id '' />" class="list-group-item list-group-item-action">
                            <img src="<@common.teamUrl userTeam.id '/image' />" height="18" alt="Team image"> ${userTeam.name}
                        </a>
                    </#list>
                </div>
            <#else>
                <p class="text-muted">Vous n'avez rejoint aucun groupe.</p>
            </#if>

        </div>

    </div>

    <div class="col-12 col-md-8">


            <div class="d-block d-md-none my-4 text-center">
                <div class="btn-group mx-auto" role="group">
                   <a class="btn btn-sm btn-outline-secondary" href="<@common.teamUrl '' '/new' />">Créer un groupe</a>
                   <a class="btn btn-sm btn-outline-secondary" href="<@common.teamUrl '' '/teams' />">Explorer les groupes</a>
               </div>
            </div>

            <@common.displayFeed true />

    </div>

</div>

<#include "_includes_tail.ftlh">