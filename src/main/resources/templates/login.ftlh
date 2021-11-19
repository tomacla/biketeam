<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header">Connexion</h5>
          <div class="card-body">
            <p class="lead">Utiliser votre compte Strava pour vous connecter à Biketeam et accéder à toutes les fonctionnalités.</p>
            <a href="<@common.teamUrl '' '/oauth2/authorization/strava?requestUri=${referer}' />"><img height="40" src="<@common.teamUrl '' '/css/btn_strava_connectwith_light.png' />" alt="connect with strava"></a>
          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">