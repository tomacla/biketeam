// display a vertical line at mousePointDist
function corshairPlugin() {
    return {
        id: 'corshair',

        afterDatasetsDraw: (chart, _, opts) => {
            const {
                mousePointDist,
                ctx,
                chartArea: {
                    top,
                    bottom,
                    left,
                    right
                }
            } = chart;

            if (mousePointDist < 0 ||
            mousePointDist <= chart.scales.x.min ||
            mousePointDist >= chart.scales.x.max) {
                return;
            }

            const x = chart.scales.x.getPixelForValue(mousePointDist);

            ctx.lineWidth = opts.width || 0;
            ctx.setLineDash(opts.dash || []);
            ctx.strokeStyle = opts.color || 'black'

            ctx.save();
            ctx.beginPath();
            ctx.moveTo(x, bottom);
            ctx.lineTo(x, top);
            ctx.stroke();
            ctx.restore();
        }
    }
}
