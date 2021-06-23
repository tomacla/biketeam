<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="d-none d-md-block col-4">

        <div class="border rounded p-2">
            <img class="d-block mt-2 mx-auto rounded" src="${_profile_image}" alt="profile">
            <h5 class="text-center mt-2 pb-2">${user.identity}</h5>

           <div class="text-center my-4">
               <div class="btn-group mx-auto" role="group">
                   <a class="btn btn-sm btn-outline-secondary" href="<@spring.url '/new' />">Créer un groupe</a>
                   <a class="btn btn-sm btn-outline-secondary" href="<@spring.url '/teams' />">Explorer les groupes</a>
               </div>
           </div>

            <#if userTeams?size != 0>
                <div class="list-group">
                    <div class="list-group-item list-group-item-secondary">Mes groupes</div>
                    <#list userTeams as userTeam>
                        <a href="<@spring.url '/${userTeam.id}' />" class="list-group-item list-group-item-action">
                            ${userTeam.name}
                        </a>
                    </#list>
                </div>
            </#if>

        </div>

    </div>

    <div class="col-12 col-md-8">


            <div class="d-block d-md-none my-4 text-center">
                <div class="btn-group mx-auto" role="group">
                   <a class="btn btn-sm btn-outline-secondary" href="<@spring.url '/new' />">Créer un groupe</a>
                   <a class="btn btn-sm btn-outline-secondary" href="<@spring.url '/teams' />">Explorer les groupes</a>
               </div>
            </div>

            <@common.displayFeed true />

    </div>

</div>

<#include "_includes_tail.ftlh">