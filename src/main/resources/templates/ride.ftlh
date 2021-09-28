<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-5 mb-4">

        <div class="border rounded p-2">
            <div class="d-flex flex-row justify-content-between">
                <h4>${ride.title}</h4>
                <div>
                    <#if _team_admin>
                        <a href="<@common.teamUrl team.id '/admin/rides/${ride.id}' />" class="btn btn-sm btn-secondary"><i class="bi-pencil bi"></i> Editer</a>
                    </#if>
                </div>
            </div>
            <#if ride.publishedStatus != 'PUBLISHED'>
                <div class="p-1 alert alert-warning" role="alert">Ce ride n'est pas publié</div>
            </#if>
            <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>
            <#if ride.imaged>
              <img src="<@common.teamUrl team.id '/rides/${ride.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.id} image">
            </#if>
            <p class="mt-4 wrap-content">${ride.description}</p>
        </div>

    </div>

    <div class="col-12 col-md-7">

        <#list ride.sortedGroups as group>
            <div class="card mb-2">
                <div class="card-body">
                    <h5 class="card-title">${group.name}</h5>
                    <h6 class="card-subtitle mb-2 text-muted">Heure de départ : ${group.meetingTime}</h6>
                    <ul class="list-unstyled">
                        <li>Allure : ${group.lowerSpeed}/${group.upperSpeed} km/h</li>
                        <li>Lieu de rendez vous : <#if (group.meetingPoint)??><a class="link-dark" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${group.meetingPoint.lat?c},${group.meetingPoint.lng?c}"></#if>${group.meetingLocation}<#if (group.meetingPoint)??></a></#if></li>
                    </ul>
                    <#if (group.mapId)??>
                        <div class="w-50 shadow rounded border d-flex flex-column justify-content-center">
                            <a href="<@common.teamUrl team.id '/maps/${group.mapId}' />"><img src="<@common.teamUrl team.id '/maps/${group.mapId}/image' />" class="d-block w-100 h-auto" alt="${group.mapId} image"></a>
                            <div class="btn-group" role="group">
                                <a download="${ride.title}-${group.name}.gpx" href="<@common.teamUrl team.id '/maps/${group.mapId}/gpx' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> GPX</a>
                                <a download="${ride.title}-${group.name}.fit" href="<@common.teamUrl team.id '/maps/${group.mapId}/fit' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> FIT</a>
                                <a href="<@common.teamUrl team.id '/maps/${group.mapId}' />" class="btn btn-light" role="button"><i class="bi bi-map"></i> Voir</a>
                            </div>
                        </div>
                    <#else>
                        <p class="fst-italic">La map n'a pas été ajoutée.</p>
                    </#if>
                </div>
                <#if _authenticated>
                    <div class="card-footer">
                        <div class="d-flex justify-content-between">
                            <#if group.hasParticipant(_user_id)>
                                <a href="<@common.teamUrl team.id '/rides/${ride.id}/remove-participant/${group.id}/${_user_id}' />" class="btn btn-sm btn-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                            <#else>
                                <a href="<@common.teamUrl team.id '/rides/${ride.id}/add-participant/${group.id}/${_user_id}' />" class="btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                             </#if>
                            <div class="p-0 m-0" style="height:30px;">
                                <#list group.participants as participant>
                                    <#if participant.profileImage??>
                                        <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="${participant.profileImage}" alt="${participant.identity}"></div>
                                    <#else>
                                        <div style="width:30px; height:30px; background:grey;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block rounded"></div>
                                    </#if>
                                </#list>
                            </div>
                        </div>
                    </div>
                <#else>
                    <div class="card-footer">
                        ${group.participants?size} participant.e<#if group.participants?size gt 1>.s</#if>
                    </div>
                </#if>
            </div>
        </#list>

    </div>

<#include "_includes_tail.ftlh">