<#-- Protection anti-robot des formulaires publics envoyant un mail (voir BotProtectionService).
     A inclure dans le <form>, le modele doit fournir formStamp. -->

<#-- champ piege : masque hors ecran par la feuille de style (classe au nom neutre), pour que
     rien dans le HTML ne le designe comme cache -->
<div class="form-extra" aria-hidden="true">
    <label for="form-website">Site web</label>
    <input type="text" name="website" id="form-website" value="" tabindex="-1" autocomplete="off">
</div>

<input type="hidden" name="formStamp" value="${formStamp}">

<div class="mb-3">
    <altcha-widget challenge="<@common.teamUrl '' '/forms/token' />" name="altcha" language="fr-fr" auto="onfocus"></altcha-widget>
</div>

<script async type="module" src="https://cdn.jsdelivr.net/npm/altcha@3.2.2/dist/main/altcha.i18n.min.js" integrity="sha384-LhAQD7NwHPx45a88iHXAnkwJwi9W5XraCKK9FMGd0gfHVPHJTLDI3Jhx/JSHXhO+" crossorigin="anonymous"></script>
