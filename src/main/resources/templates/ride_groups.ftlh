<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "ride_nav.ftlh">

    <div class="col-12">

     <div class="row row-cols-1 row-cols-md-3">

        <#list ride.sortedGroups as group>
        <div class="col mb-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">${group.name}</h5>
                    <h6 class="card-subtitle mb-2 text-muted">Heure de départ : ${group.meetingTime}</h6>
                    <ul class="list-unstyled">
                        <li>Allure : ${group.lowerSpeed}/${group.upperSpeed} km/h</li>
                        <li>Lieu de rendez vous : <#if (group.meetingPoint)??><a class="link-dark" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${group.meetingPoint.lat?c},${group.meetingPoint.lng?c}"></#if>${group.meetingLocation}<#if (group.meetingPoint)??></a></#if></li>
                    </ul>
                    <#if (group.map)??>
                        <div class="row justify-content-center">
                            <div class="col-12">
                            <div class="w-100 shadow rounded border d-flex flex-column justify-content-center">
                                <#if group.map??>
                                    <ul class="list-inline mx-auto my-1">
                                        <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${group.map.length}km</li>
                                        <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${group.map.positiveElevation}m</li>
                                        <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${group.map.negativeElevation}m</li>
                                    </ul>
                                </#if>
                                <a href="<@common.teamUrl team.id '/maps/${group.map.permalink!group.map.id}' />"><img src="<@common.teamUrl team.id '/maps/${group.map.id}/image' />" class="d-block w-100 h-auto" alt="${group.map.id} image"></a>
                                <div class="btn-group" role="group">
                                    <a download="${ride.title}-${group.name}.gpx" href="<@common.teamUrl team.id '/maps/${group.map.id}/gpx' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> GPX</a>
                                    <a download="${ride.title}-${group.name}.fit" href="<@common.teamUrl team.id '/maps/${group.map.id}/fit' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> FIT</a>
                                    <a href="<@common.teamUrl team.id '/maps/${group.map.permalink!group.map.id}' />" class="btn btn-light" role="button"><i class="bi bi-map"></i> Voir</a>
                                </div>
                            </div>
                         </div>
                        </div>
                    <#else>
                        <p class="fst-italic">La map n'a pas été ajoutée.</p>
                    </#if>
                </div>

                    <div class="card-footer">
                        <div class="d-flex flex-row align-items-start justify-content-between">
                            <div class="d-block pe-2">
                                <#if _authenticated>
                                    <#if group.hasParticipant(_user.id)>
                                        <a href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/remove-participant/${group.id}' />" class="text-nowrap d-block btn btn-sm btn-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                                    <#else>
                                        <a tabindex="-1" href="<@common.teamUrl team.id '/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />" class="text-nowrap d-block btn btn-sm btn-outline-success <#if ride.isParticipantInAnotherGroup(group.id, _user.id)>disabled</#if>"><i class="bi-bicycle bi"></i> J'y vais</a>
                                     </#if>
                                <#else>
                                        <a href="<@common.teamUrl '' '/login?requestUri=/${team.id}/rides/${ride.permalink!ride.id}/add-participant/${group.id}' />" class="text-nowrap d-block btn btn-sm btn-outline-success"><i class="bi-bicycle bi"></i> J'y vais</a>
                                </#if>
                            </div>
                            <div class="p-0 m-0 flex-wrap">
                                <#list group.sortedParticipants as participant>
                                    <#if participant.profileImage??>
                                        <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="${participant.profileImage}" alt="${participant.identity}"></div>
                                    <#else>
                                        <div style="width:30px; height:30px; background:grey;" data-bs-toggle="tooltip" data-bs-placement="top" title="${participant.identity}" class="d-inline-block rounded"></div>
                                    </#if>
                                </#list>
                            </div>
                        </div>
                    </div>
            </div>
            </div>
        </#list>

        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">