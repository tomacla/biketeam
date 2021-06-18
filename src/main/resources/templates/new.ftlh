<#include "_includes_head.ftlh">

<div class="mt-4 row">

    <div class="col-12 col-md-4">
        <h2>Créer une team</h2>
        <ul class="mt-4 lead">
            <li>Hébergez vos maps</li>
            <li>Gérez vos rides</li>
            <li>Rassemblez votre communauté</li>
        </ul>
    </div>

    <div class="col-12 col-md-8">

        <#if _authenticated>
            <form method="POST" action="<@spring.url '/new' />">
                <div class="mb-3 ">
                    <label for="new-team-name" class="form-label">Nom</label>
                    <input autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control" id="new-team-name" placeholder="Ma team">
                </div>
                <div class="mb-3 ">
                    <label for="new-team-description" class="form-label">Description</label>
                    <textarea autocomplete="off" name="description" rows="3" class="form-control" id="new-team-description">${formdata.description}</textarea>
                </div>
                <div class="mb-3 row">
                    <div class="col">
                        <label for="new-team-country" class="form-label">Pays</label>
                        <@common.countrySelect formdata.country 'country' 'new-team-country' />
                    </div>
                    <div class="col">
                        <label for="new-team-city" class="form-label">Ville</label>
                        <input autocomplete="off" required type="text" class="form-control" id="new-team-city" placeholder="Paris" value="${formdata.city}" name="city">
                    </div>
                    <div class="col">
                        <label for="new-team-timezone" class="form-label">Timezone</label>
                        <select required name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                        </select>
                    </div>
                </div>
                <div class="mb-3">
                    <input class="btn btn-secondary" type="submit" value="Créer">
                </div>

            </form>
        <#else>
            <p>Authentifiez vous pour créer une team</p>
            <a href="<@spring.url '/oauth2/authorization/strava' />"><img height="40" src="<@spring.url '/css/btn_strava_connectwith_light.png' />" alt="connect with strava"></a>
        </#if>

     </div>
</div>

<#include "_includes_tail.ftlh">