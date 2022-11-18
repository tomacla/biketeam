/** GLOBAL FORM BEHAVIOURS **/

var maxFileSizeInMB = 1;
var urlPattern = /(?:(?:https?|ftp):\/\/)?(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)(?:\.(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)*(?:\.(?:[a-z\x{00a1}\-\x{ffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?/ig;

var markedToc = [];
var markedRenderer = (function() {
     var renderer = new marked.Renderer();
     renderer.heading = function(text, level, raw) {
         var anchor = this.options.headerPrefix + raw.toLowerCase().replace(/[^\w]+/g, '-');
         if(level == 1) {
             markedToc.push({
                 anchor: anchor,
                 level: level,
                 text: text
             });
         }
         return '<h' + level + ' id="' + anchor + '">'+ text+ '</h'+ level+ '>';
     };
     return renderer;
 })();
marked.setOptions({
    renderer: markedRenderer,
    gfm: true,
    tables: true,
    breaks: false,
    pedantic: false,
    sanitize: true,
    smartLists: true,
    smartypants: false
  });

document.addEventListener("DOMContentLoaded", function() {
    Array.from(document.getElementsByClassName('form-size-check')).forEach(initFormSizeCheck);
    Array.from(document.getElementsByClassName('form-input-geocode')).forEach(initGeoCode);
    Array.from(document.getElementsByClassName('form-map-control')).forEach(initMapAutoComplete);
    Array.from(document.getElementsByClassName('wrap-content')).forEach(wrapContent);
    Array.from(document.getElementsByClassName('markdown-content')).forEach(markdownContent);
    Array.from(document.getElementsByClassName('markdown-content-toc')).forEach(markdownContentToc);
    Array.from(document.getElementsByClassName('form-unique-id')).forEach(initUniqueIdField);
    Array.from(document.getElementsByClassName('scroll-down')).forEach(scrollToBottom);
    Array.from(document.getElementsByClassName('reaction-holder')).forEach(reactionHolder);
    Tags.init();

    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
      return new bootstrap.Tooltip(tooltipTriggerEl, {
        container: 'body'
      })
    })

    if (typeof URLSearchParams !== 'undefined') {

        var params = new URLSearchParams(location.search);
        params.delete('sso');
        params.delete('logout');

        var paramsStr = params.toString();
        if(paramsStr !== '') {
            window.history.replaceState({}, '', `${location.pathname}?${paramsStr}`);
        } else {
            window.history.replaceState({}, '', `${location.pathname}`);
        }


    }

});

var leafletModalMaps = [];
function initPlaceMap(mapContainerId, lat, lng) {
    var layer = L.tileLayer('https://wxs.ign.fr/{apikey}/geoportail/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE={style}&TILEMATRIXSET=PM&FORMAT={format}&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}', {
            attribution: 'Geoportail',
            bounds: [[-75, -180], [81, 180]],
            minZoom: 2,
            maxZoom: 18,
            apikey: 'cartes',
            format: 'image/png',
            style: 'normal'
        });
    var targetMap = L.map(mapContainerId, { zoomControl: false, layers: [layer] }).setView([lat, lng], 17);
    L.marker([lat, lng], {clickable: false}).addTo(targetMap);
    leafletModalMaps.push(targetMap);
    return targetMap;
}

function scrollToBottom(element) {
    element.scrollTop = element.scrollHeight;
}

function initFormSizeCheck(input) {
    input.addEventListener('change', (event) => {
            var target = event.target;
            if (target.files && target.files[0]) {
                if (target.files[0].size > (maxFileSizeInMB * 1024 * 1024)) {
                    new bootstrap.Modal(document.getElementById('modal-filetoobig')).show();
                    target.value = ''
                }
            }
        });
}

var geoCodeTimeouts = {};
function initGeoCode(input) {

        input.addEventListener('keyup', function(event) {

           if(geoCodeTimeouts[input.id]) {
               clearTimeout(geoCodeTimeouts[input.id]);
               delete geoCodeTimeouts[input.id];
           }

           if(input.value.length > 2) {
               geoCodeTimeouts[input.id] = setTimeout(function() {
                   geoCode(input.value, function(geocodeResponse) {
                       if(geocodeResponse !== null) {
                           input.value = geocodeResponse.label;
                           document.getElementById(input.id + '-lat').value = geocodeResponse.point.lat;
                           document.getElementById(input.id + '-lng').value = geocodeResponse.point.lng;
                       }
                   });
               }, 1000);
           }
       });

    }

var uniqueIdTimeout = undefined;
function initUniqueIdField(input) {

        input.addEventListener('keyup', function(event) {

           if(uniqueIdTimeout) {
               clearTimeout(uniqueIdTimeout);
               uniqueIdTimeout = undefined;
           }

           if(input.value.length > 2) {
               uniqueIdTimeout = setTimeout(function() {
                    var element = input.getAttribute("data-element");
                    uniqueId(input.value, element, function(response) {
                        var targetIdField = document.getElementById(input.getAttribute("data-target-id-field"));
                        targetIdField.value = response;
                    })
               }, 1000);
           }
       });

        if(input.value.length > 2 && document.getElementById(input.getAttribute("data-target-id-field")).value === '') {
            input.dispatchEvent(new KeyboardEvent('keyup', {'key':' '}));
       }

    }

function initMapAutoComplete(input) {

    var autocompleteField = new Autocomplete(input, {
        data: [],
        maximumItems: 10,
        treshold: 2,
        highlightClass: 'text-warning',
        onSelectItem: (selected) => {
            var targetIdField = document.getElementById(input.getAttribute("data-target-id-field"));
            var targetNameField = document.getElementById(input.getAttribute("data-target-name-field"));
            targetIdField.value = selected.value;
            targetNameField.value = selected.label;
            input.value = '';
        },
        onInput: (fieldValue) => {
            var xmlHttp = new XMLHttpRequest();
                xmlHttp.onreadystatechange = function() {
                    if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                        var r = JSON.parse(xmlHttp.responseText);
                        var result = [];
                        Object.keys(r).forEach(function(key) {
                            result.push({
                                label: r[key],
                                value: key
                            });
                        });
                        autocompleteField.setData(result);
                    }
                }
                xmlHttp.open("GET", input.getAttribute("data-team-id") + "/maps/autocomplete?q="+encodeURIComponent(fieldValue), true); // true for asynchronous
                xmlHttp.send(null);
        }
    });

}

function loadJsonContent(url, callback) {

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            callback(JSON.parse(xmlHttp.responseText));
        }
    }
    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.send(null);

}

