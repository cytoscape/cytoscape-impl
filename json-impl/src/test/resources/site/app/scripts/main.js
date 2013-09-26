/**
 *
 * Template code for Cytoscape.js export function.
 *
 */
$(function () {
    // Basic settings for the Cytoscape window
    var options = {

        showOverlay: false,
        minZoom: 0.1,
        maxZoom: 20,

        layout: {
            name: 'preset'
        },

        ready: function () {
            cy = this;
            cy.load(networkData.elements);
            // Apply Visual Style CSS
            var targetStyleName = "vs1";
            var title = "";
            var visualstyle;
            for(var i=0; i<vs.length; i++) {
                visualStyle = vs[i];
                title = visualStyle.title;
                if(title == targetStyleName) {
                    break;
                }
            }
            cy.style().fromJson(visualStyle.style).update();
        }
    };

    var networkData = {};
    var vs = {};

    // Load data files.
    $.getJSON('data/js1.json', function(visualStyle) {
        vs = visualStyle;
        $.getJSON('data/js1.cyjs', function(network) {
            networkData = network;
            $('.network').cytoscape(options);
        });
    });
});