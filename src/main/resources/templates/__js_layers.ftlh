var overlayLayers = {
    "Voies cyclables": L.tileLayer('https://tile.waymarkedtrails.org/cycling/{z}/{x}/{y}.png', {
        maxNativeZoom: 18,
        maxZoom: 24,
        attribution: '&copy; <a href="https://www.waymarkedtrails.org" target="_blank">Waymarked Trails</a>'
    }),
    "Itinéraires VTT": L.tileLayer('https://tile.waymarkedtrails.org/mtb/{z}/{x}/{y}.png', {
        maxNativeZoom: 18,
        maxZoom: 24,
        attribution: '&copy; <a href="https://www.waymarkedtrails.org" target="_blank">Waymarked Trails</a>'
    })
    <#if customMbTilesLayerURL??>
        ,
        "🔵 Eau" : L.vectorGrid.protobuf("${customMbTilesLayerURL}", {
                 vectorTileLayerStyles: {
                         water: {
                           radius: 5,
                             fill: true,
                             fillColor: '#4DBCE9',
                             color: "#0000FF",
                             weight: 0.5,
                             opacity: 1,
                             fillOpacity: 0.8
                       },
                       supply: [],
                      rest: [],
                     shelter: []
                 },
                 minZoom: 11,
                 maxNativeZoom: 14,
                 maxZoom: 24,
             }),
         "🟢 Ravitaillement" : L.vectorGrid.protobuf("https://tiles.prendslaroue.fr/france/{z}/{x}/{y}", {
              vectorTileLayerStyles: {
                      water: [],
                    supply: {
                       radius: 5,
                       fill: true,
                       fillColor: '#ADFF2F',
                       color: "#008000",
                       weight: 0.5,
                       opacity: 1,
                       fillOpacity: 0.8
                   },
                   rest: [],
                  shelter: []
              },
              minZoom: 11,
              maxNativeZoom: 14,
              maxZoom: 24,
          }),
          "🟣 Table/Café" : L.vectorGrid.protobuf("https://tiles.prendslaroue.fr/france/{z}/{x}/{y}", {
            vectorTileLayerStyles: {
                   water: [],
                 supply: [],
                rest: {
                   radius: 5,
                      fill: true,
                      fillColor: '#DDA0DD',
                      color: "#663399",
                      weight: 0.5,
                      opacity: 1,
                      fillOpacity: 0.8
               },
               shelter: []
           },
           minZoom: 11,
           maxNativeZoom: 14,
           maxZoom: 24,
       }),
       "🟠 Abris" : L.vectorGrid.protobuf("https://tiles.prendslaroue.fr/france/{z}/{x}/{y}", {
            vectorTileLayerStyles: {
                    water: [],
                  supply: [],
                 rest: [],
                shelter: {
                  radius: 5,
                  fill: true,
                  fillColor: '#FFA500',
                  color: "#FF4500",
                  weight: 0.5,
                  opacity: 1,
                  fillOpacity: 0.8
               }
            },
            minZoom: 11,
            maxNativeZoom: 14,
            maxZoom: 24,
        })
    </#if>
    };

    var layers = {
        "OpenStreetMap": L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
            maxNativeZoom: 19,
            maxZoom: 22
        }),
        "Cyclo OSM": L.tileLayer('https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png', {
            maxNativeZoom: 17,
            maxZoom: 22,
            attribution: '<a href="https://github.com/cyclosm/cyclosm-cartocss-style/releases" title="CyclOSM - Open Bicycle render">CyclOSM</a> | Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }),
        "ESRI Satellite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
        }),

    <#if ignApiKey??>

        "IGN Scan": L.tileLayer('https://data.geopf.fr/private/wmts?SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&FORMAT=image/jpeg&LAYER=GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN25TOUR&TILEMATRIXSET=PM&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}&apikey={apikey}', {
                    minZoom: 6,
                    maxNativeZoom: 16,
                    maxZoom: 22,
                    apikey: '${ignApiKey}',
                    attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>'
                }),

    </#if>

        "IGN Satellite": L.tileLayer('https://data.geopf.fr/wmts?SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&FORMAT=image/jpeg&LAYER=ORTHOIMAGERY.ORTHOPHOTOS&TILEMATRIXSET=PM&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}', {
            minZoom: 1,
            maxNativeZoom: 19,
            maxZoom: 22,
            attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>'
        }),
        "IGN Plan": L.tileLayer('https://data.geopf.fr/wmts?SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIXSET=PM&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}', {
            minZoom: 1,
            maxNativeZoom: 19,
            maxZoom: 22,
            attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>'
        }),

         "Carto" : L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
            maxZoom: 20
         })
    };
