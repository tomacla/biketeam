<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header text-center">Mot de passe oublié</h5>
          <div class="card-body">

            <p class="lead">Indiquez votre adresse email : vous recevrez un lien permettant de choisir un nouveau mot de passe.</p>

            <form name="form-forgot-password" method="POST" action="<@common.teamUrl '' '/forgot-password' />">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <div class="mb-3">
                    <label for="form-forgot-password-email" class="form-label">Email</label>
                    <input autocomplete="username" name="email" value="${formdata.email}" type="email" class="form-control" id="form-forgot-password-email" required>
                </div>

                <button type="submit" class="btn btn-primary w-100">Envoyer le lien</button>

            </form>

            <div class="mt-3 text-center">
                <a class="small link-secondary" href="<@common.teamUrl '' '/login' />">Retour à la connexion</a>
            </div>

          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">
