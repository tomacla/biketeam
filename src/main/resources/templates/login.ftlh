<#include "_includes_head.ftlh">

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card text-center">
          <h5 class="card-header">Connexion</h5>
          <div class="card-body">
            <p class="lead">Utiliser votre compte Strava, Facebook ou Google pour vous connecter à Biketeam et accéder à toutes les fonctionnalités.</p>
            <a class="mx-auto mb-3 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#fb5304; border:1px solid #fb5304; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/strava?requestUri=${referer}' />" role="button">
                <img src="<@common.teamUrl '' '/css/stravalogo.jpg' />" width="25" alt="Strava logo">
                <span>Connexion avec Strava</span>
            </a>
            <a class="mx-auto mb-3 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#565656; border:1px solid #565656; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/google?requestUri=${referer}' />" role="button">
                <img src="<@common.teamUrl '' '/css/googlelogo.png' />" width="25" alt="Google logo">
                <span>Connexion avec Google</span>
            </a>
            <a class="mx-auto mb-3 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#1877F2; border:1px solid #1877F2; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/facebook?requestUri=${referer}' />" role="button">
                <img src="<@common.teamUrl '' '/css/facebooklogo.png' />" width="25" alt="Facebook logo">
                <span>Connexion avec Facebook</span>
            </a>
          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">