<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <aside class="col-12 col-md-4 order-2 order-md-1">

        <section class="border p-2 mb-3 rounded">
            <div class="d-flex flex-row justify-content-between mb-3">
                <h5>${team.name} <#if (team.description.facebook)??><span class="small ps-2"><a target="_blank" href="https://www.facebook.com/${team.description.facebook}"><i class="bi bi-facebook"></i></a></#if> <#if (team.description.twitter)??><a target="_blank" href="https://www.twitter.com/${team.description.twitter}"><i class="bi bi-twitter"></i></a></#if></span></h5>
                <div>
                    <#if _authenticated>
                        <#if _team_admin>
                            <button disabled class="btn btn-sm btn-success"><i class="bi-shield-fill bi"></i> Admin</button>
                        <#elseif _team_member>
                            <a href="<@common.teamUrl team.id '/leave' />" class="btn btn-sm btn-success"><i class="bi-person-fill bi"></i> Membre</a>
                        <#else>
                            <a href="<@common.teamUrl team.id '/join' />" class="btn btn-sm btn-outline-success"><i class="bi-person-plus-fill bi"></i> Rejoindre</a>
                        </#if>
                    <#else>
                        <a href="<@common.teamUrl '' '/oauth2/authorization/strava?requestUri=/${team.id}/join' />" class="btn btn-sm btn-outline-success"><i class="bi-person-plus-fill bi"></i> Rejoindre</a>
                    </#if>
                </div>
            </div>
            <div class="row align-items-center mb-3">
               <div class="col-4"><img src="<@common.teamUrl team.id '/image' />" class="d-block w-100 my-auto" alt="Team logo"></div>
               <div class="col-8">
                <address>
                   <#if (team.description.addressStreetLine)?? >${team.description.addressStreetLine}<br></#if>
                   <#if (team.description.addressPostalCode)?? >${team.description.addressPostalCode}<br></#if>
                   <#if (team.description.addressCity)?? >${team.description.addressCity}<br></#if>
                   <#if (team.description.phoneNumber)?? ><i class="bi bi-telephone-fill"></i> ${team.description.phoneNumber}<br></#if>
                   <#if (team.description.email)?? ><i class="bi bi-envelope"></i> ${team.description.email}<br></#if>
               </address>
               </div>
            </div>
             <p class="wrap-content"><em>${team.description.description}</em></p>
             <#if (team.description.other)?? >
                <p>${team.description.other}</p>
             </#if>
        </section>

        <section class="border p-2 mb-3 rounded">
            <h5>Membres (${users?size})</h5>
            <#list users as user>
                <#if user.profileImage??>
                    <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${user.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="${user.profileImage}" alt="${user.identity}"></div>
                <#else>
                    <div style="width:30px; height:30px; background:grey;" data-bs-toggle="tooltip" data-bs-placement="top" title="${user.identity}" class="d-inline-block rounded"></div>
                </#if>
            </#list>
        </section>

        <#if hasHeatmap>
            <section class="border p-2 mb-3 rounded">
                <h5>Heatmap</h5>
                <a href="<@common.teamUrl team.id '/heatmap' />" target="_blank"><img src="<@common.teamUrl team.id '/heatmap?width=500' />" class="d-block w-100 my-auto" alt="Heatmap"></a>
            </section>
        </#if>

    </aside>


    <div class="col-12 col-md-8 pb-4 order-1 order-md-2">
            <@common.displayFeed false />
    </div>


</div>

<#include "_includes_tail.ftlh">