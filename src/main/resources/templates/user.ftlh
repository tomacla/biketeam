<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 col-md-5 mb-4">

        <div class="border rounded p-2">
            <img class="rounded" src="${user.profileImage}" alt="profile">
            <h5>${(user.firstName)!'inconnu'} ${(user.lastName)!'inconnu'}</h5>
            <dl>
                <#if user.stravaId??>
                    <dt>Strava</dt>
                    <dd><a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></dd>
                </#if>
                <#if user.city??>
                    <dt>Ville</dt>
                    <dd>${user.city}</dd>
                </#if>
            </dl>
            <hr/>
            <div class="alert alert-info" role="alert">
               Pour recevoir les nouveaux rides et publications de vos groupes par email, saisissez votre adresse ci dessous.
             </div>
            <form name="form-user" action="<@spring.url '/users/me' />" method="POST">
                <div class="mb-3">
                    <label for="form-user-email" class="form-label">Email</label>
                    <input autocomplete="off" name="email" value="${user.email!''}" type="email" class="form-control" id="form-user-email">
                  </div>
                  <button type="submit" class="btn btn-secondary">Enregistrer</button>
            </form>
        </div>

    </div>

    <div class="col-12 col-md-7">

        <#if user.rideGroups?? && user.rideGroups?size != 0>
            <ul class="list-group">
                <div class="list-group-item list-group-item-secondary">Mes rides</div>
                <#list user.rideGroups as group>
                    <li class="list-group-item"><img src="<@spring.url '/api/' + group.ride.teamId + '/image' />" height="20" alt="Team image"> <a class="link-dark" href="<@spring.url '/${group.ride.teamId}/rides/${group.ride.id}' />">${group.ride.date.format(_date_formatter)} - ${group.ride.title} - ${group.name}</a></li>
                </#list>
            </ul>
        <#else>
            Cet utilisateur n'a participé à aucune ride.
        </#if>



    </div>

<#include "_includes_tail.ftlh">