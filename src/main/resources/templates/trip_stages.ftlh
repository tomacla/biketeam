<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "trip_nav.ftlh">

    <div class="col-12">

     <div class="row row-cols-1 row-cols-md-3">

        <#list trip.sortedStages as stage>
            <div class="col mb-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">${stage.name}</h5>
                        <h6>${stage.date.format(_date_formatter)}</h6>
                        <#if (stage.map)??>
                            <div class="row justify-content-center">
                                <div class="col-12">
                                <div class="w-100 shadow rounded border d-flex flex-column justify-content-center">
                                    <#if stage.map??>
                                        <ul class="list-inline mx-auto my-1">
                                            <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${stage.map.length}km</li>
                                            <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${stage.map.positiveElevation}m</li>
                                            <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${stage.map.negativeElevation}m</li>
                                        </ul>
                                    </#if>
                                    <a href="<@common.teamUrl team.id '/maps/${stage.map.permalink!stage.map.id}' />"><img src="<@common.teamUrl team.id '/maps/${stage.map.id}/image' />" class="d-block w-100 h-auto" alt="${stage.map.id} image"></a>
                                    <div class="btn-group" role="group">
                                        <a download="${trip.title}-${stage.name}.gpx" href="<@common.teamUrl team.id '/maps/${stage.map.id}/gpx' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> GPX</a>
                                        <a download="${trip.title}-${stage.name}.fit" href="<@common.teamUrl team.id '/maps/${stage.map.id}/fit' />" class="btn btn-light" role="button"><i class="bi bi-download"></i> FIT</a>
                                        <a href="<@common.teamUrl team.id '/maps/${stage.map.permalink!stage.map.id}' />" class="btn btn-light" role="button"><i class="bi bi-map"></i> Voir</a>
                                    </div>
                                </div>
                             </div>
                            </div>
                        <#else>
                            <p class="fst-italic">La map n'a pas été ajoutée.</p>
                        </#if>
                    </div>

                </div>
                </div>
            </#list>

        </div>

    </div>





</div>

<#include "_includes_tail.ftlh">