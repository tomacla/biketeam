package info.tomacla.biketeam.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/js-filtered")
public class JSLayersController {

    @Value("${ign.api.key:undefined}")
    private String ignApiKey;

    @Value("${custom.mbtiles.layer.url:undefined}")
    private String customMbTilesLayerURL;

    @ResponseBody
    @RequestMapping(value = "/leaflet-layers.js", method = RequestMethod.GET, produces = "text/javascript")
    public ResponseEntity<String> getLeafletLayers() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/javascript");

        return new ResponseEntity<>(
                getBody(),
                headers,
                HttpStatus.OK);

    }

    private String getBody() {

        StringBuilder sb = new StringBuilder();
        sb.append("""
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
                    })""");

        if (!this.customMbTilesLayerURL.equals("undefined")) {
            sb.append("""
                        ,        
                        "🔵 Eau" : L.vectorGrid.protobuf("https://tiles.prendslaroue.fr/france/{z}/{x}/{y}", {
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
                    """);
        }

        sb.append("};\n\n");

        sb.append("""
                var layers = {
                                    "Cyclo OSM": L.tileLayer('https://{s}.tile-cyclosm.openstreetmap.fr/cyclosm/{z}/{x}/{y}.png', {
                                        maxNativeZoom: 17,
                                        maxZoom: 22,
                                        attribution: '<a href="https://github.com/cyclosm/cyclosm-cartocss-style/releases" title="CyclOSM - Open Bicycle render">CyclOSM</a> | Map data: &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                                    }),
                                    "OpenStreeMap": L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>',
                                        maxNativeZoom: 19,
                                        maxZoom: 22
                                    }),
                                    "ESRI Satellite": L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                                        attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
                                    }),
                                    """);
        if (!ignApiKey.equals("undefined")) {
            sb.append("""
                    "IGN Scan": L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&TILEMATRIXSET=PM&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}&LAYER=GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN25TOUR&FORMAT=image/jpeg&STYLE=normal', {
                                        maxNativeZoom: 16,
                                        maxZoom: 22,
                                        apikey: 'f3ugilx7vq27vhzn887x8ds2',
                                        attribution: '<a target="_blank" href="https://www.geoportail.gouv.fr/">Geoportail France</a>'
                                                                }),
                    """);

        }

        sb.append("""
                  
                                            
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
                        }),
                         "Carto" : L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
                            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
                            maxZoom: 20
                         })
                    };
                """);

        return sb.toString();


    }

}
