<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <aside class="col-12 col-md-4">

        <section class="border p-2 mb-3 rounded">
            <div class="row mb-3">
               <div class="col-4"><img src="<@common.teamUrl team.id '/image' />" class="d-block w-100 my-auto" alt="Team logo"></div>
               <div class="col-8">
                   <h5 class="mb-0">${team.name}</h5>
                   <div><#if (team.description.facebook)??><span class="small"><a target="_blank" href="https://www.facebook.com/${team.description.facebook}"><i class="bi bi-facebook"></i></a></#if> <#if (team.description.twitter)??><a target="_blank" href="https://www.twitter.com/${team.description.twitter}"><i class="bi bi-twitter"></i></a></#if> <#if (team.description.instagram)??><span class="small"><a target="_blank" href="https://www.instagram.com/${team.description.instagram}"><i class="bi bi-instagram"></i></a></#if></span></div>
                    <div class="mt-2">
                        <#if _authenticated>
                                <#if _team_admin>
                                <button disabled class="btn btn-sm btn-success"><i class="bi-bookmark-check-fill bi"></i> Admin</button>
                                <#elseif _team_member>
                                <a href="<@common.teamUrl team.id '/leave' />" class="btn btn-sm btn-success"><i class="bi-bookmark-fill bi"></i> Suivi</a>
                                <#else>
                                <a href="<@common.teamUrl team.id '/join' />" class="btn btn-sm btn-outline-success"><i class="bi-bookmark bi"></i> Suivre</a>
                            </#if>
                            <#else>
                            <a href="<@common.teamUrl '' '/login?requestUri=/${team.id}/join' />" class="btn btn-sm btn-outline-success"><i class="bi-bookmark bi"></i> Suivre</a>
                        </#if>
                </div>

               </div>
            </div>
             <p class="d-none d-md-block wrap-content"><em>${team.description.description}</em></p>
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

        <form id="feed-filter" action="<@common.teamUrl team.id '' />" method="GET" class="border rounded px-2 pt-2 mb-3">
            <div class="mb-3">
                <div class="d-flex flex-row mb-2 justify-content-center">
                    <div class="pe-1"><label for="feed-filter-from" class="form-label small">Du</label></div>
                    <div><input autocomplete="off" type="date" name="from" value="${formdata.from}" required class="form-control form-control-sm" id="feed-filter-from"></div>
                    <div class="px-1"><label for="feed-filter-to" class="form-label small">Au</label></div>
                    <div><input autocomplete="off" type="date" name="to" value="${formdata.to}" required class="form-control form-control-sm" id="feed-filter-to"></div>
                </div>
                <div class="d-flex flex-row mb-2 justify-content-center">
                        <div class="me-1"><button tabindex="-1" role="button" aria-disabled="true" onclick="toggleVisibleButton('publications')" id="input-visible-publications-button" type="button" class="btn btn-sm <#if formdata.includePublications>btn-success<#else>btn-outline-success</#if>"><i class="bi bi-newspaper"></i> Publications</button></div>
                        <div class="me-1"><button tabindex="-1" role="button" aria-disabled="true" onclick="toggleVisibleButton('rides')" id="input-visible-rides-button" type="button" class="btn btn-sm <#if formdata.includeRides>btn-success<#else>btn-outline-success</#if>"><i class="bi bi-bicycle"></i> Rides</button></div>
                          <div class="me-1"><button tabindex="-1" role="button" aria-disabled="true" onclick="toggleVisibleButton('trips')" id="input-visible-trips-button" type="button" class="btn btn-sm <#if formdata.includeTrips>btn-success<#else>btn-outline-success</#if>"><i class="bi bi-signpost-2"></i> Trips</button></div>
                </div>
                <div class="d-flex flex-row justify-content-center">
                    <button type="submit" class="btn btn-secondary btn-sm"><i class="bi bi-search"></i> Rechercher</button>
                </div>
            </div>

            <input type="hidden" id="input-visible-publications-field" value="${formdata.includePublications?c}" name="includePublications" />
            <input type="hidden" id="input-visible-trips-field" value="${formdata.includeTrips?c}" name="includeTrips" />
            <input type="hidden" id="input-visible-rides-field" value="${formdata.includeRides?c}" name="includeRides" />

        </form>

        <#if hasHeatmap>
            <section class="border d-none d-md-block p-2 mb-3 rounded">
                <h5>Heatmap</h5>
                <a href="<@common.teamUrl team.id '/heatmap' />" target="_blank"><img src="<@common.teamUrl team.id '/heatmap?width=500' />" class="d-block w-100 my-auto" alt="Heatmap"></a>
            </section>
        </#if>

    </aside>


    <div class="col-12 col-md-8 pb-4">
            <@common.displayFeed false />
    </div>

    <script type="text/javascript">

        function toggleVisibleButton(id) {

            var fieldId = 'input-visible-'+id+'-field';
            var buttonId = 'input-visible-'+id+'-button';

            var field = document.getElementById(fieldId);
            var button = document.getElementById(buttonId);

            if(field.value === 'true') {
                field.value = "false";
                button.classList.remove('btn-success');
                button.classList.add('btn-outline-success');
            } else {
                field.value = "true";
                button.classList.remove('btn-outline-success');
                button.classList.add('btn-success');
            }


        }

    </script>

</div>

<#include "_includes_tail.ftlh">