function wrapContent(contentContainer) {

      var text = contentContainer.innerHTML;

      text = text.replace(urlPattern, function (url) {
                         var protocol_pattern = /^(?:(?:https?|ftp):\/\/)/i;
                         var href = protocol_pattern.test(url) ? url : 'http://' + url;
                         return '<a class="link-dark" href="' + href + '">' + url + '</a>';
                       });

      text = text.replace(new RegExp('\r?\n','g'), "<br />");

      contentContainer.innerHTML = text;

}

function markdownContent(contentContainer) {
      var text = contentContainer.innerHTML;
      contentContainer.innerHTML = DOMPurify.sanitize( marked(text) , {USE_PROFILES: {html: true}} );
}

function markdownContentToc(contentContainer) {
        var tocHTML = '<div class="list-group">';
        markedToc.forEach(function (entry) {
          tocHTML += '<a class="list-group-item list-group-item-action" href="#'+entry.anchor+'">'+entry.text+'</a>';
        });
        tocHTML += '</div>\n';
        contentContainer.innerHTML = tocHTML;
}

/** FORMS **/

function setFieldValue(fieldId, value) {
    document.getElementById(fieldId).value = value;
}

function forceSubmitForm(formId) {
    document.getElementById(formId).submit();
}

/** GEOCODE **/

