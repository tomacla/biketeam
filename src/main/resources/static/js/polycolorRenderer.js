/* Vladimir Kalachikhin v.kalachikhin@gmail.com CC BY-SA
version 0.2.1
The Renderer upon the idea https://github.com/Oliv/leaflet-polycolor 
to colorisation of L.Polyline segments
Only the following options are supported:
color, opacity, weight, lineCap, lineJoin, dashArray, dashOffset
Additional options:
colors - the array of html color strings, one per each L.Polyline point. Or array of arrays if L.Polyline is a MultiPolyline
useGradient - bool
Usage:
var polyline = L.polyline(latLngs, {
	renderer: new polycolorRenderer(),
	colors: colors,	// array of html color strings such as 'rgba(255, 255, 255, 0.75)' or '#3388ff'
	useGradient: true,
});
*/

const polycolorRenderer = L.Canvas.extend({
_updatePoly: function(layer) {

	if (!this._drawing) return;
	if (layer.options.weight === 0) return;	// заданы нулевая толщина линии -- нечего рисовать

	let i, j, len2, Point, prevPoint;
	// Polyline может быть MultiPolyline -- состоять из массива массивов координат
	// Например, когда она создаётся из GeoJSON MultiLineString.
	// Если Polyline сделана из одного массива координат, то _parts -- массив из одного массива.
	if (!layer._parts.length) return;	// нет ни одного сегмента в линни
	//console.log('polycolorRenderer.js [polycolorRenderer] layer:',layer);

	this._layers[layer._leaflet_id] = layer;	// ?
	const ctx = this._ctx;
	
	// Зададим остальные свойства линии
	if (ctx.setLineDash) {
		ctx.setLineDash(layer.options && layer.options._dashArray || []);
	}
	ctx.globalAlpha = layer.options.opacity;
	ctx.lineCap = layer.options.lineCap;
	ctx.lineJoin = layer.options.lineJoin;

	// Нормализуем массивы цветов и толщин
	let optColors, optWeights;
	if((typeof layer.options.colors !== 'undefined') || Array.isArray(layer.options.colors)) {
		if(!Array.isArray(layer.options.colors[0])) optColors = [layer.options.colors];	// сделаем из options.colors массив массивов, такой же по структуре, как _parts
		else optColors = layer.options.colors;	// считаем, что options.colors уже такой же по структуре, как _parts
	}
	if((typeof layer.options.weights !== 'undefined') || Array.isArray(layer.options.weights)) {
		if(!Array.isArray(layer.options.weights[0])) optWeights = [layer.options.weights];
		else optWeights = layer.options.weights;
	}
	
	// Каждый отрезок каждого сегмента, по всем элементам массива массивов
	for (i = 0; i < layer._parts.length; i++) {	
		for (j = 0, len2 = layer._parts[i].length - 1; j < len2; j++) {
			Point = layer._parts[i][j + 1];
			prevPoint = layer._parts[i][j];

			ctx.beginPath();
			ctx.moveTo(prevPoint.x, prevPoint.y);
			ctx.lineTo(Point.x, Point.y);

			// Покрасим линии
			if(optColors){
				if(optColors[i].length == 0) ctx.strokeStyle = layer.options.color;	// пустой массив, линию окрашиваем в умолчальный цвет (layer.options.color всегда есть?)
				else if(optColors[i].length < (j+1)) ctx.strokeStyle = optColors[i][0] || layer.options.color;	// одно значение на все сегменты или неверное число значений, +1 потому что градиент
				else {	// массив значений цветов
					if(layer.options.useGradient) {	// требуется градиент
						if(optColors[i][j] && optColors[i][j+1]) {	// рисуем градиент только если есть содержательные значения
							ctx.strokeStyle = this._getStrokeGradient(ctx, prevPoint, Point, optColors[i][j], optColors[i][j+1]);
						}
						else ctx.strokeStyle = optColors[i][j] || layer.options.color;	// не рисуем градиент к умолчальному значению, но рисуем цвет, если есть
					}
					else ctx.strokeStyle = optColors[i][j] || layer.options.color;
				}
			}
			else ctx.strokeStyle = layer.options.color;

			// Зададим толщину
			if(optWeights){
				//console.log(i,j,optWeights[i][j]);
				if(optWeights[i].length == 0) ctx.lineWidth = layer.options.weight;	// пустой массив, задаём толщину линнии по умолчанию (layer.options.weight всегда есть?)
				else if(optWeights[i].length < j) ctx.lineWidth = optWeights[i][0] || layer.options.weight;	// одно значение на все сегменты или неверное число значений
				else ctx.lineWidth = optWeights[i][j] || layer.options.weight;
			}
			else ctx.lineWidth = layer.options.weight;

			ctx.stroke();
			ctx.closePath();
		}
	}
},

_getStrokeGradient: function(ctx, prevPoint, Point, gradientStartRGB, gradientEndRGB) {
	// Create a gradient for each segment, pick start and end colors from colors options
	const gradient = ctx.createLinearGradient(prevPoint.x, prevPoint.y, Point.x, Point.y);

	gradient.addColorStop(0, gradientStartRGB);
	gradient.addColorStop(1, gradientEndRGB);

	return gradient;
}
});

