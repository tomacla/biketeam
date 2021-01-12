<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-5 mb-4">

        <div class="border rounded p-2">
            <h4>${ride.title}</h4>
            <h5 class="text-muted">${ride.date.format(_date_formatter)}</h5>
            <#if ride.imaged>
              <img src="<@spring.url '/api/rides/${ride.id}/image' />" class="d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.id} image">
            </#if>
            <p class="mt-4"><@common.displayContent ride.description /></p>
        </div>

    </div>

    <div class="col-12 col-md-7">

        <#list ride.groups as group>
            <div class="card mb-2">
                <div class="card-body">
                    <h5 class="card-title">${group.name}</h5>
                    <h6 class="card-subtitle mb-2 text-muted">Heure de départ : ${group.meetingTime}</h6>
                    <ul class="list-unstyled">
                        <li>Allure : ${group.lowerSpeed}/${group.upperSpeed} km/h</li>
                        <li>Lieu de rendez vous : <#if (group.meetingPoint)??><a class="text-reset" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${group.meetingPoint.lat?c},${group.meetingPoint.lng?c}"></#if>${group.meetingLocation}<#if (group.meetingPoint)??></a></#if></li>
                    </ul>
                    <#if (group.mapId)??>
                        <div class="btn-group" role="group">
                            <a download="${ride.title}-${group.name}.gpx" href="<@spring.url '/api/maps/${group.mapId}/gpx' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> GPX</a>
                            <a href="<@spring.url '/maps/${group.mapId}' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-map"></i> Web</a>
                        </div>
                    <#else>
                        <p class="fst-italic">La map n'a pas été ajoutée.</p>
                    </#if>
                </div>
                <#if _authenticated>
                    <div class="card-footer">
                        <div class="d-flex justify-content-between">
                            <#if group.hasParticipant(_user_id)>
                                <a href="<@spring.url '/rides/${ride.id}/remove-participant/${group.id}/${_user_id}' />" class="btn btn-sm btn-outline-secondary"><i class="bi-emoji-frown-fill bi"></i> Je n'y vais pas</a>
                            <#else>
                                <a href="<@spring.url '/rides/${ride.id}/add-participant/${group.id}/${_user_id}' />" class="btn btn-sm btn-outline-secondary"><i class="bi-emoji-laughing-fill bi"></i> J'y vais</a>
                             </#if>
                            <div class="d-flex">
                                <#list group.participants as participant>
                                    <img src="${participant.profileImage}" alt="${participant.identity}" width="30" height="30" class="d-block rounded">
                                </#list>
                            </div>
                        </div>
                    </div>
                </#if>
            </div>
        </#list>

    </div>

<#include "_includes_tail.ftlh">