var geoCodeModal = null;
function geoCode(toGeoCode, callback) {

    if(geoCodeModal === null) {
        geoCodeModal = new bootstrap.Modal(document.getElementById('modal-choose-geocode'));
    }

    var modalEl = document.getElementById('modal-choose-geocode');

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var r = JSON.parse(xmlHttp.responseText);
            if(r.features.length > 0) {
                var result = [];
                var geocodeList = document.getElementById('modal-choose-geocode-list');
                while (geocodeList.firstChild) {
                    geocodeList.removeChild(geocodeList.lastChild);
                }
                for(var i = 0; i < r.features.length; i++) {

                    var li = document.createElement("li");
                    li.classList.add('list-group-item');

                    var span = document.createElement("span");
                    span.textContent = r.features[i].properties.label;

                    var input = document.createElement("input");
                    input.classList.add('form-check-input');
                    input.classList.add('me-1');
                    input.setAttribute('type', 'radio');
                    input.setAttribute('name', 'geocode-selected-value');
                    input.setAttribute('value', ''+i);

                    li.appendChild(input);
                    li.appendChild(span);
                    geocodeList.appendChild(li);

                    result.push({
                        label : r.features[i].properties.label,
                        point: lambertToGPS({
                            x: r.features[i].properties.x,
                            y: r.features[i].properties.y
                        })
                    });
                }

                var li = document.createElement("li");
                li.classList.add('list-group-item');

                var span = document.createElement("span");
                span.textContent = 'Conserver l\'adresse saisie';

                var input = document.createElement("input");
                input.classList.add('form-check-input');
                input.classList.add('me-1');
                input.setAttribute('type', 'radio');
                input.setAttribute('name', 'geocode-selected-value');
                input.setAttribute('value', '-1');
                input.setAttribute('checked', 'checked');

                li.appendChild(input);
                li.appendChild(span);
                geocodeList.appendChild(li);

                geoCodeModal.show();

                var hideListener = function(event) {

                    var checked = modalEl.querySelector("input[type=radio]:checked");
                    if (checked && parseInt(checked.value) !== -1) {
                       callback(result[parseInt(checked.value)]);
                    } else {
                        callback(null);
                    }

                    modalEl.removeEventListener('hidden.bs.modal', hideListener, true);
                    geoCodeModalListener = null;
                }

                modalEl.addEventListener('hidden.bs.modal', hideListener, true);

            }
        }
    }
    xmlHttp.open("GET", "https://api-adresse.data.gouv.fr/search/?q="+encodeURIComponent(toGeoCode), true); // true for asynchronous
    xmlHttp.send(null);
}

function uniqueId(toUnique, element, callback) {

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var r = xmlHttp.responseText;
            callback(r);
        }
    }
    xmlHttp.open("GET", "/autocomplete/permalink/" + element + "?title="+encodeURIComponent(toUnique), true); // true for asynchronous
    xmlHttp.send(null);
}


function lambertToGPS(lambert) {

    var c= 11754255.426096; //constante de la projection
    var e= 0.0818191910428158; //première exentricité de l'ellipsoïde
    var n= 0.725607765053267; //exposant de la projection
    var xs= 700000; //coordonnées en projection du pole
    var ys= 12655612.049876; //coordonnées en projection du pole

    var a=(Math.log(c/(Math.sqrt(Math.pow((lambert.x - xs),2)+Math.pow((lambert.y - ys),2))))/n);

    var lng = ((Math.atan(-(lambert.x-xs)/(lambert.y-ys)))/n+3/180*Math.PI)/Math.PI*180;
    var lat = Math.asin(Math.tanh((Math.log(c/Math.sqrt(Math.pow((lambert.x-xs),2)+Math.pow((lambert.y-ys),2)))/n)+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*Math.sin(1))))))))))))))))))))))/Math.PI*180;

     return {
        lng : lng,
        lat : lat
     }

}

/** REACTION **/

function reactionHolder(contentContainer) {
    getReactions(contentContainer.getAttribute('data-reaction-url'), contentContainer);
}

function getReactions(url, target) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
           target.innerHTML = xmlHttp.responseText;
        }
    }
    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.setRequestHeader('Content-type', 'text/plain');
    xmlHttp.send(null);
}

function addReaction(targetId, url, reaction) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
           document.getElementById(targetId).innerHTML = xmlHttp.responseText;
        }
    }
    xmlHttp.open("GET", url + "/" + reaction, true); // true for asynchronous
    xmlHttp.setRequestHeader('Content-type', 'text/plain');
    xmlHttp.send(null);
}

function removeReaction(targetId, url) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
           document.getElementById(targetId).innerHTML = xmlHttp.responseText;
        }
    }
    xmlHttp.open("GET", url, true); // true for asynchronous
    xmlHttp.send(null);
}