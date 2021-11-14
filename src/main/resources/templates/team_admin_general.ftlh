<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-general" action="<@common.teamUrl team.id '/admin' />" method="POST" class="border rounded p-4">

            <div class="mb-3">
              <label for="form-general-name" class="form-label">Nom</label>
              <input autocomplete="off" name="name" value="${formdata.name}" type="text" class="form-control" id="form-general-name" placeholder="Ma team">
            </div>
            <div class="mb-3">
              <label for="form-general-description" class="form-label">Description</label>
              <textarea required name="description" class="form-control" id="form-general-description" rows="3">${formdata.description}</textarea>
            </div>
             <div class="mb-3">
                <label for="form-general-visibility" class="form-label">Visibilité</label>
                <select name="visibility" class="form-select" id="form-general-visibility">
                    <option<#if team.visibility == 'PUBLIC'> selected</#if> value="PUBLIC">Public</option>
                    <option<#if team.visibility == 'PUBLIC_UNLISTED'> selected</#if> value="PUBLIC_UNLISTED">Public (non listé)</option>
                    <option<#if team.visibility == 'PRIVATE'> selected</#if> value="PRIVATE">Privé</option>
                    <option<#if team.visibility == 'PRIVATE_UNLISTED'> selected</#if> value="PRIVATE_UNLISTED">Privé (non listé)</option>
                </select>
            </div>

            <button type="submit" class="btn btn-secondary">Enregistrer</button>

        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">