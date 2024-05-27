<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="d-none d-md-block col-4">

        <div class="border rounded p-2">

            <img class="d-block mt-2 mx-auto rounded" src="<@common.teamUrl '' '/users/${_user.id}/image' />" alt="profile">
            <h5 class="text-center mt-2">${_user.identity}</h5>
            <div class="d-flex flex-row justify-content-center"><a class="link-secondary me-2" href="<@common.teamUrl '' '/users/me' />">Mon compte</a><a class="link-secondary" href="<@common.teamUrl '' '/users/space' />">Mon espace</a></div>

            <div class="list-group my-4">
                <a class="list-group-item" href="<@common.teamUrl '' '/new' />"><i class="bi bi-plus-circle"></i> Créer un groupe</a>
                <a class="list-group-item" href="<@common.teamUrl '' '/teams' />"><i class="bi bi-search"></i> Explorer les groupes</a>
                <a class="list-group-item" href="<@common.teamUrl '' '/catalog' />"><i class="bi bi bi-book"></i> Catalogue</a>
                <a class="list-group-item" href="<@common.teamUrl '' '/gpxtool' />"><i class="bi bi-eye-fill"></i> Outils GPX</a>
            </div>

            <#if _user_teams?size != 0>
                <div class="list-group">
                    <div class="list-group-item list-group-item-secondary"><i class="bi bi-bookmarks"></i> Groupes suivis</div>
                    <#list _user_teams as userTeam>
                        <a href="<@common.teamUrl userTeam.id '' />" class="list-group-item list-group-item-action">
                            <img src="<@common.teamUrl userTeam.id '/image' />" height="18" alt="Team image"> ${userTeam.name}
                        </a>
                    </#list>
                </div>
            </#if>

        </div>

    </div>

    <div class="col-12 col-md-8">

            <div class="d-flex d-row justify-content-center d-md-none mb-2">

                <div class="btn-group me-2">
                  <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-list"></i>
                  </button>

                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/users/me' />">Mon compte</a></li>
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/users/space' />">Mon espace</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/maps' />">Maps</a></li>
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/gpxtool' />">Outils GPX</a></li>
                  </ul>
                </div>

                <div class="btn-group me-2">
                  <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                    Groupes
                  </button>

                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/new' />"><i class="bi bi-plus-circle"></i> Créer</a></li>
                    <li><a class="dropdown-item" href="<@common.teamUrl '' '/teams' />"><i class="bi bi-search"></i> Explorer</a></li>
                    <#if _user_teams?size != 0>
                        <li><hr class="dropdown-divider"></li>
                        <#list _user_teams as userTeam>
                            <li><a href="<@common.teamUrl userTeam.id '' />" class="dropdown-item">
                                <img src="<@common.teamUrl userTeam.id '/image' />" height="18" alt="Team image"> ${userTeam.name}
                            </a></li>
                        </#list>
                    </#if>
                  </ul>
                </div>


            </div>

            <@common.displayFeed true />

    </div>

</div>

<#include "_includes_tail.ftlh">