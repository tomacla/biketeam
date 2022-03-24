<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mb-4">

        <div class="border rounded p-2">
            <div class="d-flex flex-row justify-content-between">
                <h4>${map.name}</h4>
                <div>
                    <#if _team_admin && !_embed>
                        <a href="<@common.teamUrl team.id '/admin/maps/${map.id}' />" class="btn btn-sm btn-secondary"><i class="bi-pencil bi"></i> Editer</a>
                    </#if>
                </div>
            </div>
            <h6><small class="text-muted">Ajoutée le ${map.postedAt.format(_date_formatter)}</small></h6>
            <ul class="list-inline">
                <li class="list-inline-item"><i class="bi bi-arrow-left-right"></i> ${map.length}km</li>
                <li class="list-inline-item"><i class="bi bi-arrow-up"></i> ${map.positiveElevation}m</li>
                <li class="list-inline-item"><i class="bi bi-arrow-down"></i> ${map.negativeElevation}m</li>
                <#if map.crossing>
                    <li class="list-inline-item"><i class="bi bi-x"></i> Ca croise !</li>
                </#if>
            </ul>

            <#if map.tags?size gt 0>
                <p><strong>Tags</strong> : <#list map.tags as tag><span class="badge bg-secondary">${tag}</span><#if tag_has_next> </#if></#list></p>
            </#if>

             <#if !_embed>
                <div class="btn-group btn-group-sm" role="group">
                    <a download="${map.name}.gpx" href="<@common.teamUrl team.id '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> GPX</a>
                    <a download="${map.name}.fit" href="<@common.teamUrl team.id '/maps/${map.id}/fit' />" class="btn btn-outline-secondary" role="button"><i class="bi bi-download"></i> FIT</a>
                    <button id="toggle-track-button" onclick="toggleTrack();" class="btn btn-outline-secondary" role="button"><i class="bi bi-eye-slash-fill"></i> Masquer</button>
                </div>
             </#if>
        </div>

    </div>

    <div class="col-12">
        <div style="width:100%; height:<#if !_embed>500px<#else>31vh</#if>;" id="map-wrapper"></div>
    </div>
    <div id="chart-wrapper" style="height:20%" class="col-12 p-0 m-0"></div>

</div>

    <script type="text/javascript">

        var layers = {
            "Cyclo OSM": L.tileLayer('https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png', {
                minZoom: 1,
                maxZoom: 17,
                attribution: '<a href="https://github.com/cyclosm/cyclosm-cartocss-style/releases" title="CyclOSM - Open Bicycle render">CyclOSM</a> | Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            }),
            "ESRI Satellite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
            }),
            "IGN Satellite": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE={style}&TILEMATRIXSET=PM&FORMAT={format}&LAYER=ORTHOIMAGERY.ORTHOPHOTOS&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}', {
                attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>',
                bounds: [[-75, -180], [81, 180]],
                minZoom: 2,
                maxZoom: 19,
                apikey: 'ortho',
                format: 'image/jpeg',
                style: 'normal'
            }),
            "IGN Plan": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE={style}&TILEMATRIXSET=PM&FORMAT={format}&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}', {
                attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>',
                bounds: [[-75, -180], [81, 180]],
                minZoom: 2,
                maxZoom: 18,
                apikey: 'cartes',
                format: 'image/png',
                style: 'normal'
            })
        };

        function toggleTrack() {
            if(trackDisplayed) {
                elevation._layers.eachLayer(function (l) {
                    return l.remove();
                  });
                  trackDisplayed = false;
                  document.getElementById('toggle-track-button').innerHTML = '<i class="bi bi-eye-fill"></i> Afficher';
            } else {
                elevation._layers.eachLayer(function (l) {
                     return l.addTo(targetMap);
                  });
                  trackDisplayed = true;
                  document.getElementById('toggle-track-button').innerHTML = '<i class="bi bi-eye-slash-fill"></i> Masquer';
            }
        }

        function initMap(mapContainerId) {

            var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layers['Cyclo OSM']] }).setView([51.505, -0.09], 13);
            L.control.layers(layers, null, {position: 'bottomleft'}).addTo(targetMap);
            L.control.zoom({position: 'bottomright'}).addTo(targetMap);

            return targetMap;

        }

        function initGpx(targetMap, gpxUrl) {

           var elevation_options = {
               theme: "red-theme",
               detached: true,
               elevationDiv: "#chart-wrapper",
               followMarker: true,
               imperial: false,
               reverseCoords: false,
               acceleration: false,
               slope: false,
               speed: false,
               time: false,
               summary: false,
               downloadLink: false,
               ruler: false,
               legend: false
             };

             var controlElevation = L.control.elevation(elevation_options).addTo(targetMap);

             controlElevation.on('eledata_loaded', (data) => {

                var startPoint = data.data[0].geometry.coordinates[0];
                var endpoint = data.data[0].geometry.coordinates[data.data[0].geometry.coordinates.length - 1];

                L.marker([startPoint[1], startPoint[0]], {clickable: false, icon : L.icon({
                    iconUrl: '<@common.teamUrl '' '/css/pin-icon-start.png' />',
                    iconSize: [33, 45],
                    iconAnchor: [16, 45],
                    shadowUrl: '<@common.teamUrl '' '/css/pin-shadow.png' />',
                    shadowSize: [50, 50],
                    shadowAnchor: [25, 50]
                })}).addTo(targetMap);
                L.marker([endpoint[1], endpoint[0]], {clickable: false, icon : L.icon({
                   iconUrl: '<@common.teamUrl '' '/css/pin-icon-end.png' />',
                   iconSize: [33, 45],
                   iconAnchor: [16, 45],
                   shadowUrl: '<@common.teamUrl '' '/css/pin-shadow.png' />',
                   shadowSize: [50, 50],
                   shadowAnchor: [25, 50]
               })}).addTo(targetMap);

             });

             controlElevation.load(gpxUrl);

             return controlElevation;

        }

        var gpxUrl = '<@common.teamUrl team.id '/maps/${map.id}/gpx' />';
        var targetMap = initMap('map-wrapper');
        var elevation = initGpx(targetMap, gpxUrl);
        var trackDisplayed = true;


    </script>

<#include "_includes_tail.ftlh">