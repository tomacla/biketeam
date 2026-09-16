<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header text-center">Créer un compte</h5>
          <div class="card-body">

            <form name="form-register" method="POST" action="<@common.teamUrl '' '/register' />">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <div class="row">
                    <div class="col-12 col-md-6 mb-3">
                        <label for="form-register-firstname" class="form-label">Prénom</label>
                        <input autocomplete="given-name" name="firstName" value="${formdata.firstName}" type="text" class="form-control" id="form-register-firstname" required>
                    </div>
                    <div class="col-12 col-md-6 mb-3">
                        <label for="form-register-lastname" class="form-label">Nom</label>
                        <input autocomplete="family-name" name="lastName" value="${formdata.lastName}" type="text" class="form-control" id="form-register-lastname" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="form-register-email" class="form-label">Email</label>
                    <input autocomplete="username" name="email" value="${formdata.email}" type="email" class="form-control" id="form-register-email" required>
                </div>

                <div class="mb-3">
                    <label for="form-register-password" class="form-label">Mot de passe</label>
                    <input autocomplete="new-password" name="password" type="password" class="form-control" id="form-register-password" required>
                    <div class="form-text">Le mot de passe doit contenir au moins 10 caractères.</div>
                </div>

                <div class="mb-3">
                    <label for="form-register-password-confirm" class="form-label">Confirmation du mot de passe</label>
                    <input autocomplete="new-password" name="passwordConfirm" type="password" class="form-control" id="form-register-password-confirm" required>
                </div>

                <button type="submit" class="btn btn-primary w-100">Créer mon compte</button>

            </form>

            <div class="mt-3 text-center">
                <a class="small link-secondary" href="<@common.teamUrl '' '/login' />">J'ai déjà un compte</a>
            </div>

          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">
