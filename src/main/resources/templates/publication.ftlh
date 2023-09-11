<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <aside class="col-12 col-md-4">

        <section class="border p-2 mb-2 rounded">
            <div class="row">
               <div class="col-3 col-md-4"><img src="<@common.teamUrl team.id '/image' />" class="d-block w-100 my-auto" alt="Team logo"></div>
               <div class="col-9 col-md-8">
                   <h5 class="mb-0">${team.name}</h5>
                   <div><#if (team.description.facebook)??><span class="small"><a target="_blank" href="https://www.facebook.com/${team.description.facebook}"><i class="bi bi-facebook"></i></a></#if> <#if (team.description.twitter)??><a target="_blank" href="https://www.twitter.com/${team.description.twitter}"><i class="bi bi-twitter"></i></a></#if> <#if (team.description.instagram)??><span class="small"><a target="_blank" href="https://www.instagram.com/${team.description.instagram}"><i class="bi bi-instagram"></i></a></#if></span></div>
                    <div class="mt-2">
                        <#if _authenticated>
                            <#if _team_admin>
                                <button disabled class="px-2 py-0 btn btn-sm btn-success"><i class="bi-bookmark-fill bi"></i> Suivi</button>
                                <button disabled class="px-2 py-0 btn btn-sm btn-success"><i class="bi bi-shield-fill"></i> Admin</button>
                            <#elseif _team_member>
                                <a href="<@common.teamUrl team.id '/leave' />" class="px-2 py-0 btn btn-sm btn-success"><i class="bi-bookmark-fill bi"></i> Suivi</a>
                            <#else>
                                <a href="<@common.teamUrl team.id '/join' />" class="px-2 py-0 btn btn-sm btn-outline-success"><i class="bi-bookmark bi"></i> Suivre</a>
                            </#if>
                        <#else>
                            <a href="<@common.teamUrl team.id '/join' />" class="px-2 py-0 btn btn-sm btn-outline-success"><i class="bi-bookmark bi"></i> Suivre</a>
                        </#if>
                </div>

               </div>
            </div>
             <p class="d-none d-md-block wrap-content mt-3"><em>${team.description.description}</em></p>
            <address class="d-none d-md-block">
                <#if (team.description.addressStreetLine)?? >${team.description.addressStreetLine}<br></#if>
            <#if (team.description.addressPostalCode)?? >${team.description.addressPostalCode}<br></#if>
            <#if (team.description.addressCity)?? >${team.description.addressCity}<br></#if>
            <#if (team.description.phoneNumber)?? ><i class="bi bi-telephone-fill"></i> ${team.description.phoneNumber}<br></#if>
            <#if (team.description.email)?? ><i class="bi bi-envelope"></i> ${team.description.email}<br></#if>
            </address>
             <#if (team.description.other)?? >
                <p class="d-none d-md-block">${team.description.other}</p>
             </#if>
        </section>

        <a class="btn btn-sm btn-secondary" href="<@common.teamUrl team.id '/' />"><i class="bi bi-arrow-left-circle"></i> Retour à l'accueil</a>

    </aside>


    <div class="col-12 col-md-8 pb-4">
            <@common.displayPublication publication false />
    </div>

</div>

<#include "_includes_tail.ftlh">