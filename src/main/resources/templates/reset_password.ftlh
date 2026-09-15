<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header text-center">Nouveau mot de passe</h5>
          <div class="card-body">

            <form name="form-reset-password" method="POST" action="<@common.teamUrl '' '/reset-password' />">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <input type="hidden" name="code" value="${formdata.code}"/>

                <div class="mb-3">
                    <label for="form-reset-password-password" class="form-label">Mot de passe</label>
                    <input autocomplete="new-password" name="password" type="password" class="form-control" id="form-reset-password-password" required>
                    <div class="form-text">Le mot de passe doit contenir au moins 10 caractères.</div>
                </div>

                <div class="mb-3">
                    <label for="form-reset-password-confirm" class="form-label">Confirmation du mot de passe</label>
                    <input autocomplete="new-password" name="passwordConfirm" type="password" class="form-control" id="form-reset-password-confirm" required>
                </div>

                <button type="submit" class="btn btn-primary w-100">Enregistrer le mot de passe</button>

            </form>

            <div class="mt-3 text-center">
                <a class="small link-secondary" href="<@common.teamUrl '' '/forgot-password' />">Demander un nouveau lien</a>
            </div>

          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">
