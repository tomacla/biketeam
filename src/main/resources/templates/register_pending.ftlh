<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header text-center">Confirmez votre adresse email</h5>
          <div class="card-body text-center">

            <p class="lead">Un email vient d'être envoyé à ${pendingEmail}. Cliquez sur le lien qu'il contient pour activer votre compte.</p>

            <form name="form-register-resend" method="POST" action="<@common.teamUrl '' '/register/resend' />">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <button type="submit" class="btn btn-secondary">Renvoyer le lien</button>
            </form>

            <div class="mt-3">
                <a class="small link-secondary" href="<@common.teamUrl '' '/login' />">Retour à la connexion</a>
            </div>

          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">
