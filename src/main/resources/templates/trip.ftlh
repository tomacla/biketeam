<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-5 mb-4">

        <div class="border rounded p-2">
            <div class="d-flex flex-row justify-content-between">
                <h4>${trip.title}</h4>
                <div>
                    <#if _team_admin>
                        <a href="<@common.teamUrl team.id '/admin/trips/${trip.id}' />" class="btn btn-sm btn-secondary"><i class="bi-pencil bi"></i> Editer</a>
                    </#if>
                </div>
            </div>
            <#if trip.publishedStatus != 'PUBLISHED'>
                <div class="p-1 alert alert-warning" role="alert">Ce trip n'est pas publié</div>
            </#if>
            <h5 class="text-muted">Du ${trip.startDate.format(_date_formatter)} au ${trip.endDate.format(_date_formatter)}</h5>
            <ul class="list-unstyled">
                <li>Allure : ${trip.lowerSpeed}/${trip.upperSpeed} km/h</li>
                <li>Lieu de rendez vous : <#if (trip.meetingPoint)??><a class="link-dark" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${trip.meetingPoint.lat?c},${trip.meetingPoint.lng?c}"></#if>${trip.meetingLocation}<#if (trip.meetingPoint)??></a></#if></li>
                <li>Heure de départ : ${trip.meetingTime}</li>
            </ul>
            <#if trip.imaged>
              <img src="<@common.teamUrl team.id '/trips/${trip.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.id} image">
            </#if>
            <p class="mt-4 wrap-content">${trip.description}</p>
        </div>

    </div>

    <div class="col-12 col-md-7">

        <div class="mb-3 py-1 px-2 rounded border">
            <div class="d-flex flex-row align-items-start justify-content-between">
                <div class="d-block pe-2">
                    <#if _authenticated>
                        <#if trip.hasParticipant(_user_id)>
                            <a href="<@common.teamUrl team.id '/trips/${trip.id}/remove-participant' />" class="text-nowrap d-block btn btn-sm btn-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                        <#else>
                            <a tabindex="-1" href="<@common.teamUrl team.id '/trips/${trip.id}/add-participant' />" class="text-nowrap d-block btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                         </#if>
                    <#else>
                            <a href="<@common.teamUrl '' '/oauth2/authorization/strava?requestUri=/${team.id}/trips/${trip.id}/add-participant' />" class="text-nowrap d-block btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                    </#if>
                </div>
                <div class="p-0 m-0 flex-wrap">
                    <#list trip.participants as participant>
                        <#if participant.profileImage??>
                            <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="${participant.profileImage}" alt="${participant.identity}"></div>
                        <#else>
                            <div style="width:30px; height:30px; background:grey;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block rounded"></div>
                        </#if>
                    </#list>
                </div>
            </div>
        </div>

        <#list trip.sortedStages as stage>
            <div class="card mb-2">
                <div class="card-body">
                    <h5 class="card-title">${stage.name}</h5>
                    <h6>${stage.date.format(_date_formatter)}</h6>
                    <#if (stage.mapId)??>
                        <div class="row justify-content-center">
                            <div class="col-12 col-md-6">
                            <div class="w-100 shadow rounded border d-flex flex-column justify-content-center">
                                <#if maps[stage.mapId]??>
                                    <ul class="list-inline mx-auto my-1">
                                        <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${maps[stage.mapId].length}km</li>
                                        <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${maps[stage.mapId].positiveElevation}m</li>
                                        <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${maps[stage.mapId].negativeElevation}m</li>
                                    </ul>
                                </#if>
                                <a href="<@common.teamUrl team.id '/maps/${stage.mapId}' />"><img src="<@common.teamUrl team.id '/maps/${stage.mapId}/image' />" class="d-block w-100 h-auto" alt="${stage.mapId} image"></a>
                                <div class="btn-group" role="group">
                                    <a download="${trip.title}-${stage.name}.gpx" href="<@common.teamUrl team.id '/maps/${stage.mapId}/gpx' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> GPX</a>
                                    <a download="${trip.title}-${stage.name}.fit" href="<@common.teamUrl team.id '/maps/${stage.mapId}/fit' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> FIT</a>
                                    <a href="<@common.teamUrl team.id '/maps/${stage.mapId}' />" class="btn btn-light" role="button"><i class="bi bi-map"></i> Voir</a>
                                </div>
                            </div>
                         </div>
                        </div>
                    <#else>
                        <p class="fst-italic">La map n'a pas été ajoutée.</p>
                    </#if>
                </div>

            </div>
        </#list>

    </div>

</div>

<#include "_includes_tail.ftlh">