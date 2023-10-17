<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">

        <h2 class="border-bottom pb-2 mb-4">Créer un groupe</h2>

        <form method="POST" action="<@common.teamUrl '' '/new' />">
            <div class="mb-3 ">
                <label for="new-team-name" class="form-label">Nom</label>
                <input data-element="teams" data-target-id-field="new-team-id" minLength="2" autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control form-unique-id" id="new-team-name" placeholder="Groupe de vélo">
                <div id="new-team-name-help" class="form-text">Le nom d'usage du groupe.</div>
            </div>
            <div class="mb-3 ">
                <label for="new-team-id" class="form-label">Identifiant unique</label>
                <input data-element="teams" data-target-id-field="new-team-id" minLength="2" maxLength="19" autocomplete="off" required value="${formdata.id}" name="id" type="text" class="form-control form-unique-id" id="new-team-id" placeholder="ma_team">
                <div id="new-team-name-help" class="form-text">Non modifiable une fois que le groupe est créé</div>
            </div>
            <div class="mb-3 ">
                <label for="new-team-description" class="form-label">Description</label>
                <textarea required autocomplete="off" name="description" rows="3" class="form-control" id="new-team-description">${formdata.description}</textarea>
                <div id="new-team-description-help" class="form-text">Le groupe, en quelques mots</div>
            </div>
            <div class="row">
                <div class="col-12 col-md-4 mb-3 ">
                    <label for="new-team-country" class="form-label">Pays</label>
                    <@common.countrySelect formdata.country 'country' 'new-team-country' true />
                </div>
                <div class="col-12 col-md-4 mb-3">
                    <label for="new-team-city" class="form-label">Ville</label>
                    <input autocomplete="off" required type="text" class="form-control" id="new-team-city" placeholder="Paris" value="${formdata.city}" name="city">
                </div>
                <div class="col-12 col-md-4 mb-3">
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

    </div>

</div>

<#include "_includes_tail.ftlh">