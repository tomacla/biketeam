<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-publication"
            action="<@common.teamUrl team.id '/admin/places/' + formdata.id />"
            method="POST"
            class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-place-name" class="form-label">Nom</label>
                  <input autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control" id="form-place-name" placeholder="Bar du coin">
                </div>
                <div class="mb-3 col-6">
                        <label for="form-trip-address" class="form-label">Adresse</label>
                        <input type="text" autocomplete="off" required value="${formdata.address}" name="address" id="form-trip-address" placeholder="10 rue du coin, Nantes" class="form-input-geocode form-control">
                        <input type="hidden" value="${formdata.pointLat?c}" name="pointLat" id="form-trip-address-lat">
                        <input type="hidden" value="${formdata.pointLng?c}" name="pointLng" id="form-trip-address-lng">
                </div>
                <div class="mb-3">
                  <label for="form-place-link" class="form-label">Site web</label>
                  <input autocomplete="off" value="${formdata.link}" name="link" type="text" class="form-control" id="form-place-link" placeholder="https://www.barducoin.com">
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">