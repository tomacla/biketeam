<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <aside class="col-12 col-md-4">

        <section class="border p-2 mb-4 rounded">
            <div class="d-flex flex-row justify-content-between">
                <h5>${team.name}</h5>
                <div>
                    <#if _authenticated>
                        <#if _team_admin>
                            <button disabled class="btn btn-sm btn-success"><i class="bi-shield-fill bi"></i> Admin</button>
                        <#elseif _team_member>
                            <a href="<@spring.url '/' + team.id + '/leave/' + _user_id />" class="btn btn-sm btn-success"><i class="bi-person-fill bi"></i> Membre</a>
                        <#else>
                            <a href="<@spring.url '/' + team.id + '/join/' + _user_id />" class="btn btn-sm btn-outline-success"><i class="bi-person-plus-fill bi"></i> Rejoindre</a>
                        </#if>
                    <#else>
                        <a href="<@spring.url '/oauth2/authorization/strava' />" class="btn btn-sm btn-outline-success"><i class="bi-person-plus-fill bi"></i> Rejoindre</a>
                    </#if>
                </div>
            </div>
            <img src="<@spring.url '/api/' + team.id + '/image' />" class="d-block w-50 h-auto mx-auto my-4" alt="Team logo">
            <p class="lead">
              ${team.description.description}
            </p>
            <p class="lead">
                <#if (team.description.facebook)??>
                    <a target="_blank" href="https://www.facebook.com/${team.description.facebook}"><i class="bi bi-facebook"></i></a>
                </#if>
                <#if (team.description.twitter)??>
                    <a target="_blank" href="https://www.twitter.com/${team.description.twitter}"><i class="bi bi-twitter"></i></a>
                </#if>
            </p>
            <address>
                <#if (team.description.addressStreetLine)?? >${team.description.addressStreetLine}<br></#if>
                <#if (team.description.addressPostalCode)?? >${team.description.addressPostalCode}<br></#if>
                <#if (team.description.addressCity)?? >${team.description.addressCity}<br></#if>
                <#if (team.description.phoneNumber)?? ><i class="bi bi-telephone-fill"></i> ${team.description.phoneNumber}<br></#if>
                <#if (team.description.email)?? ><i class="bi bi-envelope"></i> ${team.description.email}<br></#if>
            </address>
             <#if (team.description.other)?? >
                <p>${team.description.other}</p>
             </#if>
        </section>

    </aside>

    <div class="col-12 col-md-8 pb-4">
            <@common.displayFeed false />
    </div>

</div>

<#include "_includes_tail.ftlh">