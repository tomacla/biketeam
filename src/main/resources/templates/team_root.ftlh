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

    <div class="col-12 col-md-8">

        <div class="row pb-4 g-4">
            <#list feed as feedItem>
                  <div class="col-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="m-0 p-0">
                            <#if feedItem.type == 'RIDE'>
                                <i class="bi bi-bicycle"></i>
                            <#elseif feedItem.type == 'PUBLICATION'>
                                <i class="bi bi-newspaper"></i>
                            </#if>
                            <#if feedItem.type == 'RIDE'>
                                Ride
                            <#elseif feedItem.type == 'PUBLICATION'>
                                Publication
                            </#if>
                            </h5>
                            <small class="text-muted">Ajouté le ${feedItem.publishedAt.format(_date_formatter)}</small>
                        </div>
                        <div class="card-body">
                        <#if (feedItem.getDetailsUrl(team.id))??>
                            <h5 class="card-title"><a class="link-dark" href="<@spring.url feedItem.getDetailsUrl(team.id) />">${feedItem.title}</a></h5>
                        <#else>
                            <h5 class="card-title">${feedItem.title}</h5>
                        </#if>
                          <p class="card-text wrap-content">${feedItem.content}</p>
                          <#if (feedItem.getImageUrl(team.id))??>
                            <img src="<@spring.url feedItem.getImageUrl(team.id) />" class="d-block shadow rounded w-50 h-auto mx-auto" alt="${feedItem.title} image">
                          </#if>
                        </div>
                        <#if (feedItem.getDetailsUrl(team.id))??>
                        <div class="card-footer text-center">
                            <a href="<@spring.url feedItem.getDetailsUrl(team.id) />" class="btn btn-secondary btn-sm" role="button">Voir</a>
                        </div>
                        </#if>
                    </div>
                  </div>
            </#list>
        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">